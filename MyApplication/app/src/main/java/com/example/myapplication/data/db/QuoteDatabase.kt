package com.example.myapplication.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.model.Quote
import com.example.myapplication.data.model.QuoteItem
import com.example.myapplication.data.model.Material
import com.example.myapplication.data.model.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Quote::class, QuoteItem::class, Material::class, Service::class],
    version = 8,
    exportSchema = false
)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteDatabase? = null

        fun getDatabase(context: Context): QuoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteDatabase::class.java,
                    "quote_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getDatabase(context).inventoryDao())
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        fun invalidateDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }

        private suspend fun populateInitialData(dao: InventoryDao) {
            val initialMaterials = listOf(
                // 🟦 TUBERÍA
                Material(name = "Tubo Conduit PVC 1/2", category = "🟦 TUBERÍA", defaultUnit = "pza"),
                Material(name = "Tubo Conduit PVC 3/4", category = "🟦 TUBERÍA", defaultUnit = "pza"),
                Material(name = "Manguera Poliflex 1/2", category = "🟦 TUBERÍA", defaultUnit = "m"),
                Material(name = "Canaleta 20x10", category = "🟦 TUBERÍA", defaultUnit = "m"),

                // 🟨 CABLEADO
                Material(name = "Cable Cal. 10 AWG", category = "🟨 CABLEADO", defaultUnit = "m"),
                Material(name = "Cable Cal. 12 AWG", category = "🟨 CABLEADO", defaultUnit = "m"),
                Material(name = "Cable Cal. 14 AWG", category = "🟨 CABLEADO", defaultUnit = "m"),

                // 🟩 CAJAS
                Material(name = "Caja Chalupa PVC", category = "🟩 CAJAS", defaultUnit = "pza"),
                Material(name = "Caja Registro 4x4", category = "🟩 CAJAS", defaultUnit = "pza"),

                // 🟥 ACCESORIOS
                Material(name = "Contacto Duplex Blanco", category = "🟥 ACCESORIOS", defaultUnit = "pza"),
                Material(name = "Apagador Sencillo", category = "🟥 ACCESORIOS", defaultUnit = "pza"),
                Material(name = "Contacto GFCI", category = "🟥 ACCESORIOS", defaultUnit = "pza"),
                Material(name = "Placa 2 ventanas", category = "🟥 ACCESORIOS", defaultUnit = "pza"),

                // 🟪 PROTECCIÓN
                Material(name = "Pastilla Termomagnética 20A", category = "🟪 PROTECCIÓN", defaultUnit = "pza"),
                Material(name = "Centro de Carga 2 Polos", category = "🟪 PROTECCIÓN", defaultUnit = "pza"),

                // ⚪ VARIOS
                Material(name = "Cinta de Aislar Super 33", category = "⚪ VARIOS", defaultUnit = "pza"),
                Material(name = "Taquetes y Tornillos", category = "⚪ VARIOS", defaultUnit = "pza")
            )

            initialMaterials.forEach { dao.insertMaterial(it) }

            // Servicios comunes
            val initialServices = listOf(
                Service(name = "Instalación de salida eléctrica", category = "INSTALACIÓN", defaultPrice = 150.0),
                Service(name = "Diagnóstico de falla", category = "MANTENIMIENTO", defaultPrice = 300.0),
                Service(name = "Cambio de interruptor", category = "REPARACIÓN", defaultPrice = 80.0)
            )
            initialServices.forEach { dao.insertService(it) }
        }
    }
}