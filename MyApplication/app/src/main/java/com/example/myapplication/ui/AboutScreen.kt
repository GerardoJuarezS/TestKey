package com.example.myapplication.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.ElectricBlue
import com.example.myapplication.ui.theme.SoftCyan
import com.example.myapplication.ui.theme.SoftBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AboutScreen(viewModel: QuoteViewModel) {
    val context = LocalContext.current
    val facebookUrl = "https://www.facebook.com/gjs.2017" // Link actualizado o editable

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.backupDatabase(it) { success ->
                if (success) {
                    Toast.makeText(context, "Respaldo creado con éxito", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error al crear respaldo", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingRestoreUri = it
            showRestoreDialog = true
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("¿RESTAURAR BASE DE DATOS?", fontWeight = FontWeight.Black) },
            text = { Text("Esta acción sobrescribirá todos tus presupuestos y catálogo actuales con los del archivo seleccionado. La aplicación se cerrará para aplicar los cambios.") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRestoreUri?.let { uri ->
                            viewModel.restoreDatabase(uri) { success ->
                                if (success) {
                                    Toast.makeText(context, "Restauración exitosa. Reiniciando...", Toast.LENGTH_LONG).show()
                                    // Cerrar la app para que al abrir se recree la DB con los nuevos datos
                                    (context as? android.app.Activity)?.finishAffinity()
                                } else {
                                    Toast.makeText(context, "Error al restaurar", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        showRestoreDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("RESTAURAR Y CERRAR") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("CANCELAR") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg)
    ) {
        // Header similar a las otras pantallas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ElectricBlue, SoftCyan)
                    )
                )
                .padding(vertical = 40.dp, horizontal = 20.dp)
                .shadow(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    "ACERCA DE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Bolt,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "TecniCotiza",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = ElectricBlue
            )
            
            Text(
                text = "Versión 1.0.0",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(40.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DESARROLLADO POR:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "Gerardo Juarez",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1877F2), // Facebook Blue
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("VISITAR FACEBOOK", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            backupLauncher.launch("TecniCotiza_Backup_$timeStamp.db")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElectricBlue),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("COPIA DE SEGURIDAD", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            restoreLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Gray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("RESTAURAR COPIA", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Text(
                text = "TecniCotiza 2026",
                fontSize = 12.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
