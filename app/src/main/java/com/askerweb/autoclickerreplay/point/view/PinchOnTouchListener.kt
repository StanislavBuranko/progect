package com.askerweb.autoclickerreplay.point.view

import android.view.View
import android.view.WindowManager
import com.askerweb.autoclickerreplay.point.PinchPoint

class PinchOnTouchListener private constructor(point: PinchPoint,
                                               wm: WindowManager,
                                               canvas: PointCanvasView,
                                               screenWidth:Int = canvas.measuredWidth,
                                               screenHeight:Int = canvas.measuredHeight)
    : PointOnTouchListener(point, wm, canvas, screenWidth, screenHeight){


    private var initialFirstX: Int = 0
    private var initialFirstY: Int = 0
    private var initialSecondX: Int = 0
    private var initialSecondY: Int = 0

    override var initPositionTouch = { x:Float, y:Float ->
        super.initPositionTouch(x,y)
        initialFirstX = point.firstPoint.x
        initialFirstY = point.firstPoint.y
        initialSecondX = point.secondPoint.x
        initialSecondY = point.secondPoint.y
    }

    override var updateView = {
        wm.updateViewLayout(point.view, point.params)
        wm.updateViewLayout(point.firstPoint.view, point.firstPoint.params)
        wm.updateViewLayout(point.secondPoint.view, point.secondPoint.params)
        canvas.invalidate()
    }

    override var calcNewPositionAndSet = { xDiff:Int, yDiff:Int, v: View ->
        super.calcNewPositionAndSet(xDiff, yDiff, v)
        val paramFirstPoint = point.firstPoint.params
        val newXFirst = initialFirstX + xDiff
        val newYFirst = initialFirstY + yDiff
        paramFirstPoint.x = if(canMoveX(newXFirst, point.firstPoint.width)) newXFirst
            else paramFirstPoint.x
        paramFirstPoint.y = if(canMoveY(newYFirst, point.firstPoint.height)) newYFirst
            else paramFirstPoint.y
        val paramSecondPoint = point.secondPoint.params
        val newXSecond = initialSecondX + xDiff
        val newYSecond = initialSecondY + yDiff
        paramSecondPoint.x = if(canMoveX(newXSecond, point.secondPoint.width)) newXSecond
            else paramSecondPoint.x
        paramSecondPoint.y = if(canMoveY(newYSecond, point.secondPoint.height)) newYSecond
            else paramSecondPoint.y
    }

    companion object{
        @JvmStatic fun create(point: PinchPoint, wm: WindowManager, canvas: PointCanvasView, bounds:Boolean): PointOnTouchListener {
            return if (bounds)
                PinchOnTouchListener(point, wm, canvas)
            else PinchOnTouchListener(point, wm, canvas, -1, -1)
        }
    }
}