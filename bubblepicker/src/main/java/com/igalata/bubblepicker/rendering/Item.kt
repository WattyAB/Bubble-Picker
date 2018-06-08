package com.igalata.bubblepicker.rendering

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import com.igalata.bubblepicker.model.BubbleGradient
import com.igalata.bubblepicker.model.PickerItem
import com.igalata.bubblepicker.physics.CircleBody
import org.jbox2d.common.Vec2

/**
 * Created by irinagalata on 1/19/17.
 */
data class Item(val pickerItem: PickerItem, val circleBody: CircleBody, val bubbleView: BubbleView) {

    val x: Float
        get() = circleBody.physicalBody.position.x

    val y: Float
        get() = circleBody.physicalBody.position.y

    val radius: Float
        get() = circleBody.currentRadius

    val initialPosition: Vec2
        get() = circleBody.position

    val currentPosition: Vec2
        get() = circleBody.physicalBody.position

    private val bitmapSize = 256f
//    private val gradient: LinearGradient?
//        get() {
//            return pickerItem.gradient?.let {
//                val horizontal = it.direction == BubbleGradient.HORIZONTAL
//                LinearGradient(if (horizontal) 0f else bitmapSize / 2f,
//                        if (horizontal) bitmapSize / 2f else 0f,
//                        if (horizontal) bitmapSize else bitmapSize / 2f,
//                        if (horizontal) bitmapSize / 2f else bitmapSize,
//                        it.startColor, it.endColor, Shader.TileMode.CLAMP)
//            }
//        }

    init {
        //bubbleView.size = radius.toDouble()
        bubbleView.x = x
        bubbleView.y = y
        //drawIcon()
    }

    fun draw(canvas: Canvas) {
        bubbleView.x = x * (canvas.width / 2) + (canvas.width / 2)
        bubbleView.y = y * (canvas.height / 2) + (canvas.height / 2)
        bubbleView.size = circleBody.currentRadius * 2000

        drawBackground()
        drawText()
        bubbleView.draw(canvas)
    }

    private fun drawBackground() {
        val bgPaint = Paint()
        bgPaint.style = Paint.Style.FILL

        pickerItem.color?.let { bgPaint.color = it }
        //pickerItem.gradient?.let { bgPaint.shader = gradient }

        bubbleView.backgroundPaint = bgPaint
    }

    private fun drawText() {
        if (pickerItem.title == null || pickerItem.textColor == null) return

        pickerItem.title?.let { bubbleView.title = it }

        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pickerItem.textColor
            textSize = pickerItem.textSize
            typeface = pickerItem.typeface
        }

        bubbleView.textPaint = paint
    }

    private fun drawIcon(canvas: Canvas, withText: Boolean) {
        pickerItem.icon?.let {
            val width = 105
            val height = 105

            val left = (bitmapSize / 2 - width / 2).toInt()
            val right = (bitmapSize / 2 + width / 2).toInt()

            if (withText.not()) {
                it.bounds = Rect(left, (bitmapSize / 2 - height / 2).toInt(), right, (bitmapSize / 2 + height / 2).toInt())
            } else if (pickerItem.iconOnTop) {
                it.bounds = Rect(left, (bitmapSize / 2 - height + 15).toInt(), right, (bitmapSize / 2).toInt() + 15)
            } else {
                it.bounds = Rect(left, (bitmapSize / 2).toInt(), right, (bitmapSize / 2 + height).toInt())
            }

            it.draw(canvas)
        }
    }
}