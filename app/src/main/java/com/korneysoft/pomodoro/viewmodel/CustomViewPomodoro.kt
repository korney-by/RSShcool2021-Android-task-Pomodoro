package com.korneysoft.pomodoro.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import com.korneysoft.pomodoro.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class CustomViewPomodoro @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var periodMs = 0L
    private var currentMs = 0L
    private var isFinished = false
    private var color = 0
    private var style = FILL
    private val paint = Paint()

    init {
        if (attrs != null) {
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CustomViewPomodoro,
                defStyleAttr,
                0
            )
            color = styledAttrs.getColor(R.styleable.CustomViewPomodoro_custom_color, Color.RED)
            style = styledAttrs.getInt(R.styleable.CustomViewPomodoro_custom_style, FILL)
            styledAttrs.recycle()
        }

        paint.color = color
        paint.style = if (style == FILL) Paint.Style.FILL else Paint.Style.STROKE
        paint.strokeWidth = 1F
        paint.isAntiAlias = true
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (periodMs == 0L || (currentMs == 0L && !isFinished)) return
        val percentAngel = (periodMs - currentMs).toFloat() / periodMs
        val startAngel = (percentAngel * 360)

        val halfWidth = width shr 1
        val halfHeight = height shr 1

        paint.color = color
        paint.style = if (style == FILL) Paint.Style.FILL else Paint.Style.STROKE

        canvas.drawArc(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            -90f,
            startAngel,
            true,
            paint
        )


//        if ((currentMs <= 0) || (currentMs == periodMs)) {
//            return
//        }

        // draw arrow
        paint.color = Color.BLACK
        val r = halfHeight.toFloat()

        val arrowAngel = (2 * percentAngel - (0.5).toFloat()) * PI.toFloat()
        val center = PointF(halfWidth.toFloat(), halfHeight.toFloat())
        val arrowEnd = PointF((r * cos(arrowAngel)) + center.x, (r * sin(arrowAngel)) + center.y)

//        paint.style = Paint.Style.FILL_AND_STROKE
//        val path = Path()
//        path.fillType = Path.FillType.EVEN_ODD;
//        path.moveTo(center.x, center.y);
//        path.lineTo(arrowEnd.x, arrowEnd.y);
//        path.close();
//        canvas.drawPath(path, paint);

        canvas.drawArc(
            arrowEnd.x - halfWidth,
            arrowEnd.y - halfHeight,
            arrowEnd.x + halfWidth,
            arrowEnd.y + halfHeight,
            startAngel + 85,
            10F,
            true,
            paint
        )
        canvas.drawCircle(center.x, center.y, r / 10, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(center.x, center.y, r / 25, paint)
        //canvas.draw
    }


    /**
     * Set lasted milliseconds
     */
    fun setCurrent(current: Long) {
        currentMs = current
        invalidate()
    }

    /**
     * Set time period
     */
    fun setPeriod(period: Long) {
        periodMs = period
    }

    fun setFinished(_isFinisher: Boolean) {
        isFinished = _isFinisher
        invalidate()
    }

    private companion object {

        private const val FILL = 0
    }
}
