package com.askerweb.autoclickerreplay.point.view

import android.content.res.Resources
import android.graphics.Path
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.askerweb.autoclickerreplay.ktExt.getWindowsParameterLayout
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

    override var initPositionTouch = { x:Float, y:Float ->
        super.initPositionTouch(x,y)
        initialEndPointX = point.endPoint.x
        initialEndPointY = point.endPoint.y
    }

    override var updateView = {
        wm.updateViewLayout(point.view, point.params)
        wm.updateViewLayout(point.endPoint.view, point.endPoint.params)
        canvas.invalidate()
    }
    var pointLocateHelper  = 0 ;
    override var calcNewPositionAndSet = { xDiff:Int, yDiff:Int, v: View ->
        val paramPoint = point.params
        val newXPoint = super.initialX + xDiff
        val newYPoint = super.initialY + yDiff
        paramPoint.x = if(canMoveX(newXPoint, point.width)) newXPoint
        else paramPoint.x
        paramPoint.y = if(canMoveY(newYPoint, point.height)) newYPoint
        else paramPoint.y





        if(AutoClickService.getParamSizePoint() == 32)
            pointLocateHelper = 37;
        else if(AutoClickService.getParamSizePoint() == 40)
            pointLocateHelper = 50;
        else if(AutoClickService.getParamSizePoint() == 56)
            pointLocateHelper = 75;
        var chekOffset = false;
        for (n in 0..point.coordinateXMove.size-1) {
            if(canMoveX(point.coordinateXMove[n], point.endPoint.width)){
            }
            else{
                chekOffset = true;
            }
            if(canMoveY(point.coordinateYMove[n], point.endPoint.height)){
            }
            else{
                chekOffset = true;
            }
        }
        if(!chekOffset) {
            val xTemp = point.x
            val yTemp = point.y

            val paramPoint = point.params
            val newXPoint = initialX + xDiff
            val newYPoint = initialY + yDiff
            paramPoint.x = if(canMoveX(newXPoint, point.width)) newXPoint
            else paramPoint.x
            paramPoint.y = if(canMoveY(newYPoint, point.height)) newYPoint
            else paramPoint.y

            val paramEndPoint = point.endPoint.params
            val newXEndPoint = initialEndPointX + xDiff
            val newYEndPoint = initialEndPointY + yDiff
            paramEndPoint.x = if(canMoveX(newXEndPoint, point.endPoint.width)) newXEndPoint
            else paramEndPoint.x
            paramEndPoint.y = if(canMoveY(newYEndPoint, point.endPoint.height)) newYEndPoint
            else paramEndPoint.y

            for (n in 0..point.coordinateXMove.size-1){
                point.coordinateXMove[n] -= (xTemp-point.x)
                point.coordinateYMove[n] -= (yTemp-point.y)
            }
            //point.path.offset()
            point.path.reset()
            var isFirstPointParcel = true
            for (n in 0..point.coordinateXMove.size-1){
                if(isFirstPointParcel) {
                    point.path.moveTo(point.coordinateXMove[n].toFloat(), point.coordinateYMove[n].toFloat())
                    isFirstPointParcel = false
                }
                else
                    point.path.lineTo(point.coordinateXMove[n].toFloat(), point.coordinateYMove[n].toFloat())
            }

            point.x =  point.coordinateXMove[0] - pointLocateHelper
            point.y =  point.coordinateYMove[0] - pointLocateHelper
            point.endPoint.x =  point.coordinateXMove.last() - pointLocateHelper
            point.endPoint.y =  point.coordinateYMove.last() - pointLocateHelper
        }
    }


    /*override var calcNewPositionAndSet = { xDiff:Int, yDiff:Int, v: View ->
        var xTempSuper = super.x
        var yTempSuper = super.y
        var xTempEndPoint = point.endPoint.x
        var yTempEndPoint = point.endPoint.y
        var screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels
        var screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels
        var checkCoordinate = false
        var checkPlusCoordinate = false
        var checkMinusCoordinate = false
        "${point.coordinateXMove.size}".logd("SizeArray")
        "${screenHeight}".logd("screenHeight")
        "${screenWidth}".logd("screenWidth")
        "${point.coordinateYMove[1]+point.offsetY}".logd("coordinateYMove[n]")
        "${point.coordinateXMove[1]+point.offsetX}".logd("coordinateXMove[n]")
        "${point.offsetX}".logd("offsetX")
        "${point.offsetY}".logd("offsetY")
        "${x}".logd("X")
        "${y}".logd("Y")

        super.calcNewPositionAndSet(xDiff, yDiff, v)
        point.offsetX += (xTempSuper - super.x)
        point.offsetY += (yTempSuper - super.y)
        point.endPoint.x -= (xTempSuper - super.x)
        point.endPoint.y -= (yTempSuper - super.y)

        for(n in 0..point.coordinateXMove.size-1) {
            if(screenHeight-10 < point.coordinateYMove[n]+point.offsetY) {
                point.y -= 50
                point.endPoint.y -= 50
                for (n in 0..point.coordinateYMove.size-1)
                {
                    point.coordinateYMove[n] -= 50;
                }
                checkCoordinate = true
                break;
            }
            if(point.coordinateYMove[n]-point.offsetY < 0) {
                point.y += 50
                point.endPoint.y += 50
                for (n in 0..point.coordinateYMove.size-1)
                {
                    point.coordinateYMove[n] += 50;
                }
                checkCoordinate = true
                break;
            }
            if(screenWidth-10 < point.coordinateXMove[n]+point.offsetX) {
                point.x -= 50
                point.endPoint.x -= 50
                for (n in 0..point.coordinateXMove.size-1)
                {
                    point.coordinateXMove[n] -= 50;
                }
                checkCoordinate = true
                break;
            }
            if(point.coordinateXMove[n]-point.offsetX < 0) {
                point.x += 50
                point.endPoint.x += 50
                for (n in 0..point.coordinateXMove.size-1)
                {
                    point.coordinateXMove[n] += 50;
                }
                checkCoordinate = true
                break;
            }
        }

        if (checkCoordinate == true) {
            point.path.reset()
            var isFirstPointParcel = true
            for (n in 0..point.coordinateXMove.size-1){
                "${point.coordinateXMove[n]}".logd("parcelXRead")
                "${point.coordinateYMove[n]}".logd("parcelYRead")
                if(isFirstPointParcel) {
                    point.path.moveTo(point.coordinateXMove[n].toFloat(), point.coordinateYMove[n].toFloat())
                    isFirstPointParcel = false
                }
                else
                    point.path.lineTo(point.coordinateXMove[n].toFloat(), point.coordinateYMove[n].toFloat())
            }

            point.x = point.coordinateXMove[0];
            point.y = point.coordinateYMove[0];
            point.endPoint.x = point.coordinateXMove.last();
            point.endPoint.y = point.coordinateYMove.last();
        }
        checkCoordinate = false
    }*/


    companion object{
        @JvmStatic fun create(point: PathPoint, wm: WindowManager, canvas: PointCanvasView, bounds:Boolean, path: Path, coordinateXMove: Array<Int>, coordinateYMove: Array<Int>): PointOnTouchListener {
            return if (bounds)
                PathOnTouchListener(point, wm, canvas)
            else PathOnTouchListener(point, wm, canvas, -1, -1)
        }
    }
}