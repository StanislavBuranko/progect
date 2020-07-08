package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog
import com.askerweb.autoclickerreplay.point.view.DataTouch
import com.askerweb.autoclickerreplay.point.view.ExtendedViewHolder
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlin.math.ceil
import kotlinx.android.synthetic.main.swipe_dialog_elements.*

class SwipePoint : Point {
   val nextPoint: Point =  PointBuilder.invoke()
            .position(DataTouch.xUp, DataTouch.yUp)
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

    constructor(parcel: Parcel):super(parcel){
        val nextPoint:SimplePoint = parcel.readParcelable(SimplePoint::class.java.classLoader)!!
        this.nextPoint.x = nextPoint.x
        this.nextPoint.y = nextPoint.y
        this.nextPoint.height = ceil(nextPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.nextPoint.width = ceil(nextPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
    }

    constructor(json: JsonObject):super(json){
        val nextPointJson =
                App.getGson().fromJson(json.get("nextPoint").asString, JsonObject::class.java)
        val nextPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, nextPointJson)
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

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        dest?.writeParcelable(nextPoint, flags)
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

    override fun createHolderDialog(viewContent: View): AbstractViewHolderDialog {
        val holder = super.createHolderDialog(viewContent)
        return ExtendedSwipeDialog(holder, viewContent, this)
    }

    override fun createViewDialog(): View {
        val vContent = super.createViewDialog() as ViewGroup
        val vSwipePoint = LayoutInflater.from(vContent.context)
                .inflate(R.layout.swipe_dialog_elements, null)
        vContent.addView(vSwipePoint)
        return vContent
    }

    /**
     * Decorator for AbstractViewHolderDialog for SwipePoint
     * add new button to swipe SwipePoint
     */
    class ExtendedSwipeDialog(dialogHolder: AbstractViewHolderDialog, override val containerView: View,
                              val point: SwipePoint) : ExtendedViewHolder(dialogHolder), LayoutContainer {

        init{
            swipeBtn.setOnClickListener { //change position nextPoint and this point
                val xTemp = point.nextPoint.x
                val yTemp = point.nextPoint.y
                point.nextPoint.x = point.x
                point.nextPoint.y = point.y
                point.x = xTemp
                point.y = yTemp
                AutoClickService.getWM().updateViewLayout(point.view, point.params)
                AutoClickService.getWM().updateViewLayout(point.nextPoint.view, point.nextPoint.params)
                dialogHolder.dialog?.cancel()
            }
        }
    }
}