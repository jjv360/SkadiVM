package com.jjv360.skadivm.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
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
import java.util.logging.Logger

/** Android view that connects to a VNC server and displays the output. */
class VNCAndroidView(ctx: Context, val hostname: String, val port: Int) : View(ctx) {

    /** Screen buffer */
    var buffer : Bitmap? = null

    /** Colors */
    val colorBlack = Paint().apply {
        color = 0xFF000000.toInt()
    }

    /** VNC thread */
    var thread : Thread? = null

    /** Called when the view becomes visible */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Just in case, kill a previous thread
        thread?.interrupt()
        thread?.stop()

        // Create the VNC thread
        thread = Thread() {
            try {
                vncThread()
            } catch (err: Throwable) {
                Logger.getLogger("VNCAndroidView").warning("Networking thread error: ${err.message}")
            }
        }

        // Start it
        thread!!.start()

    }

    /** Called when the view is no longer visible */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        // Kill the thread
        thread?.interrupt()
        thread?.stop()
        thread = null

    }

    /** Draw method */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // If no buffer, clear the canvas
        if (buffer == null) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), colorBlack)
            return
        }

        // Draw the buffer to the screen
        canvas.drawBitmap(buffer!!, 0f, 0f, null)

    }

    /** Thread main */
    private fun vncThread() {



    }

}

/** Jetpack Compose view that connects to a VNC server and displays the output. */
@Composable
fun VNCView(hostname: String, port: Int) {
    AndroidView(factory = { ctx -> VNCAndroidView(ctx, hostname, port) })
}