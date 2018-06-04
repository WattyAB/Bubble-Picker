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

    var size: Double = 0.0

    init {
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        mTextWidth = textPaint?.measureText(title) ?: 0.toFloat()

        val fontMetrics = textPaint?.fontMetrics
        mTextHeight = fontMetrics?.bottom ?: 0.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        invalidateTextPaintAndMeasurements()

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        val radius = (contentHeight / 2).toFloat()
        canvas.drawCircle(
            radius,
            radius,
            radius,
            backgroundPaint
        )

        // Draw the text.
        canvas.drawText(
            title,
            (contentWidth - mTextWidth) / 2,
            (contentHeight + mTextHeight) / 2,
            textPaint
        )
    }
}