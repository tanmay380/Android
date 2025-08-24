package com.example.weathercomposeapp.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin

val Rainy: ImageVector
    get() {
        if (_Rainy != null) return _Rainy!!

        _Rainy = ImageVector.Builder(
            name = "Rainy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(558f, 876f)
                quadToRelative(-15f, 8f, -30.5f, 2.5f)
                reflectiveQuadTo(504f, 858f)
                lineToRelative(-60f, -120f)
                quadToRelative(-8f, -15f, -2.5f, -30.5f)
                reflectiveQuadTo(462f, 684f)
                reflectiveQuadToRelative(30.5f, -2.5f)
                reflectiveQuadTo(516f, 702f)
                lineToRelative(60f, 120f)
                quadToRelative(8f, 15f, 2.5f, 30.5f)
                reflectiveQuadTo(558f, 876f)
                moveToRelative(240f, 0f)
                quadToRelative(-15f, 8f, -30.5f, 2.5f)
                reflectiveQuadTo(744f, 858f)
                lineToRelative(-60f, -120f)
                quadToRelative(-8f, -15f, -2.5f, -30.5f)
                reflectiveQuadTo(702f, 684f)
                reflectiveQuadToRelative(30.5f, -2.5f)
                reflectiveQuadTo(756f, 702f)
                lineToRelative(60f, 120f)
                quadToRelative(8f, 15f, 2.5f, 30.5f)
                reflectiveQuadTo(798f, 876f)
                moveToRelative(-480f, 0f)
                quadToRelative(-15f, 8f, -30.5f, 2.5f)
                reflectiveQuadTo(264f, 858f)
                lineToRelative(-60f, -120f)
                quadToRelative(-8f, -15f, -2.5f, -30.5f)
                reflectiveQuadTo(222f, 684f)
                reflectiveQuadToRelative(30.5f, -2.5f)
                reflectiveQuadTo(276f, 702f)
                lineToRelative(60f, 120f)
                quadToRelative(8f, 15f, 2.5f, 30.5f)
                reflectiveQuadTo(318f, 876f)
                moveToRelative(-18f, -236f)
                quadToRelative(-91f, 0f, -155.5f, -64.5f)
                reflectiveQuadTo(80f, 420f)
                quadToRelative(0f, -83f, 55f, -145f)
                reflectiveQuadToRelative(136f, -73f)
                quadToRelative(32f, -57f, 87.5f, -89.5f)
                reflectiveQuadTo(480f, 80f)
                quadToRelative(90f, 0f, 156.5f, 57.5f)
                reflectiveQuadTo(717f, 281f)
                quadToRelative(69f, 6f, 116f, 57f)
                reflectiveQuadToRelative(47f, 122f)
                quadToRelative(0f, 75f, -52.5f, 127.5f)
                reflectiveQuadTo(700f, 640f)
                close()
                moveToRelative(0f, -80f)
                horizontalLineToRelative(400f)
                quadToRelative(42f, 0f, 71f, -29f)
                reflectiveQuadToRelative(29f, -71f)
                reflectiveQuadToRelative(-29f, -71f)
                reflectiveQuadToRelative(-71f, -29f)
                horizontalLineToRelative(-60f)
                verticalLineToRelative(-40f)
                quadToRelative(0f, -66f, -47f, -113f)
                reflectiveQuadToRelative(-113f, -47f)
                quadToRelative(-48f, 0f, -87.5f, 26f)
                reflectiveQuadTo(333f, 256f)
                lineToRelative(-10f, 24f)
                horizontalLineToRelative(-25f)
                quadToRelative(-57f, 2f, -97.5f, 42.5f)
                reflectiveQuadTo(160f, 420f)
                quadToRelative(0f, 58f, 41f, 99f)
                reflectiveQuadToRelative(99f, 41f)
                moveToRelative(180f, -200f)
            }
        }.build()

        return _Rainy!!
    }

val Blood_pressure: ImageVector
    get() {
        if (_Blood_pressure != null) return _Blood_pressure!!

        _Blood_pressure = ImageVector.Builder(
            name = "Blood_pressure",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(80f, 360f)
                verticalLineToRelative(-120f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(160f, 160f)
                horizontalLineToRelative(640f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(880f, 240f)
                verticalLineToRelative(220f)
                horizontalLineToRelative(-80f)
                verticalLineToRelative(-220f)
                horizontalLineTo(160f)
                verticalLineToRelative(120f)
                close()
                moveToRelative(200f, 320f)
                quadToRelative(-11f, 0f, -21f, -5.5f)
                reflectiveQuadTo(244f, 658f)
                lineToRelative(-69f, -138f)
                horizontalLineTo(80f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(120f)
                quadToRelative(11f, 0f, 21f, 5.5f)
                reflectiveQuadToRelative(15f, 16.5f)
                lineToRelative(44f, 88f)
                lineToRelative(124f, -248f)
                quadToRelative(5f, -10f, 15f, -15f)
                reflectiveQuadToRelative(21f, -5f)
                reflectiveQuadToRelative(21f, 5f)
                reflectiveQuadToRelative(15f, 15f)
                lineToRelative(67f, 134f)
                quadToRelative(-18f, 11f, -34.5f, 23f)
                reflectiveQuadTo(478f, 486f)
                lineToRelative(-38f, -76f)
                lineToRelative(-124f, 248f)
                quadToRelative(-5f, 11f, -15f, 16.5f)
                reflectiveQuadToRelative(-21f, 5.5f)
                moveToRelative(147f, 120f)
                horizontalLineTo(160f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(80f, 720f)
                verticalLineToRelative(-120f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(120f)
                horizontalLineToRelative(243f)
                quadToRelative(3f, 21f, 9f, 41f)
                reflectiveQuadToRelative(15f, 39f)
                moveToRelative(253f, 80f)
                quadToRelative(-83f, 0f, -141.5f, -58.5f)
                reflectiveQuadTo(480f, 680f)
                reflectiveQuadToRelative(58.5f, -141.5f)
                reflectiveQuadTo(680f, 480f)
                reflectiveQuadToRelative(141.5f, 58.5f)
                reflectiveQuadTo(880f, 680f)
                reflectiveQuadToRelative(-58.5f, 141.5f)
                reflectiveQuadTo(680f, 880f)
                moveToRelative(8f, -180f)
                lineToRelative(91f, -91f)
                lineToRelative(-28f, -28f)
                lineToRelative(-91f, 91f)
                close()
            }
        }.build()

        return _Blood_pressure!!
    }

val Wind: ImageVector
    get() {
        if (_Wind != null) return _Wind!!

        _Wind = ImageVector.Builder(
            name = "Wind",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color.Black)
            ) {
                moveTo(12.5f, 2f)
                arcTo(2.5f, 2.5f, 0f, false, false, 10f, 4.5f)
                arcToRelative(0.5f, 0.5f, 0f, false, true, -1f, 0f)
                arcTo(3.5f, 3.5f, 0f, true, true, 12.5f, 8f)
                horizontalLineTo(0.5f)
                arcToRelative(0.5f, 0.5f, 0f, false, true, 0f, -1f)
                horizontalLineToRelative(12f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, 0f, -5f)
                moveToRelative(-7f, 1f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, 1f)
                arcToRelative(0.5f, 0.5f, 0f, false, true, -1f, 0f)
                arcToRelative(2f, 2f, 0f, true, true, 2f, 2f)
                horizontalLineToRelative(-5f)
                arcToRelative(0.5f, 0.5f, 0f, false, true, 0f, -1f)
                horizontalLineToRelative(5f)
                arcToRelative(1f, 1f, 0f, false, false, 0f, -2f)
                moveTo(0f, 9.5f)
                arcTo(0.5f, 0.5f, 0f, false, true, 0.5f, 9f)
                horizontalLineToRelative(10.042f)
                arcToRelative(3f, 3f, 0f, true, true, -3f, 3f)
                arcToRelative(0.5f, 0.5f, 0f, false, true, 1f, 0f)
                arcToRelative(2f, 2f, 0f, true, false, 2f, -2f)
                horizontalLineTo(0.5f)
                arcToRelative(0.5f, 0.5f, 0f, false, true, -0.5f, -0.5f)
            }
        }.build()

        return _Wind!!
    }

val Sunrise: ImageVector
    get() {
        if (_Sunrise != null) return _Sunrise!!

        _Sunrise = ImageVector.Builder(
            name = "Sunrise",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 2f)
                verticalLineToRelative(8f)
                moveToRelative(-7.07f, 0.93f)
                lineToRelative(1.41f, 1.41f)
                moveTo(2f, 18f)
                horizontalLineToRelative(2f)
                moveToRelative(16f, 0f)
                horizontalLineToRelative(2f)
                moveToRelative(-2.93f, -7.07f)
                lineToRelative(-1.41f, 1.41f)
                moveTo(22f, 22f)
                horizontalLineTo(2f)
                moveTo(8f, 6f)
                lineToRelative(4f, -4f)
                lineToRelative(4f, 4f)
                moveToRelative(0f, 12f)
                arcToRelative(4f, 4f, 0f, false, false, -8f, 0f)
            }
        }.build()

        return _Sunrise!!
    }

val Sunset: ImageVector
    get() {
        if (_Sunset != null) return _Sunset!!

        _Sunset = ImageVector.Builder(
            name = "Sunset",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 10f)
                verticalLineTo(2f)
                moveToRelative(-7.07f, 8.93f)
                lineToRelative(1.41f, 1.41f)
                moveTo(2f, 18f)
                horizontalLineToRelative(2f)
                moveToRelative(16f, 0f)
                horizontalLineToRelative(2f)
                moveToRelative(-2.93f, -7.07f)
                lineToRelative(-1.41f, 1.41f)
                moveTo(22f, 22f)
                horizontalLineTo(2f)
                moveTo(16f, 6f)
                lineToRelative(-4f, 4f)
                lineToRelative(-4f, -4f)
                moveToRelative(8f, 12f)
                arcToRelative(4f, 4f, 0f, false, false, -8f, 0f)
            }
        }.build()

        return _Sunset!!
    }

private var _Sunset: ImageVector? = null

private var _Sunrise: ImageVector? = null

private var _Wind: ImageVector? = null

private var _Blood_pressure: ImageVector? = null


private var _Rainy: ImageVector? = null

