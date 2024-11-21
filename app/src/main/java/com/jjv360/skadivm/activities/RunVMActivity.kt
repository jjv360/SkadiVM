package com.jjv360.skadivm.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jjv360.skadivm.ui.theme.SkadiVMTheme
import com.jjv360.skadivm.logic.VM
import com.jjv360.skadivm.logic.VMManager

/** Popup activity to create a VM */
class RunVMActivity : ComponentActivity() {

    /** Called on activity create */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get VM ID
        val vmID = intent.getStringExtra("vm-id")
        if (vmID.isNullOrBlank()) {
            Toast.makeText(this, "VM Error: No vm-id specified in intent.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Get VM
        val vm = VMManager.shared.getVirtualMachines(this).find { it.id == vmID }
        if (vm == null) {
            Toast.makeText(this, "VM Error: Couldn't find a VM with ID $vmID", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Start the VM
        vm.start()

        // Setup UI
        enableEdgeToEdge()
        setContent {
            RunVMComponent(vm)
        }

    }

}

@Composable
fun RunVMComponent(vm: VM) {

    // Get context
    val ctx = LocalContext.current as RunVMActivity

    // Pick a random loader image
    val loaderImg = remember { listOf(
        "lottie/loader-blocks.lottie",
        "lottie/loader-cloud.lottie",
        "lottie/loader-cpu.lottie",
        "lottie/loader-girl.lottie",
        "lottie/loader-gpu.lottie",
        "lottie/loader-hamster.lottie",
        "lottie/loader-robot.lottie",
        "lottie/loader-servers.lottie",
        "lottie/loader-servers2.lottie",
        "lottie/loader-servers3.lottie",
        "lottie/loader-wheel.lottie",
        "lottie/loader-wheel2.lottie",
    ).random() }

    // If VM ends, close the activity
    if (!vm.isRunning) {
        ctx.finish()
    }

    // Render UI
    SkadiVMTheme {
        Scaffold(

        ) { padding ->

            // Check if overlay should be visible. It's visible if the VM's overlayStatus has no text in it.
            if (vm.overlayStatus.isNotBlank()) {

                // Overlay view
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                ) {

                    // Loader animation
                    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(loaderImg))
                    LottieAnimation(
                        composition,
                        alignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(bottom = 20.dp),
                        iterations = Int.MAX_VALUE,
                    )

                    // Text label
                    Text(
                        text = vm.overlayStatus,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontSize = 4.em,
                    )

                }

            }

        }

    }

}