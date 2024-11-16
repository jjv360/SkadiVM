package com.jjv360.skadivm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.jjv360.skadivm.logic.VMRunner
import com.jjv360.skadivm.ui.VMRenderer
import com.jjv360.skadivm.ui.theme.SkadiVMColorScheme
import com.jjv360.skadivm.ui.theme.SkadiVMTheme

class MainActivity : ComponentActivity() {

    /** Called on activity create */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start qemu
        val runner = VMRunner(this).start()

        // Setup UI
        enableEdgeToEdge()
        setContent {
            AppMainComponent()
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview(device = Devices.PIXEL_TABLET, showSystemUi = true)
fun AppMainComponent() {
    SkadiVMTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            FlowRow(modifier = Modifier.padding(innerPadding), ) {
                Text(
                    text = "Skadi VM",
                    fontWeight = FontWeight.Bold,
                    fontSize = 7.em,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 20.dp, end = 20.dp),
                    color = Color(color = 0xFFEEEEEE),
                )
                Text(
                    text = "Easily run virtual machines.",
                    fontSize = 3.em,
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp, start = 20.dp, end = 20.dp),
                    color = Color(color = 0xFFAAAAAA),
                )
                VMRenderer()
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun AppMainComponent() {
//    SkadiVMTheme {
//        Greeting("Android")
//    }
//}