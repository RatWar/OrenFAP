package com.besaba.anvarov.orentsd.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "NomenData", indices = [Index("Barcode")])
data class NomenData(
    @ColumnInfo(name = "Barcode") val barcode: String,
    @ColumnInfo(name = "Name") val name: String,
    @ColumnInfo(name = "EI") val ei: String,
    @ColumnInfo(name = "MZOO") val mzoo: Int
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}