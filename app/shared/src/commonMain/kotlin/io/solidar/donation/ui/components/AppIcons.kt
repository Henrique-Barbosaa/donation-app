package io.solidar.donation.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


object AppIcons {
    val Email: ImageVector
        get() =
            ImageVector.Builder(
                name = "Email",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                    strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(20f, 4f)
                    horizontalLineTo(4f)
                    curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
                    lineTo(2f, 18f)
                    curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                    horizontalLineTo(20f)
                    curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                    lineTo(22f, 6f)
                    curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
                    close()
                    moveTo(20f, 8f)
                    lineTo(12f, 13f)
                    lineTo(4f, 8f)
                    lineTo(4f, 6f)
                    lineTo(12f, 11f)
                    lineTo(20f, 6f)
                    lineTo(20f, 8f)
                    close()
                }
            }.build()
    val Favorite: ImageVector
        get() =
            ImageVector.Builder(
                name = "Favorite",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1.0f,
                    stroke = null,
                    strokeAlpha = 1.0f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                    strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Miter,
                    strokeLineMiter = 1.0f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(12f, 21.35f)
                    lineTo(10.55f, 20.03f)
                    curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
                    curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
                    curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
                    curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
                    curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
                    curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
                    lineTo(12f, 21.35f)
                    close()
                }
            }.build()
    val Lock: ImageVector
        get() =
            ImageVector.Builder(
                name = "Lock",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                ) {
                    moveTo(18f, 8f)
                    horizontalLineTo(17f)
                    verticalLineTo(6f)
                    curveTo(17f, 3.24f, 14.76f, 1f, 12f, 1f)
                    curveTo(9.24f, 1f, 7f, 3.24f, 7f, 6f)
                    verticalLineTo(8f)
                    horizontalLineTo(6f)
                    curveTo(4.9f, 8f, 4f, 8.9f, 4f, 10f)
                    verticalLineTo(20f)
                    curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
                    horizontalLineTo(18f)
                    curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
                    verticalLineTo(10f)
                    curveTo(20f, 8.9f, 19.1f, 8f, 18f, 8f)
                    close()
                    moveTo(9f, 6f)
                    curveTo(9f, 4.34f, 10.34f, 3f, 12f, 3f)
                    curveTo(13.66f, 3f, 15f, 4.34f, 15f, 6f)
                    verticalLineTo(8f)
                    horizontalLineTo(9f)
                    verticalLineTo(6f)
                    close()
                    moveTo(12f, 17f)
                    curveTo(10.9f, 17f, 10f, 16.1f, 10f, 15f)
                    curveTo(10f, 13.9f, 10.9f, 13f, 12f, 13f)
                    curveTo(13.1f, 13f, 14f, 13.9f, 14f, 15f)
                    curveTo(14f, 16.1f, 13.1f, 17f, 12f, 17f)
                    close()
                }
            }.build()

    val ArrowBack: ImageVector
        get() =
            ImageVector.Builder(
                name = "ArrowBack",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(20f, 11f)
                    horizontalLineTo(7.83f)
                    lineTo(13.42f, 5.41f)
                    lineTo(12f, 4f)
                    lineTo(4f, 12f)
                    lineTo(12f, 20f)
                    lineTo(13.41f, 18.59f)
                    lineTo(7.83f, 13f)
                    horizontalLineTo(20f)
                    verticalLineTo(11f)
                    close()
                }
            }.build()

    val Person: ImageVector
        get() =
            ImageVector.Builder(
                name = "Person",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12f, 12f)
                    curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
                    curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
                    curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
                    curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f)
                    close()
                    moveTo(12f, 14f)
                    curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f)
                    verticalLineTo(20f)
                    horizontalLineTo(20f)
                    verticalLineTo(18f)
                    curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f)
                    close()
                }
            }.build()

    val Home: ImageVector
        get() =
            ImageVector.Builder(
                name = "Home",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(10f, 20f)
                    verticalLineTo(14f)
                    horizontalLineTo(14f)
                    verticalLineTo(20f)
                    horizontalLineTo(19f)
                    verticalLineTo(12f)
                    horizontalLineTo(22f)
                    lineTo(12f, 3f)
                    lineTo(2f, 12f)
                    horizontalLineTo(5f)
                    verticalLineTo(20f)
                    horizontalLineTo(10f)
                    close()
                }
            }.build()

    val Search: ImageVector
        get() =
            ImageVector.Builder(
                name = "Search",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(15.5f, 14f)
                    horizontalLineTo(14.71f)
                    lineTo(14.43f, 13.73f)
                    curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
                    curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
                    curveTo(5.91f, 3f, 3f, 5.91f, 3f, 9.5f)
                    curveTo(3f, 13.09f, 5.91f, 16f, 9.5f, 16f)
                    curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
                    lineTo(14f, 14.71f)
                    verticalLineTo(15.5f)
                    lineTo(19f, 20.49f)
                    lineTo(20.49f, 19f)
                    lineTo(15.5f, 14f)
                    close()
                    moveTo(9.5f, 14f)
                    curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
                    curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f)
                    curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f)
                    curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f)
                    close()
                }
            }.build()

    val Add: ImageVector
        get() =
            ImageVector.Builder(
                name = "Add",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(19f, 13f)
                    horizontalLineTo(13f)
                    verticalLineTo(19f)
                    horizontalLineTo(11f)
                    verticalLineTo(13f)
                    horizontalLineTo(5f)
                    verticalLineTo(11f)
                    horizontalLineTo(11f)
                    verticalLineTo(5f)
                    horizontalLineTo(13f)
                    verticalLineTo(11f)
                    horizontalLineTo(19f)
                    verticalLineTo(13f)
                    close()
                }
            }.build()

    val Notifications: ImageVector
        get() =
            ImageVector.Builder(
                name = "Notifications",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12f, 22f)
                    curveTo(13.1f, 22f, 14f, 21.1f, 14f, 20f)
                    horizontalLineTo(10f)
                    curveTo(10f, 21.1f, 10.9f, 22f, 12f, 22f)
                    close()
                    moveTo(18f, 16f)
                    verticalLineTo(11f)
                    curveTo(18f, 7.93f, 16.37f, 5.36f, 13.5f, 4.68f)
                    verticalLineTo(4f)
                    curveTo(13.5f, 3.17f, 12.83f, 2.5f, 12f, 2.5f)
                    curveTo(11.17f, 2.5f, 10.5f, 3.17f, 10.5f, 4f)
                    verticalLineTo(4.68f)
                    curveTo(7.64f, 5.36f, 6f, 7.92f, 6f, 11f)
                    verticalLineTo(16f)
                    lineTo(4f, 18f)
                    verticalLineTo(19f)
                    horizontalLineTo(20f)
                    verticalLineTo(18f)
                    lineTo(18f, 16f)
                    close()
                }
            }.build()

    val Camera: ImageVector
        get() =
            ImageVector.Builder(
                name = "Camera",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(12f, 12f)
                    curveTo(10.34f, 12f, 9f, 13.34f, 9f, 15f)
                    curveTo(9f, 16.66f, 10.34f, 18f, 12f, 18f)
                    curveTo(13.66f, 18f, 15f, 16.66f, 15f, 15f)
                    curveTo(15f, 13.34f, 13.66f, 12f, 12f, 12f)
                    close()
                    moveTo(9f, 2f)
                    lineTo(7.17f, 4f)
                    horizontalLineTo(4f)
                    curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
                    verticalLineTo(18f)
                    curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                    horizontalLineTo(20f)
                    curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                    verticalLineTo(6f)
                    curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
                    horizontalLineTo(16.83f)
                    lineTo(15f, 2f)
                    horizontalLineTo(9f)
                    close()
                    moveTo(12f, 19f)
                    curveTo(9.79f, 19f, 8f, 17.21f, 8f, 15f)
                    curveTo(8f, 12.79f, 9.79f, 11f, 12f, 11f)
                    curveTo(14.21f, 11f, 16f, 12.79f, 16f, 15f)
                    curveTo(16f, 17.21f, 14.21f, 19f, 12f, 19f)
                    close()
                }
            }.build()

    val Check: ImageVector
        get() =
            ImageVector.Builder(
                name = "Check",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(fill = SolidColor(Color.Black)) {
                    moveTo(9f, 16.17f)
                    lineTo(4.83f, 12f)
                    lineTo(3.41f, 13.41f)
                    lineTo(9f, 19f)
                    lineTo(21f, 7f)
                    lineTo(19.59f, 5.59f)
                    lineTo(9f, 16.17f)
                    close()
                }
            }.build()
}
