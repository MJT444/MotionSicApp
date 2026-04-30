package com.example.motsic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class BreatheView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class Phase(val seconds: Long, val label: String) {
        INHALE(4, "Inhale"),
        HOLD(7, "Hold"),
        EXHALE(8, "Exhale");

        fun next(): Phase = when (this) {
            INHALE -> HOLD
            HOLD -> EXHALE
            EXHALE -> INHALE
        }
    }

    private val circlePaint = Paint().apply {
        color = Color.parseColor("#4FC3F7"); isAntiAlias = true
    }
    private val ringPaint = Paint().apply {
        color = Color.parseColor("#80FFFFFF"); style = Paint.Style.STROKE; strokeWidth = 4f; isAntiAlias = true
    }
    private val labelPaint = Paint().apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; textSize = 64f; isAntiAlias = true
    }
    private val countPaint = Paint().apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; textSize = 96f; isAntiAlias = true
    }

    private var phase = Phase.INHALE
    private var progress = 0f
    private var animator: ValueAnimator? = null
    private var running = false

    fun start() {
        if (running) return
        running = true
        phase = Phase.INHALE
        runPhase()
    }

    fun stop() {
        running = false
        animator?.cancel()
        animator = null
        progress = 0f
        phase = Phase.INHALE
        invalidate()
    }

    private fun runPhase() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = phase.seconds * 1000L
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: Animator) {
                    if (running) {
                        phase = phase.next()
                        progress = 0f
                        runPhase()
                    }
                }
            })
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val maxR = min(width, height) / 2.6f
        val minR = maxR * 0.35f
        val r = when (phase) {
            Phase.INHALE -> minR + (maxR - minR) * progress
            Phase.HOLD -> maxR
            Phase.EXHALE -> maxR - (maxR - minR) * progress
        }
        canvas.drawCircle(cx, cy, maxR, ringPaint)
        canvas.drawCircle(cx, cy, r, circlePaint)
        canvas.drawText(phase.label, cx, cy - 10f, labelPaint)
        if (running) {
            val secondsLeft = phase.seconds - (progress * phase.seconds).toLong()
            canvas.drawText(secondsLeft.coerceAtLeast(1L).toString(), cx, cy + 80f, countPaint)
        }
    }
}
