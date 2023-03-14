package com.besaba.anvarov.orentsd.activity

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.icu.text.IDNA
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.besaba.anvarov.orentsd.R
import com.linuxense.javadbf.DBFException
import com.linuxense.javadbf.DBFReader
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import java.io.*
import java.util.*
import java.util.stream.Collectors

class LoadActivity : AppCompatActivity() {

    private var h: Handler? = null

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)
        val tvInfo = findViewById<View>(R.id.textView2) as TextView
        val btLoad = findViewById<View>(R.id.btnLoad) as Button
        btLoad.setOnClickListener { onLoad() }

        h = object : Handler() {
            override fun handleMessage(msg: Message) {
                tvInfo!!.text = "Закачано файлов: " + msg.what
            }
        }
    }

    private fun onLoad() {
        val t = Thread {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val ftpClient = FTPClient()
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val server = prefs.getString("et_preference_server", "ftp1.oas-orb.ru").toString()
            val user = prefs.getString("et_preference_login", "00000000").toString()
            val pass = prefs.getString("et_preference_password", "").toString()
            val inputDir = prefs.getString("et_preference_input", "nsi/").toString()
            val outputDir = prefs.getString("et_preference_output", "real/").toString()
            try {
                ftpClient.connect(server, FTP.DEFAULT_PORT)
                ftpClient.login(user, pass)
                val reply = ftpClient.replyCode
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect()
                    h!!. .sendEmptyMessage(i)
//                    Info = "FTP сервер не принимает подключение."
                    val msg = Message()
                    msg.obj = "Я насчитал "

                    return
                }
                ftpClient.enterLocalPassiveMode()
// получаю список файлов
                val ftpFiles = Arrays.stream(ftpClient.listFiles(inputDir
                ) { file ->
                    file.isFile
                })
                    .map { obj: FTPFile -> obj.name }
                    .collect(Collectors.toList())
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE)
// получаю файлы "dbf" и переименовываю их в ".tmp"
                for (i in 0 until ftpFiles.size) {
                    if (ftpFiles[i].substring(ftpFiles[i].length - 3) != "dbf") {
                        break
                    }
                    val file = File(path, ftpFiles[i])
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val fos: OutputStream = BufferedOutputStream(FileOutputStream(file))
                    val res = ftpClient.retrieveFile(inputDir + ftpFiles[i], fos)
                    if (res) {
                        ftpClient.rename(inputDir + ftpFiles[i], inputDir + ftpFiles[i] + ".tmp")
                    }
                    fos.close()
                }
// выгружаю расход в файлы
//
// передаю файлы
//            val nameIn = "6_20221110_00_1.json"
//            val fileIn =  File(path, nameIn)
//            val fis: InputStream = BufferedInputStream(FileInputStream(fileIn))
//            val res = ftpClient.storeFile(outputDir + nameIn, fis)
//            if (res) {
// удаляю файлы
//                println("Ok")
//            }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            try {
                if (ftpClient.isConnected) {
                    ftpClient.logout()
                    ftpClient.disconnect()
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
// загружаю приход в остатки
            val filesArray: Array<File> = path.listFiles { _, filename ->
                filename.lowercase(Locale.getDefault()).endsWith(".dbf")
            } as Array<File>
            for (fileIn in filesArray) {
                val reader: DBFReader?
                val fis: InputStream = BufferedInputStream(FileInputStream(fileIn))
                try {
                    reader = DBFReader(fis)
                    reader.charactersetName = "866"
                    val counts = reader.recordCount
                    var rowValues: Array<Any?>
                    for (i in 1..counts) {
                        reader.nextRecord().also { rowValues = it }
                        for (j in rowValues.indices) {
//                        mCurrentNomen = NomenData(
//                            rowObjects[i]
//                            jsonobj.getString("Barcode"),
//                            jsonobj.getString("Name"),
//                            jsonobj.getString("EI"),
//                            jsonobj.getInt("MZOO")
//                        )
//                        mAllViewModel.insertNomen(mCurrentNomen)
                        }
                    }
//                fileIn.delete()
                } catch (e: DBFException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        fis.close()
                    } catch (_: Exception) {
                    }
                }
            }
        }
        t.start()
    }

}
