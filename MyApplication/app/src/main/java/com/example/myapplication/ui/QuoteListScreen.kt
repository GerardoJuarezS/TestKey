package com.example.myapplication.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.myapplication.data.model.QuoteWithItems
import com.example.myapplication.ui.components.MiniMapView
import com.example.myapplication.ui.theme.ElectricBlue
import com.example.myapplication.ui.theme.SoftCyan
import com.example.myapplication.ui.theme.VoltageYellow
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QuoteListScreen(
    viewModel: QuoteViewModel,
    onAddQuote: () -> Unit,
    onEditQuote: (Long) -> Unit
) {
    val quotes by viewModel.allQuotes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val filteredQuotes = remember(quotes, searchQuery) {
        if (searchQuery.isBlank()) quotes
        else quotes.filter { 
            it.quote.clientName.contains(searchQuery, ignoreCase = true) ||
            it.quote.serviceType.contains(searchQuery, ignoreCase = true)
        }
    }
    val context = LocalContext.current
    var pendingPdfFile by remember { mutableStateOf<java.io.File?>(null) }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            pendingPdfFile?.let { file ->
                viewModel.savePdfToUri(file, it) { success ->
                    if (success) {
                        android.widget.Toast.makeText(context, "PDF guardado correctamente", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Error al guardar el PDF", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onAddQuote,
                containerColor = VoltageYellow,
                contentColor = Color.Black,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(36.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header Elite Pro Style
            EliteHeader()

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            if (filteredQuotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (searchQuery.isEmpty()) "⚡" else "🔍", fontSize = 64.sp)
                        Text(
                            if (searchQuery.isEmpty()) "SIN PRESUPUESTOS" else "SIN RESULTADOS",
                            fontWeight = FontWeight.Black,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(filteredQuotes) { quoteWithItems ->
                        EliteQuoteCard(
                            quoteWithItems = quoteWithItems,
                            onEdit = { onEditQuote(quoteWithItems.quote.id) },
                            onDelete = { viewModel.deleteQuote(quoteWithItems.quote) },
                            onSavePdf = {
                                val file = viewModel.generatePdf(quoteWithItems)
                                if (file != null) {
                                    pendingPdfFile = file
                                    val safeClientName = quoteWithItems.quote.clientName.replace(Regex("[^a-zA-Z0-9]"), "_")
                                    savePdfLauncher.launch("Cotizacion_${safeClientName}_${quoteWithItems.quote.id}.pdf")
                                }
                            },
                            onShare = {
                                val file = viewModel.generatePdf(quoteWithItems)
                                if (file != null) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir Presupuesto"))
                                }
                            },
                            onNavigate = { lat, lon ->
                                val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lon")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lon"))
                                    context.startActivity(webIntent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(6.dp, RoundedCornerShape(15.dp)),
        placeholder = { Text("Buscar cliente o servicio...", fontSize = 16.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricBlue) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = ElectricBlue,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(15.dp),
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp)
    )
}

@Composable
fun EliteHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(ElectricBlue, SoftCyan)
                )
            )
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp)
            )
            .padding(vertical = 40.dp, horizontal = 20.dp)
            .shadow(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(
                Icons.Default.Bolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(60.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "PRESUPUESTOS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun EliteQuoteCard(
    quoteWithItems: QuoteWithItems,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSavePdf: () -> Unit,
    onShare: () -> Unit,
    onNavigate: (Double, Double) -> Unit
) {
    val quote = quoteWithItems.quote

    // Optimization: Remember the formatter and formatted strings to avoid recalculation on recomposition
    val dateFormat = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val formattedDate = remember(quote.date) {
        dateFormat.format(Date(quote.date))
    }
    val formattedTotal = remember(quote.total) {
        String.format(Locale.getDefault(), "%,.2f", quote.total)
    }

    // Determine color based on theme for better legibility
    val totalColor = if (isSystemInDarkTheme()) VoltageYellow else ElectricBlue

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(25.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quote.clientName.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = ElectricBlue
                        )
                    )
                    Text(
                        text = quote.serviceType,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = ElectricBlue)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Eliminar", 
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (quote.latitude != null && quote.longitude != null) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
                ) {
                    MiniMapView(
                        modifier = Modifier.fillMaxSize(),
                        point = GeoPoint(quote.latitude, quote.longitude)
                    )
                    IconButton(
                        onClick = { onNavigate(quote.latitude, quote.longitude) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn, 
                            contentDescription = "Cómo llegar", 
                            tint = ElectricBlue, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL: $$formattedTotal",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = totalColor
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSavePdf,
                        modifier = Modifier.height(56.dp).weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("GUARDAR", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier.height(56.dp).weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoltageYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("COMPARTIR", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
