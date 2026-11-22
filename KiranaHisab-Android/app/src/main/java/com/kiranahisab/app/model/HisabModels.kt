package com.kiranahisab.app.model

import com.kiranahisab.app.data.BaseUnit
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.math.roundToLong

enum class CalcMode {
    PRICE_TO_QTY,
    QTY_TO_PRICE
}

enum class QtySubUnit(val label: String) {
    G("g"),
    KG("kg"),
    ML("ml"),
    L("L"),
    PCS("pcs")
}

data class QuickQtyOption(
    val label: String,
    val value: String,
    val subUnit: QtySubUnit
)

data class BillLineItem(
    val id: String = UUID.randomUUID().toString(),
    val itemId: Long,
    val itemName: String,
    val quantityDisplay: String,
    val price: Double
)

object HisabCalculator {
    val QUICK_RUPEES = listOf(10, 20, 50, 100, 200, 500)

    fun defaultSubUnit(baseUnit: BaseUnit): QtySubUnit = when (baseUnit) {
        BaseUnit.KG -> QtySubUnit.G
        BaseUnit.LITER -> QtySubUnit.ML
        BaseUnit.PIECE -> QtySubUnit.PCS
    }

    fun availableSubUnits(baseUnit: BaseUnit): List<QtySubUnit> = when (baseUnit) {
        BaseUnit.KG -> listOf(QtySubUnit.G, QtySubUnit.KG)
        BaseUnit.LITER -> listOf(QtySubUnit.ML, QtySubUnit.L)
        BaseUnit.PIECE -> listOf(QtySubUnit.PCS)
    }

    fun quickQtyOptions(baseUnit: BaseUnit): List<QuickQtyOption> = when (baseUnit) {
        BaseUnit.KG -> listOf(
            QuickQtyOption("50g", "50", QtySubUnit.G),
            QuickQtyOption("100g", "100", QtySubUnit.G),
            QuickQtyOption("250g", "250", QtySubUnit.G),
            QuickQtyOption("500g", "500", QtySubUnit.G),
            QuickQtyOption("1kg", "1", QtySubUnit.KG),
            QuickQtyOption("2kg", "2", QtySubUnit.KG)
        )
        BaseUnit.LITER -> listOf(
            QuickQtyOption("100ml", "100", QtySubUnit.ML),
            QuickQtyOption("250ml", "250", QtySubUnit.ML),
            QuickQtyOption("500ml", "500", QtySubUnit.ML),
            QuickQtyOption("1L", "1", QtySubUnit.L)
        )
        BaseUnit.PIECE -> listOf(
            QuickQtyOption("1", "1", QtySubUnit.PCS),
            QuickQtyOption("2", "2", QtySubUnit.PCS),
            QuickQtyOption("5", "5", QtySubUnit.PCS),
            QuickQtyOption("10", "10", QtySubUnit.PCS)
        )
    }

    fun formatRupees(amount: Double): String {
        if (!amount.isFinite() || amount <= 0.0) return "₹ 0"
        val rounded = (amount * 100.0).roundToLong() / 100.0
        return if (rounded % 1.0 == 0.0) {
            "₹ ${rounded.toLong()}"
        } else {
            String.format(Locale.US, "₹ %.2f", rounded)
        }
    }

    fun formatRate(rate: Double): String {
        val rounded = (rate * 100.0).roundToLong() / 100.0
        return if (rounded % 1.0 == 0.0) "${rounded.toLong()}" else String.format(Locale.US, "%.2f", rounded)
    }

    data class QtyResult(
        val display: String,
        val billLabel: String,
        val isValid: Boolean
    )

    fun calculateQuantityFromPrice(rupees: Double, rate: Double, unit: BaseUnit): QtyResult {
        if (!rupees.isFinite() || rupees <= 0.0 || !rate.isFinite() || rate <= 0.0) {
            val zero = when (unit) {
                BaseUnit.KG -> "0 g"
                BaseUnit.LITER -> "0 ml"
                BaseUnit.PIECE -> "0 pcs"
            }
            return QtyResult(zero, zero, false)
        }

        return when (unit) {
            BaseUnit.KG -> {
                val totalGrams = ((rupees / rate) * 1000.0).roundToInt()
                if (totalGrams <= 0) {
                    QtyResult("0 g", "0 g", false)
                } else if (totalGrams < 1000) {
                    QtyResult("$totalGrams g", "$totalGrams g", true)
                } else {
                    val kgDecimal = String.format(Locale.US, "%.3f", totalGrams / 1000.0)
                    val wholeKg = totalGrams / 1000
                    val remGrams = totalGrams % 1000
                    if (remGrams == 0) {
                        QtyResult("$wholeKg kg", "$wholeKg kg", true)
                    } else {
                        val breakdown = "$wholeKg kg $remGrams g"
                        QtyResult("$kgDecimal kg ($breakdown)", breakdown, true)
                    }
                }
            }
            BaseUnit.LITER -> {
                val totalMl = ((rupees / rate) * 1000.0).roundToInt()
                if (totalMl <= 0) {
                    QtyResult("0 ml", "0 ml", false)
                } else if (totalMl < 1000) {
                    QtyResult("$totalMl ml", "$totalMl ml", true)
                } else {
                    val literDecimal = String.format(Locale.US, "%.3f", totalMl / 1000.0)
                    val wholeL = totalMl / 1000
                    val remMl = totalMl % 1000
                    if (remMl == 0) {
                        QtyResult("$wholeL L", "$wholeL L", true)
                    } else {
                        val breakdown = "$wholeL L $remMl ml"
                        QtyResult("$literDecimal L ($breakdown)", breakdown, true)
                    }
                }
            }
            BaseUnit.PIECE -> {
                val pieces = ((rupees / rate) * 100.0).roundToLong() / 100.0
                if (pieces <= 0.0) {
                    QtyResult("0 pcs", "0 pcs", false)
                } else {
                    val label = if (pieces % 1.0 == 0.0) {
                        val count = pieces.toLong()
                        if (count == 1L) "1 pc" else "$count pcs"
                    } else {
                        "$pieces pcs"
                    }
                    QtyResult(label, label, true)
                }
            }
        }
    }

    data class PriceResult(
        val price: Double,
        val formattedPrice: String,
        val quantityLabel: String,
        val isValid: Boolean
    )

    fun calculatePriceFromQuantity(
        qty: Double,
        subUnit: QtySubUnit,
        rate: Double,
        baseUnit: BaseUnit
    ): PriceResult {
        if (!qty.isFinite() || qty <= 0.0 || !rate.isFinite() || rate <= 0.0) {
            return PriceResult(0.0, "₹ 0", "0 ${subUnit.label}", false)
        }
        val qtyInBase = when {
            baseUnit == BaseUnit.KG && subUnit == QtySubUnit.G -> qty / 1000.0
            baseUnit == BaseUnit.LITER && subUnit == QtySubUnit.ML -> qty / 1000.0
            else -> qty
        }
        val roundedPrice = ((qtyInBase * rate) * 100.0).roundToLong() / 100.0
        val qtyStr = if (qty % 1.0 == 0.0) "${qty.toLong()}" else "$qty"
        val unitLabel = if (subUnit == QtySubUnit.PCS && qty == 1.0) "pc" else subUnit.label
        return PriceResult(
            price = roundedPrice,
            formattedPrice = formatRupees(roundedPrice),
            quantityLabel = "$qtyStr $unitLabel",
            isValid = roundedPrice > 0.0
        )
    }
}
