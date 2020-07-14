package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.askerweb.autoclickerreplay.service.AutoClickService.listCommando
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.dialog_setting_point.*

class MultiPoint: Point {
    var points : Array<Point> = arrayOf()
    val countEditText: Int = 2



    constructor(parcel: Parcel):super(parcel){

    }
    init {

        showDialog()

        val button = view.findViewById<Button>(R.id.multiPointButton) as Button

        button.setOnClickListener( View.OnClickListener {view ->
            val countListCommand: Int = listCommando.size + 1;
            for (n in 1..countEditText+5) {
                var pointSm = PointBuilder.invoke()
                        .position(100+n*50,100).delay(1000).duration(1000)
                        .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.draw_point_click)!!)
                        .text(listCommando.toString())
                        .build(SimplePoint::class.java)
                points += pointSm
            }
        })


    }

    constructor(builder: PointBuilder): super(builder)

    constructor(json: JsonObject):super(json){
        /*var pointsJson: Array<JsonObject>
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
        this.secondPoint.width = ceil(secondPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()*/
    }

    fun showDialog(){
        val viewContent: View = createViewDialog()
        val dialog = AlertDialog.Builder(view.context)
                .setTitle(view.context.getString(R.string.setting_point))
                .setView(viewContent)
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

        val builder = GestureDescription.Builder()
        val path = Path()
        for (n in 1..countEditText+5) {
            path.moveTo(points[n-1].x.toFloat(), points[n-1].y.toFloat())
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
}