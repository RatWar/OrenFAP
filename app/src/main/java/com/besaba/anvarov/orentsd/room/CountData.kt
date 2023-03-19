package com.besaba.anvarov.orentsd.room

import androidx.room.ColumnInfo

data class CountData(
        @ColumnInfo(name = "Barcode") val barcode: String,
        @ColumnInfo(name = "Name") val nameNomen: String,
        @ColumnInfo(name = "Part") val partNomen: Int,
        @ColumnInfo(name = "Price") val priceNomen: Double
)
