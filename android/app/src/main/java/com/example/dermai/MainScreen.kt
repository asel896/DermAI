package com.example.dermai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Analiz", Icons.Default.Home)
    object History : BottomNavItem("history", "Geçmiş", Icons.Default.List)
    object Profile : BottomNavItem("profile", "Profil", Icons.Default.Person)
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(tokenManager, context)
    )

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(viewModel = mainViewModel)
            }
            composable(BottomNavItem.History.route) {
                HistoryScreen(viewModel = mainViewModel, snackbarHostState = snackbarHostState)
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: MainViewModel, snackbarHostState: SnackbarHostState) {
    val historyState by viewModel.historyState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    LaunchedEffect(historyState) {
        if (historyState is UiState.Error) {
            val message = (historyState as UiState.Error).message
            snackbarHostState.showSnackbar(
                message = "Hata: $message",
                duration = SnackbarDuration.Short
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Geçmiş Analizlerim",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = historyState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Analiz geçmişi yükleniyor...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            is UiState.Success -> {
                val historyList = state.data
                if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Henüz kaydedilmiş bir analiz bulunmuyor.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historyList) { item ->
                            HistoryItemCard(item = item)
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun HistoryItemCard(item: HistoryResponseItem) {
    val (riskLabel, riskColor) = when {
        item.resultJson.contains("Yüksek Risk", ignoreCase = true) || item.resultJson.contains("Tehlikeli", ignoreCase = true) -> "Yüksek Risk" to Color(0xFFE53935)
        item.resultJson.contains("Orta Risk", ignoreCase = true) || item.resultJson.contains("Dikkat", ignoreCase = true) -> "Orta Risk" to Color(0xFFFB8C00)
        else -> "Güvenli" to Color(0xFF43A047)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.createdAt,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Box(
                    modifier = Modifier
                        .background(color = riskColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = riskLabel,
                        color = riskColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.ingredientsText,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.resultJson,
                fontSize = 13.sp,
                maxLines = 3,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}