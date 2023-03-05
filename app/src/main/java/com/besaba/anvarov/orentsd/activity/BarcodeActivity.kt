package com.besaba.anvarov.orentsd.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.besaba.anvarov.orentsd.databinding.ActivityBarcodeBinding
import com.budiyev.android.codescanner.*
import com.google.zxing.BarcodeFormat

class BarcodeActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var binding: ActivityBarcodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val scannerView = binding.scannerView

        codeScanner = CodeScanner(this, scannerView)

        val intent = intent

        codeScanner.camera = intent.getIntExtra("fCamera", 0)
        codeScanner.formats = listOf(BarcodeFormat.DATA_MATRIX, BarcodeFormat.CODE_128)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            val intentAnswer = Intent()
            intentAnswer.putExtra("scancode", it.text)
            intentAnswer.putExtra("barcodeFormat", it.barcodeFormat.toString())
            setResult(Activity.RESULT_OK, intentAnswer)
            vibrate()
            finish()
        }

        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Ошибка инициализации камеры: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
            finish()
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun vibrate() {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val canVibrate: Boolean = vibrator.hasVibrator()
        val milliseconds = 500L
        if (canVibrate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(  // API 26
                    VibrationEffect.createOneShot(milliseconds, VibrationEffect.EFFECT_HEAVY_CLICK
                    )
                )
            } else {
                vibrator.vibrate(milliseconds)  // This method was deprecated in API level 26
            }
        }
    }

}
