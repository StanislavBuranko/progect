package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getWindowsParameterLayout
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.view.*
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_setting_point.*
import kotlinx.android.synthetic.main.swipe_dialog_elements.*
import java.io.Serializable
import java.util.*
import kotlin.math.ceil
import kotlin.math.log

class PathPoint : Point {

    public var coordinateXMove: Array<Int> = arrayOf()
    public var coordinateYMove: Array<Int> = arrayOf()
    var wasDraw = false

    var path = Path()
    val endPoint = PointBuilder.invoke()
            .position(x,y)
            .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
            .build(SimplePoint::class.java)

    val panel = LinearLayout(AutoClickService.getService().applicationContext)
    val panelParam = getWindowsParameterLayout(
            WindowManager.LayoutParams.MATCH_PARENT.toFloat(),
            WindowManager.LayoutParams.MATCH_PARENT.toFloat(),
            Gravity.CENTER)

    init{
        panel.setOnTouchListener(DrawPathOnTouchListener())
    }

    constructor(builder: PointBuilder): super(builder)

    constructor(parcel: Parcel):super(parcel) {
        val endPoint:SimplePoint = parcel.readParcelable(SimplePoint::class.java.classLoader)!!
        this.endPoint.x = endPoint.x
        this.endPoint.y = endPoint.y
        this.endPoint.height = ceil(endPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.endPoint.width = ceil(endPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()

        var parcelXArray = parcel.readArray(Int::class.java.classLoader)
        coordinateXMove = Arrays.copyOf(parcelXArray, parcelXArray?.size!!, Array<Int>::class.java)

        var parcelYArray = parcel.readArray(Int::class.java.classLoader)
        coordinateYMove = Arrays.copyOf(parcelYArray, parcelYArray?.size!!, Array<Int>::class.java)

        var offsetX = parcel.readInt()
        this.offsetX = offsetX

        var offsetY = parcel.readInt()
        this.offsetY = offsetY
        "${offsetX} ${offsetY}".logd("offsetXYRead")

        var isFirstPointParcel = true
        for (n in 0..coordinateXMove.size-1){
            "${coordinateXMove[n]}".logd("parcelXRead")
            "${coordinateYMove[n]}".logd("parcelYRead")
            if(isFirstPointParcel) {
                path.moveTo(coordinateXMove[n].toFloat()-offsetX, coordinateYMove[n].toFloat()-offsetY)
                isFirstPointParcel = false
            }
            else
                path.lineTo(coordinateXMove[n].toFloat()-offsetX, coordinateYMove[n].toFloat()-offsetY)
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        dest?.writeParcelable(endPoint, flags)
        dest?.writeArray(coordinateXMove)
        dest?.writeArray(coordinateYMove)
        "${offsetX} ${offsetY}".logd("offsetXYWrite")
        dest?.writeInt(offsetX)
        dest?.writeInt(offsetY)
    }

    constructor(json: JsonObject):super(json){
       /* val firstPointJson =
                gson.fromJson(json.get("PathPoint").asString, JsonObject::class.java)
        val firstPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, firstPointJson)
        super.x = firstPoint.x
        super.y = firstPoint.y
        super.height = ceil(firstPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        super.width = ceil(firstPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()*/

        /*val endPointJson =
                gson.fromJson(json.get("EndPoint"), SimplePoint::class.java)
        val endPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, endPointJson)
        this.endPoint.x = endPoint.x
        this.endPoint.y = endPoint.y
        this.endPoint.height = ceil(endPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.endPoint.width = ceil(endPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()*/

        val coordinateXJson =
                gson.fromJson(json.get("PathPointsGestureX"), JsonArray::class.java)
        var coordinateX: Array<Int> = arrayOf()
        coordinateXJson.forEach{
            "${gson.fromJson(it,JsonObject::class.java).get("coordinateX").toString()}".logd("gsonReadCoordinateX")
            coordinateX += gson.fromJson(it,JsonObject::class.java).get("coordinateX").toString().toInt()
        }
        coordinateXMove = coordinateX

        val coordinateYJson =
            gson.fromJson(json.get("PathPointsGestureY"), JsonArray::class.java)
        var coordinateY: Array<Int> = arrayOf()
        coordinateYJson.forEach{
            "${gson.fromJson(it, JsonObject::class.java).get("coordinateY").toString()}".logd("gsonReadCoordinateY")
            coordinateY += gson.fromJson(it,JsonObject::class.java).get("coordinateY").toString().toInt()
        }
        coordinateYMove = coordinateY

        val pathJson = gson.fromJson(json.get("Path"), Path::class.java)
        path = pathJson
        Log.d("123",""+gson.fromJson(json.get("OffsetX"), JsonObject::class.java))
        val offsetX = gson.fromJson(json.get("OffsetX"), JsonObject::class.java)
        this.offsetX = offsetX["offsetX"].asInt
        val offsetY = gson.fromJson(json.get("OffsetY"), JsonObject::class.java)
        this.offsetY = offsetY["offsetY"].asInt
        wasDraw = true;

        var isFirstPointParcel = true
        for (n in 0..coordinateXMove.size-1){
            "${coordinateXMove[n]}".logd("parcelXRead")
            "${coordinateYMove[n]}".logd("parcelYRead")
            if(isFirstPointParcel) {
                path.moveTo(coordinateXMove[n].toFloat()-this.offsetX.toString().toInt(), coordinateYMove[n].toFloat()-this.offsetY.toString().toInt())
                isFirstPointParcel = false
            }
            else
                path.lineTo(coordinateXMove[n].toFloat()-this.offsetX.toString().toInt(), coordinateYMove[n].toFloat()-this.offsetY.toString().toInt())
        }
        if(AutoClickService.getParamSizePoint() == 32)
            pointLocateHelper = 37;
        else if(AutoClickService.getParamSizePoint() == 40)
            pointLocateHelper = 50;
        else if(AutoClickService.getParamSizePoint() == 56)
            pointLocateHelper = 75;
        endPoint.x = coordinateXMove.last() - pointLocateHelper
        endPoint.y = coordinateYMove.last() - pointLocateHelper
    }

    override fun toJsonObject():JsonObject{
        val obj = super.toJsonObject()
        obj.add("EndPoint", endPoint.toJsonObject())
        val objArrayX  = JsonArray()
        for(n in 0..coordinateXMove.size-1){
            objArrayX.add(toJsonObjectCoordinateXY(n, coordinateXMove, "coordinateX"))
        }
        obj.add("PathPointsGestureX", objArrayX)

        val objArrayY = JsonArray()
        for(n in 0..coordinateYMove.size-1) {
            objArrayY.add(toJsonObjectCoordinateXY(n, coordinateYMove, "coordinateY"))
        }
        obj.add("PathPointsGestureY", objArrayY)
        obj.add("Path", pathToJsonObject())
        obj.add("OffsetX", offsetXToJsonObject())
        obj.add("OffsetY", offsetYToJsonObject())
        return obj
    }

    fun offsetXToJsonObject():JsonObject{
        val obj = JsonObject()
        obj.addProperty("offsetX", offsetX)
        return obj
    }

    fun offsetYToJsonObject():JsonObject{
        val obj = JsonObject()
        obj.addProperty("offsetY", offsetY)
        return obj
    }

    fun pathToJsonObject():JsonObject{
        val obj = JsonObject()
        obj.addProperty("path", gson.toJson(path))
        return obj
    }

    fun toJsonObjectCoordinateXY(n:Int, coordinate:Array<Int>, string: String):JsonObject {
        val obj = super.toJsonObject()
        obj.addProperty(string, coordinate[n])
        return  obj
    }

    var firstSwapOrientation = true
    override fun swapPointOrientation() {
        var temp = super.x
        super.x = super.y
        super.y = temp
        temp = endPoint.x
        endPoint.x = endPoint.y
        endPoint.y = endPoint.x
        path.reset()
        var isFirstPointSwap = true
        if(firstSwapOrientation) {
            for (n in 0..coordinateXMove.size - 1) {
                if (isFirstPointSwap) {
                    path.moveTo(coordinateYMove[n].toFloat(), coordinateXMove[n].toFloat())
                    isFirstPointSwap = false
                } else
                    path.lineTo(coordinateYMove[n].toFloat(), coordinateXMove[n].toFloat())
            }
        }
        else {
            for (n in 0..coordinateXMove.size - 1) {
                if (isFirstPointSwap) {
                    path.moveTo(coordinateXMove[n].toFloat(), coordinateYMove[n].toFloat())
                    isFirstPointSwap = false
                } else
                    path.lineTo(coordinateXMove[n].toFloat(), coordinateYMove[n].toFloat())
            }
        }

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
        else {
            super.detachToWindow(wm, canvas)
            endPoint.detachToWindow(wm, canvas)
        }
    }

    override fun updateViewLayout(wm: WindowManager, size: Float) {
        endPoint.updateViewLayout(wm, size)
        super.updateViewLayout(wm, size)
    }

    var offsetX = 0;
    var offsetY = 0;
    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        val l = PathOnTouchListener.create(this, wm, canvas, bounds, path, coordinateXMove, coordinateYMove);
        endPoint.view.setOnTouchListener(l);
        super.view.setOnTouchListener(l);
        panel.setOnTouchListener(DrawPathOnTouchListener())
    }

    override fun getCommand(): GestureDescription? {
        val builder = GestureDescription.Builder()
        "${offsetX} ${offsetY}".logd("offsetXYgesture")
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, super.duration))
        return builder.build()
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(PathPoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<PathPoint?> {
            return arrayOfNulls(size)
        }
    }

    var isFirsPoint = 0

    var pointLocateHelper  = 0 ;


    inner class DrawPathOnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when(event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_DOWN->{
                    if(AutoClickService.getParamSizePoint() == 32)
                        pointLocateHelper = 37;
                    else if(AutoClickService.getParamSizePoint() == 40)
                        pointLocateHelper = 50;
                    else if(AutoClickService.getParamSizePoint() == 56)
                        pointLocateHelper = 75;
                    x = event.x.toInt() - pointLocateHelper
                    y = event.y.toInt() - pointLocateHelper
                    path.moveTo(event.rawX, event.rawY)
                }
                MotionEvent.ACTION_UP->{
                    detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                    wasDraw = true
                    endPoint.x = event.rawX.toInt() - pointLocateHelper
                    endPoint.y = event.rawY.toInt() - pointLocateHelper
                    attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                    coordinateXMove.forEach { xMove ->
                       xMove.toString().logd("xMove")
                    }
                    coordinateXMove.forEach { yMove ->
                        yMove.toString().logd("yMove")
                    }
                    coordinateXMove.size.logd("SizeXArray")
                    coordinateYMove.size.logd("SizeYArray")
                    updateListener(AutoClickService.getWM(), AutoClickService.getCanvas(),AutoClickService.getParamBound())
                }
                MotionEvent.ACTION_MOVE-> {
                    path.lineTo(event.rawX, event.rawY)
                    "${event.rawX} ${event.rawY}".logd("startPoint")
                    coordinateXMove += event.getX().toInt()
                    coordinateYMove += event.getY().toInt()
                    AutoClickService.getCanvas().invalidate()
                }
            }
            return true
        }
    }

    override fun createHolderDialog(viewContent: View): AbstractViewHolderDialog {
        val holder = super.createHolderDialog(viewContent)
        return ExtendedSwipeDialog(holder, viewContent, this)
    }

    override fun createViewDialog(): View {
        val vContent = super.createViewDialog() as ViewGroup
        return vContent
    }

    /**
     * Decorator for AbstractViewHolderDialog for SwipePoint
     * add new button to swipe SwipePoint
     */
    inner class ExtendedSwipeDialog(dialogHolder: AbstractViewHolderDialog, override val containerView: View,
                              val point: PathPoint) : ExtendedViewHolder(dialogHolder), LayoutContainer {

        init{
            btn_duplicate.setOnClickListener{
                // Duplicate this point
                wasDraw = true
                AutoClickService.requestAction(point.appContext, AutoClickService.ACTION_DUPLICATE_POINT, AutoClickService.KEY_POINT, point)
                dialog?.cancel()
            }
        }
    }
}