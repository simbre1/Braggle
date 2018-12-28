package com.github.simbre1.braggle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.function.Consumer
import kotlin.math.floor
import kotlin.math.min
import android.util.TypedValue

fun getColor(context: Context?, colorId: Int) : Int? {
    val typedValue = TypedValue()
    val a = context?.obtainStyledAttributes(typedValue.data, intArrayOf(colorId))
    val color = a?.getColor(0, 0)
    a?.recycle()
    return color
}

class BoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var board = Board.random(4)
    private var hittables = listOf<Hittable>()
    private val hits = mutableListOf<BoardIndex>()

    val wordListeners = mutableListOf<Consumer<String>>()

    private val circleStroke = Paint()
    private val circleStrokeHit = Paint()
    private val circleBg = Paint()
    private val charStyle = Paint()
    private val charHitStyle = Paint()

    init {
        val strokeColor = getColor(context, R.attr.colorButtonNormal) ?: Color.GREEN
        val strokeColorHit = getColor(context, R.attr.colorControlActivated) ?: Color.CYAN

        circleStroke.color = strokeColor

        circleStroke.isAntiAlias = true
        circleStrokeHit.color = strokeColorHit

        circleStrokeHit.isAntiAlias = true
        circleBg.color = Color.WHITE
        charStyle.color = strokeColor
        charHitStyle.color = strokeColorHit
    }

    fun setBoard(newBoard : Board) {
        board = newBoard
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val hit = hittables.find { collidable ->
                    collidable.hit(
                            event.getX(event.actionIndex),
                            event.getY(event.actionIndex))
                }
                if (hit != null) {
                    if (hits.isEmpty()) {
                        hits.add(hit.getIndex())
                    } else if (hit.getIndex() != hits.last()) {
                        if (areConnected(hit.getIndex(), hits.last())
                                && !hits.contains(hit.getIndex())) {
                            hits.add(hit.getIndex())
                        }
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!hits.isEmpty()) {
                    val word = hits.map { i -> board.at(i.first, i.second) }
                            .toCharArray()
                            .joinToString("")
                    wordListeners.forEach { l -> l.accept(word) }
                    hits.clear()
                }
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        val w = canvas.width
        val h = canvas.height

        val size = min(w, h)
        val cellSize = floor(size.toFloat() / board.size().toFloat())
        val radius = cellSize / 2.2f

        val charWidth = radius * 0.75f
        setTextSizeForWidth(charStyle, charWidth, "W")
        setTextSizeForWidth(charHitStyle, charWidth, "W")

        val updatedHittables = mutableListOf<Hittable>()

        for (row in 0 until board.size()) {
            for (col in 0 until board.size()) {
                val x = col * cellSize
                val y = row * cellSize

                val cx = x + cellSize / 2
                val cy = y + cellSize / 2

                val index = BoardIndex(row, col)
                updatedHittables.add(Circle(cx, cy, charWidth, index))


                val hit = hits.contains(index)
                canvas.drawCircle(cx, cy, radius, if (hit) circleStrokeHit else circleStroke)
                canvas.drawCircle(cx, cy, radius * 0.9f, circleBg)
                canvas.drawText(
                        board.at(row, col).toString(),
                        cx - (charWidth / 2),
                        cy + (charWidth / 2),
                        if (hit) charHitStyle else charStyle)
            }
        }

        hittables = updatedHittables
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

    interface Hittable {
        fun hit(x: Float, y: Float) : Boolean
        fun getIndex() : BoardIndex
    }

    class Circle(private val cx: Float,
                 private val cy: Float,
                 private val radius: Float,
                 private val index: BoardIndex) : Hittable {
        override fun hit(x: Float, y: Float): Boolean {
            return Math.abs(x - cx) < radius
                    && Math.abs(y - cy) < radius
        }

        override fun getIndex(): BoardIndex {
            return index
        }

        override fun toString(): String {
            return "Circle(cx=$cx, cy=$cy, radius=$radius, index=$index)"
        }
    }
}

typealias BoardIndex = Pair<Int, Int>
fun areConnected(a: BoardIndex, b: BoardIndex) : Boolean {
    return Math.abs(a.first - b.first) < 2
            && Math.abs(a.second - b.second) < 2
}

