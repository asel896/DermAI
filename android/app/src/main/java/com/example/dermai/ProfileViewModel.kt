package com.example.dermai

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SkinType(val displayName: String) {
    SENSITIVE("Hassas"),
    OILY("Yağlı"),
    DRY("Kuru"),
    COMBINATION("Karma"),
    ACNE_PRONE("Akneye Meyilli"),
    NORMAL("Normal")
}

data class UserProfile(
    val name: String = "Selin",
    val selectedSkinTypes: Set<SkinType> = setOf(SkinType.OILY, SkinType.ACNE_PRONE),
    val customTags: List<String> = listOf("Göz çevresi hassas", "Rosacea var"),
    val avoidParabens: Boolean = true,
    val avoidSulfates: Boolean = true,
    val avoidAlcohol: Boolean = false,
    val avoidPerfume: Boolean = true
)

class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()


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
        _isSaved.value = true
    }

    fun resetSavedState() {
        _isSaved.value = false
    }
}