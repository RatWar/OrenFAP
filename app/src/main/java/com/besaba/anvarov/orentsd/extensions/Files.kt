package com.besaba.anvarov.orentsd.extensions

import com.besaba.anvarov.orentsd.room.InventData
import com.besaba.anvarov.orentsd.room.ScanData
import org.json.JSONObject

//fun writeJson(jsonString: String, numDoc: Int, dateDoc: String, countDoc: Int): Boolean {
//    if (isExternalStorageWritable()) {
//        return try {
//            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            val fileName = createNameFile(numDoc, dateDoc, countDoc)
//            val fileWrite = File(path, fileName)
//            val outputStream: OutputStream = fileWrite.outputStream()
//            val bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
//            bufferedWriter.write(jsonString)
//            bufferedWriter.flush()
//            bufferedWriter.close()
//            outputStream.close()
//            true
//        } catch (e: IOException) {
//            false
//        }
//    }
//    return false
//}

//fun writeJsonInvent(jsonString: String, numMD: String): Boolean {
//    if (isExternalStorageWritable()) {
//        return try {
//            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            val fileName = createNameFileInvent(numMD)
//            val fileWrite = File(path, fileName)
//            val outputStream: OutputStream = fileWrite.outputStream()
//            val bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
//            bufferedWriter.write(jsonString)
//            bufferedWriter.flush()
//            bufferedWriter.close()
//            outputStream.close()
//            true
//        } catch (e: IOException) {
//            false
//        }
//    }
//    return false
//}

fun addScan(scan: ScanData): JSONObject {
    val json = JSONObject()
    json.put("dateTime", scan.dateTime)
    json.put("numDoc", scan.numDoc)
    json.put("barcode", scan.barcode)
    json.put("nameNomen", scan.nameNomen)
    json.put("price", scan.price)
    json.put("part", scan.part)
    return json
}

fun addScanInvent(scan: InventData): JSONObject {
    val json = JSONObject()
    json.put("dateTime", scan.dateTime)
    json.put("numDoc", scan.numDoc)
    json.put("barcode", scan.barcode)
    json.put("nameNomen", scan.nameNomen)
    json.put("price", scan.price)
    json.put("part", scan.part)
    return json
}

