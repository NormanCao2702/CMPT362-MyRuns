package com.example.tranquangngoc_cao_myruns2.automatic

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.example.tranquangngoc_cao_myruns2.automatic.Globals.ACCELEROMETER_BLOCK_CAPACITY
import kotlin.text.Typography.registered

class SensorService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Buffers for accelerometer data
    private val accBuffer = ArrayList<Double>()
    private val featuresBuffer = Array<Any?>(ACCELEROMETER_BLOCK_CAPACITY * 3) { null }

    // Service binding
    private val binder = SensorBinder()
    private var msgHandler: Handler? = null

    companion object {
        const val MSG_ACTIVITY_TYPE = 2
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("SensorService", "Service Created")
        // Initialize sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Register listener
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d("SensorService", "Sensor Registered: $registered")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Add accelerometer data to buffer
                accBuffer.add(it.values[0].toDouble()) // X
                accBuffer.add(it.values[1].toDouble()) // Y
                accBuffer.add(it.values[2].toDouble()) // Z

                // Process when we have enough data
                if (accBuffer.size >= ACCELEROMETER_BLOCK_CAPACITY * 3) {
                    Log.d("SensorService", "Acc Data: X=${it.values[0]}, Y=${it.values[1]}, Z=${it.values[2]}")
                    processDataBuffer()
                }
            }
        }
    }

    private fun processDataBuffer() {
        try {
            Log.d("SensorService", "-------- Start Processing --------")
            Log.d("SensorService", "Raw Buffer Size: ${accBuffer.size}")
            // Print first few raw accelerometer values
            Log.d("SensorService", "Raw Acc Data Sample: X=${accBuffer[0]}, Y=${accBuffer[1]}, Z=${accBuffer[2]}")
            // Prepare FFT input
            val fft = FFT(ACCELEROMETER_BLOCK_CAPACITY)

            // Lists to store features before normalization
            val features = mutableListOf<Double>()

            // Process each axis
            var idx = 0
            for (i in 0 until 3) {
                val re = DoubleArray(ACCELEROMETER_BLOCK_CAPACITY)
                val im = DoubleArray(ACCELEROMETER_BLOCK_CAPACITY)

                // Fill arrays
                for (j in 0 until ACCELEROMETER_BLOCK_CAPACITY) {
                    re[j] = accBuffer[i + j * 3]
                    im[j] = 0.0
                }

                Log.d("SensorService", "Axis $i - First few values before FFT: ${re.take(5)}")

                // Apply FFT
                fft.fft(re, im)

                // Store FFT results
//                var axisFeatures = mutableListOf<Double>()
//                for (j in 0 until ACCELEROMETER_BLOCK_CAPACITY) {
//                    val feature = Math.sqrt(re[j] * re[j] + im[j] * im[j])
//                    featuresBuffer[idx++] = feature
//                    axisFeatures.add(feature)
//                }
                // Calculate and store magnitudes
                for (j in 0 until ACCELEROMETER_BLOCK_CAPACITY) {
                    val magnitude = Math.sqrt(re[j] * re[j] + im[j] * im[j])
                    features.add(magnitude)
                }
//                Log.d("SensorService", "Axis $i - First few FFT features: ${axisFeatures.take(5)}")
            }

            // Normalize features
            val maxFeature = features.maxOrNull() ?: 1.0
            for (feature in features) {
                // Normalize to range 0-20 to match classifier expectations
                featuresBuffer[idx++] = (feature / maxFeature) * 20.0
            }

            Log.d("SensorService", "Classifier Key Values:")
            Log.d("SensorService", "i[0] = ${featuresBuffer[0]}, Threshold = 13.390311")
            Log.d("SensorService", "i[64] = ${featuresBuffer[64]}, Threshold = 14.534508")

            // Classify activity
            val activityType = WekaClassifier.classify(featuresBuffer)
            Log.d("SensorService", "Classified Activity: $activityType")
            Log.d("SensorService", "-------- End Processing --------")

            // Send result to activity
            sendActivityUpdate(activityType)

            // Clear buffer
            accBuffer.clear()

        } catch (e: Exception) {
            Log.e("SensorService", "Error processing sensor data", e)
        }
    }

    private fun sendActivityUpdate(activityType: Double) {
        Log.d("SensorService", "Sending activity update: $activityType")
        msgHandler?.let { handler ->
            val message = handler.obtainMessage().apply {
                what = MSG_ACTIVITY_TYPE
                obj = when (activityType) {
                    Globals.ACTIVITY_ID_STANDING.toDouble() -> Globals.CLASS_LABEL_STANDING
                    Globals.ACTIVITY_ID_WALKING.toDouble() -> Globals.CLASS_LABEL_WALKING
                    Globals.ACTIVITY_ID_RUNNING.toDouble() -> Globals.CLASS_LABEL_RUNNING
                    else -> Globals.CLASS_LABEL_OTHER
                }
            }
            handler.sendMessage(message)
        }
    }

    inner class SensorBinder : Binder() {
        fun setHandler(handler: Handler) {
            this@SensorService.msgHandler = handler
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        msgHandler = null
    }
}