package com.igalata.bubblepicker.physics

import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*

/**
 * Created by irinagalata on 1/26/17.
 */
class CircleBody(val world: World, var position: Vec2, val startingRadius: Float, var resizedRadius: Float, var density: Float) {

    var currentRadius: Float = startingRadius

    var isResizing = false
    var toBeResized = false
    var hasBeenResized = false

    val finished: Boolean
        get() = !toBeResized && !isResizing

    lateinit var physicalBody: Body

    var isVisible = true

    private val margin = 0.01f
    private val damping = 25f
    private val shape: CircleShape
        get() = CircleShape().apply {
            m_radius = currentRadius + margin
            m_p.setZero()
        }

    private val fixture: FixtureDef
        get() = FixtureDef().apply {
            this.shape = this@CircleBody.shape
            this.density = this@CircleBody.density
        }

    private val bodyDef: BodyDef
        get() = BodyDef().apply {
            type = BodyType.DYNAMIC
            this.position = this@CircleBody.position
        }

    init {
        while (true) {
            if (world.isLocked.not()) {
                initializeBody()
                break
            }
        }
    }

    private fun initializeBody() {
        physicalBody = world.createBody(bodyDef).apply {
            createFixture(fixture)
            linearDamping = damping
        }
    }

    fun resize(step: Float) {

        isResizing = true

        if (resizedRadius > currentRadius) currentRadius += step else currentRadius -= step
        reset()

        if (Math.abs(currentRadius - resizedRadius) < step) {
            hasBeenResized = true
            clear()
        }
    }

    private fun reset() {
        physicalBody.fixtureList?.shape?.m_radius = currentRadius + margin
    }

    fun defineState() {
        toBeResized = !hasBeenResized
    }

    private fun clear() {
        toBeResized = false
        isResizing = false
    }

}