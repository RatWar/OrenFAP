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
    @ColumnInfo(name = "EI") var ei: String,
    @ColumnInfo(name = "MZOO") var mzoo: Int
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}