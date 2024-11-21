package com.jjv360.skadivm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

/** Displays a full-size notice in the center of the screen */
@Composable fun FullSizeNotice(

    /** Lottie animation path in the assets folder */
    lottieAnimation: String,

    /** Title */
    title: String,

    /** Subtitle */
    subtitle: String?,

) {

    // Render no content UI
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {

        // Lottie animation
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset(lottieAnimation))
        LottieAnimation(
            composition,
            alignment = Alignment.Center,
            modifier = Modifier.size(200.dp, 200.dp),
            iterations = Int.MAX_VALUE,
        )

        // No items text
        Text(
            text = title,
            fontSize = 5.em,
            color = Color(red = 1f, green = 1f, blue = 1f, alpha = 1f),
            textAlign = TextAlign.Center,
            modifier = Modifier.sizeIn(maxWidth = 320.dp).padding(start = 20.dp, end = 20.dp),
        )
        Text(
            text = subtitle ?: "",
            fontSize = 3.em,
            color = Color(red = 1f, green = 1f, blue = 1f, alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.sizeIn(maxWidth = 320.dp).padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 100.dp),
        )

    }

}