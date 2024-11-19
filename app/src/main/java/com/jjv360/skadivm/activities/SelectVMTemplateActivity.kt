package com.jjv360.skadivm.activities

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import com.jjv360.skadivm.ui.theme.SkadiVMTheme
import com.jjv360.skadivm.utils.VMManager
import com.jjv360.skadivm.utils.VMTemplate

class SelectVMTemplateActivity : ComponentActivity() {

    /** Called on activity create */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request to appear as an android popup
        setTheme(android.R.style.Theme_Material_Dialog_NoActionBar)

        // Ensure dialog size matches layout size
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // Make background window transparent
        setTranslucent(true)
//        window.setBackgroundDrawableResource(android.R.color.transparent)

        // Setup UI
        enableEdgeToEdge()
        setContent {
            SelectVMTemplateComponent()
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Preview(device = Devices.PIXEL_TABLET, widthDp = 400, heightDp = 500)
fun SelectVMTemplateComponent() {

    // Get VM manager
    val ctx = LocalContext.current as SelectVMTemplateActivity

    // Load templates
    val templates = remember { VMTemplate.fromYaml(ctx.assets.open("templates.yaml")) }

    // Called when the user selects a template
    val onSelect = { item: VMTemplate ->
        println("Selected ${item.name}")
    }

    // Render UI
    SkadiVMTheme {
        Scaffold(
            modifier = Modifier.sizeIn(maxHeight = 600.dp),

            // Top bar
            topBar = {
                TopAppBar(
                    title = { Text("Select Template") },
                    scrollBehavior = enterAlwaysScrollBehavior(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            },

            // Bottom bar
            bottomBar = {
                BottomAppBar(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    contentColor = MaterialTheme.colorScheme.primary,
                    actions = {

                        // Cancel button
                        Button(
                            modifier = Modifier.padding(start = 10.dp),

                            colors = ButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.secondary,
                                disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                disabledContentColor = MaterialTheme.colorScheme.tertiary,
                            ),
                            onClick = {
                                ctx.finish()
                            },
                        ) {
                            Text("Cancel")
                        }

                    }

                )
            }

        ) { padding ->

            // List of items
            FlowColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
            ) {

                // For each template
                templates.forEach {

                    // Items
                    Row(
                        modifier = Modifier
                            .padding(top = if (it == templates.elementAt(0)) 16.dp else 0.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)
                            .fillMaxWidth()
                            .border(
                                width = 0.5.dp,
                                color = Color(1f, 1f, 1f, 0.1f),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .clickable {
                                onSelect(it)
                            }
                    ) {

                        // Icon
                        AsyncImage(
                            model = it.icon ?: "file://android_assets/icons/server.png",
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).padding(20.dp),
                        )

                        // Title
                        Column(
                            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp, end = 10.dp)
                        ) {
                            Text(it.name, fontSize = 3.em, modifier = Modifier.padding(top = 5.dp, bottom = 5.dp))
                            Text(it.description ?: "(no description)", fontSize = 2.em, color = Color(1f, 1f, 1f, 0.5f))
                        }

                    }

                }

            }

        }

    }

}