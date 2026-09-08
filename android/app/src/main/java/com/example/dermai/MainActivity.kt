package com.example.dermai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val sharedPreferences = getSharedPreferences("DermAI_User_Prefs", Context.MODE_PRIVATE)
        val savedToken = sharedPreferences.getString("jwt_token", null)


        if (!savedToken.isNullOrEmpty()) {
            RetrofitClient.userToken = savedToken
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    var isAuthenticated by remember { mutableStateOf(!savedToken.isNullOrEmpty()) }

                    if (!isAuthenticated) {
                        AuthScreen(
                            onAuthSuccess = {
                                isAuthenticated = true
                            }
                        )
                    } else {
                        MainScreen(
                            mainViewModel = mainViewModel,
                            profileViewModel = profileViewModel,
                            onLogout = {

                                sharedPreferences.edit().apply {
                                    remove("jwt_token")
                                    remove("user_email")
                                    apply()
                                }
                                RetrofitClient.userToken = null
                                isAuthenticated = false
                            }
                        )
                    }
                }
            }
        }
    }
}