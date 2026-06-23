package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.ElectricBlue
import com.example.myapplication.ui.theme.SoftCyan
import kotlinx.coroutines.delay

@Composable
fun ActivationScreen(
    isTrialExpired: Boolean,
    deviceId: String,
    onValidate: (String, (Boolean, String?) -> Unit) -> Unit,
    onActivated: () -> Unit,
    onTimeout: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(60) }
    var userInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ElectricBlue, SoftCyan)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (isTrialExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isTrialExpired) "Periodo de Prueba Agotado" else "Activación de Licencia",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTrialExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "ID de este dispositivo:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = deviceId,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(deviceId)) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar ID", modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    text = if (isTrialExpired) 
                        "Sus 14 días de prueba han terminado. Envíe el ID de arriba al administrador para obtener su clave:" 
                        else "Ingrese su clave de producto para activar la versión completa:",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { input ->
                        // Filtrar para que solo acepte letras y números, y máximo 10 caracteres
                        val filtered = input.filter { it.isLetterOrDigit() }.uppercase()
                        if (filtered.length <= 10) {
                            userInput = filtered
                            isError = false
                            errorMessage = null
                        }
                    },
                    label = { Text("Clave de Licencia (10 caracteres)") },
                    placeholder = { Text("Ej: ABC1234567") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    singleLine = true,
                    enabled = !isLoading,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                if (isError) {
                    Text(
                        text = errorMessage ?: "Clave inválida para este dispositivo",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (userInput.length != 10) {
                            isError = true
                            errorMessage = "La clave debe tener 10 caracteres"
                            return@Button
                        }
                        isLoading = true
                        isError = false
                        errorMessage = null
                        onValidate(userInput) { isValid, error ->
                            isLoading = false
                            if (isValid) {
                                onActivated()
                            } else {
                                isError = true
                                errorMessage = error
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = userInput.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("ACTIVAR AHORA")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Tiempo restante para activación: ${timeLeft}s",
                    fontSize = 12.sp,
                    color = if (timeLeft < 15) Color.Red else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                
                LinearProgressIndicator(
                    progress = { timeLeft / 60f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = if (timeLeft < 15) Color.Red else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
