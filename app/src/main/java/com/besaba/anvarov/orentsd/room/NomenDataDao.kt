package com.besaba.anvarov.orentsd.room

import androidx.room.*

@Dao
interface NomenDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nomenData: NomenData)

    @Query("SELECT * from NomenData where Barcode = :barcode")
    suspend fun getNomenByCode(barcode: String): NomenData?

    @Query("SELECT count(*) from NomenData where Available > 0")
    suspend fun countNomen(): Int

    @Query("SELECT available from NomenData where Barcode = :barcode")
    suspend fun countAvailable(barcode: String): Int

    @Query("UPDATE NomenData set available = :available where Barcode = :barcode")
    suspend fun updateAvailable(barcode: String, available: Int)
}