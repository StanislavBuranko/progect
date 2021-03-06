package com.askerweb.autoclickerreplay.point.view

import android.content.Context
import android.graphics.*
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.ktExt.xCutout
import com.askerweb.autoclickerreplay.ktExt.xCutoutPathHelper
import com.askerweb.autoclickerreplay.ktExt.yCutout
import com.askerweb.autoclickerreplay.point.PathPoint
import com.askerweb.autoclickerreplay.point.PinchPoint
import com.askerweb.autoclickerreplay.point.Point
import com.askerweb.autoclickerreplay.point.SwipePoint
import com.askerweb.autoclickerreplay.service.AutoClickService
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log
import kotlin.math.sin

/**
 * Class for drawing elements or effects on the overlay.
 */
class PointCanvasView constructor(context: Context) : FrameLayout(context) {

    @JvmField internal var points: List<Point>? = null

    private val paintLineToSwipePoint:Paint = Paint()
    private val paintStandard:Paint = Paint()

    private val arrowMatrixSwipe = Matrix()
    private val arrowMatrixSwipeFirst = Matrix()
    private val arrowMatrixSwipeSecond = Matrix()

    init{
        with(paintLineToSwipePoint){
            color = ContextCompat.getColor(context, R.color.blueHippie)
            strokeWidth = 25f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            pathEffect = DashPathEffect(floatArrayOf(27f, 37f), 0f)
            style = Paint.Style.STROKE
        }
        with(paintStandard){
            color = ContextCompat.getColor(context, R.color.blueHippie)
            strokeWidth = 10f
        }
        setWillNotDraw(false)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for(a in this.points!!){
            if(!a.view.isVisible) continue
            when(a::class){
                PinchPoint::class -> drawPinch(a as PinchPoint, canvas)
                SwipePoint::class -> drawSwipe(a as SwipePoint, canvas)
                PathPoint::class -> drawPath(a as PathPoint, canvas)
            }
        }
    }

    open fun drawPath(pathPoint: PathPoint, canvas: Canvas?){
        canvas?.drawPath(pathPoint.path, paintLineToSwipePoint)
        "drawSwipe123".logd("123321")
        /*if(pathPoint.wasDraw){
            pathPoint.path.reset()
            pathPoint.path.moveTo(pathPoint.coordinateXMove[0] + xCutout(), pathPoint.coordinateYMove[0] + yCutout())
            for (n in 1..pathPoint.coordinateXMove.size-1){
                pathPoint.path.lineTo(pathPoint.coordinateXMove[n] + xCutout(), pathPoint.coordinateYMove[n] + yCutout())
            }
            canvas?.drawPath(pathPoint.path, paintLineToSwipePoint)
        }*/
    }

    open fun drawSwipe(swipePoint: SwipePoint, canvas: Canvas?){
        "drawSwipe".logd("123321")
        val radius = swipePoint.view.width - 15
        val arrowPath = initArrowDraw(Path(), 35f)
        val angle =
                atan2((swipePoint.nextPoint.yTouch - swipePoint.yTouch).toDouble(),
                        (swipePoint.nextPoint.xTouch - swipePoint.xTouch).toDouble())
        val angleD = angle * 180 / Math.PI
        val newX = (swipePoint.nextPoint.xTouch + cos(angle) * -radius).toFloat()
        val newY = (swipePoint.nextPoint.yTouch + sin(angle) * -radius).toFloat()
        Log.d("Angle", "angle: $angle")
        arrowMatrixSwipe.setRotate(angleD.toFloat())
        arrowPath.transform(arrowMatrixSwipe)
        arrowPath.offset(newX - xCutoutPathHelper(),
                newY + yCutout())
        canvas?.drawPath(arrowPath, paintStandard)
        canvas?.drawLine(swipePoint.xTouch.toFloat() - xCutoutPathHelper(),
                swipePoint.yTouch.toFloat() + yCutout(),
                newX - xCutoutPathHelper(),
                newY + yCutout(),
                paintLineToSwipePoint
        )
    }

    open fun drawPinch(pinchPoint: PinchPoint, canvas: Canvas?){
        val radius = pinchPoint.view.width - 15
        val arrowPath = initArrowDraw(Path(), 35f)
        val arrowPath2 = initArrowDraw(Path(), 35f)

        when(pinchPoint.typePinch){
            PinchPoint.PinchDirection.IN -> {
                //calc radius angle
                val angle1 =
                        atan2((pinchPoint.firstPoint.yTouch - pinchPoint.yTouch).toDouble(),
                                (pinchPoint.firstPoint.xTouch - pinchPoint.xTouch).toDouble())
                //calc radius degrees
                val angle1D =
                        atan2(-(pinchPoint.firstPoint.yTouch - pinchPoint.yTouch).toDouble(),
                                -(pinchPoint.firstPoint.xTouch - pinchPoint.xTouch).toDouble()) * 180 / Math.PI
                val newXFirst = (pinchPoint.xTouch + cos(angle1) * radius).toFloat()
                val newYFirst = (pinchPoint.yTouch + sin(angle1) * radius).toFloat()
                arrowMatrixSwipeFirst.setRotate(angle1D.toFloat())
                arrowPath.transform(arrowMatrixSwipeFirst)
                arrowPath.offset(newXFirst - xCutoutPathHelper(), newYFirst + yCutout())
                //draw line to firstPoint
                canvas?.drawLine(newXFirst - xCutoutPathHelper(),
                        newYFirst + yCutout(),
                        pinchPoint.firstPoint.xTouch.toFloat() - xCutoutPathHelper(),
                        pinchPoint.firstPoint.yTouch.toFloat() + yCutout(),
                        paintLineToSwipePoint
                )

                val angle2 =
                        atan2((pinchPoint.secondPoint.yTouch - pinchPoint.yTouch).toDouble(),
                                (pinchPoint.secondPoint.xTouch - pinchPoint.xTouch).toDouble())
                val angle2D =
                        atan2(-(pinchPoint.secondPoint.yTouch - pinchPoint.yTouch).toDouble(),
                                -(pinchPoint.secondPoint.xTouch - pinchPoint.xTouch).toDouble()) * 180 / Math.PI
                val newXSecond = (pinchPoint.xTouch + cos(angle2) * radius).toFloat()
                val newYSecond = (pinchPoint.yTouch + sin(angle2) * radius).toFloat()
                arrowMatrixSwipeSecond.setRotate(angle2D.toFloat())
                arrowPath2.transform(arrowMatrixSwipeSecond)
                arrowPath2.offset(newXSecond - xCutoutPathHelper(), newYSecond + yCutout())
                //draw line to secondPoint
                canvas?.drawLine(newXSecond - xCutoutPathHelper(),
                        newYSecond + yCutout(),
                        pinchPoint.secondPoint.xTouch.toFloat() - xCutoutPathHelper(),
                        pinchPoint.secondPoint.yTouch.toFloat() + yCutout(),
                        paintLineToSwipePoint
                )
            }
            PinchPoint.PinchDirection.OUT ->{
                //calc degrees angle
                val angle1 =
                        atan2((pinchPoint.firstPoint.yTouch - pinchPoint.yTouch).toDouble(),
                                (pinchPoint.firstPoint.xTouch - pinchPoint.xTouch).toDouble())
                val angleD1 = angle1 * 180 / Math.PI
                val newXFirst = (pinchPoint.firstPoint.xTouch + cos(angle1) * -radius).toFloat()
                val newYFirst = (pinchPoint.firstPoint.yTouch + sin(angle1) * -radius).toFloat()

                arrowMatrixSwipeFirst.setRotate(angleD1.toFloat())
                arrowPath.transform(arrowMatrixSwipeFirst)
                arrowPath.offset(newXFirst - xCutoutPathHelper(), newYFirst + yCutout())
                canvas?.drawLine(pinchPoint.xTouch.toFloat() - xCutoutPathHelper(),
                        pinchPoint.yTouch.toFloat() + yCutout(),
                        newXFirst - xCutoutPathHelper(),
                        newYFirst + yCutout(),
                        paintLineToSwipePoint
                )

                val angle2 =
                        atan2((pinchPoint.secondPoint.yTouch - pinchPoint.yTouch).toDouble(),
                                (pinchPoint.secondPoint.xTouch - pinchPoint.xTouch).toDouble())
                val angle2D = angle2 * 180 / Math.PI;
                val newXSecond = (pinchPoint.secondPoint.xTouch + cos(angle2) * -radius).toFloat()
                val newYSecond = (pinchPoint.secondPoint.yTouch + sin(angle2) * -radius).toFloat()

                arrowMatrixSwipeSecond.setRotate(angle2D.toFloat())
                arrowPath2.transform(arrowMatrixSwipeSecond)
                arrowPath2.offset(newXSecond - xCutoutPathHelper(),
                        newYSecond + yCutout())
                canvas?.drawLine(pinchPoint.xTouch.toFloat() - xCutoutPathHelper(),
                        pinchPoint.yTouch.toFloat() + yCutout(),
                        newXSecond - xCutoutPathHelper(),
                        newYSecond + yCutout(),
                        paintLineToSwipePoint
                )
            }
        }
        // draw arrows path
        canvas?.drawPath(arrowPath, paintStandard)
        canvas?.drawPath(arrowPath2, paintStandard)
    }

    open fun initArrowDraw(p:Path, size:Float):Path{
        return with(p){
            lineTo(-size, -size)
            lineTo(35f, 0f)
            lineTo(-35f, 35f)
            close()
            this
        }
    }
}