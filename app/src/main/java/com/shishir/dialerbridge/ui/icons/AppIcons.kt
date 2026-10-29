package com.shishir.dialerbridge.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Minimal stroke-based icon set drawn for this app (SF Symbols flavour).
 * Icons use a neutral black source color; tint them with Icon(tint = …).
 */
object AppIcons {

    private fun strokeIcon(name: String, pathData: String, strokeWidth: Float = 1.7f): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathData = addPathNodes(pathData)
            )
        }.build()

    private fun fillIcon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(fill = SolidColor(Color.Black), pathData = addPathNodes(pathData))
        }.build()

    val Home: ImageVector by lazy {
        strokeIcon(
            "Home",
            "M4.7,10.4 L12,4.6 L19.3,10.4 L19.3,18.6 C19.3,19.4 18.7,20 17.9,20 L6.1,20 " +
                "C5.3,20 4.7,19.4 4.7,18.6 Z M9.7,20 L9.7,14.2 L14.3,14.2 L14.3,20"
        )
    }

    val Setup: ImageVector by lazy {
        strokeIcon(
            "Setup",
            "M9,4.5 L6.5,4.5 C5.7,4.5 5,5.2 5,6 L5,19 C5,19.8 5.7,20.5 6.5,20.5 L17.5,20.5 " +
                "C18.3,20.5 19,19.8 19,19 L19,6 C19,5.2 18.3,4.5 17.5,4.5 L15,4.5 " +
                "M9,4.5 L9,3.6 C9,3 9.5,2.5 10.1,2.5 L13.9,2.5 C14.5,2.5 15,3 15,3.6 L15,4.5 " +
                "M9,12.6 L11.2,14.8 L15.2,10.8"
        )
    }

    val Sliders: ImageVector by lazy {
        strokeIcon(
            "Sliders",
            "M4,7.5 L20,7.5 M4,12 L20,12 M4,16.5 L20,16.5 " +
                "M14,5.5 L14,9.5 M8.8,10 L8.8,14 M15.5,14.5 L15.5,18.5"
        )
    }

    val ChevronRight: ImageVector by lazy {
        strokeIcon("ChevronRight", "M9.5,5.5 L16,12 L9.5,18.5")
    }

    val ChevronLeft: ImageVector by lazy {
        strokeIcon("ChevronLeft", "M14.5,5.5 L8,12 L14.5,18.5")
    }

    val Check: ImageVector by lazy {
        strokeIcon("Check", "M5,12.5 L9.8,17.3 L19,7.5")
    }

    val CheckCircle: ImageVector by lazy {
        fillIcon(
            "CheckCircle",
            "M12,2 C6.48,2 2,6.48 2,12 C2,17.52 6.48,22 12,22 C17.52,22 22,17.52 22,12 " +
                "C22,6.48 17.52,2 12,2 Z M10,17 L5,12 L6.41,10.59 L10,14.17 L17.59,6.59 L19,8 L10,17 Z"
        )
    }

    val XCircle: ImageVector by lazy {
        strokeIcon(
            "XCircle",
            "M12,3.2 C7.14,3.2 3.2,7.14 3.2,12 C3.2,16.86 7.14,20.8 12,20.8 C16.86,20.8 20.8,16.86 " +
                "20.8,12 C20.8,7.14 16.86,3.2 12,3.2 Z M9.2,9.2 L14.8,14.8 M14.8,9.2 L9.2,14.8"
        )
    }

    val Phone: ImageVector by lazy {
        fillIcon(
            "Phone",
            "M6.62,10.79c1.44,2.83,3.76,5.14,6.59,6.59l2.2-2.2c0.27-0.27,0.67-0.36,1.02-0.24" +
                "c1.12,0.37,2.33,0.57,3.57,0.57c0.55,0,1,0.45,1,1V20c0,0.55-0.45,1-1,1c-9.39,0-17-7.61-17-17" +
                "c0-0.55,0.45-1,1-1h3.5c0.55,0,1,0.45,1,1c0,1.25,0.2,2.45,0.57,3.57c0.11,0.35,0.03,0.74-0.25,1.02" +
                "L6.62,10.79z"
        )
    }

    val Handoff: ImageVector by lazy {
        ImageVector.Builder(
            name = "Handoff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                fill = SolidColor(Color.Black),
                pathData = addPathNodes(
                    "M5.12,11.29c1.44,2.83,3.76,5.14,6.59,6.59l2.2-2.2c0.27-0.27,0.67-0.36,1.02-0.24" +
                        "c1.12,0.37,2.33,0.57,3.57,0.57c0.55,0,1,0.45,1,1V19.5c0,0.55-0.45,1-1,1c-9.39,0-17-7.61-17-17" +
                        "c0-0.55,0.45-1,1-1h3.5c0.55,0,1,0.45,1,1c0,1.25,0.2,2.45,0.57,3.57c0.11,0.35,0.03,0.74-0.25,1.02" +
                        "L5.12,11.29z"
                )
            )
            addPath(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathData = addPathNodes("M14.2,7 L21,7 M18.4,4.2 L21,7 L18.4,9.8")
            )
        }.build()
    }

    val Bolt: ImageVector by lazy {
        strokeIcon("Bolt", "M13,3 L5.5,13.5 L11,13.5 L10.5,21 L18.5,10.5 L13,10.5 Z")
    }

    val Shield: ImageVector by lazy {
        strokeIcon(
            "Shield",
            "M12,3.2 L19,5.9 L19,11.2 C19,15.6 16.1,18.9 12,20.8 C7.9,18.9 5,15.6 5,11.2 L5,5.9 Z"
        )
    }

    val Bell: ImageVector by lazy {
        strokeIcon(
            "Bell",
            "M12,4 C14.8,4 17,6.2 17,9 C17,12.2 17.8,14.2 18.8,15.4 C19.1,15.8 18.8,16.4 18.3,16.4 " +
                "L5.7,16.4 C5.2,16.4 4.9,15.8 5.2,15.4 C6.2,14.2 7,12.2 7,9 C7,6.2 9.2,4 12,4 Z " +
                "M10,19 C10.3,20 11.1,20.6 12,20.6 C12.9,20.6 13.7,20 14,19"
        )
    }

    val Overlay: ImageVector by lazy {
        strokeIcon(
            "Overlay",
            "M4,8.3 L12,4.3 L20,8.3 L12,12.3 Z M5.2,12.2 L12,15.6 L18.8,12.2 M5.2,15.8 L12,19.2 L18.8,15.8"
        )
    }

    val Battery: ImageVector by lazy {
        strokeIcon(
            "Battery",
            "M3.5,8.5 L18,8.5 C18.8,8.5 19.5,9.2 19.5,10 L19.5,14 C19.5,14.8 18.8,15.5 18,15.5 " +
                "L3.5,15.5 C2.7,15.5 2,14.8 2,14 L2,10 C2,9.2 2.7,8.5 3.5,8.5 Z M22,10.6 L22,13.4"
        )
    }

    val Person: ImageVector by lazy {
        ImageVector.Builder(
            name = "Person",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                fill = SolidColor(Color.Black),
                pathData = addPathNodes(
                    "M12,3.4 C13.05,3.4 13.9,4.25 13.9,5.3 C13.9,6.35 13.05,7.2 12,7.2 " +
                        "C10.95,7.2 10.1,6.35 10.1,5.3 C10.1,4.25 10.95,3.4 12,3.4 Z"
                )
            )
            addPath(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathData = addPathNodes(
                    "M5,9.2 C9.5,10.6 14.5,10.6 19,9.2 M12,10.4 L12,14.8 " +
                        "M12,14.8 L9.2,20.4 M12,14.8 L14.8,20.4"
                )
            )
        }.build()
    }

    val AppGrid: ImageVector by lazy {
        strokeIcon(
            "AppGrid",
            "M5,5 L10.3,5 C10.9,5 11.3,5.4 11.3,6 L11.3,11.3 C11.3,11.9 10.9,12.3 10.3,12.3 L5,12.3 " +
                "C4.4,12.3 4,11.9 4,11.3 L4,6 C4,5.4 4.4,5 5,5 Z M13.7,5 L19,5 C19.6,5 20,5.4 20,6 L20,11.3 " +
                "C20,11.9 19.6,12.3 19,12.3 L13.7,12.3 C13.1,12.3 12.7,11.9 12.7,11.3 L12.7,6 " +
                "C12.7,5.4 13.1,5 13.7,5 Z M5,13.7 L10.3,13.7 C10.9,13.7 11.3,14.1 11.3,14.7 L11.3,20 " +
                "C11.3,20.6 10.9,21 10.3,21 L5,21 C4.4,21 4,20.6 4,20 L4,14.7 C4,14.1 4.4,13.7 5,13.7 Z " +
                "M13.7,13.7 L19,13.7 C19.6,13.7 20,14.1 20,14.7 L20,20 C20,20.6 19.6,21 19,21 L13.7,21 " +
                "C13.1,21 12.7,20.6 12.7,20 L12.7,14.7 C12.7,14.1 13.1,13.7 13.7,13.7 Z"
        )
    }

    val Warning: ImageVector by lazy {
        strokeIcon(
            "Warning",
            "M12,3.8 L21.2,19.6 L2.8,19.6 Z M12,10 L12,14.4 M12,17.1 L12.01,17.1"
        )
    }

    val Info: ImageVector by lazy {
        strokeIcon(
            "Info",
            "M12,3.2 C7.14,3.2 3.2,7.14 3.2,12 C3.2,16.86 7.14,20.8 12,20.8 C16.86,20.8 20.8,16.86 " +
                "20.8,12 C20.8,7.14 16.86,3.2 12,3.2 Z M12,11 L12,16.2 M12,7.8 L12.01,7.8"
        )
    }

    val Copy: ImageVector by lazy {
        strokeIcon(
            "Copy",
            "M9.5,9 L18.5,9 C19.3,9 20,9.7 20,10.5 L20,18.5 C20,19.3 19.3,20 18.5,20 L9.5,20 " +
                "C8.7,20 8,19.3 8,18.5 L8,10.5 C8,9.7 8.7,9 9.5,9 Z M16,9 L16,5.5 C16,4.7 15.3,4 14.5,4 " +
                "L5.5,4 C4.7,4 4,4.7 4,5.5 L4,13.5 C4,14.3 4.7,15 5.5,15 L8,15"
        )
    }

    val Trash: ImageVector by lazy {
        strokeIcon(
            "Trash",
            "M4.5,6.5 L19.5,6.5 M9.7,6.5 L9.7,4.8 C9.7,4.1 10.3,3.5 11,3.5 L13,3.5 C13.7,3.5 14.3,4.1 " +
                "14.3,4.8 L14.3,6.5 M6.3,6.5 L7.1,18.9 C7.1,19.8 7.9,20.5 8.8,20.5 L15.2,20.5 " +
                "C16.1,20.5 16.9,19.8 16.9,18.9 L17.7,6.5 M10,10.5 L10,16.5 M14,10.5 L14,16.5"
        )
    }

    val Play: ImageVector by lazy {
        fillIcon("Play", "M8,5.2 L19,12 L8,18.8 Z")
    }

    val DocLogs: ImageVector by lazy {
        strokeIcon(
            "DocLogs",
            "M6.5,3.5 L17.5,3.5 C18.3,3.5 19,4.2 19,5 L19,19 C19,19.8 18.3,20.5 17.5,20.5 L6.5,20.5 " +
                "C5.7,20.5 5,19.8 5,19 L5,5 C5,4.2 5.7,3.5 6.5,3.5 Z M8.5,8.5 L15.5,8.5 M8.5,12 L15.5,12 " +
                "M8.5,15.5 L12.5,15.5"
        )
    }

    val Link: ImageVector by lazy {
        strokeIcon(
            "Link",
            "M9.5,14.5 L20,4 M14.5,4 L20,4 L20,9.5 M17.5,13.5 L17.5,18 C17.5,19.1 16.6,20 15.5,20 " +
                "L6,20 C4.9,20 4,19.1 4,18 L4,8.5 C4,7.4 4.9,6.5 6,6.5 L10.5,6.5"
        )
    }

    val Key: ImageVector by lazy {
        strokeIcon(
            "Key",
            "M16.2,3.5 C18.6,3.5 20.5,5.4 20.5,7.8 C20.5,10.2 18.6,12.1 16.2,12.1 C13.8,12.1 11.9,10.2 " +
                "11.9,7.8 C11.9,5.4 13.8,3.5 16.2,3.5 Z M12.9,11.1 L4,20 M7.5,16.5 L5.2,18.8 M10.3,13.7 L8,16"
        )
    }

    /** Brand mark: a bridge arc over a phone handset. */
    val LogoMark: ImageVector by lazy {
        ImageVector.Builder(
            name = "LogoMark",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                pathData = addPathNodes(
                    "M5.5,9.4 C6.8,5.2 9.2,3.1 12,3.1 C14.8,3.1 17.2,5.2 18.5,9.4"
                )
            )
            addGroup(
                scaleX = 0.62f,
                scaleY = 0.62f,
                translationX = 4.56f,
                translationY = 7.6f
            )
            addPath(
                fill = SolidColor(Color.Black),
                pathData = addPathNodes(
                    "M6.62,10.79c1.44,2.83,3.76,5.14,6.59,6.59l2.2-2.2c0.27-0.27,0.67-0.36,1.02-0.24" +
                        "c1.12,0.37,2.33,0.57,3.57,0.57c0.55,0,1,0.45,1,1V20c0,0.55-0.45,1-1,1c-9.39,0-17-7.61-17-17" +
                        "c0-0.55,0.45-1,1-1h3.5c0.55,0,1,0.45,1,1c0,1.25,0.2,2.45,0.57,3.57c0.11,0.35,0.03,0.74-0.25,1.02" +
                        "L6.62,10.79z"
                )
            )
            clearGroup()
        }.build()
    }
}
