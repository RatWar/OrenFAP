package com.besaba.anvarov.orentsd.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.R

class ReportActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var countUnknown: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        val btnReport = findViewById<TextView>(R.id.button1)
        btnReport.setOnClickListener { onCloseReport() }
        val txtSumRemains = findViewById<TextView>(R.id.textView12)
        val txtSumInvent = findViewById<TextView>(R.id.textView22)
        val txtDiff = findViewById<TextView>(R.id.textView32)
        val txtUnknownText = findViewById<TextView>(R.id.textView41)
        val txtUnknown = findViewById<TextView>(R.id.textView42)
        mAllViewModel = ViewModelProvider(this)[AllViewModel::class.java]
        val sumRemains = mAllViewModel.sumRemains()
        txtSumRemains.text = sumRemains.toString()
        val sumInvent = 0.00
//        val sumInvent = sumInvent()
        txtSumInvent.text = sumInvent.toString()
        txtDiff.text = (sumRemains - sumInvent).toString()
        if (countUnknown > 0) {
            txtUnknown.text = countUnknown.toString()
        } else {
            txtUnknownText.isVisible = false
            txtUnknown.isVisible = false
        }
    }

    private fun sumInvent(): Double {
        var sum = 0.00
        var remPart: Int
        val all = mAllViewModel.getAllInvent()
        if (all!!.isNotEmpty()) {
            for (it in all) {
                if (it.nomId.toInt() == 0) {
                    countUnknown += 1
                    continue
                }
                if (it.part == 0) {
                    sum += it.price
                } else {
                    remPart = mAllViewModel.countPartRemains(it.sgtin)!!
                    when (remPart) {
                        0 -> {
                            remPart = 1
                        }
                    }
                    sum += (it.part / remPart) * it.price
                }
            }
        }
        return sum
    }

    private fun onCloseReport() {
        finish()
    }
}