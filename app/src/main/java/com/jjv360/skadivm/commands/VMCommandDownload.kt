package com.jjv360.skadivm.commands

import com.jjv360.skadivm.logic.VMCommandRunner
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/** Download a file */
class VMCommandDownload : VMCommand() {

    /** ID */
    override val id = "download"

    /** Return true if command is supported */
    override fun canRun(cmd: String): Boolean {
        return cmd.lowercase() == "download"
    }

    /** Execute */
    override fun run(runner: VMCommandRunner, cmd: String, args: List<String>) {

        // Start download
        runner.vm.overlaySubStatus = "Downloading..."
        val file = File(runner.vm.path, args[0])
        val url = URL(args[1])

        // Ensure directory exists
        file.parentFile!!.mkdirs()

        // Open URL connection
        val urlConnection = url.openConnection()

        // Get file size
        val totalSize = urlConnection.contentLengthLong

        // Open stream
        urlConnection.getInputStream().use { inputStream ->

            // Open connection to file
            FileOutputStream(file).use { outputStream ->

                // Pipe it across
                val buffer = ByteArray(1024*1024)
                var amountLoaded = 0L
                while (true) {

                    // Load next chunk
                    val amount = inputStream.read(buffer)
                    if (amount == -1)
                        break

                    // Write it out
                    outputStream.write(buffer, 0, amount)

                    // Update UI
                    amountLoaded += amount
                    if (totalSize == -1L)
                        runner.vm.overlaySubStatus = "Downloading ${bytesToHumanReadableSize(amountLoaded)}"
                    else
                        runner.vm.overlaySubStatus = "Downloading ${bytesToHumanReadableSize(amountLoaded)} of ${bytesToHumanReadableSize(totalSize)}"

                    // Log
//                println("[VM ${runner.vm.id}] ${runner.vm.overlaySubStatus}")

                }

            }
        }

        // Done
        runner.vm.overlaySubStatus = ""
        println("[VM ${runner.vm.id}] Download complete")

    }

    // Convert byte size to readable string
    private fun bytesToHumanReadableSize(bytes: Long) = when {
        bytes >= 1024*1024*1024 -> "%.2f GB".format(bytes.toDouble() / (1024*1024*1024))
        bytes >= 1024*1024 -> "%.2f MB".format(bytes.toDouble() / (1024*1024))
        bytes >= 1024 -> "%.2f KB".format(bytes.toDouble() / (1024))
        else -> "$bytes B"
    }

}