package com.example.tranquangngoc_cao_myruns2

object UnitConverter {
    fun milesToKilometers(miles: Double): Double {
        return miles * 1.60934
    }

    fun kilometersToMiles(kilometers: Double): Double {
        return kilometers / 1.60934
    }
}