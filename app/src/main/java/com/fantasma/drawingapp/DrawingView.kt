package com.fantasma.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private lateinit var mDrawPath : CustomPath
    private lateinit var mCanvasBitmap : Bitmap
    private lateinit var mDrawPaint : Paint
    private lateinit var mCanvasPaint : Paint
    private lateinit var canvas : Canvas
    private var brushSize : Float = 0.toFloat()
    private var mBrushSize : Float = 0.toFloat()
    private var color = Color.BLACK
    private val mPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPaint.color = color
        mDrawPaint.style = Paint.Style.STROKE
        mDrawPaint.strokeJoin = Paint.Join.ROUND
        mDrawPaint.strokeCap = Paint.Cap.ROUND
        mDrawPath = CustomPath(color, mBrushSize)
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap, 0f, 0f, mCanvasPaint)

        for(path in mPaths){
            mDrawPaint.strokeWidth = path.brushThickness
            mDrawPaint.color = path.color
            canvas.drawPath(path, mDrawPaint)
        }

        if(!mDrawPath.isEmpty) {
            mDrawPaint.strokeWidth = mDrawPath.brushThickness
            mDrawPaint.color = mDrawPath.color
            canvas.drawPath(mDrawPath, mDrawPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath.color = color
                mDrawPath.brushThickness = mBrushSize

                mDrawPath.reset()

                if(touchX != null && touchY != null) {
                    mDrawPath.moveTo(touchX, touchY)
                    mDrawPath.lineTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if(touchX != null && touchY != null)
                    mDrawPath.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }

        invalidate()

        return true
    }

    fun setSizeForBrush(newSize: Float) {
        brushSize = newSize
        mBrushSize = convertToDP(brushSize)

        mDrawPaint.strokeWidth = mBrushSize
    }

    fun convertToDP(number: Float) : Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, number, resources.displayMetrics)

    fun getBrushSize() : Float = brushSize

    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        mDrawPaint.color = color
    }

    fun undo() {
        if(mPaths.isNotEmpty()) {
            mPaths.removeAt(mPaths.size-1)
            invalidate()
        }
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {


    }

}