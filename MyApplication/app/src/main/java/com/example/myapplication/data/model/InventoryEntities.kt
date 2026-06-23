package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "materials",
    indices = [Index(value = ["category"])] // 💡 MEJORA: Acelera búsquedas por categoría
)
data class Material(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "",
    val defaultUnit: String = "pza",
    val defaultPrice: Double = 0.0
)

@Entity(
    tableName = "services",
    indices = [Index(value = ["category"])]
)
data class Service(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "",
    val defaultPrice: Double = 0.0,
    val description: String = ""
)