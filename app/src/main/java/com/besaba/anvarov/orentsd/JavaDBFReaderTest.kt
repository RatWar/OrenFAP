package com.besaba.anvarov.orentsd

import com.linuxense.javadbf.DBFException
import com.linuxense.javadbf.DBFReader
import java.io.FileInputStream
import java.io.IOException

object JavaDBFReaderTest {
    @JvmStatic
    fun main(args: Array<String>) {
        var reader: DBFReader? = null
        try {

            // create a DBFReader object
            reader = DBFReader(FileInputStream(args[0]))

            // get the field count if you want for some reasons like the following
            val numberOfFields = reader.fieldCount

            // use this count to fetch all field information
            // if required
            for (i in 0 until numberOfFields) {
                val field = reader.getField(i)

                // do something with it if you want
                // refer the JavaDoc API reference for more details
                //
                println(field.name)
            }

            // Now, lets us start reading the rows
            var rowObjects: Array<Any?>
            while (reader.nextRecord().also { rowObjects = it } != null) {
                for (i in rowObjects.indices) {
                    println(rowObjects[i])
                }
            }

            // By now, we have iterated through all of the rows
        } catch (e: DBFException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            //DBFUtils.close(reader);
        }
    }
}