package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getNavigationBar
import com.askerweb.autoclickerreplay.point.view.*
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlin.math.ceil
import kotlinx.android.synthetic.main.pinch_dialog_elements.*

class PinchPoint:Point {

    override val drawableViewDefault = ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_pinch)!!

    val firstPoint = PointBuilder.invoke()
            .position(this.x + 200, this.y)
            .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
            .build(SimplePoint::class.java)

    val secondPoint = PointBuilder.invoke()
            .position(this.x - 200, this.y)
            .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
            .build(SimplePoint::class.java)

    var typePinch = PinchDirection.OUT

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
                gson.fromJson(json.get("firstPoint").asString, JsonObject::class.java)
        val firstPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, firstPointJson)
        this.firstPoint.x = firstPoint.x
        this.firstPoint.y = firstPoint.y
        this.firstPoint.height = ceil(firstPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.firstPoint.width = ceil(firstPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
        val secondPointJson =
                gson.fromJson(json.get("secondPoint").asString, JsonObject::class.java)
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
                firstPoint.text = value
            }
            if(secondPoint != null){
                secondPoint.text = value
            }
        }

    init{
        text = text
        view.textVisible = false
    }

    override fun setVisible(visible: Int) {
        super.setVisible(visible)
        firstPoint.setVisible(visible)
        secondPoint.setVisible(visible)
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

    override fun setTouchable(touchable: Boolean, wm:WindowManager){
        if(touchable){
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            view.isClickable = true
            firstPoint.params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            firstPoint.view.isClickable = true
            secondPoint.params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            secondPoint.view.isClickable = true
        }
        else{
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            view.isClickable = false
            firstPoint.params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            firstPoint.view.isClickable = false
            secondPoint.params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            secondPoint.view.isClickable = false
        }
        wm.updateViewLayout(view, params)
        wm.updateViewLayout(firstPoint.view, firstPoint.params)
        wm.updateViewLayout(secondPoint.view, secondPoint.params)
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
        vContent.findViewById<LinearLayout>(R.id.content)
                .addView(vContentPinch)
        return vContent
    }

    override fun createHolderDialog(viewContent: View): AbstractViewHolderDialog {
        val holder = super.createHolderDialog(viewContent)
        return ExtendedPinchDialog(holder, viewContent, this)
    }

    override fun swapPointOrientation() {
        super.swapPointOrientation()
        firstPoint.swapPointOrientation()
        secondPoint.swapPointOrientation()
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
                path.moveTo(firstPoint.xTouch.toFloat()+getNavigationBar(), firstPoint.yTouch.toFloat())
                path.lineTo(xTouch+getNavigationBar(), yTouch)
                val path2 = Path()
                path2.moveTo(secondPoint.xTouch.toFloat()+getNavigationBar(), secondPoint.yTouch.toFloat())
                path2.lineTo(xTouch+getNavigationBar(), yTouch)
                val builder = GestureDescription.Builder()
                builder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                        .addStroke(GestureDescription.StrokeDescription(path2, 0, duration))
                return builder.build()

            }
        },
        OUT{
            override fun getCommand(xTouch:Float, yTouch:Float, firstPoint:Point, secondPoint:Point, delay:Long, duration:Long): GestureDescription? {
                val path = Path()
                path.moveTo(xTouch+getNavigationBar(), yTouch)
                path.lineTo(firstPoint.xTouch.toFloat()+getNavigationBar(), firstPoint.yTouch.toFloat())
                val path2 = Path()
                path2.moveTo(xTouch+getNavigationBar(), yTouch)
                path2.lineTo(secondPoint.xTouch.toFloat()+getNavigationBar(), secondPoint.yTouch.toFloat())
                val builder = GestureDescription.Builder()
                builder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                        .addStroke(GestureDescription.StrokeDescription(path2, 0, duration))
                return builder.build()
            }
        };
        abstract fun getCommand(xTouch:Float, yTouch:Float, firstPoint:Point, secondPoint:Point, delay:Long, duration:Long): GestureDescription?
    }


}