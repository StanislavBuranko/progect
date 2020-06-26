package com.askerweb.autoclickerreplay.point

import android.content.Context
import android.graphics.*
import android.util.Log
import android.widget.FrameLayout
import kotlin.math.atan2


class PointCanvasView(context: Context, var points: List<Point> ) : FrameLayout(context) {

    private val paintLineToSwipePoint:Paint = Paint()
    private val paintStandard:Paint = Paint()

    private val arrowMatrixSwipe = Matrix()
    private val arrowMatrixSwipeFirst = Matrix()
    private val arrowMatrixSwipeSecond = Matrix()

    init{
        with(paintLineToSwipePoint){
            color = Color.RED
            strokeWidth = 25f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            pathEffect = DashPathEffect(floatArrayOf(27f, 37f), 0f)
        }
        with(paintStandard){
            color = Color.RED
            strokeWidth = 10f
        }


//        isClickable = false
            setWillNotDraw(false)
//        requestDisallowInterceptTouchEvent(true)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        Log.d("CanvasView", "-----draw")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.d("CanvasView", "-----drawON")
        for(a in points){
            Log.d("CanvasView", "point - " + a::class)
            when(a::class){
                PinchPoint::class -> {
                    val arrowPath = with(Path()){
                        lineTo(-35f, -35f)
                        lineTo(35f, 0f)
                        lineTo(-35f, 35f)
                        close()
                        this
                    }
                    val arrowPath2 = with(Path()){
                        lineTo(-35f, -35f)
                        lineTo(35f, 0f)
                        lineTo(-35f, 35f)
                        close()
                        this
                    }
                    val pinchPoint = a as PinchPoint //TODO("refactoring, cast class multiple times")
                    canvas?.drawLine(pinchPoint.xTouch.toFloat(),
                            pinchPoint.yTouch.toFloat(),
                            pinchPoint.firstPoint.xTouch.toFloat(),
                            pinchPoint.firstPoint.yTouch.toFloat(),
                            paintLineToSwipePoint
                    )
                    canvas?.drawLine(pinchPoint.xTouch.toFloat(),
                            pinchPoint.yTouch.toFloat(),
                            pinchPoint.secondPoint.xTouch.toFloat(),
                            pinchPoint.secondPoint.yTouch.toFloat(),
                            paintLineToSwipePoint
                    )
                    val angle1 =
                            atan2(-(pinchPoint.yTouch - pinchPoint.firstPoint.yTouch).toDouble(),
                                    -(pinchPoint.xTouch - pinchPoint.firstPoint.xTouch).toDouble()) * 180 / Math.PI
                    val angle2 =
                            atan2(-(pinchPoint.yTouch - pinchPoint.secondPoint.yTouch).toDouble(),
                                    -(pinchPoint.xTouch - pinchPoint.secondPoint.xTouch).toDouble()) * 180 / Math.PI
                    arrowMatrixSwipeFirst.setRotate(angle1.toFloat())
                    arrowPath.transform(arrowMatrixSwipeFirst)
                    arrowPath.offset(pinchPoint.firstPoint.xTouch.toFloat(),
                            pinchPoint.firstPoint.yTouch.toFloat())
                    canvas?.drawPath(arrowPath, paintStandard)
                    arrowMatrixSwipeSecond.setRotate(angle2.toFloat())
                    arrowPath2.transform(arrowMatrixSwipeSecond)
                    arrowPath2.offset(pinchPoint.secondPoint.xTouch.toFloat(),
                            pinchPoint.secondPoint.yTouch.toFloat())
                    canvas?.drawPath(arrowPath2, paintStandard)
                }
                SwipePoint::class -> {
                    val arrowPath = with(Path()){
                        lineTo(-35f, -35f)
                        lineTo(35f, 0f)
                        lineTo(-35f, 35f)
                        close()
                        this
                    }
                    val swipePoint = a as SwipePoint //TODO("refactoring, cast class multiple times")
                    canvas?.drawLine(swipePoint.xTouch.toFloat(),
                            swipePoint.yTouch.toFloat(),
                            swipePoint.nextPoint.xTouch.toFloat(),
                            swipePoint.nextPoint.yTouch.toFloat(),
                            paintLineToSwipePoint
                    )
                    val angle =
                            atan2(-(swipePoint.yTouch - swipePoint.nextPoint.yTouch).toDouble(),
                                    -(swipePoint.xTouch - swipePoint.nextPoint.xTouch).toDouble()) * 180 / Math.PI
                    Log.d("Angle", "angle: $angle")
                    arrowMatrixSwipe.setRotate(angle.toFloat())
                    arrowPath.transform(arrowMatrixSwipe)
                    arrowPath.offset(swipePoint.nextPoint.xTouch.toFloat(),
                            swipePoint.nextPoint.yTouch.toFloat())
                    canvas?.drawPath(arrowPath, paintStandard)
                }
            }
        }
    }

}