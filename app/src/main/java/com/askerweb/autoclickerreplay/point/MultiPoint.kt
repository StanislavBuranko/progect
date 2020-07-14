package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService.listCommando
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.multi_point_dialog.*
import java.util.*

class MultiPoint: Point {

    var points: Array<Point> = arrayOf(
            PointBuilder.invoke()
                    .position(x, y)
                    .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.draw_point_click)!!)
                    .text((listCommando.size + 1).toString())
                    .build(SimplePoint::class.java),
            PointBuilder.invoke()
                    .position(x + 50, y + 50)
                    .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.draw_point_click)!!)
                    .text(listCommando.size.toString() + 1)
                    .build(SimplePoint::class.java))

    constructor(parcel: Parcel) : super(parcel) {
        var parcelPoints = parcel.readParcelableArray(MultiPoint::class.java.classLoader)
        points = Arrays.copyOf(parcelPoints, parcelPoints?.size!!, Array<Point>::class.java)
    }


    init {

        view.visibility = View.GONE
    }


    constructor(builder: PointBuilder) : super(builder) {

    }

    constructor(json: JsonObject) : super(json) {
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

    override fun updateViewLayout(wm: WindowManager, size: Float) {
        super.updateViewLayout(wm, size)
        points.forEach { point -> point.updateViewLayout(wm, size) }
    }

    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        super.updateListener(wm, canvas, bounds)
        points.forEach { point -> point.updateListener(wm, canvas, bounds) }
    }

    override fun attachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        super.attachToWindow(wm, canvas)
        points.forEach { point ->
            point.attachToWindow(wm, canvas)
            point.view.setOnLongClickListener(){viewPoint ->
                showDialog()
                true
            }
        }
    }

    override fun detachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        points.forEach { point -> point.detachToWindow(wm, canvas) }
        super.detachToWindow(wm, canvas)

    }

    override fun toJsonObject(): JsonObject {
        val obj = super.toJsonObject()
        obj.addProperty("nextPoint", points[0].toJson())
        return obj
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        dest?.writeParcelableArray(points, flags)
    }

    override fun getCommand(): GestureDescription? {

        val builder = GestureDescription.Builder()
        "${points.size}".logd()

        for (n in 0..points.size - 1) {
            val path = Path()
            path.moveTo(points[n].xTouch.toFloat(), points[n].yTouch.toFloat())
            "${points[n].x}    ${points[n].y}".logd()
            builder.addStroke(GestureDescription.StrokeDescription(path, delay, duration))
        }
        return builder.build()
    }

    companion object CREATOR : Parcelable.Creator<MultiPoint> {
        override fun createFromParcel(parcel: Parcel): MultiPoint {
            return MultiPoint(parcel)
        }

        override fun newArray(size: Int): Array<MultiPoint?> {
            return arrayOfNulls(size)
        }
    }


    fun showDialog() {
        val viewContent: View = createViewDialog()
        val dialog = AlertDialog.Builder(view.context)
                .setTitle(view.context.getString(R.string.setting_point))
                .setView(viewContent)
                .create()
        dialog.window?.setType(getWindowsTypeApplicationOverlay())
        dialog.show()
    }

    override open fun createHolderDialog(viewContent: View): AbstractViewHolderDialog {
        return PointHolderMultiPointDialogEdit(viewContent, this)
    }

    override fun createViewDialog(): View {
        return LayoutInflater.from(view.context).inflate(R.layout.multi_point_dialog, null)
    }

    class PointHolderMultiPointDialogEdit(override val containerView:View, private val point: Point) :
            AbstractViewHolderDialog(), LayoutContainer {

        init{

            btn_duplicate.setOnClickListener{

            }

            btn_delete.setOnClickListener{
                // Delete this point

            }

            /*.doAfterTextChanged{
                requireSettingEdit()
            }

            editDuration.doAfterTextChanged{
                requireSettingEdit()
            }

            editRepeat.doAfterTextChanged{
                requireSettingEdit()
            }*/

        }

        override fun updateViewDialogParam(){
            editDelay.setText("${point.delay}")
            editDuration.setText("${point.duration}")
            editRepeat.setText("${point.repeat}")
        }

        override fun saveEditDialog(){
            point.delay = editDelay.text.toString().toLong()
            point.duration = editDuration.text.toString().toLong()
            point.repeat = editRepeat.text.toString().toInt()
        }

        override fun requireSettingEdit(){
            saveButton?.isEnabled = isRequire()
        }

        override fun isRequire():Boolean{
            val delayRequire = editDelay.text.isNotEmpty() &&
                    Integer.parseInt(editDelay.text.toString()) >= 0
            val durationRequire = editDuration.text.isNotEmpty() &&
                    Integer.parseInt(editDuration.text.toString()) > 0
            val repeatRequire = editRepeat.text.isNotEmpty() &&
                    Integer.parseInt(editRepeat.text.toString()) > 0
            return delayRequire && durationRequire && repeatRequire
        }
    }
}