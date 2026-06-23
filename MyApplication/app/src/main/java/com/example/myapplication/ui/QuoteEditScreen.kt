package com.example.myapplication.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.myapplication.data.model.Quote
import com.example.myapplication.data.model.QuoteItem
import com.example.myapplication.ui.components.MapViewCompose
import com.example.myapplication.ui.theme.DeepBlack
import com.example.myapplication.ui.theme.ElectricBlue
import com.example.myapplication.ui.theme.SoftCyan
import com.example.myapplication.ui.theme.VoltageYellow
import com.example.myapplication.ui.theme.SoftBg
import com.example.myapplication.util.GeocodingResult
import com.example.myapplication.util.GeocodingService
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccessTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteEditScreen(
    viewModel: QuoteViewModel,
    quoteId: Long? = null,
    onBack: () -> Unit
) {
    val savedTech = remember { viewModel.getSavedTechProfile() }
    val catalogMaterials by viewModel.allMaterials.collectAsState()
    val catalogServices by viewModel.allServices.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val dateFormatter = remember(locale) { 
        SimpleDateFormat("yyyy-MM-dd", locale).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
    }
    val timeFormatter = remember(locale) { SimpleDateFormat("HH:mm", locale) }

    // 1. Perfil Técnico
    var companyName by remember { mutableStateOf(savedTech.companyName) }
    var technicianName by remember { mutableStateOf(savedTech.technicianName) }
    var companyPhone by remember { mutableStateOf(savedTech.companyPhone) }
    var companyAddress by remember { mutableStateOf(savedTech.companyAddress) }
    var logoUri by remember { mutableStateOf(savedTech.logoUri) }

    // 2. Información del Cliente
    var clientName by remember { mutableStateOf("") }
    var clientTaxId by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var clientAddress by remember { mutableStateOf("") }
    var mainService by remember { mutableStateOf("") }
    val initialDate = remember {
        val local = java.util.Calendar.getInstance()
        java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(local.get(java.util.Calendar.YEAR), local.get(java.util.Calendar.MONTH), local.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    var quoteDate by remember { mutableStateOf(dateFormatter.format(Date(initialDate))) }
    var quoteTime by remember { mutableStateOf(timeFormatter.format(Date())) }

    // Ubicación
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var showMapDialog by remember { mutableStateOf(false) }

    // 3. Conceptos e Instalación (Mano de Obra)
    val laborItems = remember { mutableStateListOf<QuoteItem>() }
    var laborDescription by remember { mutableStateOf("") }
    var laborPrice by remember { mutableStateOf("") }
    var laborSuggestions by remember { mutableStateOf(emptyList<com.example.myapplication.data.model.Service>()) }

    // 4. Materiales
    val materialItems = remember { mutableStateListOf<QuoteItem>() }
    var materialDescription by remember { mutableStateOf("") }
    var materialQty by remember { mutableStateOf("1") }
    var materialPrice by remember { mutableStateOf("") }
    var materialUnit by remember { mutableStateOf("pza") }
    var materialSuggestions by remember { mutableStateOf(emptyList<com.example.myapplication.data.model.Material>()) }

    // 5. Gastos Extras
    var extraLaborCost by remember { mutableStateOf("0") }
    var logisticsCost by remember { mutableStateOf("0") }
    var taxRate by remember { mutableDoubleStateOf(0.0) }
    var observations by remember { mutableStateOf("") }

    // Carga de datos si estamos editando o limpieza si es nuevo
    LaunchedEffect(quoteId) {
        if (quoteId != null) {
            viewModel.viewModelScope.launch {
                val quoteWithItems = viewModel.getQuoteById(quoteId)
                quoteWithItems?.let { qwi ->
                    val q = qwi.quote
                    companyName = q.companyName
                    technicianName = q.technicianName
                    companyPhone = q.companyPhone
                    companyAddress = q.companyAddress
                    logoUri = q.logoUri

                    clientName = q.clientName
                    clientTaxId = q.clientTaxId
                    clientPhone = q.clientPhone
                    clientAddress = q.clientAddress
                    mainService = q.serviceType
                    quoteDate = dateFormatter.format(Date(q.date))
                    quoteTime = q.time
                    if (q.latitude != null && q.longitude != null) {
                        selectedLocation = GeoPoint(q.latitude, q.longitude)
                    }

                    laborItems.clear()
                    laborItems.addAll(qwi.items.filter { it.itemType == "LABOR" })
                    materialItems.clear()
                    materialItems.addAll(qwi.items.filter { it.itemType == "MATERIAL" })

                    extraLaborCost = q.laborCostGeneral.toString()
                    logisticsCost = q.logisticsCost.toString()
                    taxRate = q.taxRate
                    observations = q.observations
                }
            }
        } else {
            // Es un nuevo presupuesto: limpiar datos del cliente y conceptos
            clientName = ""
            clientTaxId = ""
            clientPhone = ""
            clientAddress = ""
            mainService = ""
            quoteDate = dateFormatter.format(Date(initialDate))
            quoteTime = timeFormatter.format(Date())
            selectedLocation = null
            laborItems.clear()
            materialItems.clear()
            extraLaborCost = "0"
            logisticsCost = "0"
            taxRate = 0.0
            observations = ""
            
            // Cargar perfil guardado (por si cambió)
            val freshProfile = viewModel.getSavedTechProfile()
            companyName = freshProfile.companyName
            technicianName = freshProfile.technicianName
            companyPhone = freshProfile.companyPhone
            companyAddress = freshProfile.companyAddress
            logoUri = freshProfile.logoUri
        }
    }

    // Cálculos
    val laborTotal = laborItems.sumOf { it.price }
    val materialsTotal = materialItems.sumOf { it.quantity * it.price }
    val extraTotal = (extraLaborCost.toDoubleOrNull() ?: 0.0) + (logisticsCost.toDoubleOrNull() ?: 0.0)
    val subtotal = laborTotal + materialsTotal + extraTotal
    val taxAmount = subtotal * taxRate
    val grandTotal = subtotal + taxAmount

    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { logoUri = it.toString() }
    }

    // Fecha (Calendario)
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Hora (Reloj)
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
        initialMinute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
    )

    // Diálogo de salida
    var showExitConfirmation by remember { mutableStateOf(false) }
    BackHandler { showExitConfirmation = true }

    // --- Diálogos ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        quoteDate = dateFormatter.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("ACEPTAR") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("CANCELAR") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
                    .background(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        text = "Selecciona la hora",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold
                    )
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFFF1F3F4),
                            selectorColor = ElectricBlue,
                            containerColor = Color.White,
                            periodSelectorSelectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                            periodSelectorSelectedContentColor = ElectricBlue,
                            timeSelectorSelectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                            timeSelectorSelectedContentColor = ElectricBlue
                        )
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("CANCELAR", color = Color.Gray)
                        }
                        TextButton(
                            onClick = {
                                quoteTime = String.format(locale, "%02d:%02d", timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            }
                        ) {
                            Text("ACEPTAR", color = ElectricBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text(if (quoteId == null) "¿SALIR SIN GUARDAR?" else "¿DESCARTAR CAMBIOS?", fontWeight = FontWeight.Black) },
            text = { Text("Tienes cambios sin guardar en este presupuesto. ¿Qué deseas hacer?") },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.isNotBlank()) {
                            val newQuote = Quote(
                                id = quoteId ?: 0L,
                                companyName = companyName,
                                technicianName = technicianName,
                                companyPhone = companyPhone,
                                companyAddress = companyAddress,
                                logoUri = logoUri,
                                clientName = clientName,
                                clientTaxId = clientTaxId,
                                clientPhone = clientPhone,
                                clientAddress = clientAddress,
                                serviceType = mainService,
                                date = try { dateFormatter.parse(quoteDate)?.time ?: System.currentTimeMillis() } catch(_:Exception) { System.currentTimeMillis() },
                                time = quoteTime,
                                latitude = selectedLocation?.latitude,
                                longitude = selectedLocation?.longitude,
                                laborCostGeneral = extraLaborCost.toDoubleOrNull() ?: 0.0,
                                logisticsCost = logisticsCost.toDoubleOrNull() ?: 0.0,
                                taxRate = taxRate,
                                laborTotal = laborTotal,
                                materialsTotal = materialsTotal,
                                subtotal = subtotal,
                                taxAmount = taxAmount,
                                total = grandTotal,
                                observations = observations
                            )
                            viewModel.saveQuote(newQuote, laborItems + materialItems)
                            onBack()
                        }
                    },
                    enabled = clientName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) { Text("GUARDAR Y SALIR") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = onBack) { Text("DESCARTAR", color = Color.Red) }
                    TextButton(onClick = { showExitConfirmation = false }) { Text("CANCELAR") }
                }
            }
        )
    }

    if (showMapDialog) {
        LocationPickerDialog(
            initialLocation = selectedLocation,
            onLocationSelected = { selectedLocation = it; showMapDialog = false },
            onDismiss = { showMapDialog = false }
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item { EliteEditHeader(isEditing = quoteId != null) { showExitConfirmation = true } }

            // SECCIÓN: PERFIL DEL TÉCNICO
            item { EliteSectionTitle("Perfil del Técnico", Icons.Default.AccountCircle) }
            item {
                EliteCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HtmlInputField("Negocio:", "Ej: Soluciones Eléctricas", companyName, onValueChange = { companyName = it })
                            HtmlInputField("Técnico:", "Nombre completo", technicianName, onValueChange = { technicianName = it })
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) { HtmlInputField("Teléfono:", "33...", companyPhone, onValueChange = { companyPhone = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)) }
                                Box(modifier = Modifier.weight(1f)) { HtmlInputField("Domicilio:", "Ciudad/Zona", companyAddress, onValueChange = { companyAddress = it }) }
                            }
                        }
                        Box(
                            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(15.dp)).background(Color.White)
                                .border(2.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(15.dp))
                                .clickable { logoLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoUri.isEmpty()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(32.dp))
                                    Text("LOGO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                            } else {
                                AsyncImage(model = logoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                IconButton(onClick = { logoUri = "" }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Red, CircleShape)) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // SECCIÓN: INFORMACIÓN DEL CLIENTE
            item { EliteSectionTitle("Información del Cliente", Icons.Default.ContactPage) }
            item {
                EliteCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1.5f)) { HtmlInputField("Cliente:", "Nombre del cliente", clientName, onValueChange = { clientName = it }) }
                            Box(modifier = Modifier.weight(1f)) { HtmlInputField("RFC:", "Opcional", clientTaxId, onValueChange = { clientTaxId = it }) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) { HtmlInputField("WhatsApp:", "Teléfono cliente", clientPhone, onValueChange = { clientPhone = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)) }
                            Box(modifier = Modifier.weight(1f)) { HtmlInputField("Domicilio Cliente:", "Ubicación", clientAddress, onValueChange = { clientAddress = it }) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                HtmlInputField(
                                    label = "Fecha:",
                                    placeholder = "",
                                    value = quoteDate,
                                    onValueChange = { quoteDate = it },
                                    readOnly = true,
                                    onClick = { showDatePicker = true },
                                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp)) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                HtmlInputField(
                                    label = "Hora:",
                                    placeholder = "",
                                    value = quoteTime,
                                    onValueChange = { quoteTime = it },
                                    readOnly = true,
                                    onClick = { showTimePicker = true },
                                    trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp)) }
                                )
                            }
                        }
                        HtmlInputField("Tipo de Trabajo Principal:", "Ej: Instalación Residencial", mainService, onValueChange = { mainService = it })

                        Button(
                            onClick = { showMapDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedLocation != null) Color(0xFFE8F5E9) else Color.White, contentColor = ElectricBlue),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (selectedLocation != null) Color(0xFF2E7D32) else Color.LightGray)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (selectedLocation != null) "UBICACIÓN MARCADA ✅" else "MARCAR DOMICILIO EN MAPA")
                        }
                    }
                }
            }

            // SECCIÓN: MANO DE OBRA
            item { EliteSectionTitle("Mano de Obra", Icons.Default.Handyman) }
            item {
                EliteCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HtmlInputField("Concepto de Trabajo", "Escribe para buscar...", laborDescription, onValueChange = {
                            laborDescription = it
                            laborSuggestions = if (it.length > 1) {
                                catalogServices.filter { s -> s.name.contains(it, ignoreCase = true) }
                            } else emptyList()
                        })

                        if (laborSuggestions.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))) {
                                Column {
                                    laborSuggestions.take(3).forEach { service ->
                                        ListItem(
                                            headlineContent = { Text(service.name, fontSize = 12.sp) },
                                            modifier = Modifier.clickable {
                                                laborDescription = service.name
                                                laborPrice = service.defaultPrice.toString()
                                                laborSuggestions = emptyList()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) { HtmlInputField("Precio", "$ 0.00", laborPrice, onValueChange = { laborPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
                            Button(
                                onClick = {
                                    val p = laborPrice.toDoubleOrNull() ?: 0.0
                                    if (laborDescription.isNotBlank() && p > 0) {
                                        laborItems.add(QuoteItem(quoteId = 0, itemType = "LABOR", description = laborDescription, quantity = 1.0, price = p))
                                        laborDescription = ""
                                        laborPrice = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                        laborItems.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(item.description, modifier = Modifier.weight(1f), fontSize = 13.sp)
                                Text("$${String.format(locale, "%.2f", item.price)}", fontWeight = FontWeight.Bold, color = ElectricBlue)
                                IconButton(onClick = { laborItems.remove(item) }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp)) }
                            }
                        }
                    }
                }
            }

            // SECCIÓN: MATERIALES
            item { EliteSectionTitle("Materiales", Icons.Default.Inventory) }
            item {
                EliteCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HtmlInputField("Descripción Material", "Escribe para buscar...", materialDescription, onValueChange = {
                            materialDescription = it
                            materialSuggestions = if (it.length > 1) {
                                catalogMaterials.filter { m -> m.name.contains(it, ignoreCase = true) }
                            } else emptyList()
                        })

                        if (materialSuggestions.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))) {
                                Column {
                                    materialSuggestions.take(3).forEach { material ->
                                        ListItem(
                                            headlineContent = { Text(material.name, fontSize = 12.sp) },
                                            modifier = Modifier.clickable {
                                                materialDescription = material.name
                                                materialPrice = material.defaultPrice.toString()
                                                materialUnit = material.defaultUnit
                                                materialSuggestions = emptyList()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(0.5f)) { HtmlInputField("Cant.", "1", materialQty, onValueChange = { materialQty = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
                            Box(modifier = Modifier.weight(0.5f)) { HtmlInputField("Unid.", "pza", materialUnit, onValueChange = { materialUnit = it }) }
                            Box(modifier = Modifier.weight(0.8f)) { HtmlInputField("Precio U.", "$ 0.00", materialPrice, onValueChange = { materialPrice = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
                            Button(
                                onClick = {
                                    val q = materialQty.toDoubleOrNull() ?: 1.0
                                    val p = materialPrice.toDoubleOrNull() ?: 0.0
                                    if (materialDescription.isNotBlank() && p > 0) {
                                        materialItems.add(QuoteItem(quoteId = 0, itemType = "MATERIAL", description = materialDescription, quantity = q, unit = materialUnit, price = p))
                                        materialDescription = ""
                                        materialPrice = ""
                                        materialQty = "1"
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                        materialItems.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.description, fontSize = 13.sp)
                                    Text("${item.quantity} ${item.unit} x $${String.format(locale, "%.2f", item.price)}", fontSize = 10.sp, color = Color.Gray)
                                }
                                Text("$${String.format(locale, "%.2f", item.quantity * item.price)}", fontWeight = FontWeight.Bold, color = ElectricBlue)
                                IconButton(onClick = { materialItems.remove(item) }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(18.dp)) }
                            }
                        }
                    }
                }
            }

            // SECCIÓN: GASTOS E IMPUESTOS
            item { EliteSectionTitle("Gastos e Impuestos", Icons.Default.Receipt) }
            item {
                EliteCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) { HtmlInputField("M.O Extra ($):", "0", extraLaborCost, onValueChange = { extraLaborCost = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
                            Box(modifier = Modifier.weight(1f)) { HtmlInputField("Viáticos ($):", "0", logisticsCost, onValueChange = { logisticsCost = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
                        }
                        Text("Impuesto (IVA):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TaxButton("0%", taxRate == 0.0) { taxRate = 0.0 }
                            TaxButton("8%", taxRate == 0.08) { taxRate = 0.08 }
                            TaxButton("16%", taxRate == 0.16) { taxRate = 0.16 }
                        }
                    }
                }
            }

            // SECCIÓN: OBSERVACIONES
            item { EliteSectionTitle("Observaciones", Icons.Default.Edit) }
            item {
                EliteCard {
                    HtmlInputField("Notas adicionales:", "Garantías, plazos, etc.", observations, onValueChange = { observations = it })
                }
            }

            // RESUMEN FINAL (CAJA NEGRA)
            item {
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth().shadow(10.dp, RoundedCornerShape(25.dp)),
                    shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepBlack)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRowHtml("Subtotal Mano de Obra:", "$${String.format(locale, "%.2f", laborTotal)}")
                        SummaryRowHtml("Subtotal Materiales:", "$${String.format(locale, "%.2f", materialsTotal)}")
                        SummaryRowHtml("Gastos de Operación:", "$${String.format(locale, "%.2f", extraTotal)}")
                        HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))
                        SummaryRowHtml("Subtotal General:", "$${String.format(locale, "%.2f", subtotal)}")
                        SummaryRowHtml("IVA (${(taxRate * 100).toInt()}%):", "$${String.format(locale, "%.2f", taxAmount)}")

                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "TOTAL: $${String.format(locale, "%,.2f", grandTotal)}",
                            color = VoltageYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (clientName.isNotBlank()) {
                            val newQuote = Quote(
                                id = quoteId ?: 0L,
                                companyName = companyName,
                                technicianName = technicianName,
                                companyPhone = companyPhone,
                                companyAddress = companyAddress,
                                logoUri = logoUri,
                                clientName = clientName,
                                clientTaxId = clientTaxId,
                                clientPhone = clientPhone,
                                clientAddress = clientAddress,
                                serviceType = mainService,
                                date = try { dateFormatter.parse(quoteDate)?.time ?: System.currentTimeMillis() } catch(_:Exception) { System.currentTimeMillis() },
                                time = quoteTime,
                                latitude = selectedLocation?.latitude,
                                longitude = selectedLocation?.longitude,
                                laborCostGeneral = extraLaborCost.toDoubleOrNull() ?: 0.0,
                                logisticsCost = logisticsCost.toDoubleOrNull() ?: 0.0,
                                taxRate = taxRate,
                                laborTotal = laborTotal,
                                materialsTotal = materialsTotal,
                                subtotal = subtotal,
                                taxAmount = taxAmount,
                                total = grandTotal,
                                observations = observations
                            )
                            viewModel.saveQuote(newQuote, laborItems + materialItems)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(64.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Icon(if (quoteId == null) Icons.Default.PictureAsPdf else Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(if (quoteId == null) "GENERAR COTIZACIÓN" else "GUARDAR CAMBIOS", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    initialLocation: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit,
    onDismiss: () -> Unit
) {
    var tempLocation by remember { mutableStateOf(initialLocation) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<GeocodingResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var mapCenter by remember { mutableStateOf(initialLocation ?: GeoPoint(19.4326, -99.1332)) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().background(ElectricBlue).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("UBICAR DOMICILIO", color = Color.White, fontWeight = FontWeight.Black)
                            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                        }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar calle o CP...", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        isSearching = true
                                        searchResults = GeocodingService.search(searchQuery)
                                        isSearching = false
                                    }
                                }) {
                                    if (isSearching) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                    else Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    MapViewCompose(modifier = Modifier.fillMaxSize(), center = mapCenter, selectedPoint = tempLocation, onMapClick = { tempLocation = it })
                    if (searchResults.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column {
                                searchResults.forEach { result ->
                                    ListItem(
                                        headlineContent = { Text(result.displayName, fontSize = 12.sp) },
                                        modifier = Modifier.clickable {
                                            val p = GeoPoint(result.latitude, result.longitude)
                                            tempLocation = p
                                            mapCenter = p
                                            searchResults = emptyList()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { tempLocation?.let { onLocationSelected(it) } },
                    enabled = tempLocation != null,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(60.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("CONFIRMAR UBICACIÓN", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun HtmlInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val bgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    
    Column(modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier) {
        Text(
            label, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            enabled = onClick == null,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Mayor altura para mejor legibilidad y toque
                .shadow(2.dp, RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                disabledContainerColor = bgColor,
                focusedIndicatorColor = ElectricBlue,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                disabledTextColor = textColor
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium), // Texto más grande
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
fun TaxButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) ElectricBlue else Color.White,
            contentColor = if (isSelected) Color.White else ElectricBlue
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ElectricBlue)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun SummaryRowHtml(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun EliteCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(modifier = Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun EliteSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)) {
        Icon(icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            title.uppercase(), 
            fontWeight = FontWeight.Black, 
            color = MaterialTheme.colorScheme.onBackground, 
            fontSize = 15.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun EliteEditHeader(isEditing: Boolean = false, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp))
            .background(Brush.linearGradient(listOf(ElectricBlue, SoftCyan)))
            .padding(top = 40.dp, bottom = 30.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(if (isEditing) "EDITAR COTIZACIÓN" else "NUEVA COTIZACIÓN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.Bolt, contentDescription = null, tint = VoltageYellow, modifier = Modifier.size(32.dp))
        }
    }
}
