package com.besaba.anvarov.orentsd.activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.R
import com.besaba.anvarov.orentsd.room.RemainsData
import com.linuxense.javadbf.DBFException
import com.linuxense.javadbf.DBFReader
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListFAPActivity : AppCompatActivity() {

    private var h = Handler(Looper.getMainLooper())
    private lateinit var mAllViewModel: AllViewModel
    private lateinit var mCurrentRemains: RemainsData
    private var strFAP = ""
    private var nameFAP = ""
    private val tag = "myLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_fapactivity)
        val btnFTP = findViewById<Button>(R.id.btnFTP)
        btnFTP.setOnClickListener { loadListFAP() }
        val btnFTPLoad = findViewById<Button>(R.id.btnFTPLoad)
        btnFTPLoad.isEnabled = false
        btnFTPLoad.isClickable = false
        btnFTPLoad.setOnClickListener { loadRemains() }
        val tvNameFAP = findViewById<TextView>(R.id.tvNameFAP)
        val tvStatusFAP = findViewById<TextView>(R.id.tvStatusFAP)
        tvNameFAP.text = ""
        h = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0 -> {  // надпись
                        tvNameFAP.text = msg.obj.toString()
                        btnFTPLoad.isEnabled = true
                        btnFTPLoad.isClickable = true
                        nameFAP = msg.obj.toString()
                    }

                    1 -> {  // в случае ошибки
                        tvNameFAP.text = msg.obj.toString()
                        btnFTPLoad.isEnabled = false
                        btnFTPLoad.isClickable = false
                    }

                    2 -> {  // надпись
                        tvStatusFAP.text = msg.obj.toString()
                    }

                    3 -> {  // в случае ошибки
                        tvStatusFAP.text = msg.obj.toString()
                    }
                    4 -> {
                        val intentAnswer = Intent()
                        Log.d(tag, nameFAP)
                        intentAnswer.putExtra("nameFAP", nameFAP)
                        setResult(Activity.RESULT_OK, intentAnswer)
                        finish()
                    }
                }
            }
        }
    }

    private fun loadListFAP() {
        val edtFAP = findViewById<EditText>(R.id.edtFAP)
        strFAP = edtFAP.text.toString()
        if (strFAP != "") {
            onLoad(strFAP)
        }
    }

    private fun loadRemains() {
        onLoadRemains("ost_$strFAP.dbf")
    }

    private fun onLoad(numFAP: String) {
        val t = Thread {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val ftpClient = FTPClient()
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val server = prefs.getString("et_preference_server", "").toString()
            val user = "faps"
            val pass = "Tw789QwZ"
            val nameDBF = "faps.DBF"
            var msg: Message?
            val file = File(path, nameDBF)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            try {
                // открытие соединения
                ftpClient.connect(server, FTP.DEFAULT_PORT)
                ftpClient.login(user, pass)
                val reply = ftpClient.replyCode
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect()
                    msg = h.obtainMessage(
                        1,
                        "FTP сервер не принимает подключение. Код ответа - $reply"
                    )
                    h.sendMessage(msg)
                    return@Thread
                }
                // получение файла
                ftpClient.enterLocalPassiveMode()
                val fos: OutputStream = BufferedOutputStream(FileOutputStream(file))
                val res = ftpClient.retrieveFile(nameDBF, fos)
                if (!res) {
                    msg = h.obtainMessage(
                        1,
                        "на FTP сервере нет файла - $nameDBF"
                    )
                    h.sendMessage(msg)
                    return@Thread
                }
                fos.close()
            } catch (ex: IOException) {
                msg = h.obtainMessage(
                    1,
                    "Ошибка при обмене"
                )
                h.sendMessage(msg)
                ex.printStackTrace()
                return@Thread
            }
            // закрытие соединения
            try {
                if (ftpClient.isConnected) {
                    ftpClient.logout()
                    ftpClient.disconnect()
                }
            } catch (ex: IOException) {
                msg = h.obtainMessage(
                    1,
                    "Ошибка при закрытии соединения"
                )
                h.sendMessage(msg)
                ex.printStackTrace()
                return@Thread
            }
            // чтение файла
            val reader: DBFReader?
            val fis: InputStream = BufferedInputStream(FileInputStream(file))
            var flagFind = false
            try {
                reader = DBFReader(fis)
                reader.charactersetName = "866"
                val counts = reader.recordCount
                var rowValues: Array<Any?>
                var strMD: String
                for (i in 1..counts) {
//                    Log.d(tag, "Номер строки = $i")
                    reader.nextRecord().also { rowValues = it }
                    strMD = rowValues[0].toString().trim()
                    if (numFAP == strMD) {
                        flagFind = true
                        msg = h.obtainMessage(
                            0,
                            rowValues[1].toString()
                        )
                        h.sendMessage(msg)
                        break
                    }
                }
                if (!flagFind) {
                    msg = h.obtainMessage(
                        1,
                        "Не найден ФАП по такому коду"
                    )
                    h.sendMessage(msg)
                }
            } catch (e: DBFException) {
                msg = h.obtainMessage(
                    1,
                    "Ошибка при работе с dbf"
                )
                h.sendMessage(msg)
                e.printStackTrace()
                return@Thread
            } finally {
                try {
                    fis.close()
                } catch (_: Exception) {
                }
            }
        }
        t.start()
    }

    private fun onLoadRemains(numFAP: String) {
        val t = Thread {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val ftpClient = FTPClient()
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val server = prefs.getString("et_preference_server", "").toString()
            val user = "faps"
            val pass = "Tw789QwZ"
            var msg: Message?
            val file = File(path, numFAP)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            try {
                // открытие соединения
                ftpClient.connect(server, FTP.DEFAULT_PORT)
                ftpClient.login(user, pass)
                val reply = ftpClient.replyCode
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect()
                    msg = h.obtainMessage(
                        3,
                        "FTP сервер не принимает подключение. Код ответа - $reply"
                    )
                    h.sendMessage(msg)
                    return@Thread
                }
                // получение файла
                ftpClient.enterLocalPassiveMode()
                val fos: OutputStream = BufferedOutputStream(FileOutputStream(file))
                val res = ftpClient.retrieveFile(numFAP, fos)
                if (!res) {
                    msg = h.obtainMessage(
                        3,
                        "на FTP сервере нет файла - $numFAP"
                    )
                    h.sendMessage(msg)
                    return@Thread
                }
                fos.close()
            } catch (ex: IOException) {
                msg = h.obtainMessage(
                    3,
                    "Ошибка при обмене"
                )
                h.sendMessage(msg)
                ex.printStackTrace()
                return@Thread
            }
            // закрытие соединения
            try {
                if (ftpClient.isConnected) {
                    ftpClient.logout()
                    ftpClient.disconnect()
                }
            } catch (ex: IOException) {
                msg = h.obtainMessage(
                    3,
                    "Ошибка при закрытии соединения"
                )
                h.sendMessage(msg)
                ex.printStackTrace()
                return@Thread
            }
            // чтение файла
            mAllViewModel = ViewModelProvider(this)[AllViewModel::class.java]
            val reader: DBFReader?
            val fis: InputStream = BufferedInputStream(FileInputStream(file))
            try {
                reader = DBFReader(fis)
                reader.charactersetName = "866"
                val counts = reader.recordCount
                if (counts > 0) {
                    mAllViewModel.delRemains()
                    mAllViewModel.deleteDocInvent(1)
                    msg = h.obtainMessage(
                        3,
                        "Загружаю остатки в базу"
                    )
                    h.sendMessage(msg)
                    var rowValues: Array<Any?>
                    val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
                    for (i in 1..counts) {
//                        Log.d(tag, "Номер строки = $i")
                        reader.nextRecord().also { rowValues = it }
                        val available: Int = if ((rowValues[3] as Double).toInt() == 0) {
                            1
                        } else
                            (rowValues[3] as Double).toInt()
                        val sgtin: String = if (rowValues[0].toString().length > 31) {
                            rowValues[0].toString().take(31)
                        } else
                            rowValues[0].toString().take(13)
                        mCurrentRemains = RemainsData(
                            df.format(Date()),
                            file.name.toString(),
                            rowValues[0].toString(),
                            sgtin,
                            rowValues[1].toString(),
                            (rowValues[2] as Double).toDouble(),
                            (rowValues[3] as Double).toInt(),
                            available
                        )
                        mAllViewModel.insertRemainsBlocking(mCurrentRemains)
                    }
                }
                file.delete()
                msg = h.obtainMessage(
                    3,
                    ""
                )
                h.sendMessage(msg)
                msg = h.obtainMessage(
                    4,
                    ""
                )
                h.sendMessage(msg)
            } catch (e: DBFException) {
                msg = h.obtainMessage(
                    3,
                    "Ошибка при работе с dbf"
                )
                h.sendMessage(msg)
                e.printStackTrace()
                return@Thread
            } finally {
                try {
                    fis.close()
                } catch (_: Exception) {
                }
            }
        }
        t.start()
    }
}