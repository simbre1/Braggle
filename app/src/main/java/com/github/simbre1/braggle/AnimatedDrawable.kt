package com.github.simbre1.braggle

import android.graphics.Canvas
import android.graphics.Color
import android.os.SystemClock
import kotlin.math.round

abstract class AnimatedDrawable(private val durationMs: Long) {
    private val startTime = SystemClock.elapsedRealtime()

    fun draw(canvas: Canvas) {
        draw(canvas, getElapsedMs())
    }

    abstract fun draw(canvas: Canvas, elapsedMs: Long)

    fun isDone(): Boolean = getElapsedMs() >= durationMs

    private fun getElapsedMs() = SystemClock.elapsedRealtime() - startTime

    companion object {
        fun linear(from: Int, to: Int, duration: Float, elapsed: Float) : Int {
            return round(from + ((to - from) * (elapsed / duration))).toInt()
        }

        fun quadratic(from: Int, to: Int, duration: Float, elapsed: Float) : Int {
            val x = elapsed / duration
            return round(from + ((to - from) * (x * x))).toInt()
        }

        fun linear(from: Float, to: Float, duration: Float, elapsed: Float) : Float {
            return from + ((to - from) * (elapsed / duration))
        }

        fun quadratic(from: Float, to: Float, duration: Float, elapsed: Float) : Float {
            val x = elapsed / duration
            return from + ((to - from) * (x * x))
        }

        fun colorShiftHSV(fromColor: Int, toColor: Int, duration: Float, elapsed: Float) : Int {
            val fromHsv = FloatArray(3)
            Color.colorToHSV(fromColor, fromHsv)
            val toHsv = FloatArray(3)
            Color.colorToHSV(toColor, toHsv)

            return Color.HSVToColor(
                floatArrayOf(
                    quadratic(fromHsv[0], toHsv[0], duration, elapsed),
                    quadratic(fromHsv[1], toHsv[1], duration, elapsed),
                    quadratic(fromHsv[2], toHsv[2], duration, elapsed)
                )
            )
        }

        fun colorShiftRGB(fromColor: Int, toColor: Int, duration: Float, elapsed: Float) : Int {
            return Color.rgb(
                quadratic(Color.red(fromColor), Color.red(toColor), duration, elapsed),
                quadratic(Color.green(fromColor), Color.green(toColor), duration, elapsed),
                quadratic(Color.blue(fromColor), Color.blue(toColor), duration, elapsed)
            )
        }
    }
}