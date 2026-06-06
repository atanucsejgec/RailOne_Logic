package com.apk.railone.data

import android.content.Context
import android.util.Log
import org.json.JSONArray

class StationRepository(private val context: Context) {

    private var _stations: List<Station> = emptyList()
    val stations: List<Station> get() = _stations

    init {
        loadStations()
    }

    private fun loadStations() {
        try {
            val jsonString = context.assets
                .open("stations.json")
                .bufferedReader()
                .use { it.readText() }

            Log.d("StationRepo", "JSON loaded, length: ${jsonString.length}")

            val jsonArray = JSONArray(jsonString)

            Log.d("StationRepo", "Total entries in JSON: ${jsonArray.length()}")

            val stationList = mutableListOf<Station>()

            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)

                    // ✅ Your approach — clean helper function (good idea)
                    // Extended to also remove all non-numeric chars for lat/lon
                    fun cleanString(key: String): String {
                        return obj.getString(key)
                            .replace("\uFEFF", "") // remove BOM character
                            .trim()               // remove leading/trailing spaces
                    }

                    fun cleanCoordinate(key: String): String {
                        return cleanString(key)
                            .replace(Regex("[^0-9.\\-]"), "") // remove non-numeric chars
                    }

                    val latStr = cleanCoordinate("Latitude")
                    val lonStr = cleanCoordinate("Longitude")
                    val name   = cleanString("Station_Name")
                    val code   = cleanString("Station_Code")

                    // ✅ Your check — skip if name or code is empty (good idea)
                    if (name.isEmpty() || code.isEmpty()) {
                        Log.w("StationRepo", "[$i] Skipping — empty name or code")
                        continue
                    }

                    // ✅ Use toDoubleOrNull instead of toDouble
                    // toDouble() throws exception — toDoubleOrNull() returns null safely
                    val lat = latStr.toDoubleOrNull()
                    val lon = lonStr.toDoubleOrNull()

                    if (lat == null || lon == null) {
                        Log.w("StationRepo", "[$i] Skipping — invalid lat/lon: ($latStr, $lonStr)")
                        continue
                    }

                    stationList.add(
                        Station(
                            latitude = lat,
                            longitude = lon,
                            stationName = name,
                            stationCode = code
                        )
                    )

                } catch (e: Exception) {
                    // ✅ Your catch — good, keeps loading other stations
                    Log.e("StationRepo", "[$i] Error parsing station: ${e.message}")
                }
            }

            _stations = stationList.sortedBy { it.stationName }

            Log.d("StationRepo", "Stations loaded: ${_stations.size}")

        } catch (e: Exception) {
            Log.e("StationRepo", "Fatal error: ${e.message}", e)
            _stations = emptyList()
        }
    }

    fun searchStations(query: String): List<Station> {
        if (query.isBlank()) return _stations
        val lowerQuery = query.lowercase().trim()
        return _stations.filter { station ->
            station.stationName.lowercase().contains(lowerQuery) ||
                    station.stationCode.lowercase().contains(lowerQuery)
        }
    }
}