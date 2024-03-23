package com.surajrathod.powersliderview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.View

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas?.apply {
            canvas?.apply {
                val width = width.toFloat()
                val height = height.toFloat()



                // Define the path for the first circle
                val circle1Path = Path().apply {
                    addCircle(width / 4, height / 2, width / 4, Path.Direction.CW)
                }

                // Define the path for the second circle
//                val circle2Path = Path().apply {
//                    addCircle(width * 3 / 4, height / 2, width / 4, Path.Direction.CW)
//                }

                // Clip the canvas with the first circle path
                clipPath(circle1Path,Region.Op.INTERSECT)
                // Draw the second circle (it will be clipped by the first circle)
                //drawPath(circle2Path, paint)

                // Reset the clipping
                //canvas.restore()

                 //Clip the canvas with the second circle path
                //clipPath(circle2Path)
                 //Draw the first circle (it will be clipped by the second circle)
                //drawPath(circle1Path, paint.apply { color = Color.WHITE })

                // Draw the rectangle
                drawRect(0f, 0f, width, height, paint.apply {
                    color = Color.BLUE
                })
            }
        }
    }
}