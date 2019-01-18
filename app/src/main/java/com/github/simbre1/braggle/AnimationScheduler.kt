package com.github.simbre1.braggle

import android.graphics.Canvas
import android.view.View
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class AnimationScheduler(private val fps: Int,
                         private val view: View) {

    private var timer = Timer()
    private var running = false
    private val animations = mutableListOf<AnimatedDrawable>()

    fun add(animatedDrawable: AnimatedDrawable) {
        animations.add(animatedDrawable)
        if (!running) {
            start()
        }
    }

    fun draw(canvas: Canvas) {
        val done = mutableListOf<AnimatedDrawable>()
        animations.forEach {
            if (it.isDone()) {
                done.add(it)
            } else {
                it.draw(canvas)
            }
        }
        animations.removeAll(done)
        if (animations.isEmpty()) {
            try {
                timer.cancel()
                timer.purge()
            } catch (ignored: Exception) {
            }
            timer = Timer()
            running = false
        }
    }

    fun start() {
        running = true
        timer.scheduleAtFixedRate(0L, (1000/fps).toLong()) {
            view.invalidate()
        }
    }
}