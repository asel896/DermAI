package com.example.dermai

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleAuth() {
        Log.d("DERMAI_TEST", "1. Butona tıklandı! Email: $email | isSignUp: $isSignUp")
        errorMessage = null
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            errorMessage = "Lütfen e-posta ve şifrenizi girin."
            return
        }

        isLoading = true
        coroutineScope.launch {
            try {
                Log.d("DERMAI_TEST", "2. Ağ isteği atılıyor...")

                val response = if (isSignUp) {
                    RetrofitClient.apiService.register(AuthRequest(cleanEmail, cleanPassword))
                } else {
                    RetrofitClient.apiService.login(AuthRequest(cleanEmail, cleanPassword))
                }

                Log.d("DERMAI_TEST", "3. Sunucu yanıt kodu: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    val token = authData.token

                    RetrofitClient.userToken = token
                    tokenManager.saveToken(token)


                    val sharedPreferences = context.getSharedPreferences("DermAI_User_Prefs", Context.MODE_PRIVATE)
                    if (isSignUp && name.isNotBlank()) {
                        sharedPreferences.edit().putString("user_display_name", name.trim()).apply()
                    }

                    isLoading = false
                    onAuthSuccess()

                    if (isSignUp && name.isNotBlank()) {
                        val sharedPref = context.getSharedPreferences("DermAI_User_Prefs", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("user_display_name", name.trim()).apply()
                    }

                    Log.d("DERMAI_TEST", "4. Token başarıyla kaydedildi, ana ekrana geçiliyor.")
                    isLoading = false
                    onAuthSuccess()
                } else {
                    isLoading = false
                    val errorBody = response.errorBody()?.string()
                    Log.e("DERMAI_TEST", "Sunucu Hata Gövdesi: $errorBody")
                    errorMessage = if (isSignUp) "Kayıt oluşturulamadı." else "E-posta veya şifre hatalı!"
                }
            } catch (e: Exception) {
                isLoading = false
                Log.e("DERMAI_TEST", "CATCH / BAĞLANTI HATASI: ${e.localizedMessage}", e)
                errorMessage = "Sunucuya bağlanılamadı: ${e.localizedMessage}"
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
                            contentDescription = null,
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
                onValueChange = { name = it },
                label = { Text("Ad Soyad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { handleAuth() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(if (isSignUp) "Kaydol" else "Giriş Yap", fontSize = 16.sp)
            }
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