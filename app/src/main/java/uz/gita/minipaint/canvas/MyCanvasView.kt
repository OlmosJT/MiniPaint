package uz.gita.minipaint.canvas

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import uz.gita.minipaint.R
import kotlin.math.abs

private const val STROKE_WIDTH = 10f // line thickness

class MyCanvasView(context: Context): View(context) {
    // these are for caching what has been drawn before
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    // background color for view
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    // draw color for view
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    private val paint = Paint().apply {
        color = drawColor
        // smooth out edges
        isAntiAlias = true
        // affects how colors with higher-precision than the device are down-sampled
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }

    private val path = Path()

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private var currentX = 0f
    private var currentY = 0f

    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    /* to avoid drawing every pixel that is less than touchTolerance distance,
        we check if user really draw or barely touch screen
        scaledTouchSlop returns difference in pixels a touch can wander before system thinks the user
        is scrolling
    */
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private fun touchMove() {
        // Calculate traveled distance, dx dy
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        // check traveled distance is greater than touchTolerance distance
        if(dx >= touchTolerance || dy >= touchTolerance) {
            // user is really draw something. .quadTo() draws smooth line without corners
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // draw a path in the extra bitmap to cache it
            extraCanvas.drawPath(path, paint)

            invalidate() // forces redrawing of the screen with updated path.
        }
    }

    private fun touchUp() {
        // Reset path so it doesn't get drawn again
        path.reset()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            motionTouchEventX = it.x
            motionTouchEventY = it.y

            when(it.action){
                MotionEvent.ACTION_DOWN -> touchStart()
                MotionEvent.ACTION_MOVE -> touchMove()
                MotionEvent.ACTION_UP -> touchUp()
                else -> {}
            }
        }

        return true
    }

    // this method get called by Android system when screen dimensions changes
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        // every time method call new bitmap instance is created old ones remains leads to memory leak.
        // let's avoid this. remove all bitmaps before creating one
        if(::extraBitmap.isInitialized) extraBitmap.recycle()
        // create bitmap instance and given screen size with ARGB color configuration. each color 4 byte
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        // give a backgroundColor to our canvas
        extraCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(extraBitmap, 0f,0f, null)

    }
}