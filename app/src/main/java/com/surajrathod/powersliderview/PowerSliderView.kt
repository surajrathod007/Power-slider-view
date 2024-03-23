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
    private var vectorRestart: Drawable? = null
    private var drawableSize: Int = 0


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
        drawableSize = mCircleRadius.toInt()
        drawRestartVector(canvas)

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

    private fun drawRestartVector(canvas: Canvas) {
        val left = (width - drawableSize) / 2
        val right = left + drawableSize
        // Calculate top and bottom coordinates to center vertically
        val top = drawableSize
        val bottom = top + drawableSize
        vectorRestart?.setBounds(left, top, right, bottom)
        vectorRestart?.alpha = getRestartVectorAlpha()

        // Save the canvas state
        canvas.save()
        // Rotate the canvas
        canvas.rotate(
            getRotationAngle(),
            (mWidth ?: 0f) / 2,
            mCircleRadius + (mCircleRadius / 2)
        )    //take center of drawable and rotate from there

        // Draw the vector drawable onto the canvas
        vectorRestart?.draw(canvas)

        // Restore the canvas state
        canvas.restore()
    }

    private fun getRestartVectorAlpha(): Int { // Define the range of values
        val minValue = mBottomLimit
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

    private fun getExpectedYForSampleShader(): Float {
        val newValue = mCenterPoint - mCircleCenterY
        return if (mCircleCenterY <= mCenterPoint) {
            ((newValue * (mHeight ?: 0f)) / mCenterPoint) + mCenterPoint
        } else {
            0f
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