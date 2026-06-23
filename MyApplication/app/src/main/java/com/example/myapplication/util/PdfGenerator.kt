package com.example.myapplication.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.myapplication.data.model.QuoteWithItems
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil
import kotlin.math.max

object PdfGenerator {
    fun generateQuotePdf(context: Context, quoteWithItems: QuoteWithItems): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val headerPaint = Paint()
        val boxPaint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas // ✅ CORREGIDO: 'pa ge'

        val quote = quoteWithItems.quote
        val items = quoteWithItems.items
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        // --- HEADER ---
        headerPaint.color = android.graphics.Color.parseColor("#007BFF") // ElectricBlue
        canvas.drawRect(0f, 0f, 595f, 100f, headerPaint)
        headerPaint.color = android.graphics.Color.parseColor("#FFC300") // VoltageYellow
        canvas.drawRect(0f, 100f, 595f, 105f, headerPaint)

        if (quote.logoUri.isNotEmpty()) {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(quote.logoUri))
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) { // ✅ CORREGIDO: 'bitm ap'
                    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val targetHeight = 70f
                    val targetWidth = targetHeight * ratio
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth.toInt(), targetHeight.toInt(), true) // ✅ CORREGIDO: 'toI nt()'
                    canvas.drawBitmap(scaledBitmap, 40f, 15f, null)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        titlePaint.textSize = 24f
        titlePaint.isFakeBoldText = true
        titlePaint.color = android.graphics.Color.WHITE
        titlePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(if(quote.companyName.isNotEmpty()) quote.companyName.uppercase() else "PRESUPUESTO", 550f, 45f, titlePaint) // ✅ CORREGIDO: 'isN otEmpty()'

        paint.textSize = 9f
        paint.color = android.graphics.Color.WHITE
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${quote.technicianName} | ${quote.companyPhone}", 550f, 65f, paint)
        canvas.drawText(quote.companyAddress, 550f, 80f, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.color = android.graphics.Color.BLACK

        // --- GRID INFO ---
        var yPos = 140f
        paint.textSize = 11f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.parseColor("#1A237E") // ✅ CORREGIDO: 'graph ics'
        canvas.drawText("DATOS DEL CLIENTE", 40f, yPos, paint)

        paint.color = android.graphics.Color.BLACK
        paint.isFakeBoldText = false
        paint.textSize = 10f
        yPos += 20f
        canvas.drawText(quote.clientName.uppercase(), 40f, yPos, paint) // ✅ CORREGIDO: 'clientN ame'
        yPos += 15f
        if(quote.clientTaxId.isNotEmpty()) {
            canvas.drawText("RFC: ${quote.clientTaxId}", 40f, yPos, paint)
            yPos += 15f
        }
        canvas.drawText(quote.clientAddress, 40f, yPos, paint)

        val rightColX = 350f
        var yPosRight = 140f
        paint.color = android.graphics.Color.parseColor("#1A237E")
        paint.isFakeBoldText = true
        canvas.drawText("DETALLES DEL SERVICIO", rightColX, yPosRight, paint)

        paint.color = android.graphics.Color.BLACK
        paint.isFakeBoldText = false
        yPosRight += 20f
        canvas.drawText("Fecha: ${dateFormat.format(Date(quote.date))} | Hora: ${quote.time}", rightColX, yPosRight, paint)
        yPosRight += 15f
        canvas.drawText("WhatsApp Cliente: ${quote.clientPhone}", rightColX, yPosRight, paint)

        // --- CONCEPTS (Two Columns) ---
        yPos = 240f
        paint.color = android.graphics.Color.parseColor("#2D3436")
        canvas.drawRect(40f, yPos, 280f, yPos + 18f, paint)
        canvas.drawRect(315f, yPos, 555f, yPos + 18f, paint)

        paint.color = android.graphics.Color.WHITE
        paint.textSize = 8f // ✅ CORREGIDO: 'te xtSize'
        paint.isFakeBoldText = true
        canvas.drawText("DESCRIPCIÓN MANO DE OBRA", 45f, yPos + 12f, paint)
        canvas.drawText("TOTAL", 245f, yPos + 12f, paint)
        canvas.drawText("DESCRIPCIÓN MATERIALES", 320f, yPos + 12f, paint)
        canvas.drawText("TOTAL", 520f, yPos + 12f, paint)

        paint.color = android.graphics.Color.BLACK
        paint.isFakeBoldText = false
        yPos += 30f

        val midPoint = ceil(items.size / 2.0).toInt() // ✅ CORREGIDO: 'toI nt()'
        val rowHeight = 14f
        var maxTableY = yPos

        for (i in items.indices) {
            val item = items[i]
            val isLeft = i < midPoint
            val x = if (isLeft) 45f else 320f
            val xPrice = if (isLeft) 245f else 520f
            val currentY = yPos + (if (isLeft) i else i - midPoint) * rowHeight

            val typeTag = if(item.itemType == "LABOR") "[M.O] " else "[MAT] "
            val displayText = if(item.itemType == "MATERIAL") {
                val qty = if(item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format(Locale.getDefault(), "%.1f", item.quantity)
                "$typeTag $qty ${item.unit} - ${item.description}"
            } else {
                "$typeTag ${item.description}"
            }

            canvas.drawText(displayText.take(28), x, currentY, paint)
            canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", item.quantity * item.price)}", xPrice, currentY, paint)

            paint.color = android.graphics.Color.parseColor("#EEEEEE")
            canvas.drawLine(if (isLeft) 40f else 315f, currentY + 3f, if (isLeft) 280f else 555f, currentY + 3f, paint)
            paint.color = android.graphics.Color.BLACK

            if (currentY > maxTableY) maxTableY = currentY
        }

        // --- FOOTER SECTION ---
        val footerY = max(maxTableY + 40f, 650f) // ✅ CORREGIDO: 'Ma th.max'

        paint.color = android.graphics.Color.GRAY
        paint.textSize = 8f
        paint.isFakeBoldText = false
        canvas.drawText("Este documento es un presupuesto estimado. Las garantías aplican", 40f, footerY + 20f, paint)
        canvas.drawText("directamente con el fabricante en materiales y 30 días en mano de", 40f, footerY + 32f, paint)
        canvas.drawText("obra. Vigencia: 15 días naturales.", 40f, footerY + 44f, paint)

        boxPaint.color = android.graphics.Color.parseColor("#F8F9FA")
        canvas.drawRect(330f, footerY, 555f, footerY + 110f, boxPaint)

        paint.color = android.graphics.Color.BLACK
        paint.textSize = 9f
        var ySum = footerY + 18f
        canvas.drawText("Subtotal Mano de Obra:", 340f, ySum, paint) // ✅ CORREGIDO: 'c anvas'
        canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", quote.laborTotal)}", 480f, ySum, paint)

        ySum += 14f
        canvas.drawText("Subtotal Suministros:", 340f, ySum, paint)
        canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", quote.materialsTotal)}", 480f, ySum, paint)

        ySum += 14f
        canvas.drawText("Gastos de Operación:", 340f, ySum, paint)
        canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", quote.laborCostGeneral + quote.logisticsCost)}", 480f, ySum, paint)

        ySum += 8f
        canvas.drawLine(340f, ySum, 545f, ySum, paint)
        ySum += 14f
        canvas.drawText("Subtotal:", 340f, ySum, paint)
        canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", quote.subtotal)}", 480f, ySum, paint)

        ySum += 14f
        canvas.drawText("IVA (${(quote.taxRate * 100).toInt()}%):", 340f, ySum, paint)
        canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", quote.taxAmount)}", 480f, ySum, paint)

        ySum += 20f
        paint.color = android.graphics.Color.parseColor("#1A237E")
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL:", 340f, ySum, paint)
        canvas.drawText("$${String.format(Locale.getDefault(), "%,.2f", quote.total)}", 450f, ySum, paint)

        paint.color = android.graphics.Color.parseColor("#CCCCCC")
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("DESARROLLADO POR GERARDO JUAREZ SALMORAN", 297f, 820f, paint)

        pdfDocument.finishPage(page)

        val safeClientName = quote.clientName.replace(Regex("[^a-zA-Z0-9]"), "_")
        val file = File(context.getExternalFilesDir(null), "Cotizacion_${safeClientName}_${quote.id}.pdf")

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close() // ✅ CORREGIDO: 'pd fDocument'
            null
        }
    }
}