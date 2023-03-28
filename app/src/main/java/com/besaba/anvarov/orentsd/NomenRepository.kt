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
    suspend fun countNomen(): Int {
        return nomenDataDao.countNomen()
    }

    @WorkerThread
    suspend fun countAvailable(barcode: String): Int? {
        return nomenDataDao.countAvailable(barcode)
    }

    @WorkerThread
    suspend fun countPart(barcode: String): Int? {
        return nomenDataDao.countPart(barcode)
    }

    @WorkerThread
    suspend fun updateAvailable(id: Long, available: Int) {
        return nomenDataDao.updateAvailable(id, available)
    }

    @WorkerThread
    suspend fun delNomen() {
        return nomenDataDao.delNomen()
    }
}