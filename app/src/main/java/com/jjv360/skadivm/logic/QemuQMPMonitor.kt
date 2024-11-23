package com.jjv360.skadivm.logic

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.StringReader

/**
 * This class handles interacting with the Qemu instance via the QMP protocol.
 * See: https://www.qemu.org/docs/master/interop/qmp-spec.html
 * See: https://www.qemu.org/docs/master/interop/qemu-qmp-ref.html#qapidoc-1135
 *
 * NOTE: This class needs to be thread and coroutine safe. It will generally be called from the main
 * thread while the responses are coming from the Qemu monitor thread.
 */
class QemuQMPMonitor(val reader: BufferedReader, val writer: BufferedWriter) {

    /** Last used command ID */
    var lastCommandID = 0

    /** Pending responses */
    val pendingResponses = mutableMapOf<String, (result: JsonObject?, error: Exception?) -> Unit>()

    /** Read next JSON output */
    private fun readJSON(): JsonObject {

        // Read next line, remove CR if it's there
        var line = reader.readLine()
        if (line.endsWith('\r'))
            line = line.substring(0, line.length-1)

        // Try parse JSON
        try {

            // Parse JSON
            println("qemu-system: line: $line")
            return Klaxon().parseJsonObject(StringReader(line))

        } catch (err: Throwable) {

            // On errors, just try again with the next line. This is because the stdio output from
            // Qemu is noisy, so it also contains warnings and other things that are not QMP
            // responses.
            println("qemu-system: line error: ${err.message}")
            return readJSON()

        }

    }

    /** Start the connection */
    fun start() {

        // Read line
        val greeting = readJSON()

        // Log it
//        val qemuVersion = greeting.obj("QMP")?.obj("version")?.string("package")

        // Create list of capabilities we and the server supports
        val qemuCapabilities = greeting.obj("QMP")?.array<String>("capabilities") ?: listOf()
        val capabilities = mutableListOf<String>()
        if (qemuCapabilities.contains("oob")) capabilities.add("oob")

        // Feature negotiation
        sendJSON(mapOf(
            "execute" to "qmp_capabilities",
            "arguments" to mapOf(
                "enable" to capabilities,
            ),
        ))

    }

    /** Loop and receive commands */
    fun loop() {

        // Loop
        while (true) {

            // Read next JSON
            val json = readJSON()

            // Check type
            val cmdID = json.string("id") ?: ""
            if (cmdID.isBlank()) {

                // This is an event
                println("qemu-system monitor event: $json")
                continue

            }

            // Check for response callback
            val handler = pendingResponses[cmdID]
            if (handler == null) {
                println("qemu-system monitor: Received response for a request we are not waiting for! ${json.toJsonString()}")
                continue
            }

            // Remove it from the queue
            pendingResponses.remove(cmdID)

            // Check response type
            if (json.obj("return") != null)
                handler(json.obj("return")!!, null)
            else
                handler(null, Exception("Qemu error: ${json.obj("error")?.string("desc") ?: json.toJsonString()}"))

        }

    }

    /** Send JSON to Qemu */
    private fun sendJSON(any: Any) {

        // Send it
        val str = Klaxon().toJsonString(any)
        println("qemu-system: send: $str")
        synchronized(writer) {
            writer.write("$str\r\n")
            writer.flush()
        }

    }

    /** Send request */
    private fun sendRequest(cmd: Map<String, Any>, callback: (result: JsonObject?, error: Exception?) -> Unit) {

        // Add ID to request
        val id = lastCommandID++
        val request = cmd.toMutableMap()
        request["id"] = "$id"

        // Add to pending list
        pendingResponses["$id"] = callback

        // Send it
        sendJSON(request)

    }

    /** Send a request and wait for the response */
    suspend fun sendRequest(cmd: Map<String, Any>): JsonObject {

        // Send it
        var result: JsonObject? = null
        var error: Exception? = null
        sendRequest(cmd) { res, err ->
            result = res
            error = err
        }

        // Wait for it to be received
        // Wait for response
        val timeoutAt = System.currentTimeMillis() + 15000
        while (true) {

            // Check for response
            if (result != null || error != null)
                break

            // Check if timed out
            if (System.currentTimeMillis() > timeoutAt)
                throw Exception("Qemu request timed out.")

            // Wait a bit
            delay(25)

        }

        // Check for error
        if (error != null)
            throw error!!

        // Done
        return result!!

    }

    /** Query VNC information */
    suspend fun queryVNC(): QemuQueryVNCResponse {

        // Send command
        val response = sendRequest(mapOf(
            "execute" to "query-vnc",
        ))

        // Done
        return QemuQueryVNCResponse(
            enabled = response.boolean("enabled") ?: false,
            family = response.string("family") ?: "",
            host = response.string("host") ?: "",
            port = response.string("service")?.toIntOrNull() ?: 0,
        )

    }

}

/** Query VNC response */
data class QemuQueryVNCResponse(

    /** True if enabled */
    val enabled: Boolean,

    /** Listening IP family (ipv4 or ipv6 etc) */
    val family: String,

    /** Listening host address */
    val host: String,

    /** Listening port */
    val port: Int,

)