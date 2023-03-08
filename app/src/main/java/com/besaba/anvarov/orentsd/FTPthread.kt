package com.besaba.anvarov.orentsd

import android.os.Environment
import org.apache.commons.net.ftp.*
import java.io.*
import java.util.*
import java.util.stream.Collectors
import kotlin.system.exitProcess


class FTPthread : Thread(){

    var server: String = ""
    var user: String = ""
    var pass: String = ""
    var inputDir: String = ""
    var outputDir: String = ""

    override fun run() {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val ftpClient = FTPClient()
        try {
            ftpClient.connect(server, FTP.DEFAULT_PORT)
            ftpClient.login(user, pass)
            val reply = ftpClient.replyCode
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect()
                System.err.println("FTP сервер не принимает подключение.")
                exitProcess(1)
            }
            ftpClient.enterLocalPassiveMode()
            val ftpFiles = Arrays.stream(ftpClient.listFiles(inputDir
            ) { file ->
                file.isFile
            })
                .map { obj: FTPFile -> obj.name }
                .collect(Collectors.toList())
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE)
            for (i in 0 until ftpFiles.size) {
                if (ftpFiles[i].substring(ftpFiles[i].length - 4) != "json") {
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
            val nameIn = "6_20221110_00_1.json"
            val fileIn =  File(path, nameIn)
            val fis: InputStream = BufferedInputStream(FileInputStream(fileIn))
            val res = ftpClient.storeFile(outputDir + nameIn, fis)
            if (res) {
                println("Ok")
            } else {
                println("Error")
            }
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
    }

}