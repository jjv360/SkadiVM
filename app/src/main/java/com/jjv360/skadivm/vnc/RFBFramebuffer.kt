package com.jjv360.skadivm.vnc

import java.io.DataInputStream
import java.util.logging.Logger

/** Represents a framebuffer */
abstract class RFBFramebuffer {

    /** Current framebuffer width */
    open var width: Int = 0

    /** Current framebuffer height */
    open var height: Int = 0

    /** Get required image format */
    open val pixelFormat = RFBPixelFormat(
        bitsPerPixel = 32,
        depth = 24,
        bigEndian = false,
        trueColor = true,
        redMax = 255,
        greenMax = 255,
        blueMax = 255,
        redShift = 0,
        greenShift = 8,
        blueShift = 16,
    )

    /** Resize the buffer to the specified size */
    open fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    /** Called when we receive an incoming raw frame */
    open fun readIncomingRaw(input: DataInputStream, x: Int, y: Int, width: Int, height: Int) {

        // Read data
        val data = ByteArray(width * height * 4)
        input.readFully(data)

        // Ask subclass to update it's rect
        synchronized(this) {
            updateRect(x, y, width, height, data)
        }

    }

    /** Update the specified rectangle with the provided pixel data */
    abstract fun updateRect(x: Int, y: Int, width: Int, height: Int, data: ByteArray)

}