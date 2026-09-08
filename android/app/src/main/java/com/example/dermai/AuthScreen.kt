package com.example.dermai

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current

    val sharedPreferences = remember {
        context.getSharedPreferences("DermAI_User_Prefs", Context.MODE_PRIVATE)
    }

    var isSignUp by remember { mutableStateOf(false) }


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }


    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun validateAndSubmit() {
        errorMessage = null
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()
        val cleanName = name.trim()

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty() || (isSignUp && cleanName.isEmpty())) {
            errorMessage = "Lütfen tüm alanları doldurun."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            errorMessage = "Lütfen geçerli bir e-posta adresi girin."
            return
        }

        if (cleanPassword.length < 6) {
            errorMessage = "Şifreniz en az 6 karakter olmalıdır."
            return
        }

        if (isSignUp) {

            sharedPreferences.edit().apply {
                putString("user_email", cleanEmail)
                putString("user_password", cleanPassword)
                putString("user_name", cleanName)
                apply()
            }
            errorMessage = null
            onAuthSuccess()
        } else {

            val savedEmail = sharedPreferences.getString("user_email", null)
            val savedPassword = sharedPreferences.getString("user_password", null)


            val validEmail = savedEmail ?: "test@dermai.com"
            val validPassword = savedPassword ?: "123456"

            if (cleanEmail.equals(validEmail, ignoreCase = true) && cleanPassword == validPassword) {
                errorMessage = null
                onAuthSuccess()
            } else {
                errorMessage = "E-posta veya şifre hatalı! Lütfen önce Kaydolun."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DermAI",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isSignUp) "Hesap Oluşturun" else "Hoş Geldiniz",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Hata",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (isSignUp) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text("Ad Soyad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("E-posta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { validateAndSubmit() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isSignUp) "Kaydol" else "Giriş Yap", fontSize = 16.sp)
        }

        TextButton(
            onClick = {
                isSignUp = !isSignUp
                errorMessage = null
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                if (isSignUp) "Zaten hesabınız var mı? Giriş Yapın"
                else "Hesabınız yok mu? Kaydolun"
            )
        }
    }
}