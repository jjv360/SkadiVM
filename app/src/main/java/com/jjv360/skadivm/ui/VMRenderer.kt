package com.jjv360.skadivm.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview()
fun VMRenderer() {
    Card(
        modifier = Modifier.size(320.dp, 180.dp).padding(20.dp),
        colors = CardColors(
            containerColor = Color(0xFF000000),
            contentColor = Color(0xFF440000),
            disabledContainerColor = Color(0xFF000000),
            disabledContentColor = Color(0xFF440000),
        )
    ) {

    }
}


