package com.github.simbre1.braggle

import android.graphics.Canvas
import android.graphics.Paint

class ColorShiftingHittable(val hittable: BoardView.Hittable,
                            private val backgroundPaint: Paint,
                            private val foregroundPaint: Paint,
                            private val fromColor: Int,
                            private val toColor: Int,
                            private val durationMs: Long)
    : AnimatedDrawable(durationMs) {

    override fun draw(canvas: Canvas,
                      elapsedMs: Long) {
        foregroundPaint.color = colorShiftRGB(
            fromColor,
            toColor,
            durationMs.toFloat(),
            elapsedMs.toFloat())
        hittable.draw(canvas, foregroundPaint, backgroundPaint)
    }
}