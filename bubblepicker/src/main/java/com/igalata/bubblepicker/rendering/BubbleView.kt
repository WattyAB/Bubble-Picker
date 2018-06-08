package com.igalata.bubblepicker.rendering

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

// This view is not meant to be used in xml layouts
@SuppressLint("ViewConstructor")
class BubbleView(context: Context) : View(context) {
    var textPaint: TextPaint? = null
    private var mTextWidth: Float = 0.toFloat()
    private var mTextHeight: Float = 0.toFloat()

    var backgroundPaint: Paint? = null

    var applianceInstanceId: String = ""
    var title: String = ""

    var size: Float = 80.0f

    init {
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
        //layoutParams = ViewGroup.LayoutParams(size.toInt(), size.toInt())

    }

    private fun invalidateTextPaintAndMeasurements() {
        mTextWidth = textPaint?.measureText(title) ?: 0.toFloat()

        val fontMetrics = textPaint?.fontMetrics
        mTextHeight = fontMetrics?.bottom ?: 0.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        invalidateTextPaintAndMeasurements()

        val contentWidth = size - paddingLeft - paddingRight
        val contentHeight = size - paddingTop - paddingBottom

        val radius = (contentHeight / 2).toFloat()
        canvas.drawCircle(
            x,
            y,
            radius,
            backgroundPaint
        )

        // Draw the text.
        canvas.drawText(
            title,
            ((contentWidth - mTextWidth) / 2),
            ((contentHeight + mTextHeight) / 2),
            textPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(80, 80)
    }


}

class BubbleViewFactory(private val context: Context) {
    fun create(): BubbleView {
        return BubbleView(context)
    }
}