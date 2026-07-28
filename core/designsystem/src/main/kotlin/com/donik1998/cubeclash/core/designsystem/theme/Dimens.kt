package com.donik1998.cubeclash.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** The 4-based spacing scale from the design system. Nothing in the app hardcodes a dp gap. */
@Immutable
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    val huge = 48.dp
    val giant = 64.dp
}

@Immutable
object Radius {
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(20.dp)
    val button = RoundedCornerShape(14.dp)
    val card = RoundedCornerShape(18.dp)
    val pill = RoundedCornerShape(999.dp)
}

val LocalSpacing = staticCompositionLocalOf { Spacing }
