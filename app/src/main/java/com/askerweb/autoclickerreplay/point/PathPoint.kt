package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getNavigationBar
import com.askerweb.autoclickerreplay.ktExt.getWindowsParameterLayout
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.view.*
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_setting_point.*
import java.util.*
import kotlin.math.ceil

class PathPoint : Point {

    public var coordinateXMove: Array<Float> = arrayOf()
    public var coordinateYMove: Array<Float> = arrayOf()
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

    var isFirstSwap = true
    constructor(parcel: Parcel):super(parcel) {

        var parcelXArray = parcel.readArray(Int::class.java.classLoader)
        coordinateXMove = Arrays.copyOf(parcelXArray, parcelXArray?.size!!, Array<Float>::class.java)

        var parcelYArray = parcel.readArray(Int::class.java.classLoader)
        coordinateYMove = Arrays.copyOf(parcelYArray, parcelYArray?.size!!, Array<Float>::class.java)

        isFirstSwap = parcel.readBoolean()
        this.path.reset();
        if (isFirstSwap) {
            "123".logd("firstYes")
            path.moveTo(coordinateXMove[0].toFloat(), coordinateYMove[0].toFloat())
            for (n in 1..coordinateXMove.size - 1) {
                path.lineTo(coordinateXMove[n].toFloat(), coordinateYMove[n].toFloat())
            }
        } else {
            "123".logd("firstNo")
            path.moveTo(coordinateYMove[0].toFloat(), coordinateXMove[0].toFloat())
            for (n in 1..coordinateXMove.size - 1) {
                path.lineTo(coordinateYMove[n].toFloat(), coordinateXMove[n].toFloat())
            }
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        dest?.writeArray(coordinateXMove)
        dest?.writeArray(coordinateYMove)
        dest?.writeBoolean(isFirstSwap)
    }

    constructor(json: JsonObject):super(json){
        val coordinateXJson =
                gson.fromJson(json.get("PathPointsGestureX"), JsonArray::class.java)
        coordinateXJson.forEach{
            coordinateXMove += gson.fromJson(it,JsonObject::class.java).get("coordinateX").toString().toFloat()
        }

        val coordinateYJson =
            gson.fromJson(json.get("PathPointsGestureY"), JsonArray::class.java)
        coordinateYJson.forEach{
            "${gson.fromJson(it, JsonObject::class.java).get("coordinateY").toString()}".logd("gsonReadCoordinateY")
            coordinateYMove += gson.fromJson(it,JsonObject::class.java).get("coordinateY").toString().toFloat()
        }

        val pathJson = gson.fromJson(json.get("Path"), Path::class.java)
        path = pathJson
        wasDraw = true;

        path.moveTo(coordinateXMove[0].toFloat(), coordinateYMove[0].toFloat())
        for (n in 1..coordinateXMove.size - 1) {
            path.lineTo(coordinateXMove[n].toFloat(), coordinateYMove[n].toFloat())
        }

        if(AutoClickService.getParamSizePoint() == 32)
            pointLocateHelper = 37;
        else if(AutoClickService.getParamSizePoint() == 40)
            pointLocateHelper = 50;
        else if(AutoClickService.getParamSizePoint() == 56)
            pointLocateHelper = 75;
        endPoint.x = coordinateXMove.last().toInt() - pointLocateHelper
        endPoint.y = coordinateYMove.last().toInt() - pointLocateHelper
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

        return obj
    }

    fun pathToJsonObject():JsonObject{
        val obj = JsonObject()
        obj.addProperty("path", gson.toJson(path))
        return obj
    }

    fun toJsonObjectCoordinateXY(n:Int, coordinate: Array<Float>, string: String):JsonObject {
        val obj = super.toJsonObject()
        obj.addProperty(string, coordinate[n])
        return  obj
    }

    var swapOrientation = false
    override fun swapPointOrientation() {
        path.reset()
        if(!swapOrientation) {
            path.moveTo(coordinateYMove[0].toFloat(), coordinateXMove[0].toFloat())
            for (n in 1..coordinateXMove.size - 1) {
                path.lineTo(coordinateYMove[n].toFloat(), coordinateXMove[n].toFloat())
            }
            y = coordinateXMove[0].toInt() - pointLocateHelper
            x = coordinateYMove[0].toInt() - pointLocateHelper
            endPoint.y = coordinateXMove.last().toInt() - pointLocateHelper
            endPoint.x = coordinateYMove.last().toInt() - pointLocateHelper
            swapOrientation = true
            isFirstSwap = false
        }
        else {
            path.moveTo(coordinateXMove[0].toFloat(), coordinateYMove[0].toFloat())
            for (n in 1..coordinateXMove.size - 1) {
                path.lineTo(coordinateXMove[n].toFloat(), coordinateYMove[n].toFloat())
            }
            x = coordinateXMove[0].toInt() - pointLocateHelper
            y = coordinateYMove[0].toInt() - pointLocateHelper
            endPoint.x = coordinateXMove.last().toInt() - pointLocateHelper
            endPoint.y = coordinateYMove.last().toInt() - pointLocateHelper
            swapOrientation = false
            isFirstSwap = true
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

    override fun setVisible(visible:Int) {
        view.visibility = visible
        endPoint.view.visibility = visible
    }

    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        val l = PathOnTouchListener.create(this, wm, canvas, bounds);
        endPoint.view.setOnTouchListener(l);
        super.view.setOnTouchListener(l);
        panel.setOnTouchListener(DrawPathOnTouchListener())
    }

    override fun getCommand(): GestureDescription? {
        val builder = GestureDescription.Builder()
        path.offset(getNavigationBar().toFloat(), 0f)
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, super.duration))
        path.offset(-getNavigationBar().toFloat(), 0f)
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
                    x = event.getX().toInt() - pointLocateHelper
                    y = event.getY().toInt() - pointLocateHelper
                    path.moveTo(event.getX(), event.getY())
                    coordinateXMove += event.getX()
                    coordinateYMove += event.getY()
                }
                MotionEvent.ACTION_UP->{
                    detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                    wasDraw = true
                    if(coordinateXMove.size == 1)
                    {
                        path.lineTo((coordinateXMove.last()+100).toFloat(), (coordinateYMove.last()+100).toFloat())
                        coordinateXMove += coordinateXMove.last()+100
                        coordinateYMove += coordinateYMove.last()+100
                    }
                    endPoint.x = coordinateXMove.last().toInt() - pointLocateHelper
                    endPoint.y = coordinateYMove.last().toInt() - pointLocateHelper
                    attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                    updateListener(AutoClickService.getWM(), AutoClickService.getCanvas(),AutoClickService.getParamBound())
                    for (n in 0..coordinateXMove.size-1) {
                        coordinateXMove[n].logd("X")
                        coordinateYMove[n].logd("Y")
                    }
                }
                MotionEvent.ACTION_MOVE-> {
                    path.lineTo(event.getX(), event.getY())
                    coordinateXMove += event.rawX - getNavigationBar()
                    coordinateYMove += event.rawY
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