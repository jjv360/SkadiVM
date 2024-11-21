package com.jjv360.skadivm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.DialogProperties

/** Displays an alert dialog with a yes/no button */
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ConfirmAlertDialog(

    /** Icon */
    icon: ImageVector,

    /** Title */
    title: String,

    /** Text */
    text: String,

    /** Yes button title */
    yesTitle: String = "Yes",

    /** No button title */
    noTitle: String = "No",

    /** Called if the user presses Yes */
    onYes: () -> Unit,

    /** If the dialog should be visible */
    open: Boolean,

    /** Called when the dialog wants to close */
    onDismissRequest: () -> Unit,

) {

    // Stop if closed
    if (!open)
        return

    // Render dialog
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
    ) {

        // Background
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {

            // Icon
            Icon(
                imageVector = icon,
                contentDescription = "",
                modifier = Modifier
                    .padding(20.dp)
            )

            // Title
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontSize = 4.em,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp)
            )

            // Text
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 3.em,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
            )

            // Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 10.dp)
            ) {

                // Confirm button
                TextButton(
                    onClick = { onDismissRequest() ; onYes() },
                ) {
                    Text(yesTitle)
                }

                // Cancel button
                TextButton(
                    onClick = { onDismissRequest() },
                ) {
                    Text(noTitle)
                }

            }

        }

    }

}