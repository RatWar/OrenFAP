package com.besaba.anvarov.orentsd.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.besaba.anvarov.orentsd.R

class InventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invent)
        val btnRemains = findViewById<Button>(R.id.btnRemains)
        btnRemains.setOnClickListener { loadRemains() }
        val btnSaveFTP = findViewById<Button>(R.id.btnSaveFTP)
        btnSaveFTP.setOnClickListener { saveInvent() }
        val tvDoc = findViewById<TextView>(R.id.tvDoc)
        tvDoc.setOnClickListener { openDoc() }
        val tvReport = findViewById<TextView>(R.id.tvReport)
        tvReport.setOnClickListener { openReport() }
    }

    private fun loadRemains() {
        val intent = Intent(this@InventActivity, ListFAPActivity::class.java)
        startActivity(intent)
    }

    private fun saveInvent() {

    }

    private fun openDoc() {

    }

    private fun openReport() {

    }
}