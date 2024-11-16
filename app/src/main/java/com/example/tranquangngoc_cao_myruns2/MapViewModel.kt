package com.example.tranquangngoc_cao_myruns2

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

    // Service connection state
    private val _isServiceBound = MutableLiveData<Boolean>()
    val isServiceBound: LiveData<Boolean> = _isServiceBound

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder = service as TrackingService.MyBinder
        binder.setmsgHandler(messageHandler)
        _isServiceBound.value = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _isServiceBound.value = false
    }

    inner class MessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == TrackingService.MSG_INT_VALUE) {
                val bundle = msg.data

                // Update LiveData with received values
                _distance.value = bundle.getDouble("DISTANCE")
                _currentSpeed.value = bundle.getDouble("CURRENT_SPEED")
                _averageSpeed.value = bundle.getDouble("AVERAGE_SPEED")
                _calories.value = bundle.getDouble("CALORIES")
                _climb.value = bundle.getDouble("CLIMB")
                _duration.value = bundle.getDouble("DURATION")

                // Handle location points
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
        }
    }

    // Helper method to check if we have necessary data
    fun hasValidData(): Boolean {
        return !_locationPoints.value.isNullOrEmpty()
    }

    // Clean up
    override fun onCleared() {
        super.onCleared()
        _isServiceBound.value = false
    }
}

//class MapViewModel : ViewModel() {
//    // Location and map related data
//    private val _locationPoints = MutableLiveData<List<LatLng>>()
//    val locationPoints: LiveData<List<LatLng>> = _locationPoints
//
//    // Exercise stats
//    private val _exerciseStats = MutableLiveData<ExerciseStats>()
//    val exerciseStats: LiveData<ExerciseStats> = _exerciseStats
//
//    // Service connection state
//    private val _isServiceBound = MutableLiveData<Boolean>()
//    val isServiceBound: LiveData<Boolean> = _isServiceBound
//
//    // Service reference
//    private var trackingService: TrackingService? = null
//
//    // Methods to update from service
//    fun updateFromService(service: TrackingService) {
//        trackingService = service
//        observeServiceData()
//    }
//
//    data class ExerciseStats(
//        val distance: Double,
//        val currentSpeed: Double,
//        val avgSpeed: Double,
//        val calories: Int,
//        val isMetric: Boolean
//    )
//
//    private fun observeServiceData() {
//        trackingService?.let { service ->
//            service.locationUpdates.observeForever { points ->
//                _locationPoints.postValue(points)
//            }
//
//            service.exerciseStats.observeForever { stats ->
//                _exerciseStats.postValue(
//                    ExerciseStats(
//                        distance = stats.distance,
//                        currentSpeed = stats.currentSpeed,
//                        avgSpeed = stats.avgSpeed,
//                        calories = stats.calories,
//                        isMetric = false  // Will be converted in UI layer
//                    )
//                )
//            }
//        }
//    }
//
//    fun setServiceBound(bound: Boolean) {
//        _isServiceBound.value = bound
//    }
//
//    // Methods to get final data for saving
//    fun getFinalData(): ExerciseData? {
//        return trackingService?.let { service ->
//            ExerciseData(
//                points = service.getAllPoints(),
//                stats = service.getFinalStats(),
//                startTime = service.getStartTime()
//            )
//        }
//    }
//
//    data class ExerciseData(
//        val points: List<LatLng>,
//        val stats: TrackingService.ExerciseStats?,
//        val startTime: Long
//    )
//
//    override fun onCleared() {
//        super.onCleared()
//        // Clean up observers
//        trackingService?.let { service ->
//            service.locationUpdates.removeObserver { }
//            service.exerciseStats.removeObserver { }
//        }
//        trackingService = null
//    }
//}