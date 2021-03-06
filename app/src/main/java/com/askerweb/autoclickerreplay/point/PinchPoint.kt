package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
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
import androidx.core.widget.addTextChangedListener
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.activity.TablePointsActivity
import com.askerweb.autoclickerreplay.ktExt.*
import com.askerweb.autoclickerreplay.point.view.*
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.askerweb.autoclickerreplay.service.AutoClickService.*
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.pinch_dialog_elements.*
import kotlin.math.ceil

class PinchPoint:Point {

    override val drawableViewDefault = ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_pinch)!!

    val firstPoint = PointBuilder.invoke()
            .position(this.x + 200, this.y)
            .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
            .build(SimplePoint::class.java)

    val secondPoint = PointBuilder.invoke()
            .position(this.x - 200, this.y)
            .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
            .build(SimplePoint::class.java)

    var typePinch = PinchDirection.OUT

    constructor(builder: PointBuilder):super(builder)

    constructor(parcel: Parcel):super(parcel){
        typePinch = parcel.readSerializable() as PinchDirection
        val firstPoint:SimplePoint = parcel.readParcelable(SimplePoint::class.java.classLoader)!!
        this.firstPoint.x = firstPoint.x
        this.firstPoint.y = firstPoint.y
        this.firstPoint.height = ceil(firstPoint.height / getService().resources.displayMetrics.density).toInt()
        this.firstPoint.width = ceil(firstPoint.width / getService().resources.displayMetrics.density).toInt()
        val secondPoint:SimplePoint = parcel.readParcelable(SimplePoint::class.java.classLoader)!!
        this.secondPoint.x = secondPoint.x
        this.secondPoint.y = secondPoint.y
        this.secondPoint.height = ceil(secondPoint.height / getService().resources.displayMetrics.density).toInt()
        this.secondPoint.width = ceil(secondPoint.width / getService().resources.displayMetrics.density).toInt()
    }

    constructor(json: JsonObject):super(json){
        typePinch = if(json.get("type").asString == PinchDirection.IN.name) PinchDirection.IN
                else PinchDirection.OUT
        val firstPointJson =
                gson.fromJson(json.get("firstPoint").asString, JsonObject::class.java)
        val firstPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, firstPointJson)
        this.firstPoint.x = firstPoint.x
        this.firstPoint.y = firstPoint.y
        this.firstPoint.height = ceil(firstPoint.height / getService().resources.displayMetrics.density).toInt()
        this.firstPoint.width = ceil(firstPoint.width / getService().resources.displayMetrics.density).toInt()
        val secondPointJson =
                gson.fromJson(json.get("secondPoint").asString, JsonObject::class.java)
        val secondPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, secondPointJson)
        this.secondPoint.x = secondPoint.x
        this.secondPoint.y = secondPoint.y
        this.secondPoint.height = ceil(secondPoint.height / getService().resources.displayMetrics.density).toInt()
        this.secondPoint.width = ceil(secondPoint.width / getService().resources.displayMetrics.density).toInt()
    }

    override var text: String
        get() = super.text
        set(value) {
            super.text = value
            if(firstPoint != null){
                firstPoint.text = value
            }
            if(secondPoint != null){
                secondPoint.text = value
            }
        }

    init{
        text = text
        view.textVisible = false
    }

    override fun setVisible(visible: Int) {
        super.setVisible(visible)
        firstPoint.setVisible(visible)
        secondPoint.setVisible(visible)
        getCanvas().invalidate()
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

    override fun setTouchable(touchable: Boolean, wm:WindowManager){
        if(touchable){
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            view.isClickable = true
            firstPoint.params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            firstPoint.view.isClickable = true
            secondPoint.params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            secondPoint.view.isClickable = true
        }
        else{
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            view.isClickable = false
            firstPoint.params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            firstPoint.view.isClickable = false
            secondPoint.params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            secondPoint.view.isClickable = false
        }
        wm.updateViewLayout(view, params)
        wm.updateViewLayout(firstPoint.view, firstPoint.params)
        wm.updateViewLayout(secondPoint.view, secondPoint.params)
    }

    override fun updateParamsFlags(){
        super.params.flags = getParamOverlayFlags() or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        firstPoint.params.flags = getParamOverlayFlags() or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        secondPoint.params.flags = getParamOverlayFlags() or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
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
        obj.addProperty("type", typePinch.name)
        obj.addProperty("firstPoint", firstPoint.toJson())
        obj.addProperty("secondPoint", secondPoint.toJson())
        return obj
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        dest?.writeSerializable(typePinch)
        dest?.writeParcelable(firstPoint, flags)
        dest?.writeParcelable(secondPoint, flags)
    }

    override fun createTableView(tableLayout: TableLayout, inflater: LayoutInflater) {
        val tr = inflater.inflate(R.layout.table_row_for_table_setting_points, null) as TableRow
        imageTypePoint(tr)
        edX(tr)
        edY(tr)
        edDelay(tr)
        edDuration(tr)
        edRepeat(tr)
        btnDown(tr, tableLayout, inflater)
        btnUp(tr, tableLayout, inflater)
        imageBtnDown(tr)
        imageBtnUp(tr)
        tableLayout.addView(tr)
        minimalTableRow(tr, tableLayout, inflater)
    }

    fun imageTypePoint(tr: TableRow){
        val linearLayout = tr.findViewById<View>(R.id.linearLayoutTypePoint)
        val imageView = linearLayout.findViewById<View>(R.id.imageType) as ImageView
        imageView.setBackgroundResource(R.drawable.ic_pinch)
    }

    fun edX(tr: TableRow) {
        val edXPoint = tr.findViewById<View>(R.id.xPoint) as EditText
        edXPoint.setText(super.x.toString())
        edXPoint.setOnFocusChangeListener { view: View, b: Boolean ->
            setVisible(if (super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if (edXPoint.text.toString() == "") {
                edXPoint.setText(super.params.x.toString())
                edXPoint.setSelection(edXPoint.text.length)
            }
        }
        edXPoint.addTextChangedListener {
            if (edXPoint.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if (edXPoint.text.toString().toInt() > display.width) {
                    edXPoint.setText(display.width.toString())
                    super.params.x = edXPoint.text.toString().toInt()
                } else {
                    super.params.x = edXPoint.text.toString().toInt()
                }
                getWM().updateViewLayout(super.view, super.params)
            }
        }
    }

    fun edY(tr: TableRow) {
        val edYPoint = tr.findViewById<View>(R.id.yPoint) as EditText
        edYPoint.setText(super.y.toString())
        edYPoint.setOnFocusChangeListener { view: View, b: Boolean ->
            setVisible(if (super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if (edYPoint.text.toString() == "") {
                edYPoint.setText(super.y.toString())
                edYPoint.setSelection(edYPoint.text.length)
            }

        }
        edYPoint.addTextChangedListener {
            if (edYPoint.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if (edYPoint.text.toString().toInt() > display.height) {
                    edYPoint.setText(display.height.toString())
                    super.params.y = edYPoint.text.toString().toInt()
                } else {
                    super.params.y = edYPoint.text.toString().toInt()
                }
                getWM().updateViewLayout(super.view, super.params)
            }
        }
    }

    fun edDelay(tr: TableRow) {
        val edDelayPoint = tr.findViewById<View>(R.id.delayPoint) as EditText
        edDelayPoint.setText(super.delay.toString())
        edDelayPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if(edDelayPoint.text.toString() == ""){
                edDelayPoint.setText(super.delay.toString())
            }
        }
        edDelayPoint.addTextChangedListener{
            if(edDelayPoint.text.toString() != "") {
                if(edDelayPoint.text.toString().toInt() >= 100000) {
                    edDelayPoint.setText("99999")
                    edDelayPoint.setSelection(edDelayPoint.text.length)
                    super.delay = edDelayPoint.text.toString().toLong()
                }
                else {super.delay = edDelayPoint.text.toString().toLong()}
            }
        }
    }

    fun edDuration(tr: TableRow) {
        val edDurationPoint = tr.findViewById<View>(R.id.durationPoint) as EditText
        edDurationPoint.setText(super.duration.toString())
        edDurationPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if(edDurationPoint.text.toString() == ""){
                edDurationPoint.setText(super.duration.toString())
                edDurationPoint.setSelection(edDurationPoint.text.length)
            }
        }
        edDurationPoint.addTextChangedListener{
            if(edDurationPoint.text.toString() != "") {
                if(edDurationPoint.text.toString().toInt() >= 60001) {
                    edDurationPoint.setText("60000")
                    super.duration = edDurationPoint.text.toString().toLong()
                }
                else {super.duration = edDurationPoint.text.toString().toLong()}
            }
        }
    }

    fun edRepeat(tr:TableRow) {
        val edRepeatPoint = tr.findViewById<View>(R.id.repeatPoint) as EditText
        edRepeatPoint.setText(super.repeat.toString())
        edRepeatPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if(edRepeatPoint.text.toString() == ""){
                edRepeatPoint.setText(super.repeat.toString())
                edRepeatPoint.setSelection(edRepeatPoint.text.length)
            }
        }
        edRepeatPoint.addTextChangedListener{
            if(edRepeatPoint.text.toString() != "") {
                if(edRepeatPoint.text.toString().toInt() >= 100000) {
                    edRepeatPoint.setText("99999")
                    super.repeat = edRepeatPoint.text.toString().toInt()
                }
                else { super.repeat = edRepeatPoint.text.toString().toInt()}
            }
        }
    }

    fun btnDown(tr:TableRow, tableLayout: TableLayout, inflater: LayoutInflater) {
        val buttonDown = tr.findViewById<View>(R.id.butttonDownPoint) as Button
        buttonDown.setOnClickListener{
            if (super.text > "0" && super.text.toInt() < getListPoint().size){
                val tempPoint = getListPoint().get(super.text.toInt()-1)
                val tempTextPoint = super.text.toInt()
                getListPoint().set(tempTextPoint - 1, getListPoint().get(super.text.toInt()))
                getListPoint().set(super.text.toInt(), tempPoint)
                getListPoint().get(super.text.toInt()-1).text = tempTextPoint.toString()
                getListPoint().get(super.text.toInt()).text = (super.text.toInt()+1).toString()

                TablePointsActivity.updateTable(tableLayout, inflater)
            }
        }
    }

    fun btnUp(tr:TableRow , tableLayout: TableLayout, inflater: LayoutInflater) {
        val buttonUp = tr.findViewById<View>(R.id.butttonUpPoint) as Button
        buttonUp.setOnClickListener{
            getListPoint().logd()
            if (super.text > "1" && super.text.toInt() <= getListPoint().size){
                val tempPoint = getListPoint().get(super.text.toInt()-1)
                val tempTextPoint = super.text.toInt()

                getListPoint().set(tempTextPoint-1, getListPoint().get(super.text.toInt()-2))
                getListPoint().set(tempTextPoint-2, tempPoint)
                getListPoint().get(tempTextPoint-1).text = (super.text.toInt()).toString()
                getListPoint().get(tempTextPoint-2).text = (super.text.toInt()-1).toString()

                TablePointsActivity.updateTable(tableLayout, inflater)
            }
        }
    }

    fun imageBtnDown(tr: TableRow) {
        val linearLayoutButtonDown = tr.findViewById<View>(R.id.linerLayoutDownPoint)
        val imageViewButtonDown = linearLayoutButtonDown.findViewById<View>(R.id.butttonDownPoint) as Button
        if(super.text.toInt() == getListPoint().size)
            imageViewButtonDown.setBackgroundResource(R.drawable.ic_arrow_down_disable)
    }

    fun imageBtnUp(tr: TableRow) {
        val linearLayoutButtonUp = tr.findViewById<View>(R.id.linerLayoutUpPoint)
        val imageViewButtonUp = linearLayoutButtonUp.findViewById<View>(R.id.butttonUpPoint) as Button
        if(super.text == "1")
            imageViewButtonUp.setBackgroundResource(R.drawable.ic_arrow_up_disable)
    }

    fun minimalTableRow(tr:TableRow, tableLayout:TableLayout, inflater:LayoutInflater) {
        val trFirst = inflater.inflate(R.layout.table_row_for_table_setting_points_minimal, null) as TableRow

        val imagePointFirst = trFirst.findViewById<View>(R.id.ic_points) as ImageView
        imagePointFirst.setBackgroundResource(R.drawable.point_click)

        val edXPointFirst = trFirst.findViewById(R.id.xPoint) as EditText
        edXPointFirst.setText(firstPoint.params.x.toString())
        edXPointFirst.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edXPointFirst.text.toString() == ""){
                edXPointFirst.setText(firstPoint.params.x.toString())
            }
            getCanvas().invalidate()
        }
        edXPointFirst.addTextChangedListener{
            if(edXPointFirst.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edXPointFirst.text.toString().toInt() > display.width) {
                    edXPointFirst.setText(display.width.toString())
                    edXPointFirst.setSelection(edXPointFirst.text.length)
                    firstPoint.params.x = edXPointFirst.text.toString().toInt()
                }
                else {firstPoint.params.x = edXPointFirst.text.toString().toInt()}
                getWM().updateViewLayout(firstPoint.view, firstPoint.params)
            }
            getCanvas().invalidate()
        }

        val edYPointFirst = trFirst.findViewById(R.id.yPoint) as EditText
        edYPointFirst.setText(firstPoint.y.toString())
        edYPointFirst.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edYPointFirst.text.toString() == ""){
                edYPointFirst.setText(firstPoint.params.y.toString())
            }
            getCanvas().invalidate()
        }
        edYPointFirst.addTextChangedListener{
            if(edYPointFirst.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edYPointFirst.text.toString().toInt() > display.height) {
                    edYPointFirst.setText(display.height.toString())
                    edYPointFirst.setSelection(edYPointFirst.text.length)
                    firstPoint.params.y = edYPointFirst.text.toString().toInt()
                }
                else {firstPoint.params.y = edYPointFirst.text.toString().toInt()}
                getWM().updateViewLayout(firstPoint.view, firstPoint.params)
            }
            getCanvas().invalidate()
        }
        trFirst.visibility = View.GONE
        tableLayout.addView(trFirst)

        val trSecond = inflater.inflate(R.layout.table_row_for_table_setting_points_minimal, null) as TableRow
        val imagePointSecond = trSecond.findViewById<View>(R.id.ic_points) as ImageView
        imagePointSecond.setBackgroundResource(R.drawable.point_click)


        val edXPointSecond = trSecond.findViewById(R.id.xPoint) as EditText
        secondPoint.params.x.logd()
        edXPointSecond.setText(secondPoint.params.x.toString())
        edXPointSecond.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edXPointSecond.text.toString() == ""){
                edXPointSecond.setText(secondPoint.params.x.toString())
            }
            getCanvas().invalidate()
        }
        edXPointFirst.addTextChangedListener{
            if(edXPointFirst.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edXPointSecond.text.toString().toInt() > display.width) {
                    edXPointSecond.setText(display.width.toString())
                    edXPointSecond.setSelection(edXPointSecond.text.length)
                    secondPoint.params.x = edXPointSecond.text.toString().toInt()
                }
                else {secondPoint.params.x = edXPointSecond.text.toString().toInt()}
                getWM().updateViewLayout(secondPoint.view, secondPoint.params)
            }
            getCanvas().invalidate()
        }

        val edYPointSecond = trSecond.findViewById(R.id.yPoint) as EditText
        edYPointSecond.setText(secondPoint.y.toString())
        edYPointSecond.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edYPointSecond.text.toString() == ""){
                edYPointSecond.setText(secondPoint.params.y.toString())
            }
            getCanvas().invalidate()
        }
        edYPointSecond.addTextChangedListener{
            if(edYPointSecond.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edYPointSecond.text.toString().toInt() > display.height) {
                    edYPointSecond.setText(display.height.toString())
                    edYPointSecond.setSelection(edYPointSecond.text.length)
                    secondPoint.params.y = edYPointSecond.text.toString().toInt()
                }
                else {secondPoint.params.y = edYPointSecond.text.toString().toInt()}
                getWM().updateViewLayout(secondPoint.view, secondPoint.params)
            }
            getCanvas().invalidate()
        }
        trSecond.visibility = View.GONE
        tableLayout.addView(trSecond)

        val trDirection = inflater.inflate(R.layout.table_row_for_table_setting_points_pinch, null) as TableRow
        val edSpinner = trDirection.findViewById<View>(R.id.spinner) as Spinner
        edSpinner.setSelection(if(typePinch  == PinchDirection.OUT) 1 else 0)
        edSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                typePinch = if(edSpinner.selectedItemId.toInt() == 0) PinchDirection.IN else PinchDirection.OUT
                getCanvas().invalidate()
            }
        }
        trDirection.visibility = View.GONE
        tableLayout.addView(trDirection)
        val buttonShowHideRow = tr.findViewById<View>(R.id.butttonHideShowRow) as Button
        buttonShowHideRow.setBackgroundResource(R.drawable.ic_close_minimal)
        buttonShowHideRow.setOnClickListener {
            trFirst.visibility = if (trFirst.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            trSecond.visibility = if (trSecond.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            trDirection.visibility = if (trDirection.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if(trFirst.visibility == View.VISIBLE)
                buttonShowHideRow.setBackgroundResource(R.drawable.ic_open_minimal)
            else
                buttonShowHideRow.setBackgroundResource(R.drawable.ic_close_minimal)
        }
    }

    override fun getCommand(): GestureDescription? {
        return typePinch.getCommand(xTouch.toFloat(), yTouch.toFloat(), firstPoint, secondPoint, delay, duration)
    }

    override fun createViewDialog(): View {
        val vContent: ViewGroup = super.createViewDialog() as ViewGroup
        val vContentPinch = LayoutInflater.from(vContent.context)
                .inflate(R.layout.pinch_dialog_elements, null)
        vContent.findViewById<LinearLayout>(R.id.content)
                .addView(vContentPinch)
        return vContent
    }

    override fun createHolderDialog(viewContent: View): AbstractViewHolderDialog {
        val holder = super.createHolderDialog(viewContent)
        return ExtendedPinchDialog(holder, viewContent, this)
    }

    override fun swapPointOrientation() {
        super.swapPointOrientation()
        firstPoint.swapPointOrientation()
        secondPoint.swapPointOrientation()
    }

    /**
     * Decorator for AbstractViewHolderDialog for PinchPoint
     * add new control for change direction PinchPoint
     */
    class ExtendedPinchDialog(dialogHolder: AbstractViewHolderDialog, override val containerView:View,
                              val point: PinchPoint) : ExtendedViewHolder(dialogHolder), LayoutContainer{

        override val expandableSave = {
            point.typePinch = if(directionPinch.isChecked) PinchDirection.IN else PinchDirection.OUT
        }

        override val expandableUpdate = {
            directionPinch.isChecked = when(point.typePinch){
                PinchDirection.IN -> true
                PinchDirection.OUT -> false
            }
        }
    }

    enum class PinchDirection{
        IN{
            override fun getCommand(xTouch:Float, yTouch:Float, firstPoint:Point, secondPoint:Point, delay:Long, duration:Long): GestureDescription? {
                val path = Path()
                path.moveTo(firstPoint.xTouch.toFloat()+getNavigationBar(), firstPoint.yTouch.toFloat())
                path.lineTo(xTouch+getNavigationBar(), yTouch)
                val path2 = Path()
                path2.moveTo(secondPoint.xTouch.toFloat()+getNavigationBar(), secondPoint.yTouch.toFloat())
                path2.lineTo(xTouch+getNavigationBar(), yTouch)
                val builder = GestureDescription.Builder()
                builder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                        .addStroke(GestureDescription.StrokeDescription(path2, 0, duration))
                return builder.build()

            }
        },
        OUT{
            override fun getCommand(xTouch:Float, yTouch:Float, firstPoint:Point, secondPoint:Point, delay:Long, duration:Long): GestureDescription? {
                val path = Path()
                path.moveTo(xTouch + getNavigationBar(), yTouch)
                path.lineTo(firstPoint.xTouch.toFloat() + getNavigationBar(), firstPoint.yTouch.toFloat())
                val path2 = Path()
                path2.moveTo(xTouch + getNavigationBar(), yTouch)
                path2.lineTo(secondPoint.xTouch.toFloat() + getNavigationBar(), secondPoint.yTouch.toFloat())
                val builder = GestureDescription.Builder()
                builder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                        .addStroke(GestureDescription.StrokeDescription(path2, 0, duration))
                return builder.build()
            }
        };
        abstract fun getCommand(xTouch:Float, yTouch:Float, firstPoint:Point, secondPoint:Point, delay:Long, duration:Long): GestureDescription?
    }


}