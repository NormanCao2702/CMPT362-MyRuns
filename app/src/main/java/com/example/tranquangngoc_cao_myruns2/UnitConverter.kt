package com.example.tranquangngoc_cao_myruns2

import java.util.Locale

object UnitConverter {
    fun milesToKilometers(miles: Double): Double {
        return miles * 1.60934
    }

    fun kilometersToMiles(kilometers: Double): Double {
        return kilometers / 1.60934
    }

    fun formatDistance(distance: Double, useMetric: Boolean): String {
        val convertedDistance = if (useMetric) milesToKilometers(distance) else distance
        val unit = if (useMetric) "km" else "mi"
        return String.format(Locale.getDefault(), "%.2f %s", convertedDistance, unit)
    }
}