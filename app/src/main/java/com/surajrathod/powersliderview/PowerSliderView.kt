package com.surajrathod.powersliderview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat


class PowerSliderView : View {

    /* Main states */
    companion object {
        const val STATE_IDLE = 0
        const val STATE_TOP_SELECTED = 1
        const val STATE_BOTTOM_SELECTED = 2
    }


    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)


    private var mWidth: Float? = null
    private var mHeight: Float? = null

    //listeners
    private var mListener: OnCheckChangedListener? = null

    //state variables for inner circle
    private var circleDiameter = 0f

    private var mCircleRadius = 0f

    private var mTopLimit = 0f
    private var mBottomLimit = 0f

    private var mCircleTopY = 0f
    private var mCircleCenterY =
        0f //this variable is used for all the things, it changes a lot and other things will change based on this
    private var mCircleBottomY = 0f

    private var mInitialCenterY = 0f

    private var shallMove = false


    private var animator: ValueAnimator? = null

    //state variables for gradients
    private var mCenterPoint = 0f

    // Define the colors for the gradient
    private val topGradientStartColor = Color.parseColor("#2bb675")
    private val topGradientEndColor = Color.TRANSPARENT

    private val bottomGradientStartColor = Color.parseColor("#ca3144")
    private val bottomGradientEndColor = Color.TRANSPARENT

    //Define text variables
    private var textHeight = 0f

    //Capsule
    private val capsulePaint = Paint().apply {
        color = Color.parseColor("#323232") // Set color to blue
        isAntiAlias = true // Enable anti-aliasing for smooth edges
        style = Paint.Style.FILL // Fill the shape
    }


    //vector drawables
    private var vectorArrowUp: Drawable? = null
    private var vectorArrowDown: Drawable? = null
    private var vectorRestart: Drawable? = null
    private var vectorPower: Drawable? = null
    private var vectorVerticalBar: Drawable? = null
    private var drawableSize: Int = 0

    //arrow up
    private var vectorArrowUpTopLimit = 0   //it is changing for animation
    private var vectorArrowUpBottomLimit = 0

    //arrow down
    private var vectorArrowDownTopLimit = 0   //it is changing for animation
    private var vectorArrowDownBottomLimit = 0

    private var initialVectorArrowUpTopLimit = 0
    private var initialVectorArrowDownTopLimit = 0

    private var vectorTopArrowAnimator: ValueAnimator? = null
    private var vectorBottomArrowAnimator: ValueAnimator? = null


    //imp variable for down arrow
    private var totalDistanceForDownArrow = 0
    private var expectedAnimationTopLimitForBottomArrow = 0

    //vertical bar
    private var verticalBarInitialTop = 0
    private var verticalBarInitialBottom = 0f
    private var verticalBarYOffset = 0

    private var originalBottomRange = 0
    private var originalTopRange = 0
    private var initialTopRange = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    interface OnCheckChangedListener {
        fun onCheckChanged(state: Int)
    }

    fun setOnCheckChangedListener(onCheckChangedListener: OnCheckChangedListener) {
        mListener = onCheckChangedListener
    }

    private fun init() {
        circlePaint.color = Color.WHITE // Set color to green
        circlePaint.style = Paint.Style.FILL
        //Load the vector drawable
        vectorRestart = ContextCompat.getDrawable(context, R.drawable.baseline_sync_24)
        vectorArrowUp = ContextCompat.getDrawable(context, R.drawable.baseline_keyboard_arrow_up_24)
        vectorPower = ContextCompat.getDrawable(context, R.drawable.half_power_svg)
        vectorArrowDown =
            ContextCompat.getDrawable(context, R.drawable.baseline_keyboard_arrow_down_24)
        vectorVerticalBar = ContextCompat.getDrawable(context, R.drawable.vertical_bar_svg)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        if (mWidth == null) {
            mWidth = width.toFloat()
        }
        if (mHeight == null) {
            mHeight = height.toFloat()
        }
        mCenterPoint = (mHeight ?: 0f) / 2

        mTopLimit = (width / 2).toFloat() + textHeight
        mBottomLimit = (height - (width / 2)).toFloat() - textHeight

        mInitialCenterY = (mHeight ?: 0f) / 2


        logE("SURAJPAINT", "Max width : $width Max height : $height")


        val centerX = width / 2
        if (mCircleCenterY == 0f) {
            mCircleCenterY = mHeight!! / 2
        }
        //val centerY = height / 2
        mCircleRadius =
            (width / 2).toFloat() - (((width / 2).toFloat() * 20) / 100)    //20 % spacing pading for circle

        circleDiameter = mCircleRadius / 2

        mCircleTopY = mCircleCenterY - mCircleRadius
        mCircleBottomY = mCircleCenterY + mCircleRadius


        // Define the shader
        val topShader = LinearGradient(
            0f, 0f, 0f, getExpectedYForTopGradient(),
            topGradientStartColor, topGradientEndColor, Shader.TileMode.CLAMP
        )

        // Create paint and set the shader
        val topPaint = Paint()
        topPaint.shader = topShader

        val bottomShader = LinearGradient(
            0f, mHeight ?: 0f, 0f, getExpectedYForBottomGradient(),
            bottomGradientStartColor,
            bottomGradientEndColor,
            Shader.TileMode.CLAMP
        )

        val bottomPaint = Paint()
        bottomPaint.shader = bottomShader

        //draw the capsule
        drawCapsule(canvas)

        //draw the vectors
        drawableSize =
            ((mCircleRadius * 80) / 100).toInt() //80 % of circle radius old value was mCircleRadius
        drawRestartVector(canvas)
        drawPowerVector(canvas)
        drawVerticalBar(canvas)


        drawArrowUpVectors(canvas)
        drawArrowDownVectors(canvas)

        // Draw top gradient
        canvas.drawRect(0f, -(mHeight ?: 0f), mWidth ?: 0f, mHeight ?: 0f, topPaint)

        // Draw bottom
        canvas.drawRect(0f, 0f, mWidth ?: 0f, mHeight ?: 0f, bottomPaint)

        //draw our circle
        canvas.drawCircle(centerX.toFloat(), mCircleCenterY, mCircleRadius, circlePaint)
        logE(
            "SURAJPAINT",
            "Total width : $width\nTotal height : $height\nRadius : $mCircleRadius\nCenter X : $centerX\nCenterY : $mCircleCenterY\n Psddding bottmm : $paddingBottom\nTop limit : $mTopLimit"
        )
    }


    private fun drawPowerVector(canvas: Canvas) {
        val left = (width - drawableSize) / 2
        val right = left + drawableSize
        // Calculate top and bottom coordinates to center vertically

        val bottom = ((mHeight ?: 0f) - drawableSize).toInt()
        val top = bottom - drawableSize
        vectorPower?.setBounds(left, top, right, bottom)
        if (mCircleCenterY < mCenterPoint) {
            vectorPower?.alpha = getBottomVectorAlpha()
        }
        vectorPower?.draw(canvas)
    }

    private fun drawVerticalBar(canvas: Canvas) {
        val left = (width - drawableSize) / 2
        val right = left + drawableSize
        // Calculate top and bottom coordinates to center vertically

        val newDrawableSize = (drawableSize * 115) / 100

        originalBottomRange = ((mHeight ?: 0f) - drawableSize).toInt()
        originalTopRange = (originalBottomRange - drawableSize)

        initialTopRange = originalBottomRange - newDrawableSize

        verticalBarYOffset = getVerticalBarOffset()


        vectorVerticalBar?.setBounds(left, verticalBarYOffset, right, verticalBarYOffset + drawableSize)
        if (mCircleCenterY < mCenterPoint) {
            vectorVerticalBar?.alpha = getBottomVectorAlpha()
        }
        vectorVerticalBar?.draw(canvas)
    }

    private fun drawArrowUpVectors(canvas: Canvas) {
        val topBoundary = drawableSize * 2 //the refresh circles size -> top boundary
        val bottomBoundary = mCenterPoint - mCircleRadius //initial center circle top
        val drawableSize = (drawableSize * 60) / 100    //the size of each drawable
        val drawableSpacing = 0 //spacing would be 20% of original drawable size
        val totalSumOfDrawableIncludingSpace = (drawableSize * 3) + drawableSpacing * 2


        val limits =
            getLimitsForUpVectors(topBoundary, bottomBoundary, totalSumOfDrawableIncludingSpace)

        initialVectorArrowUpTopLimit = limits.first

        //setting this if it is first time other time it will be update from animator
        if (vectorArrowUpTopLimit == 0) {
            vectorArrowUpTopLimit = limits.first
        }
        vectorArrowUpBottomLimit = limits.second
        //val bottomLimit = topLimit + totalSumOfDrawableIncludingSpace

//        logE(
//            "SURAJARROW",
//            "Top : $topBoundary bottomBoundary : $bottomBoundary Drawable size : $drawableSize Total sum : ${(drawableSize * 3) + drawableSpacing * 2} \n top limit : $topLimit bottom limit : $bottomLimit"
//        )    //218 to 1000
        val left = (width - drawableSize) / 2
        val right = left + drawableSize


        // Calculate the y-coordinate for the top of the first rectangle
        val startY1 = vectorArrowUpTopLimit
        // Calculate the y-coordinate for the top of the second rectangle
        val startY2 = startY1 + drawableSize + drawableSpacing
        // Calculate the y-coordinate for the top of the third rectangle
        val startY3 = startY2 + drawableSize + drawableSpacing


        if (mCircleCenterY > mCenterPoint) {
            vectorArrowUp?.alpha = getTopVectorsAlpha()
        } else {
            vectorArrowUp?.alpha = mapValueToAlpha(
                initialVectorArrowUpTopLimit,
                vectorArrowUpBottomLimit,
                vectorArrowUpTopLimit
            )
        }

        vectorArrowUp?.setBounds(left, startY1, right, startY1 + drawableSize)
        vectorArrowUp?.draw(canvas)

        vectorArrowUp?.setBounds(left, startY2, right, startY2 + drawableSize)
        vectorArrowUp?.draw(canvas)

        vectorArrowUp?.setBounds(left, startY3, right, startY3 + drawableSize)
        vectorArrowUp?.draw(canvas)

        val isRunning = vectorTopArrowAnimator?.isRunning ?: false
        if (!isRunning) {
            vectorTopArrowAnimator = ValueAnimator.ofFloat(
                vectorArrowUpBottomLimit.toFloat(),
                initialVectorArrowUpTopLimit.toFloat()
            ).apply {
                this.duration = 1500
                interpolator = DecelerateInterpolator() // Use any interpolator you like
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener { valueAnimator ->
                    val animatedValue = valueAnimator.animatedValue as Float
                    vectorArrowUpTopLimit = animatedValue.toInt()
                    invalidate()
                }
            }
            vectorTopArrowAnimator?.start()
        }
    }

    private fun drawArrowDownVectors(canvas: Canvas) {
        val topBoundary = mCenterPoint + mCircleRadius //the refresh circles size -> top boundary
        val bottomBoundary = ((mHeight ?: 0f) - drawableSize * 2) //initial center circle top
        val drawableSize = (drawableSize * 60) / 100    //the size of each drawable
        val drawableSpacing = 0 //spacing would be 20% of original drawable size
        val totalSumOfDrawableIncludingSpace = (drawableSize * 3) + drawableSpacing * 2

        val limits =
            getLimitsForUpVectors(
                topBoundary.toInt(),
                bottomBoundary,
                totalSumOfDrawableIncludingSpace
            )

        initialVectorArrowDownTopLimit = limits.first

        //setting this if it is first time other time it will be update from animator
        if (vectorArrowDownTopLimit == 0) {
            vectorArrowDownTopLimit = limits.first
        }
        vectorArrowDownBottomLimit = limits.second

//        logE(
//            "SURAJARROWDOWN",
//            "Top limit : $initialVectorArrowDownTopLimit BottomLimit : $vectorArrowDownBottomLimit"
//        )

        val left = (width - drawableSize) / 2
        val right = left + drawableSize
        // Calculate top and bottom coordinates to center vertically
        val top = vectorArrowDownTopLimit
        val bottom = top + drawableSize

        // Calculate the y-coordinate for the top of the first rectangle
        val startY1 = vectorArrowDownTopLimit
        // Calculate the y-coordinate for the top of the second rectangle
        val startY2 = startY1 + drawableSize + drawableSpacing
        // Calculate the y-coordinate for the top of the third rectangle
        val startY3 = startY2 + drawableSize + drawableSpacing

        if (mCircleCenterY < mCenterPoint) {
            vectorArrowDown?.alpha = getBottomVectorAlpha()
        } else {
            vectorArrowDown?.alpha = mapValueToAlpha(
                expectedAnimationTopLimitForBottomArrow,
                initialVectorArrowDownTopLimit,
                vectorArrowDownTopLimit
            )
        }


        logE(
            "SURAJARROWDOWN",
            "Top limit : $vectorArrowDownTopLimit Alpha : ${
                mapValueToAlpha(
                    expectedAnimationTopLimitForBottomArrow,
                    initialVectorArrowDownTopLimit,
                    vectorArrowDownTopLimit
                )
            }"
        )

        vectorArrowDown?.setBounds(left, startY1, right, startY1 + drawableSize)
        vectorArrowDown?.draw(canvas)

        vectorArrowDown?.setBounds(left, startY2, right, startY2 + drawableSize)
        vectorArrowDown?.draw(canvas)

        vectorArrowDown?.setBounds(left, startY3, right, startY3 + drawableSize)
        vectorArrowDown?.draw(canvas)

        val isRunning = vectorBottomArrowAnimator?.isRunning ?: false
        if (!isRunning) {
            totalDistanceForDownArrow = vectorArrowDownBottomLimit - initialVectorArrowDownTopLimit
            expectedAnimationTopLimitForBottomArrow =
                initialVectorArrowDownTopLimit - totalDistanceForDownArrow
            vectorBottomArrowAnimator = ValueAnimator.ofFloat(
                expectedAnimationTopLimitForBottomArrow.toFloat(),
                initialVectorArrowDownTopLimit.toFloat()
            ).apply {
                this.duration = 1500
                interpolator = DecelerateInterpolator() // Use any interpolator you like
                //repeatCount = ValueAnimator.INFINITE
                addUpdateListener { valueAnimator ->
                    val animatedValue = valueAnimator.animatedValue as Float
                    vectorArrowDownTopLimit = animatedValue.toInt()
                    invalidate()
                }
            }
            vectorBottomArrowAnimator?.start()
        }
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
                    value < midValue -> mapValue(value, lowerBound, midValue, minAlpha, maxAlpha)
                    else -> mapValue(value, midValue, upperBound, maxAlpha, minAlpha)
                }
            }
        }
    }


    private fun mapValue(value: Int, start1: Int, stop1: Int, start2: Int, stop2: Int): Int {
        return (start2 + (stop2 - start2) * ((value - start1).toFloat() / (stop1 - start1).toFloat())).toInt()
    }

    private fun getLimitsForUpVectors(
        topBoundary: Int,
        bottomBoundary: Float,
        middlePointCounts: Int
    ): Pair<Int, Int> {
        val points = (topBoundary..bottomBoundary.toInt()).toList()
        // Find the index of the middle point
        val middleIndex = points.size / 2

        // Extract the middle size events
        val middlePoints = points.subList(
            middleIndex - (middlePointCounts / 2),
            middleIndex + (middlePointCounts / 2)
        )
        return Pair(middlePoints.first(), middlePoints.last())
    }

    private fun drawRestartVector(canvas: Canvas) {
        val left = (width - drawableSize) / 2
        val right = left + drawableSize
        // Calculate top and bottom coordinates to center vertically
        val top = drawableSize
        val bottom = top + drawableSize
        vectorRestart?.setBounds(left, top, right, bottom)
        vectorRestart?.alpha = getTopVectorsAlpha()

        // Save the canvas state
        canvas.save()
        // Rotate the canvas
        canvas.rotate(
            getRotationAngle(),
            (mWidth ?: 0f) / 2,
            drawableSize + (drawableSize / 2).toFloat()     // old value -> mCircleRadius + (mCircleRadius / 2)
        )    //take center of drawable and rotate from there

        // Draw the vector drawable onto the canvas
        vectorRestart?.draw(canvas)

        // Restore the canvas state
        canvas.restore()
    }

    private fun getTopVectorsAlpha(): Int { // Define the range of values
        val minValue = mBottomLimit
        val maxValue = mCenterPoint

        // Define the range of alpha values
        val minAlpha = 255f
        val maxAlpha = 0f

        // Calculate the alpha based on linear interpolation
        return (minAlpha + (maxAlpha - minAlpha) * (1 - (mCircleCenterY - minValue) / (maxValue - minValue))).toInt()
    }


    private fun getBottomVectorAlpha(): Int { // Define the range of values
        val minValue = mTopLimit
        val maxValue = mCenterPoint

        // Define the range of alpha values
        val minAlpha = 255f
        val maxAlpha = 0f

        // Calculate the alpha based on linear interpolation
        return (minAlpha + (maxAlpha - minAlpha) * (1 - (mCircleCenterY - minValue) / (maxValue - minValue))).toInt()
    }

    private fun getRotationAngle(): Float {
        //we used Linear interpolation to determine appropriate degree based on two known range
        //if user swipe up then and then only send rotation value otherwise send 0, that means no rotation
        // Define the range of values
        return if (mCircleCenterY < mCenterPoint) {
            val minValue = mTopLimit
            val maxValue = mCenterPoint
            // Define the range of degrees
            val minDegree = 360f
            val maxDegree = 0f
            val rotationAngle =
                minDegree + (maxDegree - minDegree) * (1 - (mCircleCenterY - minValue) / (maxValue - minValue))
            rotationAngle
        } else {
            0f
        }
    }


    private fun getVerticalBarOffset(): Int {
        return if (mCircleCenterY > mCenterPoint) {
            val minValue = mBottomLimit
            val maxValue = mCenterPoint
            // Define the range of degrees
            val minDegree = initialTopRange
            val maxDegree = originalTopRange
            val rotationAngle =
                minDegree + (maxDegree - minDegree) * (1 - (mCircleCenterY - minValue) / (maxValue - minValue))
            rotationAngle.toInt()
        } else {
            initialTopRange
        }

    }

    private fun drawCapsule(canvas: Canvas) {


        // Calculate the dimensions for the capsule shape
        val capsuleWidth = mWidth ?: 0f
        val capsuleHeight = mHeight ?: 0f


        // Calculate the dimensions for the rounded ends
        val radius = capsuleWidth / 2

        // Draw the main body of the capsule (rectangle)
        canvas.drawRect(
            0f,
            radius + textHeight,
            capsuleWidth,
            (capsuleHeight ?: 0f) - radius - textHeight,
            capsulePaint
        )

        //draw top circle
        canvas.drawCircle(capsuleWidth / 2, mTopLimit, radius, capsulePaint)

        //draw bottom circle
        canvas.drawCircle(capsuleWidth / 2, mBottomLimit, radius, capsulePaint)

        //clip the capsule shape so our gradient will be drawn withing capsule not in rectangle ;)
        val clipCapsulePath = Path().apply {
            addCircle(capsuleWidth / 2, mTopLimit, radius, Path.Direction.CW)
            addRect(
                0f,
                radius + textHeight,
                capsuleWidth,
                (capsuleHeight ?: 0f) - radius - textHeight,
                Path.Direction.CW
            )
            addCircle(capsuleWidth / 2, mBottomLimit, radius, Path.Direction.CW)
        }
        canvas.clipPath(clipCapsulePath)
    }

    private fun getExpectedYForTopGradient(): Float {
        val newValue = mCenterPoint - mCircleCenterY
        return if (mCircleCenterY < mCenterPoint) {
            ((newValue * (mHeight ?: 0f)) / mCenterPoint) + mCenterPoint
        } else {
            0f
        }
    }

    private fun getExpectedYForBottomGradient(): Float {
        val newValue = (mHeight ?: 0f) - mCircleCenterY
        return if (mCircleCenterY > mCenterPoint) {
            ((newValue * (mHeight ?: 0f)) / mCenterPoint) - mCenterPoint
        } else {
            mHeight ?: 0f
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_UP -> {
                shallMove = false
                if (event.y < mTopLimit) {
                    mCircleCenterY = mTopLimit
                    invalidate()
                    mListener?.onCheckChanged(STATE_TOP_SELECTED)
                    return true
                } else if (event.y > mBottomLimit) {
                    mCircleCenterY = mBottomLimit
                    invalidate()
                    mListener?.onCheckChanged(STATE_BOTTOM_SELECTED)
                    return true
                }
                val isRunning = animator?.isRunning ?: false
                if (!isRunning) {
                    animator = ValueAnimator.ofFloat(mCircleCenterY, mInitialCenterY).apply {
                        this.duration = 500
                        interpolator = DecelerateInterpolator() // Use any interpolator you like
                        addUpdateListener { valueAnimator ->
                            val animatedValue = valueAnimator.animatedValue as Float
                            mCircleCenterY = animatedValue
                            invalidate()
                        }
                    }
                    animator?.start()
                }
                mListener?.onCheckChanged(STATE_IDLE)
            }

            MotionEvent.ACTION_DOWN -> {
                //if user touched circle only
                shallMove = event.y > mCircleTopY && event.y < mCircleBottomY
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                //dragging only available between these two limits
                if (event.y > mTopLimit && event.y < mBottomLimit) {
                    if (shallMove) {
                        mCircleCenterY = event.y
                        invalidate()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }


}