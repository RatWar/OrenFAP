package com.besaba.anvarov.orentsd.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// таблица остатков
@Entity(tableName = "NomenData", indices = [Index("SGTIN")])
data class NomenData(
    @ColumnInfo(name = "DateTime") val dateTime: String,
    @ColumnInfo(name = "FileName") val fileName: String,
    @ColumnInfo(name = "Barcode") val barcode: String,  // полный код
    @ColumnInfo(name = "SGTIN") var sgtin: String,      // 31 символ
    @ColumnInfo(name = "Name") val name: String,
    @ColumnInfo(name = "Price") var price: Double,
    @ColumnInfo(name = "Part") var part: Int,
    @ColumnInfo(name = "Available") val available: Int
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}