package com.example.estagios

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.estagios.data.remote.InternshipOfferResponse
import com.example.estagios.data.remote.RetrofitClient

@Composable
fun InternshipOffersTestScreen() {
    var offers by remember { mutableStateOf<List<InternshipOfferResponse>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            offers = RetrofitClient.apiService.getInternshipOffers()
        } catch (e: Exception) {
            error = e.message
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Ofertas de estágio")

        if (error != null) {
            Text(text = "Erro: $error")
        }

        LazyColumn {
            items(offers) { offer ->
                Card(
                    modifier = Modifier.padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = offer.name)
                        Text(text = offer.companyName ?: "Empresa não definida")
                        Text(text = offer.location ?: "Localização não definida")
                        Text(text = offer.workModel ?: "Modelo não definido")
                        Text(text = offer.description)
                    }
                }
            }
        }
    }
}