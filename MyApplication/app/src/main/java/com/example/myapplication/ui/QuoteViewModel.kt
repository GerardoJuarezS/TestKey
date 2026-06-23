package com.example.myapplication.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.db.QuoteDatabase
import com.example.myapplication.data.model.Material
import com.example.myapplication.data.model.Quote
import com.example.myapplication.data.model.QuoteItem
import com.example.myapplication.data.model.QuoteWithItems
import com.example.myapplication.data.model.Service
import com.example.myapplication.data.repository.QuoteRepository
import com.example.myapplication.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.util.Date
import java.util.concurrent.TimeUnit

// 💡 MEJORA: Data class específica para el perfil, no usar la entidad Quote para esto
data class TechProfile(
    val companyName: String = "",
    val technicianName: String = "",
    val companyPhone: String = "",
    val companyAddress: String = "",
    val logoUri: String = ""
)

class QuoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: QuoteRepository
    val allQuotes: StateFlow<List<QuoteWithItems>>
    val allMaterials: StateFlow<List<Material>>
    val allServices: StateFlow<List<Service>>

    private val sharedPrefs = application.getSharedPreferences("tech_profile", Context.MODE_PRIVATE)
    private val activationPrefs = application.getSharedPreferences("activation_prefs", Context.MODE_PRIVATE)

    init {
        val db = QuoteDatabase.getDatabase(application)
        repository = QuoteRepository(db.quoteDao(), db.inventoryDao())

        allQuotes = repository.allQuotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allMaterials = repository.allMaterials.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allServices = repository.allServices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Inicializar fecha de inicio si no existe
        if (!activationPrefs.contains("trial_start_date")) {
            activationPrefs.edit().putLong("trial_start_date", System.currentTimeMillis()).apply()
        }
    }

    fun getTrialDaysRemaining(): Int {
        val startDate = activationPrefs.getLong("trial_start_date", System.currentTimeMillis())
        val diffInMs = System.currentTimeMillis() - startDate
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs).toInt()
        return (14 - diffInDays).coerceAtLeast(0)
    }

    fun isTrialExpired(): Boolean {
        return getTrialDaysRemaining() <= 0
    }

    fun getDeviceId(): String {
        return Settings.Secure.getString(getApplication<Application>().contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_id"
    }

    fun saveQuote(quote: Quote, items: List<QuoteItem>) {
        viewModelScope.launch {
            if (quote.id == 0L) repository.insertQuoteWithItems(quote, items)
            else repository.updateQuoteWithItems(quote, items)

            // Guardar perfil del técnico para autocompletar futuras cotizaciones
            sharedPrefs.edit().apply {
                putString("companyName", quote.companyName)
                putString("technicianName", quote.technicianName)
                putString("companyPhone", quote.companyPhone)
                putString("companyAddress", quote.companyAddress)
                putString("logoUri", quote.logoUri)
                apply()
            }
        }
    }

    fun getSavedTechProfile(): TechProfile {
        return TechProfile(
            companyName = sharedPrefs.getString("companyName", "") ?: "",
            technicianName = sharedPrefs.getString("technicianName", "") ?: "",
            companyPhone = sharedPrefs.getString("companyPhone", "") ?: "",
            companyAddress = sharedPrefs.getString("companyAddress", "") ?: "",
            logoUri = sharedPrefs.getString("logoUri", "") ?: ""
        )
    }

    fun deleteQuote(quote: Quote) = viewModelScope.launch { repository.deleteQuote(quote) }

    suspend fun getQuoteById(id: Long): QuoteWithItems? = repository.getQuoteById(id)

    fun saveMaterial(material: Material) = viewModelScope.launch {
        if (material.id == 0L) repository.insertMaterial(material) else repository.updateMaterial(material)
    }
    fun deleteMaterial(material: Material) = viewModelScope.launch { repository.deleteMaterial(material) }
    fun saveService(service: Service) = viewModelScope.launch {
        if (service.id == 0L) repository.insertService(service) else repository.updateService(service)
    }
    fun deleteService(service: Service) = viewModelScope.launch { repository.deleteService(service) }

    fun isActivated(): Boolean {
        return activationPrefs.getBoolean("is_activated", false)
    }

    fun setActivated(activated: Boolean) {
        activationPrefs.edit().putBoolean("is_activated", activated).apply()
    }

    fun validateLicenseKeyRemote(key: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Usamos la URL estándar de GitHub Raw
                val urlString = "https://raw.githubusercontent.com/GerardoJuarezS/TestKey/main/KEY.txt"
                val url = URL(urlString)
                
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                // IMPORTANTE: GitHub a veces requiere un User-Agent
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    var content = connection.inputStream.bufferedReader().use { it.readText() }
                    
                    // 1. Eliminar el BOM (Byte Order Mark) si existe (común en archivos Windows)
                    content = content.removePrefix("\uFEFF")
                    
                    // 2. Limpiar el ID local y la clave ingresada
                    val cleanDeviceId = getDeviceId().replace(" ", "").trim().uppercase()
                    
                    // Solo aceptamos los primeros 10 caracteres alfanuméricos de la clave ingresada
                    val cleanKey = key.filter { it.isLetterOrDigit() }.trim().uppercase()
                    
                    if (cleanKey.length != 10) {
                        withContext(Dispatchers.Main) {
                            onResult(false, "La clave debe tener exactamente 10 caracteres")
                        }
                        return@launch
                    }
                    
                    val targetEntry = "$cleanKey:$cleanDeviceId"
                    
                    // 3. Procesar el archivo de forma ultra-robusta
                    val keysInFile = content.lines()
                        .map { it.replace(" ", "").trim().uppercase() }
                        .filter { it.isNotBlank() }
                    
                    // 4. Comparación final
                    val isValid = keysInFile.any { it == targetEntry }
                    
                    withContext(Dispatchers.Main) {
                        if (isValid) onResult(true, null) 
                        else onResult(false, "Clave o ID no encontrados en el servidor. Verifique que el ID en GitHub sea: $cleanDeviceId")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Error del servidor: $responseCode (Verifica que el repo sea público)")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false, "Error de conexión: ${e.localizedMessage}")
                }
            }
        }
    }

    fun generatePdf(quoteWithItems: QuoteWithItems): File? {
        return PdfGenerator.generateQuotePdf(getApplication(), quoteWithItems)
    }

    fun savePdfToUri(file: File, uri: android.net.Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(file).use { input ->
                        input.copyTo(output)
                    }
                }
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun backupDatabase(destinationUri: android.net.Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Forzar el cierre de la base de datos o checkpoint para asegurar que todo esté en el archivo .db
                QuoteDatabase.getDatabase(getApplication()).close()
                
                val dbFile = getApplication<Application>().getDatabasePath("quote_database")
                val context = getApplication<Application>()
                
                context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                    FileInputStream(dbFile).use { input ->
                        input.copyTo(output)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun restoreDatabase(sourceUri: android.net.Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Cerrar e invalidar la instancia actual de la base de datos
                QuoteDatabase.invalidateDatabase()

                val context = getApplication<Application>()
                val dbFile = context.getDatabasePath("quote_database")

                // 2. Sobrescribir el archivo de la base de datos
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // 3. Eliminar archivos temporales de SQLite (WAL/SHM)
                val walFile = File(dbFile.path + "-wal")
                val shmFile = File(dbFile.path + "-shm")
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()

                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }
}