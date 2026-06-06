package com.apk.railone.utils


import android.location.Location

object DistanceCalculator {

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] // in meters
    }

    fun metersToKilometers(meters: Float): Double {
        return (meters / 1000.0)
    }

    fun formatDistance(meters: Float): String {
        val km = metersToKilometers(meters)
        return String.format("%.2f km", km)
    }

    fun formatMeters(meters: Float): String {
        return String.format("%.0f meters", meters)
    }
}