package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getWindowsParameterLayout
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonObject

class PathPoint : Point {

    var wasDraw = false

    val path = Path()
    val endPoint = PointBuilder.invoke()
            .position(x,y)
            .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_swap)!!)
            .build(SimplePoint::class.java)

    val panel = LinearLayout(appContext)
    val panelParam = getWindowsParameterLayout(
            WindowManager.LayoutParams.MATCH_PARENT.toFloat(),
            WindowManager.LayoutParams.MATCH_PARENT.toFloat(),
            Gravity.CENTER)

    override var drawableViewDefault: Drawable = ContextCompat.getDrawable(appContext, R.drawable.point_solid)!!

    constructor(builder: PointBuilder): super(builder)

    constructor(parcel: Parcel):super(parcel){

    }

    constructor(json: JsonObject):super(json){

    }

    init{
        panel.setOnTouchListener(DrawPathOnTouchListener())
    }

    override fun attachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        if(!wasDraw) wm.addView(panel, panelParam)
        else{
            super.attachToWindow(wm, canvas)
            endPoint.attachToWindow(wm, canvas)
        }
    }

    override fun detachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        if(!wasDraw) wm.removeView(panel)
        else{
            super.detachToWindow(wm, canvas)
            endPoint.detachToWindow(wm, canvas)
        }
    }

    override fun updateViewLayout(wm: WindowManager, size: Float) {
        endPoint.updateViewLayout(wm, size)
        super.updateViewLayout(wm, size)
    }

    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        super.updateListener(wm, canvas, bounds)
        endPoint.updateListener(wm, canvas, bounds)
        panel.setOnTouchListener(DrawPathOnTouchListener())
    }

    override fun getCommand(): GestureDescription? {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(PathPoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<PathPoint?> {
            return arrayOfNulls(size)
        }
    }


    inner class DrawPathOnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when(event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_DOWN->{
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                    path.moveTo(x.toFloat(), y.toFloat())
                }
                MotionEvent.ACTION_UP->{
                    detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                    wasDraw = true
                    endPoint.x = event.rawX.toInt()
                    endPoint.y = event.rawY.toInt()
                    attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                }
                MotionEvent.ACTION_MOVE->{
                    path.lineTo(event.rawX, event.rawY)
                    AutoClickService.getCanvas().invalidate()
                }
            }
            return true
        }

    }

}