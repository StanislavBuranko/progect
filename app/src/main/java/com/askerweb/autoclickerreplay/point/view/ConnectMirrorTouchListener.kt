package com.askerweb.autoclickerreplay.point.view

import android.view.View
import android.view.WindowManager
import com.askerweb.autoclickerreplay.point.Point

class ConnectMirrorTouchListener private constructor(private val mirrorPoint: Point,
                                                     point: Point,
                                                     wm: WindowManager,
                                                     canvas: PointCanvasView,
                                                     screenWidth:Int = canvas.measuredWidth,
                                                     screenHeight:Int = canvas.measuredHeight)
    : PointOnTouchListener(point, wm, canvas, screenWidth, screenHeight){

    var initialMirrorX = 0
    var initialMirrorY = 0

    override var initPositionTouch = { x:Float, y:Float ->
        super.initPositionTouch(x,y)
        initialMirrorX = mirrorPoint.x
        initialMirrorY = mirrorPoint.y
    }

    override var updateView = {
        wm.updateViewLayout(point.view, point.params);
        wm.updateViewLayout(mirrorPoint.view, mirrorPoint.params);
        canvas.invalidate()
    }

    override var calcNewPositionAndSet= { xDiff:Int, yDiff: Int, v: View ->
        val newMirrorX = initialMirrorX + (-xDiff)
        val newMirrorY = initialMirrorY + (-yDiff)
        mirrorPoint.x = if (canMoveX(newMirrorX, mirrorPoint.width)) newMirrorX
            else mirrorPoint.x
        mirrorPoint.y = if (canMoveY(newMirrorY, mirrorPoint.height)) newMirrorY
            else mirrorPoint.y
        super.calcNewPositionAndSet(xDiff, yDiff, v)
    }

    companion object{
        @JvmStatic fun create(point: Point, mirrorPoint: Point, wm: WindowManager, canvas: PointCanvasView, bounds:Boolean): PointOnTouchListener {
            return if (bounds)
                ConnectMirrorTouchListener(mirrorPoint, point, wm, canvas)
            else ConnectMirrorTouchListener(mirrorPoint, point, wm, canvas, -1, -1)
        }
    }


}