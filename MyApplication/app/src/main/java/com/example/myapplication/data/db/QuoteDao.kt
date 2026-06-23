package com.example.myapplication.data.db

import androidx.room.*
import com.example.myapplication.data.model.Quote
import com.example.myapplication.data.model.QuoteItem
import com.example.myapplication.data.model.QuoteWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Insert
    suspend fun insertQuote(quote: Quote): Long

    @Insert
    suspend fun insertItems(items: List<QuoteItem>)

    @Update
    suspend fun updateQuote(quote: Quote)

    @Delete
    suspend fun deleteQuote(quote: Quote)

    @Query("DELETE FROM quote_items WHERE quoteId = :quoteId")
    suspend fun deleteItemsForQuote(quoteId: Long)

    @Transaction
    @Query("SELECT * FROM quotes ORDER BY date DESC")
    fun getAllQuotesWithItems(): Flow<List<QuoteWithItems>>

    @Transaction
    @Query("SELECT * FROM quotes WHERE id = :quoteId")
    suspend fun getQuoteById(quoteId: Long): QuoteWithItems?

    // 💡 MEJORA CRÍTICA: Transacción atómica para evitar datos huérfanos
    @Transaction
    suspend fun insertQuoteWithItems(quote: Quote, items: List<QuoteItem>) {
        val quoteId = insertQuote(quote)
        val itemsWithQuoteId = items.map { it.copy(quoteId = quoteId) }
        insertItems(itemsWithQuoteId)
    }

    @Transaction
    suspend fun updateQuoteWithItems(quote: Quote, items: List<QuoteItem>) {
        updateQuote(quote)
        deleteItemsForQuote(quote.id)
        val itemsWithQuoteId = items.map { it.copy(quoteId = quote.id) }
        insertItems(itemsWithQuoteId)
    }
}