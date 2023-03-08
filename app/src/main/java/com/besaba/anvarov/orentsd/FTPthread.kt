package com.besaba.anvarov.orentsd

import org.apache.commons.net.ftp.*
import java.io.*
import java.util.*
import java.util.stream.Collectors
import kotlin.system.exitProcess


class FTPthread() : Thread(){

    var urisArr : File? = null

    override fun run() {
        val server = "ftp1.oas-orb.ru"
        val user = "00000000118334"
        val pass = "T08FZVqk"
        val ftpClient = FTPClient()
        try {
            ftpClient.connect(server, FTP.DEFAULT_PORT)
            ftpClient.login(user, pass)
            val reply = ftpClient.replyCode
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                System.err.println("FTP сервер не принимает подключение.")
                exitProcess(1);
            }
            ftpClient.enterLocalPassiveMode()
            val ftpFiles = Arrays.stream(ftpClient.listFiles("nsi"
            ) { file ->
                file.isFile
            })
                .map { obj: FTPFile -> obj.name }
                .collect(Collectors.toList())
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE)

            for (i in 0 until ftpFiles.size) {
//                val fos = FileOutputStream("/storage/emulated/0/Download/nom.json");
                val file = File("/storage/emulated/0/Download/nom.json")
                if (!file.exists()) {
                    file.createNewFile()
                }
                val fos: OutputStream = BufferedOutputStream(FileOutputStream(file))
                var res = ftpClient.retrieveFile("nsi/" + ftpFiles[i], fos)
                fos.close()
            }



//            try {
//                val fs =  FileInputStream("/sdcard/Download/6_20221110_00_1.json")
//                var res = ftpClient.storeFile("deleted.json", fs)
//                fs.close()
//            } catch (ex: IOException) {
//                    println("Error: " + ex.message)
//                    ex.printStackTrace()
//                } finally {
//                    try {
//                        if (ftpClient.isConnected) {
//                            ftpClient.logout()
//                            ftpClient.disconnect()
//                        }
//                    } catch (ex: IOException) {
//                        ex.printStackTrace()
//                    }
//                }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }


//            ftpClient.setFileType(FTP.ASCII_FILE_TYPE)
//            val inputStream: InputStream = FileInputStream(urisArr)
//            var res = ftpClient.storeFile("test.json", inputStream)

//            var inputStream: InputStream = FileInputStream(urisArr)
//            var secondRemoteFile = "test.json"
//            inputStream = FileInputStream(urisArr)
//            var outputStream = ftpClient.storeFileStream(secondRemoteFile)
//            var bytesIn = ByteArray(33554432)
//            var read = 0
//            while (inputStream.read(bytesIn).also { read = it } != -1) {
//                outputStream.write(bytesIn, 0, read)
//            }
//            inputStream.close()
//            outputStream.close()
//            var completed = ftpClient.completePendingCommand()
//            if (completed) {
//                infoos!!.text = ((infoos!!.text).toString().toInt() +1).toString()
//
//                if((infoos!!.text).toString().toInt() == 6){
//                    infoos!!.text  = "Finished uploading with sucess, waiting for callback"
//                }
//            }

//        } catch (ex: IOException) {
//            println("Error: " + ex.message)
//            ex.printStackTrace()
//        } finally {
//            sleep(500)
//        }
//        try {
//            if (ftpClient.isConnected) {
//                ftpClient.logout()
//                ftpClient.disconnect()
//            }
//        } catch (ex: IOException) {
//            ex.printStackTrace()
//        }


    }

}