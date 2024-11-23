package com.jjv360.skadivm.vnc

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.logging.Logger

/** Framebuffer using Android graphics */
class RFBFramebufferAndroid() : RFBFramebuffer() {

    /** Logger */
    private val logger = Logger.getLogger("VNC FBAndroid")

    /** Colors */
    val colorBlack = Paint().apply {
        color = 0xFF000000.toInt()
    }

    /** Image */
    private var bitmap: Bitmap? = null

    /** Canvas */
    private var canvas: Canvas? = null

    /** Get required image format */
    override val pixelFormat = RFBPixelFormat(
        bitsPerPixel = 32,
        depth = 24,
        bigEndian = false,
        trueColor = true,
        redMax = 0xFF,
        greenMax = 0xFF,
        blueMax = 0xFF,
        redShift = 0,
        greenShift = 8,
        blueShift = 16,
    )

    /** Called when the buffer should be resized */
    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        // Check if bitmap exists
        if (bitmap == null) {

            // Create it
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap!!)
            canvas!!.drawRect(0f, 0f, width.toFloat(), height.toFloat(), colorBlack)

        } else {

            // Save old bitmap
            val oldBitmap = bitmap!!

            // Create new one
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap!!)
            canvas!!.drawRect(0f, 0f, width.toFloat(), height.toFloat(), colorBlack)

            // Draw old bitmap into the new one
            canvas!!.drawBitmap(oldBitmap, 0f, 0f, null)

        }

    }

    /** Update the specified rectangle with the provided pixel data */
    override fun updateRect(x: Int, y: Int, width: Int, height: Int, data: ByteArray) {

        // Decode data
        logger.info("Received update: x=$x y=$y width=$width height=$height data=${data.size}")
        val ints = IntArray(width * height)
        for (i in ints.indices) {
            ints[i] =
                (0xFF shr 0) or
                (data[i * 4 + 0].toInt() shr 8) or
                (data[i * 4 + 1].toInt() shr 16) or
                (data[i * 4 + 2].toInt() shr 24)
//            if (ints[i] != 0xFF) println("${ints[i]}")
        }

        // Create bitmap
        val rectBitmap = Bitmap.createBitmap(ints, width, height, Bitmap.Config.ARGB_8888)

        // Draw rect into the main buffer
        canvas!!.drawBitmap(rectBitmap, x.toFloat(), y.toFloat(), null)

    }

    /** True if we have data */
    val hasBitmap: Boolean
        get() = bitmap != null

    /** Use the bitmap */
    fun use(cb: (bitmap: Bitmap) -> Unit) {

        // Synchronize so the VNC client can't update the bitmap while we're using it
        synchronized(this) {
            cb(bitmap!!)
        }

    }

}