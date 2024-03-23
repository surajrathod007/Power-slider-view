package com.surajrathod.powersliderview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator


class PowerSliderView : View {

    /* Main states */
    companion object {
        const val STATE_IDLE = 0
        const val STATE_TOP_SELECTED = 1
        const val STATE_BOTTOM_SELECTED = 2
    }


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()


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
    private val topGradientStartColor = Color.RED
    private val topGradientEndColor = Color.TRANSPARENT

    private val bottomGradientStartColor = Color.BLUE
    private val bottomGradientEndColor = Color.TRANSPARENT

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
        paint.color = 0xFF00FF00.toInt() // Set color to green
        paint.style = Paint.Style.FILL
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

        mTopLimit = (width / 2).toFloat()
        mBottomLimit = (height - (width / 2)).toFloat()

        mInitialCenterY = (mHeight ?: 0f) / 2


        logE("SURAJPAINT", "Max width : $width Max height : $height")


        val centerX = width / 2
        if (mCircleCenterY == 0f) {
            mCircleCenterY = mHeight!! / 2
        }
        //val centerY = height / 2
        mCircleRadius = (width / 2).toFloat() - paddingBottom

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

        // Draw top gradient
        canvas.drawRect(0f, 0f, mWidth ?: 0f, mHeight ?: 0f, topPaint)
        // Draw bottom
        canvas.drawRect(0f, 0f, mWidth ?: 0f, mHeight ?: 0f, bottomPaint)
        //canvas.drawRect(RectF(100f,100f,100f,100f),paint)
        canvas.drawCircle(centerX.toFloat(), mCircleCenterY, mCircleRadius, paint)
        logE(
            "SURAJPAINT",
            "Total width : $width\nTotal height : $height\nRadius : $mCircleRadius\nCenter X : $centerX\nCenterY : $mCircleCenterY"
        )
    }

    private fun getExpectedYForTopGradient(): Float {
        val newValue = mCenterPoint - mCircleCenterY
        return if (mCircleCenterY < mCenterPoint) {
            ((newValue * (mHeight ?: 0f)) / mCenterPoint)
        } else {
            0f
        }
    }

    private fun getExpectedYForBottomGradient(): Float {
        val newValue = (mHeight ?: 0f) - mCircleCenterY
        return if (mCircleCenterY > mCenterPoint) {
            ((newValue * (mHeight ?: 0f)) / mCenterPoint)
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
                        interpolator = OvershootInterpolator() // Use any interpolator you like
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