package com.example.dermai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation(mainViewModel = mainViewModel)
        }
    }
}

@Composable
fun AppNavigation(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val tokenState by tokenManager.getToken.collectAsState(initial = null)


    LaunchedEffect(tokenState) {
        RetrofitClient.userToken = tokenState
    }

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(tokenManager, context)
    )

    val userProfile by profileViewModel.userProfile.collectAsState()

    MaterialTheme {
        if (tokenState.isNullOrEmpty()) {
            AuthScreen(
                onAuthSuccess = {}
            )
        } else {
            MainScreen(
                mainViewModel = mainViewModel,
                onLogout = {
                    coroutineScope.launch {
                        RetrofitClient.userToken = null
                        tokenManager.clearToken()
                    }
                }
            )
        }
    }
}