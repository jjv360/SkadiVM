package com.jjv360.skadivm.utils

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

/** Extract zip file to the specified folder */
fun extractZip(inputStream: InputStream, outputDir: File) {

    // Open zip stream
    val zis = ZipInputStream(BufferedInputStream(inputStream))

    // Extract everything
    // See: https://stackoverflow.com/a/10997886/1008736
    while (true) {

        // Get next entry
        val ze = zis.nextEntry ?: break

        // Ignore directories
        if (ze.isDirectory)
            continue

        // Ensure directory for this file exists
        println("Extracting: ${ze.name}")
        val filePath = File(outputDir, ze.name)
        val dirPath = filePath.parentFile ?: throw Exception("Unable to get parent path")
        dirPath.mkdirs()

        // Write file
        val fOut = FileOutputStream(filePath)
        val buffer = ByteArray(1024 * 512)
        while (true) {

            // Read next buffer
            val amount = zis.read(buffer)
            if (amount == -1)
                break

            // Write to file
            fOut.write(buffer, 0, amount)

        }

        // Done
        fOut.close()
        zis.closeEntry()

    }

    // Done
    zis.close()

}