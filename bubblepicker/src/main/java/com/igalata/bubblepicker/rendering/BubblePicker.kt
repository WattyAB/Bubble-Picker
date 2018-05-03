package com.igalata.bubblepicker.rendering

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.igalata.bubblepicker.BubblePickerListener
import com.igalata.bubblepicker.R
import com.igalata.bubblepicker.adapter.BubblePickerAdapter
import com.igalata.bubblepicker.model.Color
import com.igalata.bubblepicker.model.PickerItem

/**
 * Created by irinagalata on 1/19/17.
 */
class BubblePicker : GLSurfaceView {

    @ColorInt var background: Int = 0
        set(value) {
            field = value
            renderer.backgroundColor = Color(value)
        }

    var items: ArrayList<PickerItem>? = null
        get() {
            return renderer.items
        }
    var adapter: BubblePickerAdapter? = null
        set(value) {
            field = value
            if (value != null) {
                renderer.items = ArrayList((0..value.totalCount - 1)
                        .map { value.getItem(it) }.toList())
            }
        }
    var maxSelectedCount: Int? = null
        set(value) {
            renderer.maxSelectedCount = value
        }
    var listener: BubblePickerListener? = null
        set(value) {
            renderer.listener = value
        }
    val selectedItems: List<PickerItem?>
        get() = renderer.selectedItems

    var centerImmediately = false
        set(value) {
            field = value
            renderer.centerImmediately = value
        }

    private val renderer = PickerRenderer(this)
    private var startX = 0f
    private var startY = 0f
    private var previousX = 0f
    private var previousY = 0f

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setZOrderOnTop(false)
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        attrs?.let { retrieveAttrubutes(attrs) }
    }

    fun addItem(item: PickerItem) {
        renderer.addItem(item)
    }

    fun removeItem(item: PickerItem) {
        renderer.removeItem(item)
    }

    fun resizeItem(item: PickerItem, newSize: Float) {
        renderer.resizeItem(item, newSize)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        renderer.clear()
        super.surfaceDestroyed(holder)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                previousX = event.x
                previousY = event.y
            }
            MotionEvent.ACTION_UP -> {
                if (isClick(event)) renderer.onClick(event.x, event.y)
                renderer.release()
            }
            MotionEvent.ACTION_MOVE -> {
                if (isSwipe(event)) {
                    renderer.swipe(previousX - event.x, previousY - event.y)
                    previousX = event.x
                    previousY = event.y
                } else {
                    release()
                }
            }
            else -> release()
        }

        return true
    }

    private fun release() = postDelayed({ renderer.release() }, 0)

    private fun isClick(event: MotionEvent) = Math.abs(event.x - startX) < 20 && Math.abs(event.y - startY) < 20

    private fun isSwipe(event: MotionEvent) = Math.abs(event.x - previousX) > 20 && Math.abs(event.y - previousY) > 20

    private fun retrieveAttrubutes(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BubblePicker)

        if (array.hasValue(R.styleable.BubblePicker_maxSelectedCount)) {
            maxSelectedCount = array.getInt(R.styleable.BubblePicker_maxSelectedCount, -1)
        }

        if (array.hasValue(R.styleable.BubblePicker_backgroundColor)) {
            background = array.getColor(R.styleable.BubblePicker_backgroundColor, -1)
        }

        array.recycle()
    }

}
