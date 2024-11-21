package com.jjv360.skadivm.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import com.jjv360.skadivm.logic.VM
import java.io.File

/** Displays a VM icon */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable fun VMIcon(

    /** VM */
    vm: VM,

    /** Called when the user selects this item */
    onClick: () -> Unit,

    /** Called when the user wants to delete this item */
    onDelete: () -> Unit,

) {

    // VM that we have the context menu open for
    var contextMenuOpen by remember { mutableStateOf(false) }

    // Confirm delete dialog open
    var deleteConfirmOpen by remember { mutableStateOf(false) }

    // Haptics
    val haptics = LocalHapticFeedback.current

    // Icon container
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .width(300.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClickLabel = "MenuHERE",
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    contextMenuOpen = true
                }
            ),
    ) {

        // Icon
        Box(
            modifier = Modifier
                .aspectRatio(16f/9f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .alpha(if (vm.isRunning) 1f else 0.5f),
        ) {

            // Last loaded icon
            AsyncImage(
                model = File(vm.path, "snapshot.jpg"),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (vm.isRunning) 1f else 0.5f),
            )

        }

        // Title
        Text(
            text = vm.name,
            fontSize = 3.em,
            modifier = Modifier.padding(top = 10.dp, bottom = 10.dp).fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

    }

    // Context menu
    if (contextMenuOpen) {
        ModalBottomSheet(
            onDismissRequest = { contextMenuOpen = false },
        ) {

            // Button to launch the VM
            ContextMenuItem("Launch VM", icon = Icons.Default.PlayArrow) {
                contextMenuOpen = false
                onClick()
            }

            // Button to delete the VM
            ContextMenuItem("Delete", icon = Icons.Default.Delete) {
                contextMenuOpen = false
                deleteConfirmOpen = true
            }

        }
    }

    // Confirm delete dialog
    ConfirmAlertDialog(
        icon = Icons.Default.Delete,
        title = "Delete ${vm.name}",
        text = "Are you sure you want to delete this virtual machine?",
        yesTitle = "Delete",
        noTitle = "Cancel",
        open = deleteConfirmOpen,
        onDismissRequest = { deleteConfirmOpen = false },
        onYes = { onDelete() },
    )

}