package com.example.refrodit.View

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.refrodit.Model.CryptoModel
import com.example.refrodit.Service.CryptoAPI
import com.example.refrodit.ui.theme.RefroditTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    private val BASE_URL = "https://raw.githubusercontent.com/" //apinin temel urlsi
    private var job: Job? = null //coroutine işlemlerini kontrol için bir nesne oluşturdum.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // tam ekran görüntüsünü sağlar.
        setContent { //UI'nin başlatılmasını sağlar.
            RefroditTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {

            var cryptoModels by remember { mutableStateOf<List<CryptoModel>>(emptyList()) }
            //remember: compose da durum tutmak için kullanılır. Burada crytoModelsde apiden alınan veriyi tutar.
            // API'den veriyi almak için LaunchedEffect kullanıldı. Bir kez tetiklenmesi gereken işlemler için kullanılır.
            LaunchedEffect(Unit) {
                loadData { models ->
                    cryptoModels = models
                }
            }

            // Listeyi göstermek için ayrı bir Composable
            CryptoList(cryptoModels)

    }




    private fun loadData(onResult: (List<CryptoModel>) -> Unit) {
        val retrofit = Retrofit.Builder() //retrofit nesnesi oluşturulur. Api çağrıları yapılır.
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CryptoAPI::class.java)

        job = CoroutineScope(Dispatchers.IO).launch { //disp.IO ile ağ çağrısı yapıluır. Main ile UI guncellenir.
            try {
                val response = retrofit.getData()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            onResult(it)
                        }
                    } else {
                        println("Response error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("Exception occurred: ${e.message}")
                }
            }
        }
    }

    @Composable
    fun CryptoList(models: List<CryptoModel>) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(models.size) { index ->
                val model = models[index]
                CryptoItem(crypto = model)
            }
        }
    }

    @Composable
    fun CryptoItem(crypto: CryptoModel) {
        Text(
            text = "${crypto.currency}: ${crypto.price}",
            modifier = Modifier.padding(16.dp)
        )
    }

    @Preview
    @Composable
    fun PreviewScreen() {
        RefroditTheme {
            CryptoList(
                models = listOf(
                    CryptoModel("Bitcoin", "$50,000"),
                    CryptoModel("Ethereum", "$4,000")
                )
            )
        }
    }
}
