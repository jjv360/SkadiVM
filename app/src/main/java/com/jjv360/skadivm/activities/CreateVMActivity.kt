package com.jjv360.skadivm.activities

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import coil3.compose.AsyncImage
import com.jjv360.skadivm.ui.theme.SkadiVMTheme
import com.jjv360.skadivm.utils.VMTemplate
import java.net.URL

class CreateVMActivity : ComponentActivity() {

    /** Called on activity create */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request to appear as an android popup
        setTheme(android.R.style.Theme_Material_Dialog_NoActionBar)

        // Ensure dialog size matches layout size
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // Make background window transparent
        setTranslucent(true)

        // Setup UI
        enableEdgeToEdge()
        setContent {
            CreateVMComponent()
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Preview(device = Devices.PIXEL_TABLET, widthDp = 400, heightDp = 500)
fun CreateVMComponent() {

    // Get context
    val ctx = LocalContext.current as CreateVMActivity

    // Load template
    val templatesURL = remember { ctx.intent.getStringExtra("config-url") }
    val templates = remember { VMTemplate.fromYaml(ctx, URL(templatesURL)) }
    val templateID = remember { ctx.intent.getStringExtra("vm-id") }
    val template = templates.find { it.id == templateID }

    // Called when the user selects the Install button
    val onInstall = {
        println("Installing!")
    }

    // Render UI
    SkadiVMTheme {
        Scaffold(
            modifier = Modifier.sizeIn(maxHeight = 600.dp),

            // Top bar
            topBar = {
                TopAppBar(
                    title = { Text("Create virtual machine") },
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
                    actions = {

                        // Install button
                        Button(
                            modifier = Modifier.padding(start = 10.dp),
                            colors = ButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                disabledContentColor = MaterialTheme.colorScheme.tertiary,
                            ),
                            onClick = {
                                onInstall()
                            },
                        ) {
                            Text("Install")
                        }

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
                horizontalArrangement = Arrangement.Center,
            ) {

                // Stop if template not loaded
                if (template == null) {
                    Text("Unable to load template.")
                    return@FlowColumn
                }

                // Template icon
                AsyncImage(
                    model = template.icon ?: "file:///android_asset/icons/server.png",
                    contentDescription = null,
                    modifier = Modifier.size(128.dp, 128.dp).padding(top = 30.dp).align(Alignment.CenterHorizontally),
                )

                // Title and description
                Text(
                    text = template.name,
                    fontSize = 5.em,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = template.description ?: "(no description)",
                    fontSize = 3.em,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(),
                )

            }

        }

    }

}