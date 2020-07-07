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
import com.askerweb.autoclickerreplay.point.view.*
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlin.math.ceil
import kotlinx.android.synthetic.main.pinch_dialog_elements.*

class PinchPoint:Point {

    val firstPoint = PointBuilder.invoke()
            .position(this.x + 200, this.y)
            .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.point_swap)!!)
            .build(SimplePoint::class.java)

    val secondPoint = PointBuilder.invoke()
            .position(this.x - 200, this.y)
            .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.point_swap)!!)
            .build(SimplePoint::class.java)

    var typePinch = PinchDirection.OUT

    override var drawableViewDefault: Drawable = ContextCompat.getDrawable(App.getContext(), R.drawable.point_solid)!!

    constructor(builder: PointBuilder):super(builder)

    constructor(parcel: Parcel):super(parcel){
        typePinch = parcel.readSerializable() as PinchDirection
        val firstPoint:SimplePoint = parcel.readParcelable(SimplePoint::class.java.classLoader)!!
        this.firstPoint.x = firstPoint.x
        this.firstPoint.y = firstPoint.y
        this.firstPoint.height = ceil(firstPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.firstPoint.width = ceil(firstPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
        val secondPoint:SimplePoint = parcel.readParcelable(SimplePoint::class.java.classLoader)!!
        this.secondPoint.x = secondPoint.x
        this.secondPoint.y = secondPoint.y
        this.secondPoint.height = ceil(secondPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.secondPoint.width = ceil(secondPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
    }

    constructor(json: JsonObject):super(json){
        typePinch = if(json.get("type").asString == PinchDirection.IN.name) PinchDirection.IN
                else PinchDirection.OUT
        val firstPointJson =
                App.getGson().fromJson(json.get("firstPoint").asString, JsonObject::class.java)
        val firstPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, firstPointJson)
        this.firstPoint.x = firstPoint.x
        this.firstPoint.y = firstPoint.y
        this.firstPoint.height = ceil(firstPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.firstPoint.width = ceil(firstPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
        val secondPointJson =
                App.getGson().fromJson(json.get("secondPoint").asString, JsonObject::class.java)
        val secondPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, secondPointJson)
        this.secondPoint.x = secondPoint.x
        this.secondPoint.y = secondPoint.y
        this.secondPoint.height = ceil(secondPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.secondPoint.width = ceil(secondPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
    }

    override var text: String
        get() = super.text
        set(value) {
            super.text = value
            if(firstPoint != null){
                firstPoint.text = "`$value"
            }
            if(secondPoint != null){
                secondPoint.text = "`$value"
            }
        }

    init{
        text = text
    }

    override fun updateViewLayout(wm: WindowManager, size: Float) {
        firstPoint.updateViewLayout(wm, size)
        secondPoint.updateViewLayout(wm, size)
        super.updateViewLayout(wm, size)
    }

    override fun attachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        super.attachToWindow(wm, canvas)
        firstPoint.attachToWindow(wm, canvas)
        secondPoint.attachToWindow(wm, canvas)
    }

    override fun detachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        super.detachToWindow(wm, canvas)
        firstPoint.detachToWindow(wm, canvas)
        secondPoint.detachToWindow(wm, canvas)
    }

    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        val l = PinchOnTouchListener.create(this, wm, canvas, bounds)
        view.setOnTouchListener(l)
        firstPoint.view.setOnTouchListener(ConnectMirrorTouchListener.create(firstPoint, secondPoint, wm, canvas, bounds))
        secondPoint.view.setOnTouchListener(ConnectMirrorTouchListener.create(secondPoint, firstPoint, wm, canvas, bounds))
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(PinchPoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }

    override fun toJsonObject(): JsonObject {
        val obj = super.toJsonObject()
        obj.addProperty("type", typePinch.name)
        obj.addProperty("firstPoint", firstPoint.toJson())
        obj.addProperty("secondPoint", secondPoint.toJson())
        return obj
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        dest?.writeSerializable(typePinch)
        dest?.writeParcelable(firstPoint, flags)
        dest?.writeParcelable(secondPoint, flags)
    }

    override fun getCommand(): GestureDescription? {
        return typePinch.getCommand(xTouch.toFloat(), yTouch.toFloat(), firstPoint, secondPoint, delay, duration)
    }

    override fun createViewDialog(): View {
        val vContent: ViewGroup = super.createViewDialog() as ViewGroup
        val vContentPinch = LayoutInflater.from(vContent.context)
                .inflate(R.layout.pinch_dialog_elements, null)
        vContent.addView(vContentPinch)
        return vContent
    }

    override fun createHolderDialog(viewContent: View): AbstractViewHolderDialog {
        val holder = super.createHolderDialog(viewContent)
        return ExtendedPinchDialog(holder, viewContent, this)
    }

    /**
     * Decorator for AbstractViewHolderDialog for PinchPoint
     * add new control for change direction PinchPoint
     */
    class ExtendedPinchDialog(dialogHolder: AbstractViewHolderDialog, override val containerView:View,
                              val point: PinchPoint) : ExtendedViewHolder(dialogHolder), LayoutContainer{

        override val expandableSave = {
            point.typePinch = if(directionPinch.isChecked) PinchDirection.IN else PinchDirection.OUT
        }

        override val expandableUpdate = {
            directionPinch.isChecked = when(point.typePinch){
                PinchDirection.IN -> true
                PinchDirection.OUT -> false
            }
        }
    }

    enum class PinchDirection{
        IN{
            override fun getCommand(xTouch:Float, yTouch:Float, firstPoint:Point, secondPoint:Point, delay:Long, duration:Long): GestureDescription? {
                val path = Path()
                path.moveTo(firstPoint.xTouch.toFloat(), firstPoint.yTouch.toFloat())
                path.lineTo(xTouch, yTouch)
                val path2 = Path()
                path2.moveTo(secondPoint.xTouch.toFloat(), secondPoint.yTouch.toFloat())
                path2.lineTo(xTouch, yTouch)
                val builder = GestureDescription.Builder()
                return builder
                        .addStroke(GestureDescription.StrokeDescription(path, delay, duration))
                        .addStroke(GestureDescription.StrokeDescription(path2, delay, duration))
                        .build()
            }
        },
        OUT{
            override fun getCommand(xTouch:Float, yTouch:Float, firstPoint:Point, secondPoint:Point, delay:Long, duration:Long): GestureDescription? {
                val path = Path()
                path.moveTo(xTouch, yTouch)
                path.lineTo(firstPoint.xTouch.toFloat(), firstPoint.yTouch.toFloat())
                val path2 = Path()
                path2.moveTo(xTouch, yTouch)
                path2.lineTo(secondPoint.xTouch.toFloat(), secondPoint.yTouch.toFloat())
                val builder = GestureDescription.Builder()
                return builder
                        .addStroke(GestureDescription.StrokeDescription(path, delay, duration))
                        .addStroke(GestureDescription.StrokeDescription(path2, delay, duration))
                        .build()
            }
        };
        abstract fun getCommand(xTouch:Float, yTouch:Float, firstPoint:Point, secondPoint:Point, delay:Long, duration:Long): GestureDescription?
    }


}