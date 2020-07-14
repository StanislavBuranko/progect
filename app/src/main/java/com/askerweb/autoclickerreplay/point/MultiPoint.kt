package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.RecordPoints.point
import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService
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
                    .text((listCommando.size + 1).toString())
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
        val holder = createHolderDialog(viewContent)
        holder.updateViewDialogParam()
        val dialog = AlertDialog.Builder(view.context)
                .setTitle(view.context.getString(R.string.setting_point))
                .setView(viewContent)
                .setPositiveButton(R.string.save) { _, _ ->
                    holder.saveEditDialog()
                    attachToWindow(AutoClickService.getWM(),AutoClickService.getCanvas())
                    updateListener(AutoClickService.getWM(),AutoClickService.getCanvas(), AutoClickService.getParamBound())
                    "${points.size}".logd()
                    AutoClickService.getCanvas()?.invalidate()
                }.create()
        holder.dialog = dialog;
        dialog.window?.setType(getWindowsTypeApplicationOverlay())
        dialog.show()
        detachToWindow(AutoClickService.getWM(),AutoClickService.getCanvas())
        "123123123123".logd()
        holder.saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    }

    override open fun createHolderDialog(viewContent: View): AbstractViewHolderDialog {
        return PointHolderMultiPointDialogEdit(viewContent)
    }

    override fun createViewDialog(): View {
        return LayoutInflater.from(view.context).inflate(R.layout.multi_point_dialog, null)
    }

    inner class PointHolderMultiPointDialogEdit(override val containerView:View) :
            AbstractViewHolderDialog(), LayoutContainer {

        init{

            multiPointDialogButtonPlus.setOnClickListener{
                editNumbMultiPoint.setText((editNumbMultiPoint.text.toString().toInt()+1).toString())
                true
            }

            multiPointDialogButtonMinus.setOnClickListener{
                editNumbMultiPoint.setText((editNumbMultiPoint.text.toString().toInt()-1).toString())
                true
            }
            editNumbMultiPoint.addTextChangedListener {
                if (editNumbMultiPoint.text.toString() != "")
                    if (editNumbMultiPoint.text.toString().toInt() < 2)
                        editNumbMultiPoint.setText((2).toString())
                    else if (editNumbMultiPoint.text.toString().toInt() > 10)
                        editNumbMultiPoint.setText((10).toString())
            }


            editDelay.addTextChangedListener {
                if (editDelay.text.toString() != "")
                    if (editDelay.text.toString().toInt() < 0)
                        editDelay.setText((0).toString())
                    else if (editDelay.text.toString().toInt() > 100000)
                        editDelay.setText((100000).toString())
            }

            editDuration.addTextChangedListener {
                if (editDuration.text.toString() != "")
                    if (editDuration.text.toString().toInt() < 0)
                        editDuration.setText((0).toString())
                    else if (editDuration.text.toString().toInt() > 100000)
                        editDuration.setText((100000).toString())
            }

            editRepeat.addTextChangedListener {
                if (editRepeat.text.toString() != "")
                    if (editRepeat.text.toString().toInt() < 0)
                        editRepeat.setText((0).toString())
                    else if (editRepeat.text.toString().toInt() > 100000)
                        editRepeat.setText((100000).toString())
            }

            editDelay.doAfterTextChanged{
                editNumbMultiPoint.setText(points.size.toString())
                requireSettingEdit()
            }

            editDuration.doAfterTextChanged{
                requireSettingEdit()
            }

            editRepeat.doAfterTextChanged{
                requireSettingEdit()
            }

        }

        override fun updateViewDialogParam() {
            editDelay.setText("${points[0].delay}")
            editDuration.setText("${points[0].duration}")
            editRepeat.setText("${points[0].repeat}")
        }

        override fun saveEditDialog(){
            "${points.size}  ${editNumbMultiPoint.text.toString().toInt()}".logd()
            if(points.size-1 != editNumbMultiPoint.text.toString().toInt()) {
                var differencePoints = -points.size + editNumbMultiPoint.text.toString().toInt()
                "${differencePoints}".logd()
                if (differencePoints > 0)
                    for (n in 1..differencePoints) {
                        points += PointBuilder.invoke()
                                .position(points.last().x + 50, points.last().y + 50)
                                .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.draw_point_click)!!)
                                .text(points[0].text)
                                .build(SimplePoint::class.java)
                    }
                else if (differencePoints < 0) {
                    points.dropLast(differencePoints*-1)
                    "Ok".logd()
                }
            }
            "${points.size}".logd()
            points.forEach { point ->
                point.delay = editDelay.text.toString().toLong()
                point.duration = editDuration.text.toString().toLong()
                point.repeat = editRepeat.text.toString().toInt()

                "${point.delay}".logd()
                "${point.duration}".logd()
                "${point.repeat}".logd()
            }

        }

        override fun requireSettingEdit(){
            saveButton?.isEnabled = isRequire()
        }

        override fun isRequire(): Boolean {
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