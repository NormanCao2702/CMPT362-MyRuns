package com.example.tranquangngoc_cao_myruns2.googleMap

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tranquangngoc_cao_myruns2.automatic.SensorService
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapViewModel : ViewModel(), ServiceConnection {
    // Handler for receiving messages from service
    private var messageHandler: MessageHandler = MessageHandler(Looper.getMainLooper())

    // LiveData for UI updates
    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> = _distance

    private val _duration = MutableLiveData<Double>()
    val duration: LiveData<Double> = _duration

    private val _currentSpeed = MutableLiveData<Double>()
    val currentSpeed: LiveData<Double> = _currentSpeed

    private val _averageSpeed = MutableLiveData<Double>()
    val averageSpeed: LiveData<Double> = _averageSpeed

    private val _calories = MutableLiveData<Double>()
    val calories: LiveData<Double> = _calories

    private val _climb = MutableLiveData<Double>()
    val climb: LiveData<Double> = _climb

    private val _locationPoints = MutableLiveData<List<LatLng>>()
    val locationPoints: LiveData<List<LatLng>> = _locationPoints

    private val _startTime = MutableLiveData<Long>()
    val startTime: LiveData<Long> = _startTime

    // Service connection state
    private val _isServiceBound = MutableLiveData<Boolean>()
    val isServiceBound: LiveData<Boolean> = _isServiceBound

    // Add new LiveData for activity type
    private val _activityType = MutableLiveData<String>()
    val activityType: LiveData<String> = _activityType

    // Keep track of both services
    private var isSensorServiceBound = false
    private var sensorBinder: SensorService.SensorBinder? = null

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        when (service) {
            is TrackingService.MyBinder -> {
                service.setmsgHandler(messageHandler)
                _isServiceBound.value = true
            }
            is SensorService.SensorBinder -> {
                sensorBinder = service
                service.setHandler(messageHandler)
                isSensorServiceBound = true
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        when (name?.className) {
            SensorService::class.java.name -> {
                isSensorServiceBound = false
                sensorBinder = null
            }
            else -> {
                _isServiceBound.value = false
            }
        }
    }

    inner class MessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                TrackingService.MSG_INT_VALUE -> {
                    // Existing tracking service handling remains the same
                    val bundle = msg.data

                    _startTime.value = bundle.getLong("START_TIME")
                    _distance.value = bundle.getDouble("DISTANCE")
                    _currentSpeed.value = bundle.getDouble("CURRENT_SPEED")
                    _averageSpeed.value = bundle.getDouble("AVERAGE_SPEED")
                    _calories.value = bundle.getDouble("CALORIES")
                    _climb.value = bundle.getDouble("CLIMB")
                    _duration.value = bundle.getDouble("DURATION")

                    bundle.getString("LOCATION")?.let { locationJson ->
                        try {
                            val type = object : TypeToken<List<LatLng>>() {}.type
                            val points = Gson().fromJson<List<LatLng>>(locationJson, type)
                            _locationPoints.value = points
                        } catch (e: Exception) {
                            Log.e("MapViewModel", "Error parsing location data", e)
                        }
                    }
                }
                SensorService.MSG_ACTIVITY_TYPE -> {
                    // Handle activity type updates from sensor service
                    msg.obj?.let { activityType ->
                        _activityType.value = activityType.toString()
                    }
                }
            }
        }
    }

    // Clean up
    override fun onCleared() {
        super.onCleared()
        _isServiceBound.value = false
        isSensorServiceBound = false
    }
}