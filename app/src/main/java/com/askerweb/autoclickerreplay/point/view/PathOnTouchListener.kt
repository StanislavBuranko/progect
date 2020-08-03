package com.askerweb.autoclickerreplay.point.view

import android.content.res.Resources
import android.graphics.Path
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.askerweb.autoclickerreplay.ktExt.getWindowsParameterLayout
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.PathPoint


class PathOnTouchListener private constructor(point: PathPoint,
                                              wm: WindowManager,
                                              canvas: PointCanvasView,
                                              screenWidth:Int = canvas.measuredWidth,
                                              screenHeight:Int = canvas.measuredHeight,
                                              path: Path,
                                              coordinateXMove: Array<Int>,
                                              coordinateYMove: Array<Int>)
    : PointOnTouchListener(point, wm, canvas, screenWidth, screenHeight){


    private var initialFirstX: Int = 0
    private var initialFirstY: Int = 0
    private var initialSecondX: Int = 0
    private var initialSecondY: Int = 0

    override var initPositionTouch = { x:Float, y:Float ->
        super.initPositionTouch(x,y)
        initialFirstX = point.x
        initialFirstY = point.y
        initialSecondX = point.endPoint.x
        initialSecondY = point.endPoint.y
    }

    override var updateView = {
        wm.updateViewLayout(point.view, point.params)
        wm.updateViewLayout(point.endPoint.view, point.endPoint.params)
        canvas.invalidate()
    }

    override var calcNewPositionAndSet = { xDiff:Int, yDiff:Int, v: View ->
        var xTempSuper = super.x
        var yTempSuper = super.y
        var xTempEndPoint = point.endPoint.x
        var yTempEndPoint = point.endPoint.y
        var screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels
        var screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels
        var checkCoordinate = false
        var checkPlusCoordinate = false
        var checkMinusCoordinate = false
        "${coordinateXMove.size}".logd("SizeArray")
        "${screenHeight}".logd("screenHeight")
        "${screenWidth}".logd("screenWidth")
        "${coordinateYMove[1]+point.offsetY}".logd("coordinateYMove[n]")
        "${coordinateXMove[1]+point.offsetX}".logd("coordinateXMove[n]")
        "${point.offsetX}".logd("offsetX")
        "${point.offsetY}".logd("offsetY")
        "${x}".logd("X")
        "${y}".logd("Y")

        super.calcNewPositionAndSet(xDiff, yDiff, v)
        point.offsetX += (xTempSuper - super.x)
        point.offsetY += (xTempSuper - super.x)
        point.endPoint.x -= (xTempSuper - super.x)
        point.endPoint.y -= (yTempSuper - super.y)
        path.offset(-(xTempSuper - super.x).toFloat(), -(yTempSuper - super.y).toFloat())
        checkPlusCoordinate = false
        checkMinusCoordinate = false

        for(n in 0..coordinateXMove.size-1) {
            if(screenHeight-10 < coordinateYMove[n]+point.offsetY) {
                point.y -= 50
                point.endPoint.y -= 50
                path.offset(0f, -50f)
                checkCoordinate = true
            }
            if(coordinateYMove[n]+point.offsetY < 0) {
                point.y -= 50
                point.endPoint.y -= 50
                path.offset(0f, -50f)
                checkCoordinate = true
            }
            /*if(0 > coordinateYMove[n]-point.offsetY) {
                point.y += 50
                point.endPoint.y += 50
                path.offset(0f, +50f)
                checkCoordinate = true
            }
            if(screenWidth-10 <= coordinateXMove[n]-point.offsetX && screenWidth+100 > coordinateXMove[n]-point.offsetX) {
                point.x -= 50
                point.endPoint.x -= 50
                path.offset(-50f, 0f)
                checkCoordinate = true
            }
            if(0 > coordinateXMove[n]-point.offsetX) {
                point.x += 50
                point.endPoint.x += 50
                path.offset(50f, 0f)
                checkCoordinate = true
            }*/
        }

        if (checkCoordinate == false) {

        }
        checkCoordinate = false
    }


    companion object{
        @JvmStatic fun create(point: PathPoint, wm: WindowManager, canvas: PointCanvasView, bounds:Boolean, path: Path, coordinateXMove: Array<Int>, coordinateYMove: Array<Int>): PointOnTouchListener {
            return if (bounds)
                PathOnTouchListener(point, wm, canvas, -1, -1, path, coordinateXMove, coordinateYMove)
            else PathOnTouchListener(point, wm, canvas, -1, -1, path, coordinateXMove, coordinateYMove)
        }
    }
}