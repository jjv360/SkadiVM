package com.jjv360.skadivm.activities

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jjv360.skadivm.ui.theme.SkadiVMTheme
import com.jjv360.skadivm.utils.VM
import com.jjv360.skadivm.utils.VMManager
import com.jjv360.skadivm.utils.VMTemplate
import java.net.URL

/** Popup activity to create a VM */
class RunVMActivity : ComponentActivity() {

    /** Called on activity create */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get VM ID
        val vmID = intent.getStringExtra("vm-id")
        if (vmID == null || vmID.isBlank()) {
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

    // Render UI
    SkadiVMTheme {
        Scaffold(

        ) { padding ->



        }

    }

}