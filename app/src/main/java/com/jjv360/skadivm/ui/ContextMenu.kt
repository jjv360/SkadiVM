package com.jjv360.skadivm.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

/** Renders a context menu row item */
@Composable fun ContextMenuItem(

    /** Title */
    title: String,

    /** Text color */
    textColor: Color? = null,

    /** Icon */
    icon: ImageVector = Icons.Default.PlayArrow,

    /** Called on select */
    onClick: () -> Unit,

) {

    // Render row
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(44.dp)
            .clickable { onClick() }
    ) {

        // Icon
        Icon(
            imageVector = icon,
            contentDescription = "",
            modifier = Modifier
                .padding(start = 20.dp)
        )

        // Text area
        Text(
            text = title,
            fontSize = 3.em,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 5.dp)
        )

    }

}