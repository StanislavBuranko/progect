package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getNavigationBar
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.MultiPoint.ExtendedPinchDialog
import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog
import com.askerweb.autoclickerreplay.point.view.ExtendedViewHolder
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_setting_point.*
import kotlinx.android.synthetic.main.multi_point_dialog.*
import kotlinx.android.synthetic.main.pinch_dialog_elements.*
import java.util.*
import kotlin.math.ceil

class MultiPoint: Point {

    private val listCommands:MutableList<Point> = App.serviceComponent.getListPoint()

    public var points: Array<Point> = arrayOf()

    constructor(parcel: Parcel) : super(parcel) {
        var parcelPoints = parcel.readParcelableArray(MultiPoint::class.java.classLoader)
        points = Arrays.copyOf(parcelPoints, parcelPoints?.size!!, Array<Point>::class.java)
    }

    init {
        view.visibility = View.GONE
        points += PointBuilder.invoke()
                .position(x, y)
                .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
                .text((listCommands.size + 1).toString())
                .build(SimplePoint::class.java)
        points.last().updateListener(AutoClickService.getWM(),AutoClickService.getCanvas(), AutoClickService.getParamBound())
        points += PointBuilder.invoke()
                .position(x + 50, y + 50)
                .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
                .text((listCommands.size + 1).toString())
                .build(SimplePoint::class.java)
        points.last().updateListener(AutoClickService.getWM(),AutoClickService.getCanvas(), AutoClickService.getParamBound())
        setTextArray = true
    }

    public fun createPointsForRecordPanelChangeElemenent(indexElement: Int, x:Int, y:Int) {
        points[indexElement].x = x;
        points[indexElement].y = y;
    }

    public fun createPointsForRecordPanel(x:Int, y:Int) {
        points += PointBuilder.invoke()
                .position(x, y)
                .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
                .text((listCommands.size + 1).toString())
                .build(SimplePoint::class.java)
        points.last().updateListener(AutoClickService.getWM(), AutoClickService.getCanvas(), AutoClickService.getParamBound())

    }

    public fun clearArray() {
        val points1: Array<Point> = arrayOf()
        points = points1;
    }

    constructor(builder: PointBuilder) : super(builder) {

    }

    constructor(json: JsonObject) : super(json) {
        val pointJson =
                gson.fromJson(json.get("MultiPoints"), JsonArray::class.java)
        var pointsJson: Array<Point> = arrayOf()
        pointJson.forEach{
            pointsJson += PointBuilder.invoke().buildFrom(SimplePoint::class.java,gson.fromJson(it,JsonObject::class.java))
        }
        points = pointsJson
    }

    override fun toJsonObject():JsonObject{
        val obj = super.toJsonObject()
        val objArray  = JsonArray()
        for(n in 0..points.size-1){
            objArray.add(points[n].toJsonObject())
        }
        obj.add("MultiPoints", objArray)
        return obj
    }



    override fun setVisible(visible: Int) {
        points.forEach { it.setVisible(visible) }
    }

    override fun updateViewLayout(wm: WindowManager, size: Float) {
        points.forEach { point -> point.updateViewLayout(wm, size) }
    }

    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        points.forEach { point -> point.updateListener(wm, canvas, bounds) }
    }

    var isAttach = false
    override fun attachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        if(!isAttach) {
            super.attachToWindow(wm, canvas)
            isAttach = true;
        }
        points.forEach {
            it.attachToWindow(wm, canvas)
            it.view.setOnLongClickListener{
                showDialog()
                true
            }
        }
    }

    public fun setDelayRecord(delay: Int) {
        points.forEach { point ->
            point.delay = delay.toLong();
        }
    }

    public fun setDurationRecord(duration: Int) {
        points.forEach { point ->
            point.duration = duration.toLong();
        }
    }

    override fun detachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        if(isAttach) {
            super.detachToWindow(wm, canvas)
            isAttach = false;
        }
        points.forEach {
            it.detachToWindow(wm, canvas)
        }
    }
    var setTextArray = false
    override var text:String
        get() = view.text
        set(value){
            view.text = value
            if(setTextArray)
                points.forEach { it.view.text = value }
        }

    override fun setTouchable(touchable: Boolean, wm:WindowManager){
        points.forEach {
            if (touchable) {
                it.params.flags = it.params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                it.view.isClickable = true
            } else {
                it.params.flags = it.params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                it.view.isClickable = false
            }
            wm.updateViewLayout(it.view, it.params)
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        dest?.writeParcelableArray(points, flags)
    }

    override fun getCommand(): GestureDescription? {
        val builder = GestureDescription.Builder()
        for (n in 0..points.size - 1) {
            val path = Path()
            path.moveTo(points[n].xTouch.toFloat()+ getNavigationBar(), points[n].yTouch.toFloat())
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, points[n].duration))
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

    override fun createViewDialog(): View {
        val vContent: ViewGroup = super.createViewDialog() as ViewGroup
        val vContentPinch = LayoutInflater.from(vContent.context)
                .inflate(R.layout.multi_point_dialog, null)
        vContent.findViewById<LinearLayout>(R.id.content)
                .addView(vContentPinch)
        return vContent
    }

    override fun createHolderDialog(viewContent: View): AbstractViewHolderDialog {
        val holder = super.createHolderDialog(viewContent)
        return ExtendedPinchDialog(holder, viewContent)
    }

    inner class ExtendedPinchDialog(dialogHolder: AbstractViewHolderDialog, override val containerView:View) : ExtendedViewHolder(dialogHolder), LayoutContainer{

        init {
            multiPointDialogButtonPlus.setOnClickListener {
                editNumbMultiPoint.setText((editNumbMultiPoint.text.toString().toInt() + 1).toString())
                true
            }

            multiPointDialogButtonMinus.setOnClickListener {
                editNumbMultiPoint.setText((editNumbMultiPoint.text.toString().toInt() - 1).toString())
                true
            }

            editNumbMultiPoint.addTextChangedListener {
                if (editNumbMultiPoint.text.toString() != "")
                    if (editNumbMultiPoint.text.toString().toInt() < 2)
                        editNumbMultiPoint.setText((2).toString())
                    else if (editNumbMultiPoint.text.toString().toInt() > 10)
                        editNumbMultiPoint.setText((10).toString())
                editNumbMultiPoint.setSelection(editNumbMultiPoint.text.length)
            }

            editDelay.doAfterTextChanged {
                editNumbMultiPoint.setText(points.size.toString())
                requireSettingEdit()
            }

            editDuration.doAfterTextChanged {
                requireSettingEdit()
            }

            editRepeat.doAfterTextChanged {
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
        if(points.size != editNumbMultiPoint.text.toString().toInt()) {
            var differencePoints = -points.size + editNumbMultiPoint.text.toString().toInt()
            "diff:${differencePoints}".logd()
            if (differencePoints > 0)
                for (n in 1..differencePoints) {
                    points += PointBuilder.invoke()
                            .position(points.last().x + 50, points.last().y)
                            .drawable(ContextCompat.getDrawable(appContext, R.drawable.point_click)!!)
                            .text(points[0].text)
                            .build(SimplePoint::class.java)
                    points.last().attachToWindow(AutoClickService.getWM(),AutoClickService.getCanvas())
                    points.last().updateListener(AutoClickService.getWM(),AutoClickService.getCanvas(), AutoClickService.getParamBound())
                    points.last().updateListener(AutoClickService.getWM(),AutoClickService.getCanvas(), AutoClickService.getParamBound())
                }
            else if (differencePoints < 0) {
                for (n in 1..differencePoints* -1) {
                    points.last().detachToWindow(AutoClickService.getWM(),AutoClickService.getCanvas())
                    points.last().updateListener(AutoClickService.getWM(),AutoClickService.getCanvas(), AutoClickService.getParamBound())
                    val points1 = points.dropLast(1).toTypedArray()
                    points = points1
                }


            }
        }
        points.forEach { point ->
            point.delay = editDelay.text.toString().toLong()
            point.duration = editDuration.text.toString().toLong()
            point.repeat = editRepeat.text.toString().toInt()
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

        override val expandableSave = {

        }

        override val expandableUpdate = {

        }
    }

    fun showDialog() {
        val viewContent: View = createViewDialog()
        val holder = createHolderDialog(viewContent)
        holder.updateViewDialogParam()
        super.appContext = points[0].appContext
        val dialog = AlertDialog.Builder(view.context)
                .setTitle(view.context.getString(R.string.setting_point))
                .setView(viewContent)
                .setPositiveButton(R.string.save) { _, _ ->
                    holder.saveEditDialog()
                    detachToWindow(AutoClickService.getWM(),AutoClickService.getCanvas())
                    attachToWindow(AutoClickService.getWM(),AutoClickService.getCanvas())
                    updateListener(AutoClickService.getWM(),AutoClickService.getCanvas(), AutoClickService.getParamBound())
                    super.repeat = points[0].repeat
                    super.delay = points[0].delay
                }.setNegativeButton(R.string.cancel) { _, _ ->
                }
                .setOnCancelListener { _ ->
                }
                .create()
        holder.dialog = dialog;
        dialog.window?.setType(getWindowsTypeApplicationOverlay())
        dialog.show()
        holder.saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    }
}