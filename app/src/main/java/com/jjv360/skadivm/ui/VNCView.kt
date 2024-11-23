package com.jjv360.skadivm.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jjv360.skadivm.vnc.RFBFramebufferAndroid
import com.jjv360.skadivm.vnc.VNCClient
import java.util.logging.Logger
import kotlin.math.min

/** Logger */
val logger = Logger.getLogger("VNCView")

/** Android view that connects to a VNC server and displays the output. */
class VNCAndroidView(ctx: Context, val hostname: String, val port: Int) : View(ctx) {

    /** Screen buffer */
    var buffer : Bitmap? = null

    /** Colors */
    val colorBlack = Paint().apply {
        color = 0xFF000000.toInt()
    }

    /** VNC client */
    var client: VNCClient? = null

    /** Framebuffer */
    val framebuffer = RFBFramebufferAndroid()

    /** Called when the view becomes visible */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Create VNC config
        logger.info("Creating view. host=$hostname port=$port")
        client = VNCClient(hostname, port)
        client!!.framebuffer = framebuffer
        client!!.onConnect = { onConnect(it) }
        client!!.onFramebufferResize = { w, h -> onFramebufferResize(w, h) }
        client!!.onFramebufferUpdate = { onFramebufferUpdate() }
        client!!.start()

    }

    /** Called when the view is no longer visible */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        // Kill the thread
        logger.info("Closing VNC client")
        client?.stop()
        client = null

    }

    /** Called when the VNC client is connected */
    private fun onConnect(serverName: String) {
        logger.info("Connected! Server name = $serverName")
    }

    /** Called when the VNC client resizes the framebuffer */
    private fun onFramebufferResize(width: Int, height: Int) {

        // Calculate rect to draw into
        onResized()

        // Ask Android to redraw this view
        this.postInvalidate()

    }

    /** Called when the VNC client has updated the framebuffer */
    private fun onFramebufferUpdate() {

        // Ask Android to redraw this view
        this.postInvalidate()

    }

    /** Called on Android view resize */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        onResized()
    }

    /** Rect to draw into */
    var bitmapRect = Rect(0, 0, 100, 100)

    /** Calculate new image rect */
    private fun onResized() {

        // Stop if no size yet
        if (framebuffer.width == 0 || framebuffer.height == 0 || width == 0 || height == 0)
            return

        // Get view size
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()

        // Get framebuffer size
        val fbWidth = framebuffer.width.toFloat()
        val fbHeight = framebuffer.height.toFloat()

        // Calculate size
        // See: https://stackoverflow.com/a/23105310/1008736
        val hRatio = screenWidth / fbWidth
        val vRatio = screenHeight / fbHeight
        val ratio = min(hRatio, vRatio)
        val rWidth = fbWidth * ratio
        val rHeight = fbHeight * ratio
//        val screenRatio = screenWidth / screenHeight
//        val fbRatio = fbWidth / fbHeight
//        var rwidth = 0f
//        var rheight = 0f
//        if (screenRatio < fbRatio) {
//
//            // Use max width
//            rwidth = screenWidth
//            rheight = rwidth / fbRatio
//
//        } else {
//
//            // Use max height
//            rheight = screenHeight
//            rwidth = rheight * fbRatio
//
//        }

        // Update rect
        val x = screenWidth / 2 - rWidth / 2
        val y = screenHeight / 2 - rHeight / 2
        bitmapRect = Rect(
            x.toInt(),
            y.toInt(),
            x.toInt() + rWidth.toInt(),
            y.toInt() + rHeight.toInt(),
        )

    }

    /** Draw method */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Stop if no bitmap has been received yet
        if (!framebuffer.hasBitmap)
            return

        // Use bitmap
        framebuffer.use {

            // Draw the buffer to the screen
            canvas.drawBitmap(it, null, bitmapRect, null)

        }

    }

}

/** Jetpack Compose view that connects to a VNC server and displays the output. */
@Composable
fun VNCView(hostname: String, port: Int) {
    AndroidView(
        factory = { ctx -> VNCAndroidView(ctx, hostname, port) },
        modifier = Modifier.fillMaxSize()
    )
}