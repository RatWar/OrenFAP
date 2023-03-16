package com.besaba.anvarov.orentsd.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ScanData", indices = [Index("NumDoc")])
data class ScanData(
    @ColumnInfo(name = "DateTime") val dateTime: String,
    @ColumnInfo(name = "NumDoc") val numDoc: Int,
    @ColumnInfo(name = "Barcode") var scanCode: String,
    @ColumnInfo(name = "SGTIN") var sgtin: String,
    @ColumnInfo(name = "Name") var nameNomen: String,
    @ColumnInfo(name = "Price") var price: Double,
    @ColumnInfo(name = "Part") var part: Int
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}