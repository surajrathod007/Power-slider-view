package com.surajrathod.powersliderview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator

class SampleView : View {

    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }


    private val paint = Paint().apply {
        color = Color.BLUE // Set color to blue
        isAntiAlias = true // Enable anti-aliasing for smooth edges
        //style = Paint.Style.FILL // Fill the shape
//        strokeWidth = 2f
    }


    private var topYOffset = 0f
    private var animator: ValueAnimator? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            val width = width.toFloat()
            val height = height.toFloat()

            val rectangleHeight = 100f // Set the height of each rectangle

            // Calculate the y-coordinate for the top of the first rectangle
            val startY1 = topYOffset
            // Calculate the y-coordinate for the top of the second rectangle
            val startY2 = startY1 + rectangleHeight + 50f
            // Calculate the y-coordinate for the top of the third rectangle
            val startY3 = startY2 + rectangleHeight + 50f

            logE("SURAJALPHA","Value of startY3 $startY3 its alpha : ${mapValueToAlpha2(startY2.toInt())}")
            // Draw the first rectangle
            drawRect(0f, startY1, width, startY1 + rectangleHeight, paint.apply {
                alpha = mapValueToAlpha1(startY1.toInt())
            })
            // Draw the second rectangle
            drawRect(0f, startY2, width, startY2 + rectangleHeight, paint.apply {
                alpha = mapValueToAlpha2(startY2.toInt())
            })
//            // Draw the third rectangle
            drawRect(0f, startY3, width, startY3 + rectangleHeight, paint.apply {
                alpha = mapValueToAlpha3(startY3.toInt())
            })

            /*val spacing = 50f
            val rectHeight = 300f
            canvas.drawRect(0f, rectHeight, width, rectHeight * 2, paint)
            canvas.drawRect(0f, (rectHeight * 2)+spacing, width, (rectHeight * 3)+spacing, paint.apply {
                color = Color.RED
            })
            canvas.drawRect(0f, (rectHeight * 3)+spacing*2, width, (rectHeight * 4)+spacing*2, paint.apply {
                color = Color.YELLOW
            })*/
        }
    }

    fun startAnimation(){
        val isRunning = animator?.isRunning ?: false
        if (!isRunning) {
            animator = ValueAnimator.ofFloat(500f, 0f).apply {
                this.duration = 1000
                interpolator = DecelerateInterpolator() // Use any interpolator you like
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener { valueAnimator ->
                    val animatedValue = valueAnimator.animatedValue as Float
                    topYOffset = animatedValue
                    invalidate()
                }
            }
            animator?.start()
        }
    }

    private fun mapValueToAlpha1(value: Int): Int {
        val minAlpha = 0
        val maxAlpha = 255

        return when {
            value  < 250 -> mapValue(value, 0, 250, minAlpha, maxAlpha)
            else -> mapValue(value, 250, 500, maxAlpha, minAlpha)
        }
    }

    private fun mapValueToAlpha2(value: Int): Int {
        val minAlpha = 0
        val maxAlpha = 255

        return when {
            value  < 400 -> mapValue(value, 150, 400, minAlpha, maxAlpha)
            else -> mapValue(value, 400, 650, maxAlpha, minAlpha)
        }
    }

    private fun mapValueToAlpha3(value: Int): Int {
        val minAlpha = 0
        val maxAlpha = 255

        return when {
            value  < 550 -> mapValue(value, 300, 550, minAlpha, maxAlpha)
            else -> mapValue(value, 550, 800, maxAlpha, minAlpha)
        }
    }

    private fun mapValue(value: Int, start1: Int, stop1: Int, start2: Int, stop2: Int): Int {
        return (start2 + (stop2 - start2) * ((value - start1).toFloat() / (stop1 - start1).toFloat())).toInt()
    }
}