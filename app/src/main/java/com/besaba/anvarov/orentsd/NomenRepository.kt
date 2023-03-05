package com.besaba.anvarov.orentsd

import androidx.annotation.WorkerThread
import com.besaba.anvarov.orentsd.room.NomenData
import com.besaba.anvarov.orentsd.room.NomenDataDao

class NomenRepository (private val nomenDataDao: NomenDataDao) {

    @WorkerThread
    suspend fun insert(nomenData: NomenData) {
        nomenDataDao.insert(nomenData)
    }

    @WorkerThread
    suspend fun getNomenByCode(barcode: String): NomenData? {
        return nomenDataDao.getNomenByCode(barcode)
    }

    @WorkerThread
    suspend fun delNomen() {
        return nomenDataDao.delNomen()
    }

    @WorkerThread
    suspend fun countNomen(): Int {
        return nomenDataDao.countNomen()
    }
}