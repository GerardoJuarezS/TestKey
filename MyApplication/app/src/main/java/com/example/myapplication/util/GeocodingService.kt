package com.example.myapplication.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GeocodingResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

object GeocodingService {
    private const val NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"

    suspend fun search(query: String): List<GeocodingResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList<GeocodingResult>()

        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val urlString = "$NOMINATIM_URL?q=$encodedQuery&format=json&limit=5&addressdetails=1"

        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "MiPresupuestoApp/1.0")
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() } // ✅ CORREGIDO: 'connec tion'
                val jsonArray = JSONArray(response)
                val results = mutableListOf<GeocodingResult>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    results.add(
                        GeocodingResult(
                            displayName = obj.getString("display_name"),
                            latitude = obj.getDouble("lat"),
                            longitude = obj.getDouble("lon")
                        )
                    )
                }
                results
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace() // ✅ CORREGIDO: 'printStackTra ce()'
            emptyList()
        }
    }
}