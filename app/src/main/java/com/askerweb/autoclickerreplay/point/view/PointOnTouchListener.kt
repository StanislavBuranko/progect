package com.askerweb.autoclickerreplay.point.view

import android.view.WindowManager
import com.askerweb.autoclickerreplay.point.Point

open class PointOnTouchListener protected constructor(private val point: Point,
                                                      wm: WindowManager,
                                                      canvas: PointCanvasView,
                                                      screenWidth:Int = canvas.measuredWidth,
                                                      screenHeight:Int = canvas.measuredHeight) :
        OnTouchListener(wm, screenWidth, screenHeight){

    override var updateView = {
            wm.updateViewLayout(point.view, point.params)
            canvas.invalidate()
    }

    override var x: Int
        get() = point.x
        set(value) {
            point.x = value
        }

    override var y: Int
        get() = point.y
        set(value) {
            point.y = value
        }

    companion object{
        @JvmStatic fun create(point: Point, wm: WindowManager, canvas: PointCanvasView, bounds:Boolean): PointOnTouchListener {
            return if (bounds)
                PointOnTouchListener(point, wm, canvas)
            else PointOnTouchListener(point, wm, canvas, -1, -1)
        }
    }
}