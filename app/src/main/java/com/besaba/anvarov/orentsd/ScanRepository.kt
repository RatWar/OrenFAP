package com.besaba.anvarov.orentsd

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.besaba.anvarov.orentsd.room.*

class ScanRepository(private val scanDataDao: ScanDataDao) {

    var mNumDoc: Int = 0
    val mAllDocs: LiveData<List<DocumentData>> = scanDataDao.getAllDocs()

    fun getScans(numDoc: Int): LiveData<List<CountData>> = scanDataDao.getAllScans(numDoc)
    fun getCodes(numDoc: Int, barcode: String): LiveData<List<CodesData>> = scanDataDao.getAllCodes(numDoc, barcode)

    @WorkerThread
    suspend fun getSGTINfromDocument(numDoc: Int): List<String> =
        scanDataDao.getSGTINfromDocument(numDoc)

    @WorkerThread
    suspend fun getDuplicate(numDoc: Int, sgtin: String): Int? =
        scanDataDao.getDuplicate(numDoc, sgtin)

    @WorkerThread
    suspend fun insert(scanData: ScanData) {
        scanDataDao.insert(scanData)
    }

    @WorkerThread
    suspend fun deleteBarcode(numDoc: Int, barcode: String) {
        scanDataDao.delBarcode(numDoc, barcode)
    }

    @WorkerThread
    suspend fun deleteSGTIN(numDoc: Int, sgtin: String) {
        scanDataDao.delSGTIN(numDoc, sgtin)
    }

    @WorkerThread
    suspend fun deleteCodes(sgtin: String) {
        scanDataDao.delCodes(sgtin)
    }

    @WorkerThread
    suspend fun getNumberDocument(): Int {
        return scanDataDao.getNumberDocument()
    }

    @WorkerThread
    suspend fun deleteDoc(numDoc: Int) {
        scanDataDao.delDoc(numDoc)
    }

    @WorkerThread
    suspend fun getAll(): List<ScanData>? {
        return scanDataDao.getAll()
    }

}