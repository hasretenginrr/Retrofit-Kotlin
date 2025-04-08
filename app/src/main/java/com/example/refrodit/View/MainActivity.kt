package com.example.refrodit.View

import WeatherAPI
import WeatherDetailsScreen
import WeatherResponse
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.refrodit.Model.CryptoModel
import com.example.refrodit.Service.CryptoAPI
import com.example.refrodit.ui.theme.RefroditTheme
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val BASE_URL = "https://raw.githubusercontent.com/"
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RefroditTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("weather_details") {
                        WeatherDetailsScreen()
                    }
                }
            }
        }
    }

    @Composable
    fun HomeScreen(navController: NavController) {
        var cryptoModels by remember { mutableStateOf<List<CryptoModel>>(emptyList()) }
        var filteredList by remember { mutableStateOf<List<CryptoModel>>(emptyList()) }
        var searchQuery by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            loadData(
                onSuccess = {
                    cryptoModels = it
                    filteredList = it
                    isLoading = false
                },
                onError = {
                    errorMessage = it
                    isLoading = false
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Crypto Verileri") },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("weather_details")
                        }) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Weather Icon"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        filteredList = if (it.isBlank()) cryptoModels
                        else cryptoModels.filter { crypto ->
                            crypto.currency.contains(it, ignoreCase = true)
                        }
                    },
                    label = { Text("Crypto Ara") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null -> {
                        Text(
                            text = "Error: $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    filteredList.isEmpty() -> {
                        Text(
                            text = "Sonuç Bulunamadı",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredList) { model ->
                                CryptoItem(crypto = model)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CryptoItem(crypto: CryptoModel) {
        val priceValue = crypto.price.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
        val priceColor = when {
            priceValue > 30000 -> Color(0xFF2E7D32) // Green
            priceValue < 1000 -> Color(0xFFC62828)  // Red
            else -> Color(0xFF1565C0)              // Blue
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = priceColor.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = crypto.currency, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = crypto.price,
                    color = priceColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    private fun loadData(
        onSuccess: (List<CryptoModel>) -> Unit,
        onError: (String) -> Unit
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CryptoAPI::class.java)

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofit.getData()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("Server error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Exception: ${e.localizedMessage}")
                }
            }
        }
    }
}