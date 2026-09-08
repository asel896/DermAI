package com.example.dermai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val profile by profileViewModel.userProfile.collectAsState()
    val isSaved by profileViewModel.isSaved.collectAsState()
    var newCustomTagText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(isSaved) {
        if (isSaved) {
            kotlinx.coroutines.delay(2000)
            profileViewModel.resetSavedState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kişisel Cilt Profilim",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Çıkış Yap",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }


        Text(
            text = "Cilt Tipleriniz (Birden fazla seçebilirsiniz)",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SkinType.values().forEach { skinType ->
                val isSelected = profile.selectedSkinTypes.contains(skinType)
                FilterChip(
                    selected = isSelected,
                    onClick = { profileViewModel.toggleSkinType(skinType) },
                    label = { Text(skinType.displayName) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        Text(
            text = "Özel Durum veya Alerjen Ekleyin",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCustomTagText,
                onValueChange = { newCustomTagText = it },
                placeholder = { Text("Örn: Egzemam var, Niasinamid yakıyor...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    profileViewModel.addCustomTag(newCustomTagText)
                    newCustomTagText = ""
                },
                modifier = Modifier.height(56.dp)
            ) {
                Text("Ekle")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))


        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            profile.customTags.forEach { tag ->
                InputChip(
                    selected = true,
                    onClick = { profileViewModel.removeCustomTag(tag) },
                    label = { Text(tag) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Sil",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        Text(
            text = "Kaçınılacak Genel Bileşenler",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PreferenceSwitchRow(
            label = "Parabenler",
            checked = profile.avoidParabens,
            onCheckedChange = { value -> profileViewModel.togglePreference("paraben", value) }
        )
        PreferenceSwitchRow(
            label = "Sülfatlar",
            checked = profile.avoidSulfates,
            onCheckedChange = { value -> profileViewModel.togglePreference("sulfate", value) }
        )
        PreferenceSwitchRow(
            label = "Sert Alkoller",
            checked = profile.avoidAlcohol,
            onCheckedChange = { value -> profileViewModel.togglePreference("alcohol", value) }
        )
        PreferenceSwitchRow(
            label = "Parfüm / Esans",
            checked = profile.avoidPerfume,
            onCheckedChange = { value -> profileViewModel.togglePreference("perfume", value) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { profileViewModel.saveProfile() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSaved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Save,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSaved) "Profil Kaydedildi!" else "Tercihlerimi Kaydet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PreferenceSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}