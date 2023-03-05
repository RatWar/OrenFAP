package com.besaba.anvarov.orentsd.activity

import android.content.*
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.besaba.anvarov.orentsd.BarcodeActivityContract
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.R
import com.besaba.anvarov.orentsd.ScanListAdapter
import com.besaba.anvarov.orentsd.databinding.ActivityDocumentBinding
import com.besaba.anvarov.orentsd.room.CountData
import com.besaba.anvarov.orentsd.room.ScanData
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.ViewModelProvider as ViewModelProvider1


class DocumentActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var mBarcode: String = ""
    private var mSGTIN: String = ""
    private var fCamera: String? = ""
    private var fScan: String? = ""
    private var mDocumentNumber: Int = 0
    private lateinit var mCurrentScan: ScanData
    private var keycode: Int = 0
    private val tableScan = mutableListOf<String>()
    private lateinit var binding: ActivityDocumentBinding
    private lateinit var errSound: SoundPool
    private var soundId: Int = 0
    private var spLoaded = false

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
                    mAllViewModel.deleteBarcode(mDocumentNumber, scan.barcode)
                    tableScan.clear()
                    tableScan.addAll(mAllViewModel.getSGTINfromDocument(mDocumentNumber))
                    setLayoutCount()
                } else {
                    onCodes(scan.barcode)
                }
            }
        }
        scanAdapter.scanAdapter(onScanClickListener)

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fCamera = prefs.getString("reply", "0")
        fScan = prefs.getString("scan", "1")

        val intent = intent
        mDocumentNumber = intent.getIntExtra("documentNumber", 0)
        mAllViewModel = ViewModelProvider1(this).get(AllViewModel::class.java)
        mAllViewModel.setNumDoc(mDocumentNumber)
        mAllViewModel.mAllScans.observe(this, { scans ->
            scans?.let { scanAdapter.setScans(it) }
        })
        tableScan.clear()
        tableScan.addAll(mAllViewModel.getSGTINfromDocument(mDocumentNumber))
        setLayoutCount()

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        errSound = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(10)
            .build()
        errSound.setOnLoadCompleteListener { _, _, status ->
            spLoaded = status == 0
        }
        soundId = errSound.load(this, R.raw.cat, 1)
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
        tableScan.addAll(mAllViewModel.getSGTINfromDocument(mDocumentNumber))
        setLayoutCount()
        errSound.resume(soundId)
    }

    override fun onPause() {
        if (fCamera!!.toInt() == 2) {
            unregisterReceiver(broadCastReceiver)
            closeDevice()
        }
        errSound.pause(soundId)
        super.onPause()
    }

    override fun onDestroy() {
        errSound.release()
        super.onDestroy()
    }

    private fun onCodes(barcode: String) {
        val intent = Intent(this, CodesActivity::class.java)
        intent.putExtra("Barcode", barcode)
        intent.putExtra("NumDoc", mDocumentNumber)
        startActivity(intent)
    }

    private fun onScanner() {
        getBarcode.launch(fCamera!!.toInt())
    }

    private fun onScannerResult(codes: Array<String>?){
        if (codes == null) return
        val mType = codes[0]
        mBarcode = codes[1]
        if (mType == "DATA_MATRIX") {
            findBarcode()
        }
        if (mType == "CODE_128"){
            mSGTIN = mBarcode
            handlerTransport()
        }
    }

    private fun onScan(intent: Intent?) {
        val mType: String = intent?.getStringExtra("EXTRA_BARCODE_DECODING_SYMBOLE").toString() // "Data Matrix"
        mBarcode = intent?.getStringExtra("EXTRA_BARCODE_DECODING_DATA").toString()
        if (mType == "Data Matrix") {
            findBarcode()
        }
        if (mType == "EAN-128"){
            mSGTIN = mBarcode
            handlerTransport()
        }
    }

    private fun findBarcode(){
        mSGTIN = mBarcode
        if (mSGTIN[0] == '\u001D' || mSGTIN[0] == '\u00E8') {
            mSGTIN = mSGTIN.substring(1)
        }
        mBarcode = mSGTIN.substring(2, 16)
        handlerBarcode()
    }

    private fun handlerBarcode() {
        if (checkNotDoubleScan(mSGTIN)) {
            tableScan.add(mSGTIN)
            val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
            val mNomen = mAllViewModel.getNomenByCode(mBarcode)
            if (mNomen != null) {
                mCurrentScan = ScanData(
                    df.format(Date()),
                    mDocumentNumber,
                    mBarcode,
                    mSGTIN,
                    mNomen.name,
                    mNomen.ei,
                    mNomen.mzoo
                )
            } else {
                val mNomen0 = mAllViewModel.getNomenByCode(mBarcode.trimStart('0'))
                if (mNomen0 != null){
                    mCurrentScan = ScanData(
                        df.format(Date()),
                        mDocumentNumber,
                        mBarcode,
                        mSGTIN,
                        mNomen0.name,
                        mNomen0.ei,
                        mNomen0.mzoo
                    )
                } else {
                    mCurrentScan = ScanData(
                        df.format(Date()),
                        mDocumentNumber,
                        mBarcode,
                        mSGTIN,
                        "",
                        " ",
                        0
                    )
                }
            }
            mAllViewModel.insertScan(mCurrentScan)
            setLayoutCount()
        } else {
            soundPlay()
        }
    }

    private fun handlerTransport() {
        if (checkNotDoubleScan(mSGTIN)) {
            tableScan.add(mSGTIN)
            val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
            mCurrentScan = ScanData(
                df.format(Date()),
                mDocumentNumber,
                "",
                mSGTIN,
                "Транспортная",
                " ",
                0
            )
            mAllViewModel.insertScan(mCurrentScan)
            setLayoutCount()
        } else {
            soundPlay()
        }
    }

    private fun setLayoutCount() {
        binding.matrixLayoutCount.text = tableScan.filter { it.length > 22 }.size.toString()
        binding.transportLayoutCount.text = tableScan.filter { it.length <= 22 }.size.toString()
    }

    private fun soundPlay(){
        if (spLoaded) {
            errSound.play(soundId, 1F, 1F, 0, 0, 1F)
        }
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

    // проверка, что скан не дубль
    private fun checkNotDoubleScan(scan: String): Boolean{
        return mAllViewModel.getDuplicate(mDocumentNumber, scan) != 1
    }

}
