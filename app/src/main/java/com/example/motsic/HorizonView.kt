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
import android.view.View

class HorizonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val skyPaint = Paint().apply { color = Color.parseColor("#4FC3F7"); style = Paint.Style.FILL }
    private val groundPaint = Paint().apply { color = Color.parseColor("#8D6E63"); style = Paint.Style.FILL }
    private val linePaint = Paint().apply {
        color = Color.WHITE; strokeWidth = 6f; style = Paint.Style.STROKE; isAntiAlias = true
    }
    private val markerPaint = Paint().apply {
        color = Color.parseColor("#FFEB3B"); strokeWidth = 8f; style = Paint.Style.STROKE; isAntiAlias = true
    }
    private val markerFill = Paint().apply {
        color = Color.parseColor("#FFEB3B"); style = Paint.Style.FILL; isAntiAlias = true
    }

    private var pitch = 0f
    private var roll = 0f

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
        val matrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(matrix, event.values)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(matrix, orientation)
        pitch = orientation[1]
        roll = orientation[2]
        invalidate()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f

        canvas.save()
        val pitchPx = (pitch * h / Math.PI.toFloat())
        canvas.rotate(Math.toDegrees(roll.toDouble()).toFloat(), cx, cy)

        val horizonY = cy + pitchPx
        canvas.drawRect(-w, -h, 2f * w, horizonY, skyPaint)
        canvas.drawRect(-w, horizonY, 2f * w, 2f * h, groundPaint)
        canvas.drawLine(-w, horizonY, 2f * w, horizonY, linePaint)
        canvas.restore()

        canvas.drawCircle(cx, cy, 10f, markerFill)
        canvas.drawLine(cx - 80f, cy, cx - 24f, cy, markerPaint)
        canvas.drawLine(cx + 24f, cy, cx + 80f, cy, markerPaint)
    }
}
