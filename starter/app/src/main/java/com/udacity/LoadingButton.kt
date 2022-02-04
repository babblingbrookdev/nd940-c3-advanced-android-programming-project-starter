package com.udacity

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

private const val PROGRESS = "progress"
private const val ANIMATION_DURATION = 2000L

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var buttonProgress = 0f

    private var buttonArcMargin = 50f
    private var buttonArcRadius = 40f

    private var buttonTextSize = context.resources.getDimension(R.dimen.default_button_text_size)
    private var buttonTextColor = context.getColor(R.color.default_button_text_color)
    private var buttonBackgroundColor = context.getColor(R.color.default_button_background_color)
    private var buttonCircleColor = context.getColor(R.color.default_button_circle_color)
    private var buttonProgressColor = context.getColor(R.color.default_button_progress_color)

    private val valueAnimator = ValueAnimator()

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = buttonTextSize
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, old, new ->
        if (old == new) return@observable
        when (new) {
            ButtonState.Clicked -> {
                buttonProgress = 0f
                valueAnimator.cancel()
            }
            ButtonState.Loading -> {
                startAnimation()
            }
            ButtonState.Completed -> {
                buttonProgress = 1f
                valueAnimator.cancel()
            }
        }
    }


    init {
        // from https://developer.android.com/training/custom-views/create-view
        // typed array objects are a shared resource, so recycle
        context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0).apply {
            try {
                buttonBackgroundColor =
                    getColor(R.styleable.LoadingButton_buttonBackgroundColor, buttonBackgroundColor)
                buttonProgressColor =
                    getColor(R.styleable.LoadingButton_buttonProgressColor, buttonProgressColor)
                buttonCircleColor =
                    getColor(R.styleable.LoadingButton_buttonCircleColor, buttonCircleColor)
                buttonTextColor =
                    getColor(R.styleable.LoadingButton_buttonTextColor, buttonTextColor)
                buttonTextSize = getFloat(R.styleable.LoadingButton_buttonTextSize, buttonTextSize)
            } finally {
                recycle()
            }
        }
        isClickable = true
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        drawInitialBackground(canvas)

        if (buttonState == ButtonState.Loading) {
            drawButtonProgress(canvas)
            drawButtonLoadingText(canvas)
            drawButtonCircle(canvas)
        } else {
            drawInitialText(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun startAnimation() {
        valueAnimator.apply {
            removeAllUpdateListeners()
            setValues(PropertyValuesHolder.ofFloat(PROGRESS, 0f, 1f))
            duration = ANIMATION_DURATION
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener {
                buttonProgress = it.getAnimatedValue(PROGRESS) as Float
                invalidate()
            }
        }.start()
    }

    private fun drawInitialBackground(canvas: Canvas) {
        paint.color = buttonBackgroundColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawInitialText(canvas: Canvas) {
        paint.color = buttonTextColor
        canvas.drawText(
            resources.getString(R.string.button_name),
            width.toFloat() / 2,
            (height.toFloat() / 2) - (paint.descent() + paint.ascent() / 2),
            paint
        )
    }

    private fun drawButtonProgress(canvas: Canvas) {
        paint.color = buttonProgressColor
        canvas.drawRect(0f, 0f, buttonProgress * width, height.toFloat(), paint)
    }

    private fun drawButtonLoadingText(canvas: Canvas) {
        paint.color = buttonTextColor
        canvas.drawText(
            resources.getString(R.string.button_loading),
            width.toFloat() / 2,
            (height.toFloat() / 2) - (paint.descent() + paint.ascent() / 2),
            paint
        )
    }

    private fun drawButtonCircle(canvas: Canvas) {
        paint.color = buttonCircleColor
        canvas.drawArc(
            width.toFloat() - buttonArcMargin - (buttonArcRadius * 2),
            height.toFloat() / 2f - buttonArcRadius,
            width.toFloat() - buttonArcMargin,
            height.toFloat() / 2f + buttonArcRadius,
            270f,
            360f * buttonProgress,
            true,
            paint
        )
    }

    fun onStartDownload() {
        isEnabled = false
        buttonState = ButtonState.Loading
    }

    fun onDownloadCompleted() {
        isEnabled = true
        buttonState = ButtonState.Completed
    }
}