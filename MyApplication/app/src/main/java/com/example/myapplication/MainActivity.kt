package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.AboutScreen
import com.example.myapplication.ui.ActivationScreen
import com.example.myapplication.ui.InventoryScreen
import com.example.myapplication.ui.QuoteEditScreen
import com.example.myapplication.ui.QuoteListScreen
import com.example.myapplication.ui.QuoteViewModel
import com.example.myapplication.ui.SplashScreen
import com.example.myapplication.ui.theme.ElectricBlue
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.VoltageYellow

class MainActivity : ComponentActivity() {
    private val viewModel: QuoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            
            MyApplicationTheme(darkTheme = darkTheme) {
                var showSplash by remember { mutableStateOf(true) }
                val isActivated = viewModel.isActivated()
                val isTrialExpired = viewModel.isTrialExpired()
                var showActivation by remember { mutableStateOf(!isActivated && isTrialExpired) }

                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else if (showActivation) {
                    ActivationScreen(
                        isTrialExpired = isTrialExpired,
                        deviceId = viewModel.getDeviceId(),
                        onValidate = { key, callback ->
                            viewModel.validateLicenseKeyRemote(key, callback)
                        },
                        onActivated = {
                            viewModel.setActivated(true)
                            showActivation = false
                        },
                        onTimeout = {
                            finish()
                        }
                    )
                } else {
                    var currentScreen by remember { mutableStateOf("list") }
                    var selectedTab by remember { mutableIntStateOf(0) }
                    var editingQuoteId by remember { mutableStateOf<Long?>(null) }

                    Scaffold(
                        topBar = {
                            if (!isActivated) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Periodo de prueba: ${viewModel.getTrialDaysRemaining()} días restantes",
                                        modifier = Modifier.padding(8.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            if (currentScreen != "edit") {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    NavigationBarItem(
                                        selected = selectedTab == 0,
                                        onClick = { 
                                            selectedTab = 0
                                            currentScreen = "list"
                                            editingQuoteId = null
                                        },
                                        icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                                        label = { Text("Presupuestos") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.tertiary,
                                            selectedTextColor = MaterialTheme.colorScheme.tertiary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 1,
                                        onClick = { 
                                            selectedTab = 1
                                            currentScreen = "inventory"
                                            editingQuoteId = null
                                        },
                                        icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                                        label = { Text("Catálogo") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.tertiary,
                                            selectedTextColor = MaterialTheme.colorScheme.tertiary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                    // Botón de Tema
                                    NavigationBarItem(
                                        selected = false,
                                        onClick = { darkTheme = !darkTheme },
                                        icon = { Icon(if (darkTheme) Icons.Default.BrightnessHigh else Icons.Default.Brightness4, contentDescription = "Cambiar Tema") },
                                        label = { Text(if (darkTheme) "Luz" else "Noche") },
                                        colors = NavigationBarItemDefaults.colors(
                                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 2,
                                        onClick = { 
                                            selectedTab = 2
                                            currentScreen = "about"
                                            editingQuoteId = null
                                        },
                                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                                        label = { Text("Info") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.tertiary,
                                            selectedTextColor = MaterialTheme.colorScheme.tertiary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                "list" -> QuoteListScreen(
                                    viewModel = viewModel,
                                    onAddQuote = { 
                                        editingQuoteId = null
                                        currentScreen = "edit" 
                                    },
                                    onEditQuote = { id ->
                                        editingQuoteId = id
                                        currentScreen = "edit"
                                    }
                                )
                                "inventory" -> InventoryScreen(viewModel = viewModel)
                                "about" -> AboutScreen(viewModel = viewModel)
                                "edit" -> QuoteEditScreen(
                                    viewModel = viewModel,
                                    quoteId = editingQuoteId,
                                    onBack = { 
                                        currentScreen = if (selectedTab == 0) "list" else if (selectedTab == 1) "inventory" else "about"
                                        editingQuoteId = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
