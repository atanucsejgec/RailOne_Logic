package com.apk.railone.utils

import com.apk.railone.data.Station

object FuzzySearch {

    /**
     * Calculates the Levenshtein distance between two strings using space-optimized (2 x 1D arrays) DP.
     * Complexity: O(n * m) time, O(min(n, m)) space.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        // Optimization: Ensure s2 is the shorter string to minimize space usage
        val str1 = if (s1.length >= s2.length) s1 else s2
        val str2 = if (s1.length >= s2.length) s2 else s1

        val s2Len = str2.length
        var prev = IntArray(s2Len + 1) { it }
        var curr = IntArray(s2Len + 1)

        for (i in 1..str1.length) {
            curr[0] = i
            for (j in 1..s2Len) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                curr[j] = minOf(
                    curr[j - 1] + 1,
                    prev[j] + 1,
                    prev[j - 1] + cost,
                )
            }
            // Swap references
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[s2Len]
    }

    /**
     * Calculates a relevance score for a station given a query.
     * Lower score means higher relevance (0 is exact match).
     * Returns 100+ if it's not a match.
     */
    fun calculateScore(query: String, station: Station): Int {
        if (query.isBlank()) return 0

        val q = query.lowercase().trim()
        val name = station.stationName.lowercase()
        val code = station.stationCode.lowercase()

        // 1. Precise Matches
        return when {
            code == q -> 0        // Exact Code
            name == q -> 1        // Exact Name
            code.startsWith(q) -> 2
            name.startsWith(q) -> 3
            name.contains(q) -> 4
            code.contains(q) -> 5
            else -> {
                // 2. Fuzzy Matching (Typo Tolerance)
                val words = name.split(" ", "-", "/")
                val bestWordDist = words.minOfOrNull { word ->
                    levenshteinDistance(q, word)
                } ?: 100

                val fullDist = levenshteinDistance(q, name)
                val dist = minOf(bestWordDist, fullDist)

                // Threshold: Allow 1 error for length 3-4, 2 errors for length 5+
                val threshold = when {
                    q.length <= 2 -> 0
                    q.length <= 4 -> 1
                    else -> 2
                }

                if (dist <= threshold) 6 + dist else 100
            }
        }
    }
}
