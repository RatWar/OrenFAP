package com.besaba.anvarov.orentsd

import androidx.annotation.WorkerThread
import com.besaba.anvarov.orentsd.room.RemainsData
import com.besaba.anvarov.orentsd.room.RemainsDataDao

class RemainsRepository(private val remainsDataDao: RemainsDataDao) {

    @WorkerThread
    suspend fun insert(remainsData: RemainsData) {
        remainsDataDao.insert(remainsData)
    }

    @WorkerThread
    suspend fun getRemainsByCode(barcode: String): RemainsData? {
        return remainsDataDao.getRemainsByCode(barcode)
    }

    @WorkerThread
    suspend fun countRemains(): Int {
        return remainsDataDao.countRemains()
    }

    @WorkerThread
    suspend fun countAvailable(barcode: String): Int? {
        return remainsDataDao.countAvailable(barcode)
    }

    @WorkerThread
    suspend fun countPart(barcode: String): Int? {
        return remainsDataDao.countPart(barcode)
    }

    @WorkerThread
    suspend fun updateAvailable(id: Long, available: Int) {
        return remainsDataDao.updateAvailable(id, available)
    }

    @WorkerThread
    suspend fun delRemains() {
        return remainsDataDao.delRemains()
    }
}