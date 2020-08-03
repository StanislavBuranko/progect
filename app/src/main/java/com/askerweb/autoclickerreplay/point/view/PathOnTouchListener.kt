package com.askerweb.autoclickerreplay.point.view

import android.graphics.Path
import android.view.View
import android.view.WindowManager
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.PathPoint
import com.askerweb.autoclickerreplay.point.PinchPoint

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
        var xTemp = super.x
        var yTemp = super.y
        super.calcNewPositionAndSet(xDiff, yDiff, v)
        point.offsetX += xTemp - super.x
        point.offsetY += yTemp - super.y
        point.endPoint.x -=  (xTemp - super.x)
        point.endPoint.y -=  (yTemp - super.y)
        path.offset(-(xTemp - super.x).toFloat(), -(yTemp - super.y).toFloat())
/*        "${coordinateXMove.size}".logd("CalcNewPos")
        for (n in 0..coordinateXMove.size-1) {
            coordinateXMove[n] = +(xTemp - super.x)
            coordinateYMove[n] = -(yTemp - super.y)
            "${coordinateXMove[n]}".logd("CalcNewPos")
        }*/
    }

    companion object{
        @JvmStatic fun create(point: PathPoint, wm: WindowManager, canvas: PointCanvasView, bounds:Boolean, path: Path, coordinateXMove: Array<Int>, coordinateYMove: Array<Int>): PointOnTouchListener {
            return if (bounds)
                PathOnTouchListener(point, wm, canvas, -1, -1, path, coordinateXMove, coordinateYMove)
            else PathOnTouchListener(point, wm, canvas, -1, -1, path, coordinateXMove, coordinateYMove)
        }
    }
}