package com.kiranahisab.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiranahisab.app.data.BaseUnit
import com.kiranahisab.app.data.KiranaDatabase
import com.kiranahisab.app.data.KiranaItem
import com.kiranahisab.app.data.KiranaRepository
import com.kiranahisab.app.model.BillLineItem
import com.kiranahisab.app.model.CalcMode
import com.kiranahisab.app.model.HisabCalculator
import com.kiranahisab.app.model.QtySubUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KiranaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = KiranaRepository(
        KiranaDatabase.getInstance(application).kiranaDao()
    )

    val items: StateFlow<List<KiranaItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedItemId = MutableStateFlow<Long?>(null)
    val selectedItemId: StateFlow<Long?> = _selectedItemId.asStateFlow()

    private val _calcMode = MutableStateFlow(CalcMode.PRICE_TO_QTY)
    val calcMode: StateFlow<CalcMode> = _calcMode.asStateFlow()

    private val _rupeeInput = MutableStateFlow("50")
    val rupeeInput: StateFlow<String> = _rupeeInput.asStateFlow()

    private val _qtyInput = MutableStateFlow("250")
    val qtyInput: StateFlow<String> = _qtyInput.asStateFlow()

    private val _qtySubUnit = MutableStateFlow(QtySubUnit.G)
    val qtySubUnit: StateFlow<QtySubUnit> = _qtySubUnit.asStateFlow()

    private val _billItems = MutableStateFlow<List<BillLineItem>>(emptyList())
    val billItems: StateFlow<List<BillLineItem>> = _billItems.asStateFlow()

    private val _inventoryQuery = MutableStateFlow("")
    val inventoryQuery: StateFlow<String> = _inventoryQuery.asStateFlow()

    fun selectItem(item: KiranaItem) {
        _selectedItemId.value = item.id
        _qtySubUnit.value = HisabCalculator.defaultSubUnit(item.unit)
    }

    fun setCalcMode(mode: CalcMode) {
        _calcMode.value = mode
    }

    fun updateRupeeInput(value: String) {
        _rupeeInput.value = value
    }

    fun updateQtyInput(value: String) {
        _qtyInput.value = value
    }

    fun setQtySubUnit(subUnit: QtySubUnit) {
        _qtySubUnit.value = subUnit
    }

    fun applyQuickQty(value: String, subUnit: QtySubUnit) {
        _qtyInput.value = value
        _qtySubUnit.value = subUnit
    }

    fun updateInventoryQuery(query: String) {
        _inventoryQuery.value = query
    }

    fun addCurrentCalculationToBill(item: KiranaItem) {
        if (_calcMode.value == CalcMode.PRICE_TO_QTY) {
            val rupees = _rupeeInput.value.toDoubleOrNull() ?: return
            val qtyResult = HisabCalculator.calculateQuantityFromPrice(rupees, item.rate, item.unit)
            if (!qtyResult.isValid) return
            _billItems.update { list ->
                list + BillLineItem(
                    itemId = item.id,
                    itemName = item.name,
                    quantityDisplay = qtyResult.billLabel,
                    price = rupees
                )
            }
        } else {
            val qty = _qtyInput.value.toDoubleOrNull() ?: return
            val priceResult = HisabCalculator.calculatePriceFromQuantity(
                qty = qty,
                subUnit = _qtySubUnit.value,
                rate = item.rate,
                baseUnit = item.unit
            )
            if (!priceResult.isValid) return
            _billItems.update { list ->
                list + BillLineItem(
                    itemId = item.id,
                    itemName = item.name,
                    quantityDisplay = priceResult.quantityLabel,
                    price = priceResult.price
                )
            }
        }
    }

    fun removeBillLine(lineId: String) {
        _billItems.update { list -> list.filterNot { it.id == lineId } }
    }

    fun clearBill() {
        _billItems.value = emptyList()
    }

    fun addItem(name: String, rate: Double, unit: BaseUnit) {
        viewModelScope.launch {
            repository.addItem(name, rate, unit)
        }
    }

    fun updateItem(item: KiranaItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: KiranaItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}
