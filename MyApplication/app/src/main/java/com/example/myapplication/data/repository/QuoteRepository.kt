package com.example.myapplication.data.repository

import com.example.myapplication.data.db.InventoryDao
import com.example.myapplication.data.db.QuoteDao
import com.example.myapplication.data.model.Material
import com.example.myapplication.data.model.Quote
import com.example.myapplication.data.model.QuoteItem
import com.example.myapplication.data.model.QuoteWithItems
import com.example.myapplication.data.model.Service
import kotlinx.coroutines.flow.Flow

class QuoteRepository(
    private val quoteDao: QuoteDao,
    private val inventoryDao: InventoryDao
) {
    val allQuotes: Flow<List<QuoteWithItems>> = quoteDao.getAllQuotesWithItems()

    suspend fun insertQuoteWithItems(quote: Quote, items: List<QuoteItem>) {
        quoteDao.insertQuoteWithItems(quote, items) // ✅ Delegado al DAO transaccional
    }

    suspend fun getQuoteById(id: Long): QuoteWithItems? = quoteDao.getQuoteById(id)

    suspend fun deleteQuote(quote: Quote) = quoteDao.deleteQuote(quote)

    suspend fun updateQuoteWithItems(quote: Quote, items: List<QuoteItem>) {
        quoteDao.updateQuoteWithItems(quote, items) // ✅ Delegado al DAO transaccional
    }

    val allMaterials: Flow<List<Material>> = inventoryDao.getAllMaterials()
    suspend fun insertMaterial(material: Material) = inventoryDao.insertMaterial(material)
    suspend fun updateMaterial(material: Material) = inventoryDao.updateMaterial(material)
    suspend fun deleteMaterial(material: Material) = inventoryDao.deleteMaterial(material)

    val allServices: Flow<List<Service>> = inventoryDao.getAllServices()
    suspend fun insertService(service: Service) = inventoryDao.insertService(service)
    suspend fun updateService(service: Service) = inventoryDao.updateService(service)
    suspend fun deleteService(service: Service) = inventoryDao.deleteService(service)
}