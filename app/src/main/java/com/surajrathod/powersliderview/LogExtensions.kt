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
    println("Middle 4 points: ${middlePoints.first()}")
}