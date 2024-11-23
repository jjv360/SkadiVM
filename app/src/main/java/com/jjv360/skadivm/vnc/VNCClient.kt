package com.jjv360.skadivm.vnc

import okio.ByteString.Companion.readByteString
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.util.logging.Logger

/**
 * Connects to a VNC server and returns image data. This uses the Remote Framebuffer (RFB) protocol.
 * See: https://datatracker.ietf.org/doc/html/rfc6143
 */
class VNCClient(val hostname: String, val port: Int) {

    /** Logger */
    val logger = Logger.getLogger("VNC Client")

    /** Thread */
    private var thread: Thread? = null

    /** Framebuffer */
    var framebuffer: RFBFramebuffer? = null

    /** Event: Connection opened and we will be receiving frames soon */
    var onConnect: ((serverName: String) -> Unit)? = null

    /** Event: When framebuffer size has been determined */
    var onFramebufferResize: ((width: Int, height: Int) -> Unit)? = null

    /** Event: Framebuffer has been updated */
    var onFramebufferUpdate: (() -> Unit)? = null

    /** Current socket */
    private var socket: Socket? = null

    /** Current output stream */
    private var outputStream: DataOutputStream? = null

    /** Requested frame rate */
    var frameRate = 60

    /** Start the connection */
    fun start() {

        // Check if framebuffer has been set
        if (framebuffer == null)
            throw Exception("Please assign a value to the framebuffer property first.")

        // Stop if already running
        if (thread != null)
            return

        // Create thread
        thread = Thread {

            // Catch errors
            try {

                // Open socket
                logger.info("Opening connection")
                Socket(hostname, port).use { socket ->
                    this.socket?.close()
                    this.socket = socket
                    DataInputStream(BufferedInputStream(socket.getInputStream())).use { input ->
                        DataOutputStream(BufferedOutputStream(socket.getOutputStream())).use { output ->

                            // Handle connection
                            outputStream = output
                            runThread(input, output)

                        }
                    }
                }

            } catch (err: Throwable) {

                // Error
                logger.warning("Error: $err")
                err.printStackTrace()

            } finally {

                // Done, cleanup
                logger.info("Connection ended")
                thread = null
                outputStream = null

            }

        }

        // Start it
        thread!!.start()

    }

    /** Stop */
    fun stop() {

        // Close socket
        socket?.close()
        socket = null
        outputStream = null

        // Kill thread
        thread?.interrupt()
        thread = null

    }

    /** Run connection */
    private fun runThread(input: DataInputStream, output: DataOutputStream) {

        // Read the ProtocolVersion message
        val utf8 = Charset.forName("UTF-8")
        val ascii = Charset.forName("ASCII")
        val protocolString = input.readByteString(12).string(ascii)
        val protocolMajor = protocolString.substring(4, 7).toInt()
        val protocolMinor = protocolString.substring(8, 11).toInt()
        val protocolVersion = "$protocolMajor.$protocolMinor".toFloat()

        // We only support protocol 3.8 and higher
        if (protocolVersion < 3.8f)
            throw Exception("We only support RFB protocol 3.8 and higher, server is using $protocolVersion")

        // Send our protocol message
        output.write("RFB 003.008\n".toByteArray(ascii))
        output.flush()

        // Get number of security types the server supports
        val securityTypeCount = input.readUnsignedByte()
        val securityTypes = mutableListOf<Byte>()
        for (i in 0 ..< securityTypeCount) {

            // Get security type
            val securityType = input.readByte()
            securityTypes.add(securityType)

        }

        // Spec says that no security types at all is a protocol error and will return an error response
        if (securityTypeCount == 0) {
            val stringLen = input.readInt().coerceAtMost(1024 * 1024)
            val string = input.readByteString(stringLen).string(utf8)
            throw Exception("Server error: $string")
        }

        // Ensure we have the none security option
        if (!securityTypes.contains(RFBConstants.SecurityType.None.toByte()))
            throw Exception("We don't support authentication, but the server requires it.")

        // Set security type to None
        output.writeByte(RFBConstants.SecurityType.None)
        output.flush()

        // Get response
        val responseCode = input.readInt()
        if (responseCode != RFBConstants.OK) {

            // Failed, throw error
            val stringLen = input.readInt().coerceAtMost(1024 * 1024)
            val string = input.readByteString(stringLen).string(utf8)
            throw Exception("Authentication error: $string")

        }

        // Send ClientInit
        output.writeByte(RFBConstants.ClientInit.Shared)
        output.flush()

        // Receive ServerInit
        val framebufferWidth = input.readUnsignedShort()
        val framebufferHeight = input.readUnsignedShort()
        RFBPixelFormat.read(input)
        val nameLen = input.readInt()
        if (nameLen > 1024*1024) throw Exception("Server name field specified too much data. size=$nameLen")
        val serverName = input.readByteString(nameLen).string(utf8)
        logger.info("Server name: $serverName")

        // Update framebuffer
        synchronized(framebuffer!!) {
            framebuffer!!.resize(framebufferWidth, framebufferHeight)
        }
        if (onFramebufferResize != null)
            onFramebufferResize!!(framebufferWidth, framebufferHeight)

        // Send our required pixel format
//        output.writeByte(RFBConstants.ClientToServerMessageType.SetPixelFormat)
//        output.writeByte(0)
//        output.writeByte(0) // <-- padding
//        output.writeByte(0)
//        framebuffer!!.pixelFormat.writeTo(output)
//        output.flush()

        // Supported encodings, priority first
        val encodings = listOf(
//            RFBConstants.Encoding.ZRLE,
//            RFBConstants.Encoding.CopyRect,
            RFBConstants.Encoding.Raw,
        )

        // Send supported encodings
        output.writeByte(RFBConstants.ClientToServerMessageType.SetEncodings)
        output.writeByte(0) // padding
        output.writeShort(encodings.size)
        for (enc in encodings)
            output.writeInt(enc)
        output.flush()

        // Request updates to the entire screen
        requestNextFrame(true)

        // Create thread to continually request new frames
//        Thread {
//
//            // While connection is active
//            try {
//                while (outputStream != null) {
//
//                    // Wait for frame rate
//                    Thread.sleep(1000 / frameRate.toLong())
//
//                    // Send request
//                    requestNextFrame()
//
//                }
//            } catch (err: Throwable) {
//                // ignore errors, just exit the thread
//            }
//
//        }.start()

        // Notify
        if (onConnect != null)
            onConnect!!(serverName)

        // Continuously receive messages from the server
        while (true)
            receiveMessage(input, output)

    }

    /** Request a new frame update */
    private fun requestNextFrame(fullRefresh: Boolean = false) {

        // Stop if no output stream
        if (outputStream == null || framebuffer == null)
            return

        // Request updates to the entire screen
        synchronized(outputStream!!) {
//            logger.info("Requesting next frame")
            outputStream!!.writeByte(RFBConstants.ClientToServerMessageType.FramebufferUpdateRequest)
            outputStream!!.writeByte(if (fullRefresh) 0 else 1) // incremental flag
            outputStream!!.writeShort(0) // X coordinate
            outputStream!!.writeShort(0) // Y coordinate
            outputStream!!.writeShort(framebuffer!!.width)
            outputStream!!.writeShort(framebuffer!!.height)
            outputStream!!.flush()
        }

    }

    /** Receive a message from the server */
    private fun receiveMessage(input: DataInputStream, output: DataOutputStream) {

        // Get message type
        val msgType = input.readByte().toInt()
//        logger.info("receiveMessage $msgType")
        if (msgType == RFBConstants.ServerToClientMessageType.FramebufferUpdate)
            receiveFramebufferUpdate(input, output)
        else
            throw Exception("Invalid message type received: $msgType")

    }

    /** Receive a framebuffer update */
    private fun receiveFramebufferUpdate(input: DataInputStream, output: DataOutputStream) {

        // Get number of rectangles
        input.readByte() // padding
        val rectangleCount = input.readUnsignedShort()
        for ( i in 0 ..< rectangleCount) {

            // Get values
            val x = input.readUnsignedShort()
            val y = input.readUnsignedShort()
            val width = input.readUnsignedShort()
            val height = input.readUnsignedShort()
            val encodingType = input.readInt()

            // Check encoding
//            logger.info("receiveFramebufferUpdate x=$x y=$y width=$width height=$height encodingType=$encodingType")
            if (encodingType == RFBConstants.Encoding.Raw)
                framebuffer!!.readIncomingRaw(input, x, y, width, height)
            else
                throw Exception("Unknown encoding for incoming frame: $encodingType")

        }

        // Notify
        if (onFramebufferUpdate != null)
            onFramebufferUpdate!!()

        // If no updates received, wait for the frame rate counter
//        if (rectangleCount == 0)
            Thread.sleep(1000 / frameRate.toLong())
//        Thread.sleep(1000)

        // Request another frame
        requestNextFrame()

    }

}