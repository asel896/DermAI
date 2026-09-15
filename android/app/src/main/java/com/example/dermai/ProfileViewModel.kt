package com.example.dermai

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SkinType(val displayName: String) {
    SENSITIVE("Hassas"),
    OILY("Yağlı"),
    DRY("Kuru"),
    COMBINATION("Karma"),
    ACNE_PRONE("Akneye Meyilli"),
    NORMAL("Normal")
}

data class UserProfile(
    val name: String = "Kullanıcı",
    val selectedSkinTypes: Set<SkinType> = emptySet(),
    val customTags: List<String> = emptyList(),
    val avoidParabens: Boolean = false,
    val avoidSulfates: Boolean = false,
    val avoidAlcohol: Boolean = false,
    val avoidPerfume: Boolean = false,
    val sensitivityLevel: Float = 0.6f,
    val isDarkMode: Boolean = false
)

class ProfileViewModel(
    private val tokenManager: TokenManager,
    private val context: Context
) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("DermAI_User_Prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val rawEmail = sharedPreferences.getString("user_email", null)
        val fallbackNameFromEmail = rawEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }

        val savedName = sharedPreferences.getString("user_display_name", null)
            ?: fallbackNameFromEmail
            ?: "Kullanıcı"

        val savedSkinTypesRaw = sharedPreferences.getStringSet("selected_skin_types", emptySet()) ?: emptySet()
        val loadedSkinTypes = savedSkinTypesRaw.mapNotNull { name ->
            try { SkinType.valueOf(name) } catch (e: Exception) { null }
        }.toSet()

        val savedTagsRaw = sharedPreferences.getStringSet("custom_tags", emptySet()) ?: emptySet()

        val avoidParabens = sharedPreferences.getBoolean("avoid_parabens", false)
        val avoidSulfates = sharedPreferences.getBoolean("avoid_sulfates", false)
        val avoidAlcohol = sharedPreferences.getBoolean("avoid_alcohol", false)
        val avoidPerfume = sharedPreferences.getBoolean("avoid_perfume", false)

        val sensitivityLevel = sharedPreferences.getFloat("sensitivity_level", 0.6f)
        val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", false)

        _userProfile.value = UserProfile(
            name = savedName,
            selectedSkinTypes = loadedSkinTypes,
            customTags = savedTagsRaw.toList(),
            avoidParabens = avoidParabens,
            avoidSulfates = avoidSulfates,
            avoidAlcohol = avoidAlcohol,
            avoidPerfume = avoidPerfume,
            sensitivityLevel = sensitivityLevel,
            isDarkMode = isDarkMode
        )
    }

    fun updateSensitivity(value: Float) {
        _userProfile.value = _userProfile.value.copy(sensitivityLevel = value)
    }

    fun toggleDarkMode(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(isDarkMode = enabled)
    }

    fun toggleSkinType(skinType: SkinType) {
        val current = _userProfile.value.selectedSkinTypes.toMutableSet()
        if (current.contains(skinType)) {
            current.remove(skinType)
        } else {
            current.add(skinType)
        }
        _userProfile.value = _userProfile.value.copy(selectedSkinTypes = current)
    }

    fun addCustomTag(tag: String) {
        if (tag.isNotBlank()) {
            val current = _userProfile.value.customTags.toMutableList()
            if (!current.contains(tag.trim())) {
                current.add(tag.trim())
                _userProfile.value = _userProfile.value.copy(customTags = current)
            }
        }
    }

    fun removeCustomTag(tag: String) {
        val current = _userProfile.value.customTags.toMutableList()
        current.remove(tag)
        _userProfile.value = _userProfile.value.copy(customTags = current)
    }

    fun togglePreference(key: String, value: Boolean) {
        val current = _userProfile.value
        _userProfile.value = when (key) {
            "paraben" -> current.copy(avoidParabens = value)
            "sulfate" -> current.copy(avoidSulfates = value)
            "alcohol" -> current.copy(avoidAlcohol = value)
            "perfume" -> current.copy(avoidPerfume = value)
            else -> current
        }
    }

    fun saveProfile() {
        val profile = _userProfile.value
        sharedPreferences.edit().apply {
            putStringSet("selected_skin_types", profile.selectedSkinTypes.map { it.name }.toSet())
            putStringSet("custom_tags", profile.customTags.toSet())
            putBoolean("avoid_parabens", profile.avoidParabens)
            putBoolean("avoid_sulfates", profile.avoidSulfates)
            putBoolean("avoid_alcohol", profile.avoidAlcohol)
            putBoolean("avoid_perfume", profile.avoidPerfume)
            putFloat("sensitivity_level", profile.sensitivityLevel)
            putBoolean("is_dark_mode", profile.isDarkMode)
            apply()
        }
        _isSaved.value = true
    }

    fun resetSavedState() {
        _isSaved.value = false
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearToken()
            sharedPreferences.edit().clear().apply()
            onLogoutComplete()
        }
    }

    class Factory(
        private val tokenManager: TokenManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(tokenManager, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}