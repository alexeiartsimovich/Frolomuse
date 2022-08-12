package com.frolo.muse.views.spring

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory.*
import kotlin.reflect.KMutableProperty0


class SpringEdgeEffect(
        context: Context,
        private val getMax: () -> Int,
        private val target: KMutableProperty0<Float>,
        private val activeEdge: KMutableProperty0<SpringEdgeEffect?>,
        private val velocityMultiplier: Float,
        private val reverseAbsorb: Boolean) : EdgeEffect(context) {

    private val enablePhysics = true

    private val shiftProperty = KFloatProperty(target, "value")
    private val spring = SpringAnimation(this, KFloatPropertyCompat(target, "value"), 0f).apply {
        spring = SpringForce(0f).setStiffness(850f).setDampingRatio(0.5f)
    }
    private var distance = 0f

    override fun draw(canvas: Canvas) = false

    override fun onAbsorb(velocity: Int) {
        if (reverseAbsorb) {
            releaseSpring(-velocityMultiplier * velocity)
        } else {
            releaseSpring(velocityMultiplier * velocity)
        }
    }

    override fun onPull(deltaDistance: Float, displacement: Float) {
        activeEdge.set(this)
        distance += deltaDistance * (velocityMultiplier * 2)
        target.set(OverScroll.dampedScroll(distance * getMax(), getMax()).toFloat())
    }

    override fun onRelease() {
        distance = 0f
        releaseSpring(0f)
    }

    private fun releaseSpring(velocity: Float) {
        if (enablePhysics) {
            spring.setStartVelocity(velocity)
            spring.setStartValue(target.get())
            spring.start()
        } else {
            ObjectAnimator.ofFloat(this, shiftProperty, 0f)
                    .setDuration(100)
                    .start()
        }
    }

    class Manager(val view: View) {

        var shiftX = 0f
            set(value) {
                if (field != value) {
                    field = value
                    view.invalidate()
                }
            }
        var shiftY = 0f
            set(value) {
                if (field != value) {
                    field = value
                    view.invalidate()
                }
            }

        var activeEdgeX: SpringEdgeEffect? = null
            set(value) {
                if (field != value) {
                    field?.run { value?.distance = distance }
                }
                field = value
            }
        var activeEdgeY: SpringEdgeEffect? = null
            set(value) {
                if (field != value) {
                    field?.run { value?.distance = distance }
                }
                field = value
            }

        inline fun withSpring(canvas: Canvas, allow: Boolean = true, body: () -> Boolean): Boolean {
            val result: Boolean
            if ((shiftX == 0f && shiftY == 0f) || !allow) {
                result = body()
            } else {
                canvas.translate(shiftX, shiftY)
                result = body()
                canvas.translate(-shiftX, -shiftY)
            }
            return result
        }

        inline fun withSpringNegative(canvas: Canvas, allow: Boolean = true, body: () -> Boolean): Boolean {
            val result: Boolean
            if ((shiftX == 0f && shiftY == 0f) || !allow) {
                result = body()
            } else {
                canvas.translate(-shiftX, -shiftY)
                result = body()
                canvas.translate(shiftX, shiftY)
            }
            return result
        }

        fun createFactory() = SpringEdgeEffectFactory()

        fun createEdgeEffect(
                direction: Int,
                reverseAbsorb: Boolean = false,
                velocityMultiplier: Float = 0.3f): EdgeEffect? {
            return when (direction) {
                DIRECTION_LEFT -> SpringEdgeEffect(view.context, view::getWidth, ::shiftX, ::activeEdgeX, velocityMultiplier, reverseAbsorb)
                DIRECTION_TOP -> SpringEdgeEffect(view.context, view::getHeight, ::shiftY, ::activeEdgeY, velocityMultiplier, reverseAbsorb)
                DIRECTION_RIGHT -> SpringEdgeEffect(view.context, view::getWidth, ::shiftX, ::activeEdgeX, -velocityMultiplier, reverseAbsorb)
                DIRECTION_BOTTOM -> SpringEdgeEffect(view.context, view::getWidth, ::shiftY, ::activeEdgeY, -velocityMultiplier, reverseAbsorb)
                else -> null
            }
        }

        inner class SpringEdgeEffectFactory : RecyclerView.EdgeEffectFactory() {

            override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
                return createEdgeEffect(direction = direction, velocityMultiplier = 0.3f)
                        ?: super.createEdgeEffect(recyclerView, direction)
            }
        }
    }
}