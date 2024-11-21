package com.jjv360.skadivm.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jjv360.skadivm.ui.FullSizeNotice
import com.jjv360.skadivm.ui.VMIcon
import com.jjv360.skadivm.ui.theme.SkadiVMTheme
import com.jjv360.skadivm.logic.VM
import com.jjv360.skadivm.logic.VMManager

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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppMainComponent() {

    // Get activity
    val ctx = LocalContext.current

    // Get lifecycle owner
    val lifecycleOwner = LocalLifecycleOwner.current

    // Get VMs
    var vms by remember { mutableStateOf(VMManager.shared.getVirtualMachines(ctx)) }

    // Update VM list whenever this screen becomes active
    DisposableEffect(lifecycleOwner) {

        // Create observer
        val observer = LifecycleEventObserver { _, event ->

            // Check lifecycle event
            if (event == Lifecycle.Event.ON_RESUME) {

                // On resume, refresh VM list
                vms = VMManager.shared.getVirtualMachines(ctx)

            }

        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }

    }

    // Called when the user clicks the add button
    val onAdd = {

        // Show the add dialog
        ctx.startActivity(Intent(ctx, SelectVMTemplateActivity::class.java))

    }

    // Called when the user selects a VM
    val onSelectVM = { vm: VM ->

        // Start the VM runner activity
        val intent = Intent(ctx, RunVMActivity::class.java)
        intent.putExtra("vm-id", vm.id)
        ctx.startActivity(intent)

    }

    // Called when the user deletes a VM
    val onDeleteVM = { vm: VM ->

        // Delete it
        vm.delete()

        // Refresh VM list
        vms = VMManager.shared.getVirtualMachines(ctx)

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

            // Check if no VMs available, if so show notice
            if (vms.isEmpty()) {
                FullSizeNotice("lottie/laptop.lottie", "No virtual machines", "Press the + button below to create a new virtual machine.")
                return@Scaffold
            }

            // Container for VM icons
            FlowRow(
                modifier = Modifier.padding(padding).padding(10.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // Render each VM
                vms.forEach {
                    VMIcon(
                        vm = it,
                        onClick = { onSelectVM(it) },
                        onDelete = { onDeleteVM(it) },
                    )
                }

            }

        }

    }

}
