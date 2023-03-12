package com.besaba.anvarov.orentsd.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// таблица остатков
@Entity(tableName = "NomenData", indices = [Index("Barcode")])
data class NomenData(
    @ColumnInfo(name = "DateTime") val dateTime: String,
    @ColumnInfo(name = "FileName") val fileName: String,
    @ColumnInfo(name = "Barcode") val barcode: String,
    @ColumnInfo(name = "Name") val name: String,
    @ColumnInfo(name = "Price") var price: Double,
    @ColumnInfo(name = "Part") var part: Int,
    @ColumnInfo(name = "Available") val available: Int
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}