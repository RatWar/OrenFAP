package com.besaba.anvarov.orentsd

import android.os.*
import com.besaba.anvarov.orentsd.room.NomenData
import com.linuxense.javadbf.DBFException
import com.linuxense.javadbf.DBFReader
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import java.io.*
import java.util.*
import java.util.stream.Collectors

// https://translated.turbopages.org/proxy_u/en-ru.ru.5a384aea-640e0d5f-2927de4e-74722d776562/https/stackoverflow.com/questions/65001259/correct-way-to-communicate-the-result-of-a-background-thread-to-the-ui-thread-in
// https://questu.ru/articles/283158/
class FTPthread() : HandlerThread("NetworkOperation"){

    var Info: String = ""
    var server: String = ""
    var user: String = ""
    var pass: String = ""
    var inputDir: String = ""
    var outputDir: String = ""
    private lateinit var mAllViewModel: AllViewModel
    private lateinit var mCurrentNomen: NomenData
    var mainHandler = Handler(Looper.getMainLooper())

    override fun run() {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val ftpClient = FTPClient()
        try {
            ftpClient.connect(server, FTP.DEFAULT_PORT)
            ftpClient.login(user, pass)
            val reply = ftpClient.replyCode
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect()
                Info = "FTP сервер не принимает подключение."
                val msg = Message()
                msg.obj = "Я насчитал "
                mainHandler.post {
                    msg
                }
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

}