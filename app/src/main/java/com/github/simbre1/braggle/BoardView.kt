package com.github.simbre1.braggle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.floor
import kotlin.math.min

typealias BoardIndex = Pair<Int, Int>

class BoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var board: Board? = null
    private var hittables = listOf<Hittable>()
    private val hits = mutableListOf<BoardIndex>()

    val wordListeners = mutableListOf<(String) -> Unit>()

    private val strokePaint = Paint()
    private val strokeHitPaint = Paint()
    private val backgroundPaint = Paint()

    init {
        val strokeColor = getColor(context, R.attr.colorPrimary) ?: Color.BLACK
        val bgColor = Color.WHITE
        val strokeColorHit = getColor(context, R.attr.colorAccent) ?: Color.RED

        strokePaint.color = strokeColor
        strokePaint.isAntiAlias = true

        strokeHitPaint.color = strokeColorHit
        strokeHitPaint.isAntiAlias = true

        backgroundPaint.color = bgColor
        backgroundPaint.isAntiAlias = true

        addOnLayoutChangeListener { _: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->
            updateLayout()
        }
    }

    fun setBoard(newBoard : Board) {
        board = newBoard
        updateLayout()
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val currentBoard = board ?: return true

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                hittables.find { collidable ->
                    collidable.hit(
                        event.getX(event.actionIndex),
                        event.getY(event.actionIndex))
                }?.also {
                    if (hits.isEmpty()) {
                        hits.add(it.getIndex())
                    } else if (it.getIndex() != hits.last()) {
                        if (areConnected(it.getIndex(), hits.last())
                            && !hits.contains(it.getIndex())) {
                            hits.add(it.getIndex())
                        }
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!hits.isEmpty()) {
                    val word = hits.map { i -> currentBoard.at(i.first, i.second) }
                        .joinToString("")
                        .toUpperCase()
                    wordListeners.forEach { l -> l.invoke(word) }
                    hits.clear()
                }
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.run {
            hittables.forEach {
                val hit = hits.contains(it.getIndex())
                it.draw(
                    this,
                    if(hit) strokeHitPaint else strokePaint,
                    backgroundPaint)
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

            val updatedHittables = mutableListOf<Hittable>()

            for (row in 0 until size()) {
                for (col in 0 until size()) {
                    val x = col * cellSize
                    val y = row * cellSize

                    val cx = x + cellSize / 2
                    val cy = y + cellSize / 2

                    val index = BoardIndex(row, col)
                    updatedHittables.add(
                        Circle(
                            cx,
                            cy,
                            radius,
                            index,
                            at(index)))
                }
            }

            hittables = updatedHittables
        }
    }

    interface Hittable {
        fun hit(x: Float, y: Float) : Boolean
        fun getIndex() : BoardIndex
        fun draw(canvas: Canvas,
                 paintStroke: Paint,
                 paintBackground: Paint)
    }

    class Circle(private val cx: Float,
                 private val cy: Float,
                 private val radius: Float,
                 private val index: BoardIndex,
                 private val text: String) : Hittable {
        override fun hit(x: Float, y: Float): Boolean {
            return Math.abs(x - cx) < (radius * 0.75)
                    && Math.abs(y - cy) < (radius * 0.75)
        }

        override fun getIndex(): BoardIndex {
            return index
        }

        override fun toString(): String {
            return "Circle(cx=$cx, cy=$cy, radius=$radius, index=$index)"
        }

        override fun draw(canvas: Canvas,
                          paintStroke: Paint,
                          paintBackground: Paint) {
            val charWidth = radius * 0.75f
            canvas.drawCircle(cx, cy, radius, paintStroke)
            canvas.drawCircle(cx, cy, radius * 0.9f, paintBackground)
            canvas.drawText(
                text,
                cx - (charWidth / 2),
                cy + (charWidth / 2),
                paintStroke)
        }
    }

    companion object {
        fun getColor(context: Context?, colorId: Int) : Int? {
            val typedValue = TypedValue()
            val a = context?.obtainStyledAttributes(typedValue.data, intArrayOf(colorId))
            val color = a?.getColor(0, 0)
            a?.recycle()
            return color
        }

        private fun areConnected(a: BoardIndex, b: BoardIndex) : Boolean {
            return Math.abs(a.first - b.first) < 2
                    && Math.abs(a.second - b.second) < 2
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
