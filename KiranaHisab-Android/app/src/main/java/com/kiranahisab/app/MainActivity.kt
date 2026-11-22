package com.kiranahisab.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kiranahisab.app.model.HisabCalculator
import com.kiranahisab.app.ui.KiranaViewModel
import com.kiranahisab.app.ui.screens.HisabScreen
import com.kiranahisab.app.ui.screens.ItemsScreen
import com.kiranahisab.app.ui.theme.KiranaHisabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiranaHisabTheme {
                KiranaHisabApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiranaHisabApp(viewModel: KiranaViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Hisab (Default), 1 = Items
    val items by viewModel.items.collectAsState()
    val selectedItemId by viewModel.selectedItemId.collectAsState()
    val calcMode by viewModel.calcMode.collectAsState()
    val rupeeInput by viewModel.rupeeInput.collectAsState()
    val qtyInput by viewModel.qtyInput.collectAsState()
    val qtySubUnit by viewModel.qtySubUnit.collectAsState()
    val billItems by viewModel.billItems.collectAsState()
    val inventoryQuery by viewModel.inventoryQuery.collectAsState()

    val selectedItem = remember(items, selectedItemId) {
        items.firstOrNull { it.id == selectedItemId } ?: items.firstOrNull()
    }

    LaunchedEffect(selectedItem?.id, selectedItem?.unit) {
        if (selectedItem != null && selectedItemId == null) {
            viewModel.selectItem(selectedItem)
        } else if (selectedItem != null) {
            val allowed = HisabCalculator.availableSubUnits(selectedItem.unit)
            if (qtySubUnit !in allowed) {
                viewModel.setQtySubUnit(HisabCalculator.defaultSubUnit(selectedItem.unit))
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Kirana Hisab",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                    label = { Text("Hisab", fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                    label = { Text("Items", fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            HisabScreen(
                items = items,
                selectedItem = selectedItem,
                calcMode = calcMode,
                rupeeInput = rupeeInput,
                qtyInput = qtyInput,
                qtySubUnit = qtySubUnit,
                billItems = billItems,
                onSelectItem = viewModel::selectItem,
                onModeChange = viewModel::setCalcMode,
                onRupeeInputChange = viewModel::updateRupeeInput,
                onQtyInputChange = viewModel::updateQtyInput,
                onSubUnitChange = viewModel::setQtySubUnit,
                onApplyQuickQty = viewModel::applyQuickQty,
                onAddToBill = viewModel::addCurrentCalculationToBill,
                onRemoveBillItem = viewModel::removeBillLine,
                onClearBill = viewModel::clearBill,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            ItemsScreen(
                items = items,
                query = inventoryQuery,
                onQueryChange = viewModel::updateInventoryQuery,
                onAddItem = viewModel::addItem,
                onUpdateItem = viewModel::updateItem,
                onDeleteItem = viewModel::deleteItem,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
