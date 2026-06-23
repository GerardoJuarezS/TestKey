package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Perfil Técnico
    val companyName: String = "",
    val technicianName: String = "",
    val companyPhone: String = "",
    val companyAddress: String = "",
    val logoUri: String = "",
    // Información del Cliente
    val clientName: String = "",
    val clientTaxId: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val serviceType: String = "",
    val date: Long = System.currentTimeMillis(),
    val time: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    // Gastos y Totales
    val laborCostGeneral: Double = 0.0,
    val logisticsCost: Double = 0.0,
    val taxRate: Double = 0.0,
    val laborTotal: Double = 0.0,
    val materialsTotal: Double = 0.0,
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val total: Double = 0.0,
    val observations: String = ""
)

@Entity(
    tableName = "quote_items", // ✅ CORREGIDO: Espacio eliminado
    foreignKeys = [
        ForeignKey(
            entity = Quote::class,
            parentColumns = ["id"],       // ✅ CORREGIDO: Espacio eliminado
            childColumns = ["quoteId"],   // ✅ CORREGIDO: Espacio eliminado
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quoteId"])] // ✅ CORREGIDO: Espacio eliminado
)
data class QuoteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteId: Long,
    val itemType: String, // "LABOR" o "MATERIAL"
    val description: String,
    val quantity: Double,
    val unit: String = "pza", // ✅ CORREGIDO: Valor por defecto limpio
    val price: Double
)

data class QuoteWithItems(
    @Embedded val quote: Quote,
    @Relation(
        parentColumn = "id",
        entityColumn = "quoteId"
    )
    val items: List<QuoteItem>
)