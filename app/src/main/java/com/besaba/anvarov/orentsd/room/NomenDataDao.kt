package com.besaba.anvarov.orentsd.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NomenDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nomenData: NomenData)

    @Query("SELECT * from NomenData where Barcode = :barcode")
    suspend fun getNomenByCode(barcode: String): NomenData?

    @Query("DELETE from NomenData")
    suspend fun delNomen()

    @Query("SELECT count(*) from NomenData")
    suspend fun countNomen(): Int
}