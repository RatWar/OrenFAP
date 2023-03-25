package com.besaba.anvarov.orentsd.room

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE

@Dao
interface ScanDataDao {

    @Query("SELECT SGTIN from ScanData where NumDoc = :numDoc and Barcode = :barcode and NotTrans")
    fun getAllCodes(numDoc: Int, barcode: String): LiveData<List<CodesData>>

    @Query("SELECT Barcode as Barcode, substr(Name, 1, 30) as Name, Part as Part, Price as Price, id as id FROM ScanData where NumDoc = :numDoc and NotTrans")
    fun getAllScans(numDoc: Int): LiveData<List<CountData>>

    @Query("SELECT DateTime, NumDoc from ScanData where NotTrans group by NumDoc")
    fun getAllDocs(): LiveData<List<DocumentData>>

    @Query("SELECT * from ScanData where NotTrans order by NumDoc")
    suspend fun getAll(): List<ScanData>?

    @Query("SELECT IFNULL(max(NumDoc), '0') + 1 FROM scanData where NotTrans")
    suspend fun getNumberDocument(): Int

    @Query("SELECT SGTIN FROM scanData where NumDoc = :numDoc and NotTrans")
    suspend fun getSGTINfromDocument(numDoc: Int): List<String>

    @Query("UPDATE ScanData set NotTrans = False where NumDoc = :numDoc")
    suspend fun delDoc(numDoc: Int)

    @Query("DELETE from ScanData where id = :id and NotTrans")
    suspend fun delBarcodeId(id: Long)

    @Query("DELETE from ScanData where NumDoc = :numDoc and SGTIN = :sgtin and NotTrans")
    suspend fun delSGTIN(numDoc: Int, sgtin: String)

    @Query("DELETE from ScanData where SGTIN = :sgtin and NotTrans")
    suspend fun delCodes(sgtin: String)

    @Insert(onConflict = REPLACE)
    suspend fun insert(scanData: ScanData)

}