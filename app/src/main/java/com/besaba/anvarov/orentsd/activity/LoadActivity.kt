package com.besaba.anvarov.orentsd.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.besaba.anvarov.orentsd.AllViewModel
import com.besaba.anvarov.orentsd.R
import com.besaba.anvarov.orentsd.extensions.addScan
import com.besaba.anvarov.orentsd.extensions.writeJson
import com.besaba.anvarov.orentsd.room.NomenData
import com.linuxense.javadbf.DBFException
import com.linuxense.javadbf.DBFReader
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors


class LoadActivity : AppCompatActivity() {

    private var h = Handler(Looper.getMainLooper())
    private lateinit var mAllViewModel: AllViewModel
    private lateinit var mCurrentNomen: NomenData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)
        val tvInfoLoad = findViewById<View>(R.id.tvStatusL) as TextView
        val tvInfoUpload = findViewById<View>(R.id.tvStatusU) as TextView
        val btLoad = findViewById<View>(R.id.btnLoad) as Button
        btLoad.setOnClickListener { onLoad() }
        h = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0 -> {  // кнопка - надпись
                        btLoad.text = msg.obj.toString()
                    }
                    1 -> {  // сколько загружено
                        tvInfoLoad.text = msg.obj.toString()
                    }
                    2 -> {  // сколько выгружено
                        tvInfoUpload.text = msg.obj.toString()
                    }
                    3 -> {  // кнопка - надпись + отключение
                        btLoad.text = msg.obj.toString()
                        btLoad.isEnabled = false
                    }
                }
            }
        }
    }

    private fun onLoad() {
        val t = Thread {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val ftpClient = FTPClient()
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val server = prefs.getString("et_preference_server", "").toString()
            val user = prefs.getString("et_preference_login", "").toString()
            val pass = prefs.getString("et_preference_password", "").toString()
            val inputDir = prefs.getString("et_preference_input", "").toString()
            val outputDir = prefs.getString("et_preference_output", "").toString()
            var msg: Message?
            var countLoad: Int
            var countUpload: Int
            try {
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
                msg = h.obtainMessage(
                    0,
                    "Идет обмен..."
                )
                h.sendMessage(msg)
                ftpClient.enterLocalPassiveMode()
// получаю список файлов
                val ftpFiles = Arrays.stream(ftpClient.listFiles(
                    inputDir
                ) { file ->
                    file.isFile
                })
                    .map { obj: FTPFile -> obj.name }
                    .collect(Collectors.toList())
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE)
// получаю файлы "dbf" и переименовываю их в ".tmp"
                msg = h.obtainMessage(
                    1,
                    "Получаю файлы остатков"
                )
                h.sendMessage(msg)
                countLoad = 0
                for (i in 0 until ftpFiles.size) {
                    if (ftpFiles[i].substring(ftpFiles[i].length - 3) != "DBF") {
                        continue
                    }
                    val file = File(path, ftpFiles[i])
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val fos: OutputStream = BufferedOutputStream(FileOutputStream(file))
                    val res = ftpClient.retrieveFile(inputDir + ftpFiles[i], fos)
                    if (res) {
                        ftpClient.rename(inputDir + ftpFiles[i], inputDir + ftpFiles[i] + ".tmp")
                        countLoad += 1
                    }
                    fos.close()
                }
                msg = if (countLoad > 0) {
                    h.obtainMessage(
                        1,
                        "Загружено файлов - $countLoad"
                    )
                } else {
                    h.obtainMessage(
                        1,
                        ""
                    )
                }
                h.sendMessage(msg)
// выгружаю расход в файлы
                mAllViewModel = ViewModelProvider(this)[AllViewModel::class.java]
                onSaveCodesToJSON()
// передаю файлы
                val filesArray: Array<File> = path.listFiles { _, filename ->
                    filename.lowercase(Locale.getDefault()).endsWith(".json")
                } as Array<File>
                countUpload = 0
                for (fileIn in filesArray) {
                    val fi = fileIn.name
                    val fis: InputStream = BufferedInputStream(FileInputStream(fileIn))
                    val res = ftpClient.storeFile(outputDir + fi, fis)
                    if (res) {
                        countUpload += 1
                        fileIn.delete()
                    }
                }
                msg = if (countUpload > 0) {
                    h.obtainMessage(
                        2,
                        "Выгружено файлов - $countUpload"
                    )
                } else {
                    h.obtainMessage(
                        2,
                        ""
                    )
                }
                h.sendMessage(msg)
            } catch (ex: IOException) {
                msg = h.obtainMessage(
                    1,
                    "Ошибка при обмене"
                )
                h.sendMessage(msg)
                ex.printStackTrace()
                return@Thread
            }
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
// загружаю приход в остатки
            val filesArray: Array<File> = path.listFiles { _, filename ->
                filename.lowercase(Locale.getDefault()).endsWith(".dbf")
            } as Array<File>
            if (filesArray.isNotEmpty()) {
                mAllViewModel.delNomen()
                msg = h.obtainMessage(
                    1,
                    "Загружаю остатки в базу"
                )
                h.sendMessage(msg)
            }
            for (fileIn in filesArray) {
                val reader: DBFReader?
                val fis: InputStream = BufferedInputStream(FileInputStream(fileIn))
                try {
                    reader = DBFReader(fis)
                    reader.charactersetName = "866"
                    val counts = reader.recordCount
                    var rowValues: Array<Any?>
                    val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
                    for (i in 1..counts) {
                        reader.nextRecord().also { rowValues = it }
                        val available: Int = if ((rowValues[3] as Double).toInt() == 0) {
                            1
                        } else
                            (rowValues[3] as Double).toInt()
                        val sgtin: String = if (rowValues[0].toString().length > 31) {
                            rowValues[0].toString().take(31)
                        } else
                            rowValues[0].toString().take(13)
                        mCurrentNomen = NomenData(
                            df.format(Date()),
                            fileIn.toString(),
                            rowValues[0].toString(),
                            sgtin,
                            rowValues[1].toString(),
                            (rowValues[2] as Double).toDouble(),
                            (rowValues[3] as Double).toInt(),
                            available
                        )
                        mAllViewModel.insertNomenBlocking(mCurrentNomen)
                    }
                    fileIn.delete()
                } catch (e: DBFException) {
                    msg = h.obtainMessage(
                        1,
                        "Ошибка при работе с dbf"
                    )
                    h.sendMessage(msg)
                    e.printStackTrace()
                    return@Thread
                } catch (e: IOException) {
                    msg = h.obtainMessage(
                        1,
                        "Ошибка при записи остатков"
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
            msg = h.obtainMessage(
                3,
                "Обмен завершен!"
            )
            h.sendMessage(msg)
            msg = h.obtainMessage(
                1,
                "загружено файлов - $countLoad"
            )
            h.sendMessage(msg)
        }
        t.start()
    }

    private fun onSaveCodesToJSON() {
        val scans = mutableListOf<JSONObject>()
        var numberDoc: Int = -1
        var buf: Int = -1
        var dateDoc = ""
        var bufDoc = ""
        val all = mAllViewModel.getAll()
        if (all!!.isNotEmpty()) {
            for (it in all) {
                if ((it.numDoc == numberDoc) or (numberDoc == -1)) { // продолжаю заполнять массив
                    scans.add(addScan(it))
                    mAllViewModel.updateAvailable(it.nomId, -it.part)
                    buf = it.numDoc
                    bufDoc = it.dateTime
                } else {                      // записываю документ и начинаю заполнять снова массив
                    if (numberDoc == -1) {
                        numberDoc = buf
                        dateDoc = bufDoc
                    }
                    writeJson(scans.toString(), numberDoc, dateDoc, scans.size)
                    mAllViewModel.deleteDoc(numberDoc)
                    numberDoc = it.numDoc
                    dateDoc = it.dateTime
                    scans.clear()
                    scans.add(addScan(it))
                    mAllViewModel.updateAvailable(it.nomId, -it.part)
                }
                if (numberDoc == -1) {
                    numberDoc = buf
                    dateDoc = bufDoc
                }
            }
            writeJson(scans.toString(), numberDoc, dateDoc, scans.size)
            mAllViewModel.deleteDoc(numberDoc)
        }
    }

}
