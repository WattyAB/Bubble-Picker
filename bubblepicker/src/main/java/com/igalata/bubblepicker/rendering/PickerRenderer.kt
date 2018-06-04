package com.igalata.bubblepicker.rendering

import android.util.Log
import android.view.View
import com.igalata.bubblepicker.*
import com.igalata.bubblepicker.model.Color
import com.igalata.bubblepicker.model.PickerItem
import com.igalata.bubblepicker.physics.Engine
import org.jbox2d.common.Vec2
import java.util.*
import javax.microedition.khronos.opengles.GL10

/**
 * Created by irinagalata on 1/19/17.
 */
class PickerRenderer(private val bubblePicker: BubblePicker) {

    var backgroundColor: Color? = null
    var maxSelectedCount: Int? = null
        set(value) {
            Engine.maxSelectedCount = value
        }
    var listener: BubblePickerListener? = null
    var items = ArrayList<PickerItem>()
    val selectedItems: List<PickerItem?>
        get() = Engine.selectedBodies.map { circles.firstOrNull { circle -> circle.circleBody == it }?.pickerItem }
    var centerImmediately = false
        set(value) {
            field = value
            Engine.centerImmediately = value
        }

    private var vertices: FloatArray? = null

    private var hasItemsToAdd = false
    private var hasItemsToRemove = false
    private var hasItemsToResize = false

    private val newItems = mutableListOf<PickerItem>()
    private val removedItems = mutableListOf<PickerItem>()
    private val resizedItems = hashMapOf<PickerItem, Float>()

    private val scaleX: Float
        get() = if (bubblePicker.width < bubblePicker.height) bubblePicker.height.toFloat() / bubblePicker.width.toFloat() else 1f
    private val scaleY: Float
        get() = if (bubblePicker.width < bubblePicker.height) 1f else bubblePicker.width.toFloat() / bubblePicker.height.toFloat()
    private val circles = ArrayList<Item>()

    init {
        initialize()
    }

    fun addItem(pickerItem: PickerItem) {
        synchronized(this) {
            newItems.add(pickerItem)
            hasItemsToAdd = true
        }
    }

    fun removeItem(pickerItem: PickerItem) {
        synchronized(this) {
            removedItems.add(pickerItem)
            hasItemsToRemove = true
        }
    }

    fun resizeItem(pickerItem: PickerItem, newSize: Float) {
        synchronized(this) {
            resizedItems[pickerItem] = newSize
            hasItemsToResize = true
        }
    }

    fun onDrawFrame() {
        synchronized(this) {
            if (hasItemsToAdd) {
                Log.d("PickerRenderer", "has new items: ${newItems.count()}")
                newItems.forEach {
                    val newBody = Engine.build(1, scaleX, scaleY).last()
                    circles.add(Item(it, newBody, bubblePicker.bubbleViewFactory.create()))
                    items.add(it)
                }
                resizeArrays()
                newItems.forEach { pickerItem ->
                    val circle = circles.first { it.pickerItem == pickerItem }
                    initializeItem(circle, circles.indexOf(circle))
                }
                newItems.clear()
                hasItemsToAdd = false
                Log.d("PickerRenderer", "circles.count: ${circles.size}, items.size: ${items.size}")
            }
        }
        synchronized(this) {
            if (hasItemsToRemove) {
                Log.d("PickerRenderer", "has removed items: ${removedItems.count()}")
                removedItems.forEach { pickerItem ->
                    circles.firstOrNull { pickerItem == it.pickerItem }?.let {

                        Engine.destroyBody(it.circleBody)
                        circles.remove(it)
                    }

                    items.remove(pickerItem)
                }
                removedItems.clear()
                resizeArrays()
                hasItemsToRemove = false
            }
        }
        synchronized(this) {
            if (hasItemsToResize) {
                //Log.d("PickerRenderer", "has hasBeenResized items: ${resizedItems.count()}")
                resizedItems.forEach { (item, finalSize) ->
                    circles.firstOrNull { it.pickerItem == item }?.let {
                        Engine.resize(it, finalSize)
                    }
                }
                hasItemsToResize = false
            }
        }
        calculateVertices()
        Engine.move(circles.map { it.circleBody })
    }

    private fun resizeArrays() {
        vertices = vertices?.copyOf(circles.size * 8)
    }

    private fun initialize() {
        clear()
        Engine.centerImmediately = centerImmediately
        Engine.createBorders(scaleX, scaleY)
        Engine.build(items.size, scaleX, scaleY).forEachIndexed { index, body ->
            circles.add(Item(items[index], body, bubblePicker.bubbleViewFactory.create()))
        }
        items.forEach { if (it.isSelected) Engine.resize(circles.first { circle -> circle.pickerItem == it }, 50F) }
        initializeArrays()
    }

    private fun initializeArrays() {
        vertices = FloatArray(circles.size * 8)
        circles.forEachIndexed { i, item -> initializeItem(item, i) }
    }

    private fun initializeItem(item: Item, index: Int) {
        initializeVertices(item, index)
    }

    private fun calculateVertices() {
        circles.forEachIndexed { i, item -> initializeVertices(item, i) }
        //vertices?.forEachIndexed { i, float -> verticesBuffer?.put(i, float) }
    }

    private fun initializeVertices(body: Item, index: Int) {
        val radius = body.radius
        val radiusX = radius * scaleX
        val radiusY = radius * scaleY

        body.initialPosition.apply {
            vertices?.put(
                8 * index, floatArrayOf(
                    x - radiusX, y + radiusY, x - radiusX, y - radiusY,
                    x + radiusX, y + radiusY, x + radiusX, y - radiusY
                )
            )
        }
    }

    fun swipe(x: Float, y: Float) = Engine.swipe(
        x.convertValue(bubblePicker.width, scaleX),
        y.convertValue(bubblePicker.height, scaleY)
    )

    fun release() = Engine.release()

    private fun getItem(position: Vec2) = position.let {
        val x = it.x.convertPoint(bubblePicker.width, scaleX)
        val y = it.y.convertPoint(bubblePicker.height, scaleY)
        circles.find { Math.sqrt(((x - it.x).sqr() + (y - it.y).sqr()).toDouble()) <= it.radius }
    }

    fun onClick(x: Float, y: Float) = getItem(Vec2(x, bubblePicker.height - y))?.apply {
        listener?.onBubbleSelected(pickerItem)
    }

    fun clear() {
        synchronized(this) {
            circles.forEach { Engine.destroyBody(it.circleBody) }
            circles.clear()
            items.clear()
            newItems.clear()
            removedItems.clear()
            resizedItems.clear()
            vertices = null
            Engine.clear()
        }
    }
}