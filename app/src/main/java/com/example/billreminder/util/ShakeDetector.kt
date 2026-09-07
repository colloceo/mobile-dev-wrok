package com.example.billreminder.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Wraps the accelerometer to detect a "shake" gesture. Registers/unregisters
 * itself with the SensorManager so it only listens while the hosting
 * Activity is in the foreground.
 */
class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastUpdate = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    val isAvailable: Boolean get() = accelerometer != null

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val now = System.currentTimeMillis()
            if (now - lastUpdate < SAMPLE_INTERVAL_MS) return

            val deltaTime = now - lastUpdate
            lastUpdate = now

            val (x, y, z) = event.values
            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ
            lastX = x
            lastY = y
            lastZ = z

            val speed = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / deltaTime * 10000
            if (speed > SHAKE_THRESHOLD) {
                onShake()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }

    companion object {
        private const val SHAKE_THRESHOLD = 800
        private const val SAMPLE_INTERVAL_MS = 100
    }
}
