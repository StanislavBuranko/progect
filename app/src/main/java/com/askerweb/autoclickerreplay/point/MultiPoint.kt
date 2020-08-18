package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.content.Context
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.context
import com.askerweb.autoclickerreplay.ktExt.getNavigationBar
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog
import com.askerweb.autoclickerreplay.point.view.ExtendedViewHolder
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_setting_point.*
import kotlinx.android.synthetic.main.multi_point_dialog.*
import java.util.*


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

    public fun createPointsForRecordPanel(x:Int, y:Int, i:Int) {
        points += PointBuilder.invoke()
                .position(x, y)
                .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
                .text((listCommands.size + i).toString())
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

    public fun setRepeatRecord(repeat: Int) {
        points.forEach { point ->
            point.repeat = repeat;
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

    override fun createTableView(tableLayout: TableLayout, inflater: LayoutInflater) {
        val tr = inflater.inflate(R.layout.table_row_for_table_setting_points, null) as TableRow

        val linearLayout = tr.findViewById<View>(R.id.linearLayoutTypePoint)
        val imageView = linearLayout.findViewById<View>(R.id.imageType) as ImageView
        imageView.setBackgroundResource(R.drawable.ic_multi_point)

        val edXPoint = tr.findViewById<View>(R.id.xPoint) as EditText
        edXPoint.setText(points[0].x.toString())
        edXPoint.setOnFocusChangeListener { view: View, b: Boolean ->
            if (edXPoint.text.toString() == "") {
                edXPoint.setText(points[0].params.x.toString())
            }
        }
        edXPoint.addTextChangedListener {
            if (edXPoint.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if (edXPoint.text.toString().toInt() > display.width) {
                    edXPoint.setText(display.width.toString())
                    edXPoint.setSelection(edXPoint.text.length)
                    points[0].params.x = edXPoint.text.toString().toInt()
                } else {
                    points[0].params.x = edXPoint.text.toString().toInt()
                }
                AutoClickService.getWM().updateViewLayout(points[0].view, points[0].params)
            }
        }

        val edYPoint = tr.findViewById<View>(R.id.yPoint) as EditText
        edYPoint.setText(points[0].y.toString())
        edYPoint.setOnFocusChangeListener { view: View, b: Boolean ->
            if (edYPoint.text.toString() == "") {
                edYPoint.setText(points[0].y.toString())
            }

        }
        edYPoint.addTextChangedListener {
            if (edYPoint.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if (edYPoint.text.toString().toInt() > display.height) {
                    edYPoint.setText(display.height.toString())
                    edYPoint.setSelection(edYPoint.text.length)
                    points[0].params.y = edYPoint.text.toString().toInt()
                } else {
                    points[0].params.y = edYPoint.text.toString().toInt()
                }
                AutoClickService.getWM().updateViewLayout(points[0].view, points[0].params)
            }
        }

        val edDelayPoint = tr.findViewById<View>(R.id.delayPoint) as EditText
        edDelayPoint.setText(points[0].delay.toString())
        edDelayPoint.setOnFocusChangeListener { view: View, b: Boolean ->
            if (edDelayPoint.text.toString() == "") {
                edDelayPoint.setText(points[0].delay.toString())
            }
        }
        edDelayPoint.addTextChangedListener {
            if (edDelayPoint.text.toString() != "") {
                if (edDelayPoint.text.toString().toInt() >= 100000) {
                    edDelayPoint.setText("99999")
                    edDelayPoint.setSelection(edDelayPoint.text.length)
                    setDelayRecord(edDelayPoint.text.toString().toInt())
                } else {
                    setDelayRecord(edDelayPoint.text.toString().toInt())
                }
            }
        }

        val edDurationPoint = tr.findViewById<View>(R.id.durationPoint) as EditText
        edDurationPoint.setText(super.duration.toString())
        edDurationPoint.setOnFocusChangeListener { view: View, b: Boolean ->
            if (edDurationPoint.text.toString() == "") {
                edDurationPoint.setText(points[0].duration.toString())
            }
        }
        edDurationPoint.addTextChangedListener {
            if (edDurationPoint.text.toString() != "") {
                if (edDurationPoint.text.toString().toInt() >= 60001) {
                    edDurationPoint.setText("60000")
                    edDurationPoint.setSelection(edDurationPoint.text.length)
                    setDurationRecord(edDelayPoint.text.toString().toInt())
                } else {
                    setDurationRecord(edDelayPoint.text.toString().toInt())
                }
            }
        }

        val edRepeatPoint = tr.findViewById<View>(R.id.repeatPoint) as EditText
        edRepeatPoint.setText(points[0].repeat.toString())
        edRepeatPoint.setOnFocusChangeListener { view: View, b: Boolean ->
            if (edRepeatPoint.text.toString() == "") {
                edRepeatPoint.setText(points[0].repeat.toString())
            }
        }
        edRepeatPoint.addTextChangedListener {
            if (edRepeatPoint.text.toString() != "") {
                if (edRepeatPoint.text.toString().toInt() >= 100000) {
                    edRepeatPoint.setText("99999")
                    edRepeatPoint.setSelection(edRepeatPoint.text.length)
                    setRepeatRecord(edRepeatPoint.text.toString().toInt())
                } else {
                    setRepeatRecord(edRepeatPoint.text.toString().toInt())
                }
            }

        }
        tableLayout.addView(tr)

        var tableRows: Array<TableRow> = arrayOf()
        for (i in 1..points.size - 1) {
            val trArray = inflater.inflate(R.layout.table_row_for_table_setting_points_minimal, null) as TableRow
            val imagePoint = trArray.findViewById<View>(R.id.ic_points) as ImageView
            imagePoint.setBackgroundResource(R.drawable.ic_multi_point)

            val edXPointNext = trArray.findViewById<View>(R.id.xPoint) as EditText
            edXPointNext.setText(points[i].x.toString())
            edXPointNext.setOnFocusChangeListener { view: View, b: Boolean ->
                points[i].view.visibility = if (points[i].view.visibility == View.GONE) View.VISIBLE else View.GONE
                if (edXPointNext.text.toString() == "") {
                    edXPointNext.setText(points[i].params.x.toString())
                }
            }
            edXPointNext.addTextChangedListener {
                if (edXPointNext.text.toString() != "") {
                    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val display = wm.defaultDisplay
                    if (edXPointNext.text.toString().toInt() > display.width) {
                        edXPointNext.setText(display.width.toString())
                        edXPointNext.setSelection(edXPointNext.text.length)
                        points[i].params.x = edXPointNext.text.toString().toInt()
                    } else {
                        points[i].params.x = edXPointNext.text.toString().toInt()
                    }
                    AutoClickService.getWM().updateViewLayout(points[i].view, points[i].params)
                }
            }

            val edYPointNext = trArray.findViewById<View>(R.id.yPoint) as EditText
            edYPointNext.setText(points[i].y.toString())
            edYPointNext.setOnFocusChangeListener { view: View, b: Boolean ->
                points[i].view.visibility = if (points[i].view.visibility == View.GONE) View.VISIBLE else View.GONE
                if (edYPointNext.text.toString() == "") {
                    edYPointNext.setText(points[i].y.toString())
                }
            }
            edYPointNext.addTextChangedListener {
                if (edYPointNext.text.toString() != "") {
                    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val display = wm.defaultDisplay
                    if (edYPointNext.text.toString().toInt() > display.height) {
                        edYPointNext.setText(display.height.toString())
                        edYPointNext.setSelection(edYPointNext.text.length)
                        points[i].params.y = edYPointNext.text.toString().toInt()
                    } else {
                        points[i].params.y = edYPointNext.text.toString().toInt()
                    }
                    AutoClickService.getWM().updateViewLayout(points[i].view, points[i].params)
                }
            }
            trArray.visibility = View.GONE
            tableLayout.addView(trArray)
            tableRows += trArray
        }

        val trMultiPoint = inflater.inflate(R.layout.table_row_for_table_setting_points_multi, null) as TableRow
        val edNumbMultiPoint = trMultiPoint.findViewById<View>(R.id.numbMultiPoint) as EditText
        edNumbMultiPoint.setText(points.size.toString())
        edNumbMultiPoint.setOnFocusChangeListener { view: View, b: Boolean ->
            if (edNumbMultiPoint.text.toString() == "") {
                edNumbMultiPoint.setText(points.size.toString())
            }
        }
        edNumbMultiPoint.addTextChangedListener {
            if (edNumbMultiPoint.text.toString() != "" && edNumbMultiPoint.text.toString().toInt() >= 2 && edNumbMultiPoint.text.toString().toInt() <= 10) {
                if (edNumbMultiPoint.text.toString().toInt() > points.size) {
                    detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                    for (n in 0..edNumbMultiPoint.text.toString().toInt() - points.size - 1) {
                        createPointsForRecordPanel(points.last().x + 35, points.last().y, 0)
                    }
                    attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                }
                if (edNumbMultiPoint.text.toString().toInt() < points.size) {
                    val to = points.size - edNumbMultiPoint.text.toString().toInt() - 1
                    for (n in 0..to) {
                        points.last().detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                        points = points.dropLast(1).toTypedArray()
                    }
                }
                tableLayout.removeAllViews()
                val trHeading = inflater.inflate(R.layout.table_row_heading, null) as TableRow
                tableLayout.addView(trHeading)
                AutoClickService.getListPoint().forEach { point ->
                    point.createTableView(tableLayout, inflater)
                    point.setVisible(View.GONE)
                }
            } else if (edNumbMultiPoint.text.toString() != "") {
                if (edNumbMultiPoint.text.toString().toInt() < 1) {
                    edNumbMultiPoint.setText("2")
                    edNumbMultiPoint.setSelection(edNumbMultiPoint.text.length)
                }
                if (edNumbMultiPoint.text.toString().toInt() > 10) {
                    edNumbMultiPoint.setText("10")
                    edNumbMultiPoint.setSelection(edNumbMultiPoint.text.length)
                }
            }
        }
        trMultiPoint.visibility = View.GONE
        tableLayout.addView(trMultiPoint)

        val buttonShowHideRow = tr.findViewById<View>(R.id.butttonHideShowRow) as Button
        buttonShowHideRow.setBackgroundResource(R.drawable.ic_close_minimal)
        buttonShowHideRow.setOnClickListener {
            tableRows.forEach { tableRow ->
                tableRow.visibility = if (tableRow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                setVisible(if (points[0].view.visibility == View.GONE) View.VISIBLE else View.GONE)
                trMultiPoint.visibility = if (trMultiPoint.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                if (tableRow.visibility == View.VISIBLE)
                    buttonShowHideRow.setBackgroundResource(R.drawable.ic_open_minimal)
                else
                    buttonShowHideRow.setBackgroundResource(R.drawable.ic_close_minimal)
            }
        }

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
            point.delay = if(editDelay.text.toString() != "") editDelay.text.toString().toLong() else 0
            point.duration = if(editDuration.text.toString() != "") editDuration.text.toString().toLong() else 0
            point.repeat = if(editRepeat.text.toString() != "") editRepeat.text.toString().toInt() else 0
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