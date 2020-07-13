package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog
import com.askerweb.autoclickerreplay.point.view.ExtendedViewHolder
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.askerweb.autoclickerreplay.service.AutoClickService.listCommando
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.pinch_dialog_elements.*
import java.util.*

class MultiPoint: Point {
    var points : Array<Point> = arrayOf()




    constructor(parcel: Parcel):super(parcel){
        var parcelPoints = parcel.readParcelableArray(MultiPoint::class.java.classLoader)
        points = Arrays.copyOf(parcelPoints, parcelPoints?.size!!, Array<Point>::class.java)
    }

    init {
        view.visibility = View.GONE
    }

    constructor(builder: PointBuilder): super(builder){

    }

    constructor(json: JsonObject):super(json){
        points.forEach { point ->
            point.repeat = json.get("repeat").asInt
            point.delay = json.get("delay").asLong
            point.duration = json.get("duration").asLong
            val _params =
                    App.getGson().fromJson(json.get("params").asString, WindowManager.LayoutParams::class.java)
            point.width = _params.width
            point.height = _params.height
            point.x = _params.x
            point.y = _params.y
            text = json.get("text").asString
        }
    }

    var  isSetPositive = false
    fun showDialog() {
        val viewContent: View = createViewDialog()
        val dialog = AlertDialog.Builder(view.context)
                .setTitle(view.context.getString(R.string.setting_point))
                .setView(viewContent)
                .setPositiveButton("Yes") { dialogInterface, which ->
                    Log.d("123asd", "rtdhfgdsfdsafsdf")
                    val fdgjs = viewContent.findViewById<EditText>(R.id.editNumbMultiPoint)
                    for (n in 1..fdgjs.text.toString().toInt()) {

                        points += PointBuilder.invoke()
                                .position(100 + 150 * n, 300)
                                .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.draw_point_click)!!)
                                .text(listCommando.toString())
                                .build(SimplePoint::class.java)
                        points.last().attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                        Log.d("123", "" + n.toInt())
                        isSetPositive = true
                    }
                }
                .setNegativeButton("No") { dialogInterface, which ->
                    //listCommando.removeLast()
                    //        .detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                }
                .setOnCancelListener(){ dialogInterface ->
                    if (!isSetPositive)
                        listCommando.removeLast().detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                }
                .create()
        dialog.window?.setType(getWindowsTypeApplicationOverlay())
        dialog.show()
    }

    override fun updateViewLayout(wm: WindowManager, size: Float) {
        points.forEach { point ->  point.updateViewLayout(wm, size)}
        super.updateViewLayout(wm, size)
    }


    override fun attachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        super.attachToWindow(wm, canvas)
        points.forEach { point ->  point.attachToWindow(wm, canvas)}

    }

    override fun detachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        super.detachToWindow(wm, canvas)
        points.forEach { point ->  point.detachToWindow(wm, canvas)}
    }

    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        super.updateListener(wm, canvas, bounds)
        points.forEach { point ->  point.updateListener(wm,canvas,bounds)}
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        dest?.writeParcelableArray(points, flags)
    }

    override fun getCommand(): GestureDescription? {

        val builder = GestureDescription.Builder()
        "${points.size}".logd()

        for (n in 0..points.size-1) {
            val path = Path()
            path.moveTo(points[n].xTouch.toFloat(), points[n].yTouch.toFloat())
            "${points[n].x}    ${points[n].y}".logd()
            builder.addStroke(GestureDescription.StrokeDescription(path, delay, duration))
        }
        return  builder.build()
    }

    companion object CREATOR : Parcelable.Creator<MultiPoint> {
        override fun createFromParcel(parcel: Parcel): MultiPoint {
            return MultiPoint(parcel)
        }

        override fun newArray(size: Int): Array<MultiPoint?> {
            return arrayOfNulls(size)
        }
    }
    override fun createViewDialog():View{
        return LayoutInflater.from(view.context).inflate(R.layout.multi_point_dialog, null)
    }

    class ExtendedPinchDialog(dialogHolder: AbstractViewHolderDialog, override val containerView:View,
                              val point: PinchPoint) : ExtendedViewHolder(dialogHolder), LayoutContainer {

        override val expandableSave = {
            point.typePinch = if(directionPinch.isChecked) PinchPoint.PinchDirection.IN else PinchPoint.PinchDirection.OUT
        }

        override val expandableUpdate = {
            directionPinch.isChecked = when(point.typePinch){
                PinchPoint.PinchDirection.IN -> true
                PinchPoint.PinchDirection.OUT -> false
            }
        }
    }
}