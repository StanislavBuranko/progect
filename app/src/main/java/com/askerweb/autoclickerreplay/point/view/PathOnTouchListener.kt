package com.askerweb.autoclickerreplay.point.view

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.hardware.SensorManager
import android.provider.Settings.System.getConfiguration
import android.view.View
import android.view.WindowManager
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.context
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.PathPoint
import com.askerweb.autoclickerreplay.service.AutoClickService


class PathOnTouchListener private constructor(point: PathPoint,
                                              wm: WindowManager,
                                              canvas: PointCanvasView,
                                              screenWidth:Int = canvas.measuredWidth,
                                              screenHeight:Int = canvas.measuredHeight)
    : PointOnTouchListener(point, wm, canvas, screenWidth, screenHeight){


    private var initialEndPointX: Int = 0
    private var initialEndPointY: Int = 0
    private var initialFirstPointX: Int = 0
    private var initialFirstPointY: Int = 0

    override var initPositionTouch = { x:Float, y:Float ->
        super.initPositionTouch(x,y);
        initialFirstPointX = point.x
        initialFirstPointY = point.y
        initialEndPointX = point.endPoint.x
        initialEndPointY = point.endPoint.y

    }

    override var updateView = {
        wm.updateViewLayout(point.view, point.params)
        wm.updateViewLayout(point.endPoint.view, point.endPoint.params)
        canvas.invalidate()
    }

    var chekOffset: Boolean = false
    var pointLocateHelper  = 0 ;
    override var calcNewPositionAndSet = { xDiff:Int, yDiff:Int, v: View ->
        var xTemp = x
        var yTemp = y
        super.calcNewPositionAndSet(xDiff, yDiff, v)
        val newXEnd =initialEndPointX + xDiff
        val newYEnd = initialEndPointY + yDiff
        point.endPoint.x = if(canMoveX(newXEnd, point.endPoint.width)) newXEnd
        else point.endPoint.x
        point.endPoint.y = if(canMoveY(newYEnd, point.endPoint.height)) newYEnd
        else point.endPoint.y
        context.resources.configuration.orientation.logd()
        if (Configuration.ORIENTATION_PORTRAIT == context.resources.configuration.orientation && point.isFirstSwap) {
            for(n in 0..point.coordinateXMove.size-1){
                point.coordinateYMove[n] -= (yTemp-(initialY + yDiff)).toFloat()
                point.coordinateXMove[n] -= (xTemp-(initialX + xDiff)).toFloat()
            }
            point.path.reset()
            point.path.moveTo(point.coordinateXMove[0], point.coordinateYMove[0])
            for (n in 1.. point.coordinateXMove.size - 1) {
                point.path.lineTo(point.coordinateXMove[n], point.coordinateYMove[n])
            }
        } else if (Configuration.ORIENTATION_PORTRAIT == context.resources.configuration.orientation && !point.isFirstSwap) {
            for(n in 0..point.coordinateXMove.size-1){
                point.coordinateYMove[n] -= (xTemp-x).toFloat()
                point.coordinateXMove[n] -= (yTemp-y).toFloat()
            }
            point.path.reset()
            point.path.moveTo(point.coordinateYMove[0], point.coordinateXMove[0])
            for (n in 1.. point.coordinateXMove.size - 1) {

                point.path.lineTo(point.coordinateYMove[n], point.coordinateXMove[n])
            }
        } else if(Configuration.ORIENTATION_LANDSCAPE == context.resources.configuration.orientation && !point.isFirstSwap) {
            for(n in 0..point.coordinateXMove.size-1){
                point.coordinateYMove[n] -= (xTemp-x).toFloat()
                point.coordinateXMove[n] -= (yTemp-y).toFloat()
            }
            point.path.reset()
            point.path.moveTo(point.coordinateYMove[0], point.coordinateXMove[0])
            for (n in 1.. point.coordinateXMove.size - 1) {

                point.path.lineTo(point.coordinateYMove[n], point.coordinateXMove[n])
            }
        } else if(Configuration.ORIENTATION_LANDSCAPE == context.resources.configuration.orientation && point.isFirstSwap) {
            for(n in 0..point.coordinateXMove.size-1){
                point.coordinateYMove[n] -= (yTemp-(initialY + yDiff)).toFloat()
                point.coordinateXMove[n] -= (xTemp-(initialX + xDiff)).toFloat()
            }
            point.path.reset()
            point.path.moveTo(point.coordinateXMove[0], point.coordinateYMove[0])
            for (n in 1.. point.coordinateXMove.size - 1) {
                point.path.lineTo(point.coordinateXMove[n], point.coordinateYMove[n])
            }
        }

    }

    companion object{
        @JvmStatic fun create(point: PathPoint, wm: WindowManager, canvas: PointCanvasView, bounds:Boolean): PointOnTouchListener {
            return if (bounds)
                PathOnTouchListener(point, wm, canvas)
            else PathOnTouchListener(point, wm, canvas, -1, -1)
        }
    }
}