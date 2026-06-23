package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.Material
import com.example.myapplication.data.model.Service
import com.example.myapplication.ui.theme.ElectricBlue
import com.example.myapplication.ui.theme.VoltageYellow

@Composable
fun InventoryScreen(viewModel: QuoteViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("MATERIALES", "TRABAJOS")
    
    val materials by viewModel.allMaterials.collectAsState()
    val services by viewModel.allServices.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMaterial by remember { mutableStateOf<Material?>(null) }
    var editingService by remember { mutableStateOf<Service?>(null) }

    Scaffold(
        topBar = {
            Column {
                EliteHeader()
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ElectricBlue,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = VoltageYellow
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    editingMaterial = null
                    editingService = null
                    showAddDialog = true 
                },
                containerColor = VoltageYellow,
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { 
                    Text(
                        if (selectedTab == 0) "AÑADIR MATERIAL" else "AÑADIR TRABAJO",
                        fontWeight = FontWeight.Bold
                    )
                },
                shape = RoundedCornerShape(15.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                MaterialList(
                    materials = materials,
                    onEdit = { 
                        editingMaterial = it
                        showAddDialog = true 
                    },
                    onDelete = { viewModel.deleteMaterial(it) }
                )
            } else {
                ServiceList(
                    services = services,
                    onEdit = { 
                        editingService = it
                        showAddDialog = true 
                    },
                    onDelete = { viewModel.deleteService(it) }
                )
            }
        }
        
        if (showAddDialog) {
            if (selectedTab == 0) {
                MaterialDialog(
                    initialMaterial = editingMaterial,
                    onSave = { 
                        viewModel.saveMaterial(it)
                        showAddDialog = false
                    },
                    onDismiss = { showAddDialog = false }
                )
            } else {
                ServiceDialog(
                    initialService = editingService,
                    onSave = { 
                        viewModel.saveService(it)
                        showAddDialog = false
                    },
                    onDismiss = { showAddDialog = false }
                )
            }
        }
    }
}

@Composable
fun MaterialList(materials: List<Material>, onEdit: (Material) -> Unit, onDelete: (Material) -> Unit) {
    val grouped = materials.groupBy { it.category }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp), 
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        grouped.forEach { (category, items) ->
            item {
                Text(
                    text = category.ifBlank { "SIN CATEGORÍA" },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(items) { material ->
                EliteInventoryCard(
                    title = material.name,
                    subtitle = "${material.defaultUnit} - $${material.defaultPrice}",
                    onEdit = { onEdit(material) },
                    onDelete = { onDelete(material) }
                )
            }
        }
    }
}

@Composable
fun ServiceList(services: List<Service>, onEdit: (Service) -> Unit, onDelete: (Service) -> Unit) {
    val grouped = services.groupBy { it.category }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp), 
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        grouped.forEach { (category, items) ->
            item {
                Text(
                    text = category.ifBlank { "TRABAJOS GENERALES" },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(items) { service ->
                EliteInventoryCard(
                    title = service.name,
                    subtitle = "$${service.defaultPrice}",
                    onEdit = { onEdit(service) },
                    onDelete = { onDelete(service) }
                )
            }
        }
    }
}

@Composable
fun EliteInventoryCard(title: String, subtitle: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(15.dp)),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { // Más padding
            Column(modifier = Modifier.weight(1f)) {
                Text(title.uppercase(), fontWeight = FontWeight.Black, color = ElectricBlue, fontSize = 16.sp) // Texto más grande
                Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricBlue) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.5f)) }
        }
    }
}

@Composable
fun MaterialDialog(initialMaterial: Material?, onSave: (Material) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initialMaterial?.name ?: "") }
    var unit by remember { mutableStateOf(initialMaterial?.defaultUnit ?: "pza") }
    var price by remember { mutableStateOf(initialMaterial?.defaultPrice?.toString() ?: "") }
    var category by remember { mutableStateOf(initialMaterial?.category ?: "") }

    val categories = listOf("🟦 TUBERÍA", "🟨 CABLEADO", "🟩 CAJAS", "🟥 ACCESORIOS", "🟪 PROTECCIÓN", "⚪ VARIOS")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(initialMaterial == null) "NUEVO MATERIAL" else "EDITAR MATERIAL", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del Material") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unidad (pza, m, kg)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio Base") }, modifier = Modifier.fillMaxWidth())
                
                Text("Categoría:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(Material(id = initialMaterial?.id ?: 0L, name = name, category = category, defaultUnit = unit, defaultPrice = price.toDoubleOrNull() ?: 0.0))
            }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}

@Composable
fun ServiceDialog(initialService: Service?, onSave: (Service) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initialService?.name ?: "") }
    var price by remember { mutableStateOf(initialService?.defaultPrice?.toString() ?: "") }
    var desc by remember { mutableStateOf(initialService?.description ?: "") }
    var category by remember { mutableStateOf(initialService?.category ?: "TRABAJOS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(initialService == null) "NUEVO TRABAJO" else "EDITAR TRABAJO", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del Servicio") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio Base M.O") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría (opcional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(Service(id = initialService?.id ?: 0L, name = name, category = category, defaultPrice = price.toDoubleOrNull() ?: 0.0, description = desc))
            }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(horizontalArrangement: Arrangement.Horizontal, content: @Composable FlowRowScope.() -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = horizontalArrangement, content = content)
}
