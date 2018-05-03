package com.igalata.bubblepicker.rendering

import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.View
import com.igalata.bubblepicker.*
import com.igalata.bubblepicker.model.Color
import com.igalata.bubblepicker.model.PickerItem
import com.igalata.bubblepicker.physics.Engine
import com.igalata.bubblepicker.rendering.BubbleShader.A_POSITION
import com.igalata.bubblepicker.rendering.BubbleShader.A_UV
import com.igalata.bubblepicker.rendering.BubbleShader.U_BACKGROUND
import com.igalata.bubblepicker.rendering.BubbleShader.fragmentShader
import com.igalata.bubblepicker.rendering.BubbleShader.vertexShader
import org.jbox2d.common.Vec2
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by irinagalata on 1/19/17.
 */
class PickerRenderer(val glView: View) : GLSurfaceView.Renderer {

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

    private var programId = 0
    private var verticesBuffer: FloatBuffer? = null
    private var uvBuffer: FloatBuffer? = null
    private var vertices: FloatArray? = null
    private var textureVertices: FloatArray? = null
    private var textureIds: IntArray? = null

    private var hasItemsToAdd = false
    private var hasItemsToRemove = false
    private var hasItemsToResize = false

    private val newItems = mutableListOf<PickerItem>()
    private val removedItems = mutableListOf<PickerItem>()
    private val resizedItems = hashMapOf<PickerItem, Float>()

    private val scaleX: Float
        get() = if (glView.width < glView.height) glView.height.toFloat() / glView.width.toFloat() else 1f
    private val scaleY: Float
        get() = if (glView.width < glView.height) 1f else glView.width.toFloat() / glView.height.toFloat()
    private val circles = ArrayList<Item>()

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

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(
            backgroundColor?.red ?: 1f, backgroundColor?.green ?: 1f,
            backgroundColor?.blue ?: 1f, backgroundColor?.alpha ?: 1f
        )
        enableTransparency()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        initialize()
    }

    override fun onDrawFrame(gl: GL10?) {
        synchronized(this) {
            if (hasItemsToAdd) {
                Log.d("PickerRenderer", "has new items: ${newItems.count()}")
                newItems.forEach {
                    val newBody = Engine.build(1, scaleX, scaleY).last()
                    circles.add(Item(it, newBody))
                    items.add(it)
                }
                resizeArrays()
                newItems.forEach { pickerItem ->
                    val circle = circles.first { it.pickerItem == pickerItem }
                    initializeItem(circle, circles.indexOf(circle))
                }
                updateBuffers()
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
                updateBuffers()
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
        drawFrame()
    }

    private fun resizeArrays() {
        textureIds = textureIds?.copyOf(circles.size * 2)

        vertices = vertices?.copyOf(circles.size * 8)
        textureVertices = textureVertices?.copyOf(circles.size * 8)
    }

    private fun updateBuffers() {
        verticesBuffer = vertices?.toFloatBuffer()
        uvBuffer = textureVertices?.toFloatBuffer()
    }

    private fun initialize() {
        clear()
        Engine.centerImmediately = centerImmediately
        Engine.createBorders(scaleX, scaleY)
        Engine.build(items.size, scaleX, scaleY).forEachIndexed { index, body ->
            circles.add(Item(items[index], body))
        }
        items.forEach { if (it.isSelected) Engine.resize(circles.first { circle -> circle.pickerItem == it }, 50F) }
        if (textureIds == null) textureIds = IntArray(circles.size * 2)
        initializeArrays()
    }

    private fun initializeArrays() {
        vertices = FloatArray(circles.size * 8)
        textureVertices = FloatArray(circles.size * 8)
        circles.forEachIndexed { i, item -> initializeItem(item, i) }
        verticesBuffer = vertices?.toFloatBuffer()
        uvBuffer = textureVertices?.toFloatBuffer()
    }

    private fun initializeItem(item: Item, index: Int) {
        initializeVertices(item, index)
        textureVertices?.passTextureVertices(index)
        item.bindTextures(textureIds ?: IntArray(0), index)
    }

    private fun calculateVertices() {
        circles.forEachIndexed { i, item -> initializeVertices(item, i) }
        vertices?.forEachIndexed { i, float -> verticesBuffer?.put(i, float) }
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

    private fun drawFrame() {
        glClear(GL_COLOR_BUFFER_BIT)
        glUniform4f(glGetUniformLocation(programId, U_BACKGROUND), 1f, 1f, 1f, 0f)
        verticesBuffer?.passToShader(programId, A_POSITION)
        uvBuffer?.passToShader(programId, A_UV)
        synchronized(this) {
            circles.forEachIndexed { i, circle -> circle.drawItself(programId, i, scaleX, scaleY) }
        }
    }

    private fun enableTransparency() {
        glEnable(GLES20.GL_BLEND)
        glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        attachShaders()
    }

    private fun attachShaders() {
        programId = createProgram(
            createShader(GL_VERTEX_SHADER, vertexShader),
            createShader(GL_FRAGMENT_SHADER, fragmentShader)
        )
        glUseProgram(programId)
    }

    fun createProgram(vertexShader: Int, fragmentShader: Int) = glCreateProgram().apply {
        glAttachShader(this, vertexShader)
        glAttachShader(this, fragmentShader)
        glLinkProgram(this)
    }

    fun createShader(type: Int, shader: String) = GLES20.glCreateShader(type).apply {
        glShaderSource(this, shader)
        glCompileShader(this)
    }

    fun swipe(x: Float, y: Float) = Engine.swipe(
        x.convertValue(glView.width, scaleX),
        y.convertValue(glView.height, scaleY)
    )

    fun release() = Engine.release()

    private fun getItem(position: Vec2) = position.let {
        val x = it.x.convertPoint(glView.width, scaleX)
        val y = it.y.convertPoint(glView.height, scaleY)
        circles.find { Math.sqrt(((x - it.x).sqr() + (y - it.y).sqr()).toDouble()) <= it.radius }
    }

    fun onClick(x: Float, y: Float) = getItem(Vec2(x, glView.height - y))?.apply {
        listener?.let {
            it.onBubbleSelected(pickerItem)
        }
    }

//    fun resize(x: Float, y: Float) = getItem(Vec2(x, glView.height - y))?.apply {
//        if (Engine.resize(this, bubbleSize)) {
//            listener?.let {
//                if (circleBody.hasBeenResized) it.onBubbleDeselected(pickerItem) else it.onBubbleSelected(pickerItem)
//            }
//        }
//    }


    fun clear() {
        synchronized(this) {
            circles.forEach { Engine.destroyBody(it.circleBody) }
            circles.clear()
            items.clear()
            newItems.clear()
            removedItems.clear()
            resizedItems.clear()
            verticesBuffer = null
            uvBuffer = null
            vertices = null
            textureVertices = null
            textureIds = null
            Engine.clear()
        }
    }
}