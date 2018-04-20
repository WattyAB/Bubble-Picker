package com.igalata.bubblepicker.physics

import com.igalata.bubblepicker.rendering.Item
import com.igalata.bubblepicker.sqr
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by irinagalata on 1/26/17.
 */
object Engine {
    val selectedBodies: List<CircleBody>
        get() = bodies.filter { it.hasBeenResized || it.toBeResized || it.isResizing }
    var maxSelectedCount: Int? = null
    var centerImmediately = false

    private var standardIncreasedGravity = interpolate(500f, 800f, 0.5f)
    private var increasedGravity = 55f

    private var defaultRadius = 0.17f

    private val world = World(Vec2(0f, 0f), false)
    private const val moveStep = 0.0001f
    private val bodies: ArrayList<CircleBody> = ArrayList()
    private var borders: ArrayList<Border> = ArrayList()
    private val resizeStep = 0.001f
    private var scaleY = 0f
    private var touch = false
    private var gravityCenter = Vec2(0f, 0f)
    private val toBeResized = HashMap<Item, Float>()
    private const val startX = 0.5f
    private var stepsCount = 0

    fun build(bodiesCount: Int, scaleX: Float, scaleY: Float): List<CircleBody> {
        val density = interpolate(0.8f, 0.2f, 0.5f)
        for (i in 0..bodiesCount - 1) {
            val x = if (Random().nextBoolean()) -startX else startX
            val y = if (Random().nextBoolean()) -0.5f / scaleY else 0.5f / scaleY
            bodies.add(CircleBody(world, Vec2(x, y), defaultRadius * scaleX, density))
        }

        return bodies
    }

    fun move() {
        toBeResized.forEach {
            it.key.circleBody.resize(getBubbleRadius(it.value), resizeStep)
        }
        world.step(if (centerImmediately) 0.035f else moveStep, 11, 11)
        bodies.forEach { move(it) }
        toBeResized.keys.removeAll(toBeResized.filterKeys { it.circleBody.finished }.map { it.key })

        stepsCount++
        if (stepsCount >= 10) {
            centerImmediately = false
        }
    }

    fun swipe(x: Float, y: Float) {
        if (Math.abs(gravityCenter.x) < 2) gravityCenter.x += -x
        if (Math.abs(gravityCenter.y) < 0.5f / scaleY) gravityCenter.y += y
        increasedGravity = standardIncreasedGravity * Math.abs(x * 13) * Math.abs(y * 13)
        touch = true
    }

    fun release() {
        gravityCenter.setZero()
        touch = false
    }

    fun clear() {
        borders.forEach { world.destroyBody(it.itemBody) }
        bodies.forEach { world.destroyBody(it.physicalBody) }
        borders.clear()
        bodies.clear()
    }

    fun resize(item: Item, finalSize: Float): Boolean {
        if (selectedBodies.size >= maxSelectedCount ?: bodies.size && !item.circleBody.hasBeenResized) return false

        if (item.circleBody.isResizing) return false

        item.circleBody.defineState()

        toBeResized[item] = finalSize

        return true
    }

    fun createBorders(scaleX: Float, scaleY: Float) {
        borders = arrayListOf(
                Border(world, Vec2(0f, 0.5f / scaleY), Border.HORIZONTAL),
                Border(world, Vec2(0f, -0.5f / scaleY), Border.HORIZONTAL),
                Border(world, Vec2(0.5f / scaleX, 0f), Border.VERTICAL),
                Border(world, Vec2(-0.5f / scaleX, 0f), Border.VERTICAL)
        )
    }

    private fun move(body: CircleBody) {
        body.physicalBody.apply {
            val direction = gravityCenter.sub(position)
            val distance = direction.length()
            val gravity = getGravity(body.currentRadius)
            if (distance > moveStep * 200) {
                applyForce(direction.mul(gravity / distance.sqr()), position)
            }
        }
    }

    private fun getGravity(radius: Float): Float {
        return if (touch) {
            increasedGravity
        } else {
            interpolate(20f, 80f, radius / 100f)
        }
    }

    private fun interpolate(start: Float, end: Float, f: Float) = start + f * (end - start)

    private fun getBubbleRadius(value: Float) = interpolate(0.1f, 0.25f, value / 100f)

}