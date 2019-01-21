package com.github.simbre1.braggle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.github.simbre1.braggle.domain.Board
import kotlin.math.floor
import kotlin.math.min

class Tile(val row: Int, val col: Int, val str: String)

class BoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var board: Board? = null
    private var active = true
    private var hittables = mapOf<Tile, Hittable>()
    private val hits = mutableListOf<Hittable>()

    val wordListeners = mutableListOf<(List<Tile>) -> Unit>()

    private val strokePaint = Paint()
    private val strokeHitPaint = Paint()
    private val strokeDisabledPaint = Paint()
    private val backgroundPaint = Paint()

    private val cowView: AnimatedVectorDrawableCompat?
    private var cowVisible = false

    private val animationScheduler = AnimationScheduler(30, this)

    init {

        strokePaint.color = context?.getColorFromAttr(R.attr.colorDice) ?: Color.BLACK
        strokePaint.isAntiAlias = true

        strokeHitPaint.color = context?.getColorFromAttr(R.attr.colorDiceHit) ?: Color.RED
        strokeHitPaint.isAntiAlias = true

        strokeDisabledPaint.color = context?.getColorFromAttr(R.attr.colorDiceDisabled) ?: Color.GRAY
        strokeDisabledPaint.isAntiAlias = true

        backgroundPaint.color = context?.getColorFromAttr(R.attr.colorDiceBackground) ?: Color.WHITE
        backgroundPaint.isAntiAlias = true

        cowView = context?.run {
            AnimatedVectorDrawableCompat.create(
                this,
                R.drawable.animated_cow)
        }?.apply {
            registerAnimationCallback(object: Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    setCowVisibility(false)
                }
            })
        }

        addOnLayoutChangeListener { _: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->
            updateLayout()
        }
    }

    fun setBoard(newBoard : Board) {
        board = newBoard
        updateLayout()
        invalidate()
    }

    fun setActive(active: Boolean) {
        this.active = active
        invalidate()
    }

    private fun setCowVisibility(visible: Boolean) {
        cowVisible = visible
        invalidate()
    }

    fun showAnimatedCow() {
        setCowVisibility(true)
        cowView?.start()
    }

    fun highlightTiles(tiles: List<Tile>,
                       color: Int,
                       durationMs: Long = 1000) {
        tiles.mapNotNull { tile ->
            hittables[tile]?.let {
                ColorShiftingHittable(
                    it,
                    Paint(backgroundPaint),
                    Paint(strokeHitPaint),
                    color,
                    strokePaint.color,
                    durationMs)
            }
        }.forEach { h -> animationScheduler.add(h) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                hittables.values.find { collidable ->
                    collidable.hit(
                        event.getX(event.actionIndex),
                        event.getY(event.actionIndex))
                }?.also {
                    if (hits.isEmpty()) {
                        hits.add(it)
                    } else if (it != hits.last()) {
                        if (areConnected(it.getTile(), hits.last().getTile())
                            && !hits.contains(it)) {
                            hits.add(it)
                        }
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!hits.isEmpty()) {
                    val tiles = hits.map(Hittable::getTile)
                    wordListeners.forEach { l -> l.invoke(tiles) }
                    hits.clear()
                }
                invalidate()
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            hittables.forEach {
                it.value.draw(
                    this,
                    if(!active) strokeDisabledPaint
                    else strokePaint,
                    backgroundPaint)
            }

            animationScheduler.draw(this)

            if (active) {
                hits.forEach {
                    it.draw(
                        this,
                        strokeHitPaint,
                        backgroundPaint)
                }
            }

            cowView?.also {
                if (cowVisible) {
                    it.setBounds(
                        (width - it.intrinsicWidth) / 2,
                        (height - it.intrinsicHeight) / 2,
                        (width + it.intrinsicWidth) / 2,
                        (height + it.intrinsicHeight) / 2)
                    it.draw(this)
                }
            }
        }
    }

    private fun updateLayout() {
        board?.run {
            val canvasSize = min(width, height)
            val cellSize = floor(canvasSize.toFloat() / size().toFloat())
            val radius = cellSize / 2.2f

            val charWidth = radius * 0.75f

            setTextSizeForWidth(strokePaint, charWidth, "W")
            setTextSizeForWidth(strokeHitPaint, charWidth, "W")
            setTextSizeForWidth(strokeDisabledPaint, charWidth, "W")

            val updatedHittables = mutableMapOf<Tile, Hittable>()

            for (row in 0 until size()) {
                for (col in 0 until size()) {
                    val x = col * cellSize
                    val y = row * cellSize

                    val cx = x + cellSize / 2
                    val cy = y + cellSize / 2

                    val index = Tile(row, col, at(row, col))
                    updatedHittables[index] = Circle(
                        cx,
                        cy,
                        radius,
                        index)
                }
            }

            hittables = updatedHittables
        }
    }

    interface Hittable {
        fun hit(x: Float, y: Float) : Boolean
        fun getTile() : Tile
        fun draw(canvas: Canvas,
                 paintStroke: Paint,
                 paintBackground: Paint)
    }

    class Circle(private val cx: Float,
                 private val cy: Float,
                 private val radius: Float,
                 private val tile: Tile) : Hittable {
        override fun hit(x: Float, y: Float): Boolean {
            return Math.abs(x - cx) < (radius * 0.75)
                    && Math.abs(y - cy) < (radius * 0.75)
        }

        override fun getTile(): Tile {
            return tile
        }

        override fun toString(): String {
            return "Circle(cx=$cx, cy=$cy, radius=$radius, tile=$tile)"
        }

        override fun draw(canvas: Canvas,
                          paintStroke: Paint,
                          paintBackground: Paint) {
            val charWidth = radius * 0.75f
            canvas.drawCircle(cx, cy, radius, paintStroke)
            canvas.drawCircle(cx, cy, radius * 0.9f, paintBackground)
            canvas.drawText(
                tile.str,
                cx - (charWidth / 2),
                cy + (charWidth / 2),
                paintStroke)
        }
    }

    companion object {

        private fun areConnected(a: Tile, b: Tile) : Boolean {
            return Math.abs(a.row - b.row) < 2
                    && Math.abs(a.col - b.col) < 2
        }

        private fun setTextSizeForWidth(paint: Paint,
                                        desiredWidth: Float,
                                        text: String) {

            // Pick a reasonably large value for the test. Larger values produce
            // more accurate results, but may cause problems with hardware
            // acceleration. But there are workarounds for that, too; refer to
            // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
            val testTextSize = 48f

            // Get the bounds of the text, using our testTextSize.
            paint.textSize = testTextSize
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)

            // Calculate the desired size as a proportion of our testTextSize.
            val desiredTextSize = testTextSize * desiredWidth / bounds.width()

            // Set the paint for that size.
            paint.textSize = desiredTextSize
        }
    }
}

fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue()) : Int {
    val a = obtainStyledAttributes(typedValue.data, intArrayOf(attrColor))
    val color = a?.getColor(0, 0)
    a?.recycle()
    return color ?: Color.RED
}