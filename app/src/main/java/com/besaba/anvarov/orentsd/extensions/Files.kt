package com.besaba.anvarov.orentsd.extensions

import com.besaba.anvarov.orentsd.room.InventData
import com.besaba.anvarov.orentsd.room.ScanData
import org.json.JSONObject

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
