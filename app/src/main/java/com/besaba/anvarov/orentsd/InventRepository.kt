package com.besaba.anvarov.orentsd

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.besaba.anvarov.orentsd.room.CodesData
import com.besaba.anvarov.orentsd.room.CountData
import com.besaba.anvarov.orentsd.room.DocumentData
import com.besaba.anvarov.orentsd.room.InventData
import com.besaba.anvarov.orentsd.room.InventDataDao

class InventRepository(private val inventDataDao: InventDataDao) {

    var mNumDoc: Int = 0
    val mAllDocs: LiveData<List<DocumentData>> = inventDataDao.getAllDocs()

    fun getScans(numDoc: Int): LiveData<List<CountData>> = inventDataDao.getAllScans(numDoc)
    fun getCodes(numDoc: Int, barcode: String): LiveData<List<CodesData>> = inventDataDao.getAllCodes(numDoc, barcode)

    @WorkerThread
    suspend fun getSGTINfromDocument(numDoc: Int): List<String> =
        inventDataDao.getSGTINfromDocument(numDoc)

    @WorkerThread
    suspend fun insert(inventData: InventData) {
        inventDataDao.insert(inventData)
    }

    @WorkerThread
    suspend fun deleteBarcodeId(id: Long) {
        inventDataDao.delBarcodeId(id)
    }

    @WorkerThread
    suspend fun deleteSGTIN(numDoc: Int, sgtin: String) {
        inventDataDao.delSGTIN(numDoc, sgtin)
    }

    @WorkerThread
    suspend fun deleteCodes(sgtin: String) {
        inventDataDao.delCodes(sgtin)
    }

    @WorkerThread
    suspend fun getNumberDocument(): Int {
        return inventDataDao.getNumberDocument()
    }

    @WorkerThread
    suspend fun deleteDoc(numDoc: Int) {
        inventDataDao.delDoc(numDoc)
    }

    @WorkerThread
    suspend fun getAll(): List<InventData>? {
        return inventDataDao.getAll()
    }

}