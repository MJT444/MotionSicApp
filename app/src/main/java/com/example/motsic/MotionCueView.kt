package com.example.motsic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

class MotionCueView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val choreographer = Choreographer.getInstance()
    private var running = false
    private var lastFrameNanos = 0L

    private val dotFill = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val dotStroke = Paint().apply {
        color = Color.parseColor("#80000000")
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        isAntiAlias = true
    }

    private class Dot(
        var x: Float = 0f,
        var y: Float = 0f,
        var radius: Float = 4f,
        var ageMs: Long = 0L,
        var lifeMs: Long = 1L
    )

    private val dotCount = 80
    private val dots = ArrayList<Dot>(dotCount)
    private var driftX = 0f
    private var driftY = 0f

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            val dtMs = if (lastFrameNanos == 0L) 16L
            else ((frameTimeNanos - lastFrameNanos) / 1_000_000L).coerceAtMost(64L)
            lastFrameNanos = frameTimeNanos
            update(dtMs)
            invalidate()
            if (running) choreographer.postFrameCallback(this)
        }
    }

    fun start() {
        if (running) return
        running = true
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        lastFrameNanos = 0L
        choreographer.postFrameCallback(frameCallback)
    }

    fun stop() {
        if (!running) return
        running = false
        sensorManager.unregisterListener(this)
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]
        val scale = 80f
        val targetX = -ax * scale
        val targetY = (ay - az) * scale
        driftX = lerp(driftX, targetX, 0.18f)
        driftY = lerp(driftY, targetY, 0.18f)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        seedDots()
    }

    private fun seedDots() {
        dots.clear()
        repeat(dotCount) { dots.add(spawnDot(randomAge = true)) }
    }

    private fun spawnDot(randomAge: Boolean = false): Dot {
        val (x, y) = peripheralPoint()
        val life = Random.nextLong(1500L, 3200L)
        return Dot(
            x = x,
            y = y,
            radius = Random.nextDouble(2.5, 5.5).toFloat(),
            ageMs = if (randomAge) Random.nextLong(0L, life) else 0L,
            lifeMs = life
        )
    }

    private fun peripheralPoint(): Pair<Float, Float> {
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return 0f to 0f
        val cx = w / 2f
        val cy = h / 2f
        repeat(8) {
            val x = Random.nextInt(w).toFloat()
            val y = Random.nextInt(h).toFloat()
            val nx = (x - cx) / (w / 2f)
            val ny = (y - cy) / (h / 2f)
            if (max(abs(nx), abs(ny)) > 0.55f) return x to y
        }
        return 4f to 4f
    }

    private fun update(dtMs: Long) {
        val dt = dtMs / 1000f
        val w = width.toFloat()
        val h = height.toFloat()
        val margin = 12f
        for (d in dots) {
            d.ageMs += dtMs
            d.x += driftX * dt
            d.y += driftY * dt
            val expired = d.ageMs >= d.lifeMs ||
                d.x < -margin || d.x > w + margin ||
                d.y < -margin || d.y > h + margin
            if (expired) {
                val fresh = spawnDot()
                d.x = fresh.x
                d.y = fresh.y
                d.radius = fresh.radius
                d.ageMs = 0L
                d.lifeMs = fresh.lifeMs
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        for (d in dots) {
            val t = (d.ageMs.toFloat() / d.lifeMs.toFloat()).coerceIn(0f, 1f)
            val alpha = sin(t * Math.PI).toFloat().coerceIn(0f, 1f)
            dotFill.alpha = (alpha * 220f).toInt()
            dotStroke.alpha = (alpha * 140f).toInt()
            canvas.drawCircle(d.x, d.y, d.radius, dotFill)
            canvas.drawCircle(d.x, d.y, d.radius, dotStroke)
        }
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
}
