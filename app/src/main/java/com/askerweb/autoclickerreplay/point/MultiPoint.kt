package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
import com.askerweb.autoclickerreplay.point.view.ConnectMirrorTouchListener
import com.askerweb.autoclickerreplay.point.view.PinchOnTouchListener
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.askerweb.autoclickerreplay.service.AutoClickService.listCommando
import com.google.gson.JsonObject
import kotlin.math.ceil

class MultiPoint: Point {
    var points : Array<SimplePoint> = arrayOf()
    val countEditText: Int = 2
    constructor(builder: PointBuilder): super(builder){}


    constructor(parcel: Parcel):super(parcel){
        showDialog()
        val countListCommand: Int = listCommando.size + 1;
        for (n in 1..countEditText) {
            val pointSm: SimplePoint = parcel.readParcelable(SimplePoint::class.java.classLoader)!!
            points += pointSm
            this.points.last().x = countListCommand
            this.points.last().x = pointSm.x
            this.points.last().y = pointSm.y
            this.points.last().height = ceil(pointSm.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
            this.points.last().width = ceil(pointSm.width / AutoClickService.getService().resources.displayMetrics.density).toInt()

        }
    }


    constructor(json: JsonObject):super(json){
        var pointsJson: Array<JsonObject>
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

    fun showDialog(){
        val viewContent: View = createViewDialog()
        val dialog = AlertDialog.Builder(view.context)
                .setTitle(view.context.getString(R.string.setting_point))
                .setView(viewContent)
                .setPositiveButton(R.string.save) { _, _ ->
                    val editText: EditText? = viewContent.findViewById<EditText>(R.id.editNumbMultiPoint)
                    val countEditText: Int = editText?.text.toString().toInt()
                    if(editText != null) {
                        editText!!.addTextChangedListener(object : TextWatcher {
                            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                                /* if (editText.text.toString().toInt() > 10)
                                     editText.text = "10";
                                 else if (editText.text.toString().toInt() < 2)
                                     editText.text = "2";*/
                            }

                            override fun afterTextChanged(editable: Editable) { }
                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
                        })
                        Log.d("123123","1 "+countEditText);
                    }
                }
                .setNegativeButton(R.string.cancel){ _, _ ->

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
        points.forEach { point -> dest?.writeParcelable(point, flags)}
    }

    override fun getCommand(): GestureDescription? {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<MultiPoint> {
        override fun createFromParcel(parcel: Parcel): MultiPoint {
            return MultiPoint(parcel)
        }

        override fun newArray(size: Int): Array<MultiPoint?> {
            return arrayOfNulls(size)
        }
    }
}