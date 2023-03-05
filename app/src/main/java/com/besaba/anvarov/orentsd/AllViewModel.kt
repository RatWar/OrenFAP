package com.besaba.anvarov.orentsd

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.besaba.anvarov.orentsd.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AllViewModel(application: Application) : AndroidViewModel(application) {

    private val mScanRepository: ScanRepository
    private val mNomenRepository: NomenRepository
    var mAllCodes: LiveData<List<CodesData>>
    var mAllScans: LiveData<List<CountData>>
    val mAllDocs: LiveData<List<DocumentData>>

    init {
        val scansDao = TSDDatabase.getDatabase(application).scanDataDao()
        val nomenDao = TSDDatabase.getDatabase(application).nomenDataDao()
        mScanRepository = ScanRepository(scansDao)
        mNomenRepository = NomenRepository(nomenDao)
        mAllCodes = mScanRepository.getCodes(0, "")
        mAllScans = mScanRepository.getScans(0)
        mAllDocs = mScanRepository.mAllDocs
    }

    fun insertScan(scanData: ScanData) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.insert(scanData)
    }

    fun deleteBarcode(numDoc: Int, barcode: String) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.deleteBarcode(numDoc, barcode)
    }

    fun deleteSGTIN(numDoc: Int, sgtin: String) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.deleteSGTIN(numDoc, sgtin)
    }

    fun deleteCodes(sgtin: String) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.deleteCodes(sgtin)
    }

    fun deleteDoc(numDoc: Int) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.deleteDoc(numDoc)
    }

    fun getNumberDocument(): Int {
        var res: Int
        runBlocking { res = mScanRepository.getNumberDocument() }
        return res
    }

    fun setNumDocAndBarcode(numDoc: Int, barcode: String){
        mScanRepository.mNumDoc = numDoc
        mAllCodes = mScanRepository.getCodes(numDoc, barcode)
    }

    fun setNumDoc(numDoc: Int){
        mScanRepository.mNumDoc = numDoc
        mAllScans = mScanRepository.getScans(numDoc)
    }

    fun insertNomen(nomenData: NomenData) = viewModelScope.launch(Dispatchers.IO) {
        mNomenRepository.insert(nomenData)
    }

    fun getNomenByCode(barcode: String): NomenData? {
        var res: NomenData?
        runBlocking {res = mNomenRepository.getNomenByCode(barcode) }
        return res
    }

    fun getAll(): List<ScanData>? {
        var res: List<ScanData>?
        runBlocking { res = mScanRepository.getAll() }
        return res
    }

    fun delNomen() {
        runBlocking { mNomenRepository.delNomen() }
    }

    fun countNomen(): Int {
        var res: Int
        runBlocking { res = mNomenRepository.countNomen() }
        return res
    }

    fun getSGTINfromDocument(numDoc: Int): List<String> {
        var res: List<String>
        runBlocking { res = mScanRepository.getSGTINfromDocument(numDoc) }
        return res
    }

    fun getDuplicate(numDoc: Int, sgtin: String): Int? {
        var res: Int?
        runBlocking { res = mScanRepository.getDuplicate(numDoc, sgtin) }
        return res
    }

}

