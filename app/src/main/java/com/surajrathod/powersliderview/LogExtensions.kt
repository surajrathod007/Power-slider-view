package com.surajrathod.powersliderview

import android.util.Log


fun logE(tag: String, message: String) {

    Log.e(tag, message)

}

fun logD(tag: String, message: String) {

    Log.d(tag, message)

}

fun logI(tag: String, message: String) {

    Log.i(tag, message)
}

fun main(){
    val points = (218..1000).toList()

    // Find the index of the middle point
    val middleIndex = points.size / 2

    // Extract the middle 4 points
    val middlePoints = points.subList(middleIndex - (295/2), middleIndex + (295/2))

    // Print the middle 4 points
    //println("Middle 4 points: ${middlePoints.first()}")
    val expectedValue = mapValueToAlpha(1400,1555,1555)
    println("Expected alpha : $expectedValue")
}

private fun mapValueToAlpha(lowerBound: Int, upperBound: Int, value: Int): Int {
    val minAlpha = 0
    val maxAlpha = 255

    return when (value) {
        lowerBound -> minAlpha
        upperBound -> minAlpha
        (lowerBound + upperBound) / 2 -> maxAlpha
        else -> {
            val midValue = (lowerBound + upperBound) / 2
            when {
                value < midValue -> mapValueForBottomArrow(value, lowerBound, midValue, minAlpha, maxAlpha)
                else -> mapValueForBottomArrow(value, midValue, upperBound, maxAlpha, minAlpha)
            }
        }
    }
}

private fun mapValueForBottomArrow(value: Int, fromLow: Int, fromHigh: Int, toLow: Int, toHigh: Int): Int {
    return (toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow)).coerceIn(toLow, toHigh)
}