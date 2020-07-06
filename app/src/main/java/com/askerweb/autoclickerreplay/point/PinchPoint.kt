package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.logd
import com.askerweb.autoclickerreplay.services.AutoClickService
import com.google.gson.JsonObject
import kotlin.math.ceil

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

    constructor(parcel: Parcel):super(parcel)

    constructor(json: JsonObject):super(json){
        val firstPointJson =
                AutoClickService.getGson().fromJson(json.get("nextPoint").asString, JsonObject::class.java)
        val firstPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, firstPointJson)
        this.firstPoint.x = firstPoint.x
        this.firstPoint.y = firstPoint.y
        this.firstPoint.height = ceil(firstPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.firstPoint.width = ceil(firstPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
        val secondPointJson =
                AutoClickService.getGson().fromJson(json.get("nextPoint").asString, JsonObject::class.java)
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
        obj.addProperty("firstPoint", firstPoint.toJson())
        obj.addProperty("secondPoint", secondPoint.toJson())
        return obj
    }

    override fun getCommand(): GestureDescription? {
        return typePinch.getCommand(xTouch.toFloat(), yTouch.toFloat(), firstPoint, secondPoint, delay, duration)
    }

    enum class PinchDirection{
        IN {
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


    class PinchOnTouchListener private constructor(point: PinchPoint,
                                                  wm: WindowManager,
                                                  canvas: PointCanvasView,
                                                  screenWidth:Int = canvas.measuredWidth,
                                                  screenHeight:Int = canvas.measuredHeight)
        : Point.PointOnTouchListener(point, wm, canvas, screenWidth, screenHeight){


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

        override var calcNewPositionAndSet = { xDiff:Int, yDiff:Int, v:View ->
            super.calcNewPositionAndSet(xDiff, yDiff, v)
            val paramFirstPoint = point.firstPoint.params
            val newXFirst = initialFirstX + xDiff
            val newYFirst = initialFirstY + yDiff
            paramFirstPoint.x = if((screenWidth < 0 && screenHeight < 0) || ((screenWidth > -1 && screenHeight > -1) && (newXFirst >= 0  && newXFirst <= screenWidth - point.firstPoint.view.width)))
                newXFirst else paramFirstPoint.x
            paramFirstPoint.y = if((screenWidth < 0 && screenHeight < 0) || ((screenWidth > -1 && screenHeight > -1) && (newYFirst >= 0  && newYFirst <= screenHeight - point.firstPoint.view.height)))
                newYFirst else paramFirstPoint.y
            val paramSecondPoint = point.secondPoint.params
            val newXSecond = initialSecondX + xDiff
            val newYSecond = initialSecondY + yDiff
            paramSecondPoint.x = if((screenWidth < 0 && screenHeight < 0) || ((screenWidth > -1 && screenHeight > -1) && (newXSecond >= 0  && newXSecond <= screenWidth - point.secondPoint.view.width)))
                newXSecond else paramSecondPoint.x
            paramSecondPoint.y = if((screenWidth < 0 && screenHeight < 0) || ((screenWidth > -1 && screenHeight > -1) && (newYSecond >= 0  && newYSecond <= screenHeight - point.secondPoint.view.height)))
                newYSecond else paramSecondPoint.y
        }

        companion object{
            @JvmStatic fun create(point: PinchPoint, wm:WindowManager, canvas:PointCanvasView, bounds:Boolean):PointOnTouchListener{
                return if (bounds)
                    PinchOnTouchListener(point, wm,canvas)
                else PinchOnTouchListener(point, wm, canvas, -1, -1)
            }
        }
    }

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

        override var calcNewPositionAndSet= { xDiff:Int, yDiff: Int, v:View ->
            super.calcNewPositionAndSet(xDiff, yDiff, v)
            val newMirrorX = initialMirrorX + (-xDiff)
            val newMirrorY = initialMirrorY + (-yDiff)
            mirrorPoint.x = if((screenWidth < 0 && screenHeight < 0) || ((screenWidth > -1 && screenHeight > -1) && (newMirrorX >= 0  && newMirrorX <= screenWidth - mirrorPoint.view.width)))
                newMirrorX else mirrorPoint.x
            mirrorPoint.y = if((screenWidth < 0 && screenHeight < 0) || ((screenWidth > -1 && screenHeight > -1) && (newMirrorY >= 0  && newMirrorY <= screenHeight - mirrorPoint.view.height)))
                newMirrorY else mirrorPoint.y
        }

        companion object{
            @JvmStatic fun create(point: Point, mirrorPoint: Point, wm:WindowManager, canvas:PointCanvasView, bounds:Boolean):PointOnTouchListener{
                return if (bounds)
                    ConnectMirrorTouchListener(mirrorPoint, point, wm,canvas)
                else ConnectMirrorTouchListener(mirrorPoint, point, wm, canvas, -1, -1)
            }
        }


    }
}