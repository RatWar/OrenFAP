package com.besaba.anvarov.orentsd.activity

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.besaba.anvarov.orentsd.R
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

class ListFAPActivity : AppCompatActivity() {

    private var h: Handler? = null
    private var nameFAP = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_fapactivity)
        val btnFTP = findViewById<Button>(R.id.btnFTP)
        btnFTP.setOnClickListener { loadListFAP() }
        val tvNameFAP = findViewById<TextView>(R.id.tvNameFAP)
        tvNameFAP.text = ""
        val edtFAP = findViewById<EditText>(R.id.edtFAP)
        h = object : Handler() {
            @SuppressLint("HandlerLeak")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    0 -> {  // кнопка - надпись
                        tvNameFAP.text = msg.obj.toString()
                    }
                    1 -> {  // сколько загружено
                        tvNameFAP.text = msg.obj.toString()
                    }
                }
            }
        }
    }

    private fun loadListFAP() {
        onLoad(438917)
    }

    private fun onLoad(numFAP: Int) {
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
            if (!file.exists()) {
                file.createNewFile()
            }
            try {
                // открытие соединения
                ftpClient.connect(server, FTP.DEFAULT_PORT)
                ftpClient.login(user, pass)
                val reply = ftpClient.replyCode
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect()
                    msg = h!!.obtainMessage(
                        0,
                        "FTP сервер не принимает подключение. Код ответа - $reply"
                    )
                    h!!.sendMessage(msg)
                    return@Thread
                }
                // получение файла
                ftpClient.enterLocalPassiveMode()
                val fos: OutputStream = BufferedOutputStream(FileOutputStream(file))
                val res = ftpClient.retrieveFile(nameDBF, fos)
                if (!res) {
                    msg = h!!.obtainMessage(
                        0,
                        "на FTP сервере нет файла - $nameDBF"
                    )
                    h!!.sendMessage(msg)
                    return@Thread
                }
                fos.close()
            } catch (ex: IOException) {
                msg = h!!.obtainMessage(
                    0,
                    "Ошибка при обмене"
                )
                h!!.sendMessage(msg)
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
                msg = h!!.obtainMessage(
                    0,
                    "Ошибка при закрытии соединения"
                )
                h!!.sendMessage(msg)
                ex.printStackTrace()
                return@Thread
            }
            // чтение файла
            val reader: DBFReader?
            val fis: InputStream = BufferedInputStream(FileInputStream(file))
            try {
                reader = DBFReader(fis)
                reader.charactersetName = "866"
                val counts = reader.recordCount
                var rowValues: Array<Any?>
                for (i in 1..counts) {
                    reader.nextRecord().also { rowValues = it }
                    if (numFAP == rowValues[0].toString().toInt()) {
                        nameFAP = rowValues[1].toString()
                        break
                    }
                }
            } catch (e: DBFException) {
                msg = h!!.obtainMessage(
                    1,
                    "Ошибка при работе с dbf"
                )
                h!!.sendMessage(msg)
                e.printStackTrace()
                return@Thread
            } catch (e: IOException) {
                msg = h!!.obtainMessage(
                    1,
                    "Ошибка при записи остатков"
                )
                h!!.sendMessage(msg)
                e.printStackTrace()
                return@Thread
            } finally {
                try {
                    fis.close()
                } catch (_: Exception) {
                }
            }
            msg = h!!.obtainMessage(
                1,
                "загружено файлов - "
            )
            h!!.sendMessage(msg)
        }
        t.start()
    }
}