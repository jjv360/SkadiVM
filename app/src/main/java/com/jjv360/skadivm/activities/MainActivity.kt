package com.jjv360.skadivm.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jjv360.skadivm.ui.theme.SkadiVMTheme
import com.jjv360.skadivm.utils.VMManager

class MainActivity : ComponentActivity() {

    /** Called on activity create */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start Qemu with VNC
//        val qemu = Qemu(this).start("x86_64", arrayOf(
//            "-m", "128",                                        // <-- RAM size
//            "-display", "vnc=localhost:0,to=99,id=default",     // <-- VNC support, with listening host and port
//        ))

        // List all files in the app's data directories

        // Setup UI
        enableEdgeToEdge()
        setContent {
            AppMainComponent()
        }

    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview(device = Devices.PIXEL_TABLET, showSystemUi = true)
fun AppMainComponent() {

    // Get VM manager
    val ctx = LocalContext.current
    val vmManager = remember { VMManager(ctx) }

    // Called when the user clicks the add button
    val onAdd = {

        // Show the add dialog
        ctx.startActivity(Intent(ctx, SelectVMTemplateActivity::class.java))

    }

    // Render UI
    SkadiVMTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),

            // Top bar
            topBar = {
                TopAppBar(
                    title = { Text("Skadi VM") },
                    scrollBehavior = enterAlwaysScrollBehavior(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            },

            // Floating action button
            floatingActionButton = {
                FloatingActionButton(onClick = onAdd) {
                    Icon(Icons.Filled.Add, "Add VM")
                }
            },

        ) { padding ->

            // Check if no VMs available
            if (vmManager.virtualMachines.size == 0) {

                // Render no content UI
                Column(modifier = Modifier.padding(padding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

                    // Lottie animation
                    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie/laptop.lottie"))
                    LottieAnimation(
                        composition,
                        alignment = Alignment.Center,
                        modifier = Modifier.size(200.dp, 200.dp),
                        iterations = Int.MAX_VALUE,
                    )

                    // No items text
                    Text(
                        text = "No virtual machines",
                        fontSize = 5.em,
                        color = Color(red = 1f, green = 1f, blue = 1f, alpha = 1f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.sizeIn(maxWidth = 320.dp).padding(start = 20.dp, end = 20.dp),
                    )
                    Text(
                        text = "Press the + button below to create a new virtual machine.",
                        fontSize = 3.em,
                        color = Color(red = 1f, green = 1f, blue = 1f, alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.sizeIn(maxWidth = 320.dp).padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 100.dp),
                    )

                }

                // Stop here
                return@Scaffold

            }



        }

    }

}
