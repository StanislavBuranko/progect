package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.Application
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.Dimension
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.logd
import com.askerweb.autoclickerreplay.services.AutoClickService
import com.google.gson.JsonObject
import kotlin.math.ceil

class SwipePoint : Point {
   val nextPoint: Point =  PointBuilder.invoke()
            .position(this.x + 200, this.y)
            .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.point_swap)!!)
            .build(SimplePoint::class.java)


    override var text: String
        get() = super.text
        set(value) {
            super.text = value
            if(nextPoint != null){
                nextPoint.text = "`$value"
            }
        }

    init{
        text = text
    }

    constructor(builder: PointBuilder): super(builder)

    constructor(parcel: Parcel):super(parcel)

    constructor(json: JsonObject):super(json){
        val nextPointJson =
                AutoClickService.getGson().fromJson(json.get("nextPoint").asString, JsonObject::class.java)
        val nextPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, nextPointJson)
        "$nextPoint".logd()
        this.nextPoint.x = nextPoint.x
        this.nextPoint.y = nextPoint.y
        this.nextPoint.height = ceil(nextPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.nextPoint.width = ceil(nextPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(SwipePoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }


    override var drawableViewDefault: Drawable = ContextCompat.getDrawable(App.getContext(), R.drawable.point_swap)!!

    override fun updateViewLayout(wm: WindowManager, size: Float) {
        nextPoint.updateViewLayout(wm, size)
        super.updateViewLayout(wm, size)
    }

    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        super.updateListener(wm, canvas, bounds)
        nextPoint.updateListener(wm,canvas,bounds)
    }

    override fun attachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        super.attachToWindow(wm, canvas)
        nextPoint.attachToWindow(wm, canvas)
    }

    override fun detachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        super.detachToWindow(wm, canvas)
        nextPoint.detachToWindow(wm, canvas)
    }

    override fun toJsonObject():JsonObject{
        val obj = super.toJsonObject()
        obj.addProperty("nextPoint", nextPoint.toJson())
        return obj
    }

    override fun getCommand() : GestureDescription {
        "swipe from $xTouch $yTouch to ${nextPoint.xTouch} ${nextPoint.yTouch}".logd()
        val path = Path()
        path.moveTo(xTouch.toFloat(), yTouch.toFloat())
        path.lineTo(nextPoint.xTouch.toFloat(), nextPoint.yTouch.toFloat())
        val builder = GestureDescription.Builder()
        return builder
                .addStroke(GestureDescription.StrokeDescription(path, delay, duration))
                .build()
    }




}