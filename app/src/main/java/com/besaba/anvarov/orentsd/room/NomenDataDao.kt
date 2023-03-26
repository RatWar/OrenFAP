package com.besaba.anvarov.orentsd.room

import androidx.room.*

@Dao
interface NomenDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nomenData: NomenData)

    @Query("SELECT * from NomenData where SGTIN = :barcode")
    suspend fun getNomenByCode(barcode: String): NomenData?

    @Query("SELECT count(*) from NomenData")
    suspend fun countNomen(): Int

    @Query("SELECT Available from NomenData where SGTIN = :barcode")
    suspend fun countAvailable(barcode: String): Int?

    @Query("UPDATE NomenData set available = available + :available where id = :id")
    suspend fun updateAvailable(id: Long, available: Int)

    @Query("DELETE from NomenData")
    suspend fun delNomen()
}