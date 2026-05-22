package com.example.estagios.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.ui.theme.Azul
import com.example.estagios.ui.theme.TextoSecundario

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegistar: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var palavraPasse by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Ícone de idioma no canto superior direito
        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Outlined.Language, contentDescription = "Idioma")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Entrar",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Campo Email
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Email",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = TextoSecundario)
                    },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        focusedBorderColor = Azul
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Palavra-Passe
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Palavra-Passe",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = palavraPasse,
                    onValueChange = { palavraPasse = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextoSecundario)
                    },
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        focusedBorderColor = Azul
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Entrar
            Button(
                onClick = onLoginSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Azul)
            ) {
                Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "OU",
                color = TextoSecundario,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Google
            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) {
                Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Entrar com Google", fontWeight = FontWeight.SemiBold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botão Facebook
            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) {
                Text("f", fontWeight = FontWeight.Bold, color = Color(0xFF1877F2), fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Entrar com Facebook", fontWeight = FontWeight.SemiBold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link de registo
            Row(horizontalArrangement = Arrangement.Center) {
                Text("Não tens conta? ", color = TextoSecundario, fontSize = 14.sp)
                TextButton(onClick = onRegistar, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        "Regista-te",
                        color = Color(0xFF5B4FFF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
