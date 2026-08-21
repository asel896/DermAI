package com.example.dermai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    var textInput by remember { mutableStateOf("") }
    val analysisState by viewModel.analysisState.collectAsState()


    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "DermAI Analiz",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )


            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Kozmetik İçerik Listesi (INCI)...") },
                placeholder = { Text("Örn: Aqua, Glycerin, Niacinamide...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(12.dp))


            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.analyzeText(textInput)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("İçeriği Analiz Et")
            }

            Spacer(modifier = Modifier.height(16.dp))

            
            when (val state = analysisState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = "Hata: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                is UiState.Success -> {
                    AnalysisResultView(response = state.data)
                }
                is UiState.Idle -> {
                    Text(
                        text = "Analiz yapmak için yukarıya içerik listenizi yapıştırın.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AnalysisResultView(response: AnalysisResponse) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Genel Değerlendirme: ${response.overallRating}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = response.summary)
                }
            }
        }

        item {
            Text(
                text = "İçerik Detayları",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(response.ingredients) { ingredient ->
            IngredientCard(ingredient = ingredient)
        }
    }
}

@Composable
fun IngredientCard(ingredient: IngredientDetail) {
    val badgeColor = if (ingredient.isHarmful) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
    val textColor = if (ingredient.isHarmful) Color(0xFFB71C1C) else Color(0xFF2E7D32)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = ingredient.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(text = "Amaç: ${ingredient.purpose}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(badgeColor, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Skor: ${ingredient.safetyScore}",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}