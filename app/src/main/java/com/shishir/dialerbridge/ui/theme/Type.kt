@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.shishir.dialerbridge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shishir.dialerbridge.R

/**
 * Inter variable font with explicit weight axes — a close visual match to
 * Apple's SF Pro text styles.
 */
val InterFamily = FontFamily(
    Font(
        R.font.inter_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.inter_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    )
)

private val iosStyle = TextStyle(fontFamily = InterFamily)

/** iOS text scale mapped onto Material 3 slots. */
val BridgeTypography = Typography(
    displaySmall = iosStyle.copy(fontSize = 34.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp),
    headlineLarge = iosStyle.copy(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.36.sp),
    headlineMedium = iosStyle.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    headlineSmall = iosStyle.copy(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    titleLarge = iosStyle.copy(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = iosStyle.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.15.sp),
    titleSmall = iosStyle.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = iosStyle.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.15.sp),
    bodyMedium = iosStyle.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodySmall = iosStyle.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    labelLarge = iosStyle.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = iosStyle.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
    labelSmall = iosStyle.copy(fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp)
)
