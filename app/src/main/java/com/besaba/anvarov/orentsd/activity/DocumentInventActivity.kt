package com.besaba.anvarov.orentsd.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.BarcodeActivityContract
import com.besaba.anvarov.orentsd.R
import com.besaba.anvarov.orentsd.ScanListAdapter
import com.besaba.anvarov.orentsd.databinding.ActivityDocumentBinding
import com.besaba.anvarov.orentsd.extensions.toast
import com.besaba.anvarov.orentsd.room.CountData
import com.besaba.anvarov.orentsd.room.InventData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentInventActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var mSGTIN: String = ""
    private var fCamera: String? = ""
    private var fScan: String? = ""
    private var mDocumentNumber: Int = 0
    private lateinit var mCurrentScanInvent: InventData
    private var keycode: Int = 0
    private val tableScan = mutableListOf<String>()
    private lateinit var binding: ActivityDocumentBinding
    private var partScan: Int = 0
    private var partAvailable: Int = 0
    private var partTotal: Int = 0

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST" -> onScan(intent)
            }
        }
    }

    private val getBarcode = registerForActivityResult(BarcodeActivityContract()) { result ->
        onScannerResult(result)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.fabCamera.setOnClickListener { onScanner() }
        binding.fabSave.setOnClickListener { finish() }
        val scanRecyclerView = binding.recyclerScanList
        val scanAdapter = ScanListAdapter(this)
        scanRecyclerView.adapter = scanAdapter
        scanRecyclerView.layoutManager = LinearLayoutManager(this)

        val onScanClickListener = object : ScanListAdapter.OnScanClickListener {
            override fun onScanClick(scan: CountData, del: Boolean) {
                if (del) {
                    mAllViewModel.deleteBarcodeIdInvent(scan.id)
                    tableScan.clear()
                    tableScan.addAll(mAllViewModel.getSGTINfromDocumentInvent(mDocumentNumber))
                    setLayoutCount()
                }
            }
        }
        scanAdapter.scanAdapter(onScanClickListener)

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fCamera = prefs.getString("reply", "0")
        fScan = prefs.getString("scan", "1")

        val intent = intent
        mDocumentNumber = intent.getIntExtra("documentNumber", 0)
        mAllViewModel = ViewModelProvider(this)[AllViewModel::class.java]
        mAllViewModel.setNumDocInvent(mDocumentNumber)
        mAllViewModel.mAllScansInvent.observe(this) { scans ->
            scans?.let { scanAdapter.setScans(it) }
        }
        tableScan.clear()
        tableScan.addAll(mAllViewModel.getSGTINfromDocumentInvent(mDocumentNumber))
        setLayoutCount()
    }

    override fun onResume() {
        super.onResume()
        if (fCamera!!.toInt() == 2) {
            binding.fabCamera.hide()
            binding.fabSave.hide()
            setTriggerStates()
            openDevice(keycode)
            val filter = IntentFilter()
            filter.addAction("com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST")
            registerReceiver(broadCastReceiver, filter)
        }
        tableScan.clear()
        tableScan.addAll(mAllViewModel.getSGTINfromDocumentInvent(mDocumentNumber))
        setLayoutCount()
    }

    override fun onPause() {
        if (fCamera!!.toInt() == 2) {
            unregisterReceiver(broadCastReceiver)
            closeDevice()
        }
        super.onPause()
    }

    private fun onScanner() {
        getBarcode.launch(fCamera!!.toInt())
    }

    private fun onScannerResult(codes: Array<String>?) {
        if (codes == null) return
        mSGTIN = codes[1]
        if (mSGTIN[0] == '\u001D' || mSGTIN[0] == '\u00E8') {  // для QR-кода убираю 1-й служебный
            mSGTIN = mSGTIN.substring(1)
        }
        val barcode = mSGTIN
        mSGTIN = mSGTIN.take(31)
        partAvailable = checkInNomen(mSGTIN)
        if (partAvailable == 0) {
//            toast("Данной номенклатуры нет на остатках")
            tableScan.add(mSGTIN)
            val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
            mCurrentScanInvent = InventData(
                df.format(Date()),
                mDocumentNumber,
                barcode.trim(),
                mSGTIN,
                barcode,
                0.00,
                1,
                0
            )
            mAllViewModel.insertScanInvent(mCurrentScanInvent)
            setLayoutCount()
        } else {
            if (partAvailable > 1) {
                partTotal = checkPartNomen(mSGTIN)
                queryPart()
            } else {
                partScan = 1
                handlerBarcode()
            }
        }
    }

    private fun onScan(intent: Intent?) {
        intent?.getStringExtra("EXTRA_BARCODE_DECODING_SYMBOLE").toString() // "Data Matrix"
        mSGTIN = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA").toString()
        if (mSGTIN[0] == '\u001D' || mSGTIN[0] == '\u00E8') {  // для QR-кода убираю 1-й служебный
            mSGTIN = mSGTIN.substring(1)
        }
        mSGTIN = mSGTIN.take(31)
        partAvailable = checkInNomen(mSGTIN)
        if (partAvailable == 0) {
            toast("Данной номенклатуры нет на остатках")
        } else {
            if (partAvailable > 1) {
                partTotal = checkPartNomen(mSGTIN)
                queryPart()
            } else {
                partScan = 1
                handlerBarcode()
            }
        }
    }

    private fun handlerBarcode() {
        if (partAvailable - partScan < 0) {
            toast("Данной номенклатуры нехватает на остатках, в остатке $partAvailable частей")
        }
        tableScan.add(mSGTIN)
        val mCurrentNom = mAllViewModel.getRemainsByCode(mSGTIN.padEnd(31))
        val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
        if (mCurrentNom != null) {
            mCurrentScanInvent = InventData(
                df.format(Date()),
                mDocumentNumber,
                mCurrentNom.barcode.trim(),
                mSGTIN,
                mCurrentNom.name,
                mCurrentNom.price,
                partScan,
                mCurrentNom.id
            )
            mAllViewModel.insertScanInvent(mCurrentScanInvent)
            setLayoutCount()
        }
    }

    private fun setLayoutCount() {
        binding.matrixLayoutCount.text = tableScan.filter { it.length <= 13 }.size.toString()
        binding.transportLayoutCount.text = tableScan.filter { it.length > 13 }.size.toString()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_UP && fScan == "1") {
            onScanner()
            return true
        }
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && fScan == "-1") {
            onScanner()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Установите статус Trigger buttons, Trigger buttons включены по умолчанию
    private val actionControlScankey: String = "com.xcheng.scanner.action.ACTION_CONTROL_SCANKEY"
    private val actionCloseScan = "com.xcheng.scanner.action.CLOSE_SCAN_BROADCAST"
    private val extraScankeyCode = "extra_scankey_code"
    private val extraScankeyStatus = "extra_scankey_STATUS"
    private var triggersKeys =
        intArrayOf(KeyEvent.KEYCODE_F3, KeyEvent.KEYCODE_CAMERA, KeyEvent.KEYCODE_FOCUS)

    private fun setTriggerStates() {
        triggersKeys.forEach {
            val intent = Intent()
            intent.action = actionControlScankey
            intent.putExtra(extraScankeyCode, it)
            intent.putExtra(extraScankeyStatus, true)
            sendBroadcast(intent)
        }
    }

    // Включение Scanner Atol
    private val actionOpenScan: String = "com.xcheng.scanner.action.OPEN_SCAN_BROADCAST"
    private val scankey = "scankey"
    private fun openDevice(keycode: Int) {
        val intent = Intent()
        intent.action = actionOpenScan
        intent.putExtra(scankey, keycode)
        sendBroadcast(intent)
    }

    // Отключение Scanner Атол
    private fun closeDevice() {
        val intent = Intent()
        intent.action = actionCloseScan
        sendBroadcast(intent)
    }

    // проверка скана в остатках
    private fun checkInNomen(scan: String): Int{
        val res = mAllViewModel.countAvailableRemains(scan.padEnd(31))
        return if ((res == null) || (res == 0)) {
            0
        } else res
    }

    // сколько всего частей в остатках
    private fun checkPartNomen(scan: String): Int{
        val res = mAllViewModel.countPartRemains(scan.padEnd(31))
        return if ((res == null) || (res == 0)) {
            0
        } else res
    }

    @SuppressLint("SetTextI18n")
    private fun queryPart() {
        val li = LayoutInflater.from(this)
        val partsView: View = li.inflate(R.layout.query_part, null)
        val mDialogBuilder = AlertDialog.Builder(this)
        mDialogBuilder.setView(partsView)
        val userInput = partsView.findViewById<View>(R.id.input_part) as EditText
        val avPart = partsView.findViewById<View>(R.id.available_part) as TextView
        avPart.text = "Доступно частей - $partAvailable из $partTotal"
        mDialogBuilder
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                val memPartScan = userInput.text.toString()
                if (memPartScan != "") {
                    partScan = memPartScan.toInt()
                    handlerBarcode()
                }
            }
            .setNegativeButton(
                "Отмена"
            ) { dialogInterface, _ -> dialogInterface.cancel() }
        val alertDialog: AlertDialog = mDialogBuilder.create()
        alertDialog.show()
    }

}