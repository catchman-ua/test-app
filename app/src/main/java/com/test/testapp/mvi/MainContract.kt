package com.test.testapp.mvi

sealed interface IconIntent {
    data class OnMeasured(val containerW: Int, val containerH: Int, val iconW: Int, val iconH: Int) : IconIntent
    data class OnTouch(val action: Int, val xInContainer: Float, val yInContainer: Float) : IconIntent
    object Noop : IconIntent
}

data class IconState(
    val containerW: Int = 0,
    val containerH: Int = 0,
    val iconW: Int = 0,
    val iconH: Int = 0,

    val xPx: Float = 0f,
    val yPx: Float = 0f,

    val isDragging: Boolean = false,
    val grabDx: Float = 0f,
    val grabDy: Float = 0f
) {
    val canInteract: Boolean get() = containerW > 0 && containerH > 0 && iconW > 0 && iconH > 0
}

sealed interface IconEffect
