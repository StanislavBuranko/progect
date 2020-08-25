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
import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog
import com.askerweb.autoclickerreplay.point.view.ExtendedViewHolder
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.swipe_dialog_elements.*
import org.w3c.dom.Text
import kotlin.math.ceil


class SwipePoint : Point {
    val nextPoint: Point =  PointBuilder.invoke()
            .position(params.x+50, params.y)
            .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!)
            .build(SimplePoint::class.java)


    override var text: String
        get() = super.text
        set(value) {
            super.text = value
            if(nextPoint != null){
                nextPoint.text = "$value"
            }
        }

    init{
        text = text
    }

    constructor(builder: PointBuilder): super(builder)

    constructor(parcel: Parcel):super(parcel){
        val nextPoint:SimplePoint = parcel.readParcelable(SimplePoint::class.java.classLoader)!!
        this.nextPoint.x = nextPoint.x
        this.nextPoint.y = nextPoint.y
        this.nextPoint.height = ceil(nextPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.nextPoint.width = ceil(nextPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
    }

    constructor(json: JsonObject):super(json){
        val nextPointJson =
                gson.fromJson(json.get("nextPoint").asString, JsonObject::class.java)
        val nextPoint =
                PointBuilder.invoke().buildFrom(SimplePoint::class.java, nextPointJson)
        this.nextPoint.x = nextPoint.x
        this.nextPoint.y = nextPoint.y
        this.nextPoint.height = ceil(nextPoint.height / AutoClickService.getService().resources.displayMetrics.density).toInt()
        this.nextPoint.width = ceil(nextPoint.width / AutoClickService.getService().resources.displayMetrics.density).toInt()
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(SwipePoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }


    override fun setVisible(visible: Int) {
        super.setVisible(visible)
        nextPoint.setVisible(visible)
        AutoClickService.getCanvas().invalidate()
    }

    override fun updateViewLayout(wm: WindowManager, size: Float) {
        nextPoint.updateViewLayout(wm, size)
        super.updateViewLayout(wm, size)
    }

    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        super.updateListener(wm, canvas, bounds)
        nextPoint.updateListener(wm,canvas,bounds)
    }

    override fun attachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        super.attachToWindow(wm, canvas)
        nextPoint.attachToWindow(wm, canvas)
    }

    override fun detachToWindow(wm: WindowManager, canvas: PointCanvasView) {
        super.detachToWindow(wm, canvas)
        nextPoint.detachToWindow(wm, canvas)
    }

    override fun updateParamsFlags(){
        super.params.flags = getParamOverlayFlags() or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        nextPoint.params.flags = getParamOverlayFlags() or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    }

    override fun toJsonObject():JsonObject{
        val obj = super.toJsonObject()
        obj.addProperty("nextPoint", nextPoint.toJson())
        return obj
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        dest?.writeParcelable(nextPoint, flags)
    }

    override fun createTableView(tableLayout: TableLayout, inflater: LayoutInflater) {
        val tr = inflater.inflate(R.layout.table_row_for_table_setting_points, null) as TableRow

        val linearLayout = tr.findViewById<View>(R.id.linearLayoutTypePoint)
        val imageView = linearLayout.findViewById<View>(R.id.imageType) as ImageView
        imageView.setBackgroundResource(R.drawable.ic_swap)

        val edXPoint = tr.findViewById<View>(R.id.xPoint) as EditText
        edXPoint.setText(super.params.x.toString())
        edXPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edXPoint.text.toString() == ""){
                edXPoint.setText(super.params.x.toString())
            }
            AutoClickService.getCanvas().invalidate()
        }
        edXPoint.addTextChangedListener{
            if(edXPoint.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edXPoint.text.toString().toInt() > display.width) {
                    edXPoint.setText(display.width.toString())
                    edXPoint.setSelection(edXPoint.text.length)
                    super.params.x = edXPoint.text.toString().toInt()
                }
                else {super.params.x = edXPoint.text.toString().toInt()}
                AutoClickService.getWM().updateViewLayout(super.view, super.params)
            }
            AutoClickService.getCanvas().invalidate()
        }

        val edYPoint = tr.findViewById<View>(R.id.yPoint) as EditText
        edYPoint.setText(super.params.y.toString())
        edYPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edYPoint.text.toString() == ""){
                edYPoint.setText(super.y.toString())
            }
            AutoClickService.getCanvas().invalidate()
        }
        edYPoint.addTextChangedListener{
            if(edYPoint.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edYPoint.text.toString().toInt() > display.height) {
                    edYPoint.setText(display.height.toString())
                    edYPoint.setSelection(edYPoint.text.length)
                    super.params.y = edYPoint.text.toString().toInt()
                }
                else {super.params.y = edYPoint.text.toString().toInt()}
                AutoClickService.getWM().updateViewLayout(super.view, super.params)
            }
            AutoClickService.getCanvas().invalidate()
        }

        val edDelayPoint = tr.findViewById<View>(R.id.delayPoint) as EditText
        edDelayPoint.setText(super.delay.toString())
        edDelayPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edDelayPoint.text.toString() == "") {
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

        val edDurationPoint = tr.findViewById<View>(R.id.durationPoint) as EditText
        edDurationPoint.setText(super.duration.toString())
        edDurationPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edDurationPoint.text.toString() == ""){
                edDurationPoint.setText(super.duration.toString())
            }
        }
        edDurationPoint.addTextChangedListener{
            if(edDurationPoint.text.toString() != "") {
                if(edDurationPoint.text.toString().toInt() >= 60001) {
                    edDurationPoint.setText("60000")
                    edDurationPoint.setSelection(edDurationPoint.text.length)
                    super.duration = edDurationPoint.text.toString().toLong()
                }
                else {super.duration = edDurationPoint.text.toString().toLong()}
            }
        }

        val edRepeatPoint = tr.findViewById<View>(R.id.repeatPoint) as EditText
        edRepeatPoint.setText(super.repeat.toString())
        edRepeatPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edRepeatPoint.text.toString() == ""){
                edRepeatPoint.setText(super.repeat.toString())
            }
        }
        edRepeatPoint.addTextChangedListener{
            if(edRepeatPoint.text.toString() != "") {
                if(edRepeatPoint.text.toString().toInt() >= 100000) {
                    edRepeatPoint.setText("99999")
                    edRepeatPoint.setSelection(edRepeatPoint.text.length)
                    super.repeat = edRepeatPoint.text.toString().toInt()
                }
                else { super.repeat = edRepeatPoint.text.toString().toInt()}
            }

        }

        tableLayout.addView(tr)


        val trEnd = inflater.inflate(R.layout.table_row_for_table_setting_points_minimal, null) as TableRow
        val imagePoint = trEnd.findViewById<View>(R.id.ic_points) as ImageView
        imagePoint.setBackgroundResource(R.drawable.point_click)


        val edXPointEnd = trEnd.findViewById(R.id.xPoint) as EditText
        edXPointEnd.setText(nextPoint.params.x.toString())
        edXPointEnd.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edXPointEnd.text.toString() == ""){
                edXPointEnd.setText(nextPoint.params.x.toString())
            }
            AutoClickService.getCanvas().invalidate()
        }
        edXPointEnd.addTextChangedListener{
            if(edXPointEnd.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edXPointEnd.text.toString().toInt() > display.width) {
                    edXPointEnd.setText(display.width.toString())
                    edXPointEnd.setSelection(edXPointEnd.text.length)
                    nextPoint.params.x = edXPointEnd.text.toString().toInt()
                }
                else {nextPoint.params.x = edXPointEnd.text.toString().toInt()}
                AutoClickService.getWM().updateViewLayout(nextPoint.view, nextPoint.params)
            }
            AutoClickService.getCanvas().invalidate()
        }

        val edYPointEnd = trEnd.findViewById(R.id.yPoint) as EditText
        edYPointEnd.setText(nextPoint.y.toString())
        edYPointEnd.setOnFocusChangeListener{ view: View, b: Boolean ->
            if(edYPointEnd.text.toString() == ""){
                edYPointEnd.setText(nextPoint.params.y.toString())
            }
            AutoClickService.getCanvas().invalidate()
        }
        edYPointEnd.addTextChangedListener{
            if(edYPointEnd.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edYPointEnd.text.toString().toInt() > display.height) {
                    edYPointEnd.setText(display.height.toString())
                    edYPointEnd.setSelection(edYPointEnd.text.length)
                    nextPoint.params.y = edYPointEnd.text.toString().toInt()
                }
                else {nextPoint.params.y = edYPointEnd.text.toString().toInt()}
                AutoClickService.getWM().updateViewLayout(nextPoint.view, nextPoint.params)
            }
            AutoClickService.getCanvas().invalidate()
        }
        trEnd.visibility = View.GONE
        tableLayout.addView(trEnd)

        val trDirection = inflater.inflate(R.layout.table_row_for_table_setting_points_swipe, null) as TableRow
        //val textView = tableRowDirection.findViewById<View>(R.id.direction) as TextView
        val btnEnvert = trDirection.findViewById<Button>(R.id.invert) as Button
        btnEnvert.setOnClickListener{
            val xTemp = nextPoint.x
            val yTemp = nextPoint.y
            nextPoint.x = x
            nextPoint.y = y
            x = xTemp
            y = yTemp
            AutoClickService.getWM().updateViewLayout(view, params)
            AutoClickService.getWM().updateViewLayout(nextPoint.view, nextPoint.params)
            AutoClickService.getCanvas().invalidate()
        }
        trDirection.visibility = View.GONE
        tableLayout.addView(trDirection)

        val buttonShowHideRow = tr.findViewById<View>(R.id.butttonHideShowRow) as Button
        buttonShowHideRow.setBackgroundResource(R.drawable.ic_close_minimal)
        buttonShowHideRow.setOnClickListener {
            trEnd.visibility = if(trEnd.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            trDirection.visibility = if(trDirection.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if(trDirection.visibility == View.VISIBLE)
                buttonShowHideRow.setBackgroundResource(R.drawable.ic_open_minimal)
            else
                buttonShowHideRow.setBackgroundResource(R.drawable.ic_close_minimal)
        }

        val buttonDown = tr.findViewById<View>(R.id.butttonDownPoint) as Button
        buttonDown.setOnClickListener{
            if (super.text > "0" && super.text.toInt() < AutoClickService.getListPoint().size){
                val tempPoint = AutoClickService.getListPoint().get(super.text.toInt()-1)
                val tempTextPoint = super.text.toInt()
                AutoClickService.getListPoint().set(tempTextPoint - 1, AutoClickService.getListPoint().get(super.text.toInt()))
                AutoClickService.getListPoint().set(super.text.toInt(), tempPoint)
                AutoClickService.getListPoint().get(super.text.toInt()-1).text = tempTextPoint.toString()
                AutoClickService.getListPoint().get(super.text.toInt()).text = (super.text.toInt()+1).toString()

                TablePointsActivity.updateTable(tableLayout, inflater)
            }
            true
        }

        val buttonUp = tr.findViewById<View>(R.id.butttonUpPoint) as Button
        buttonUp.setOnClickListener{
            AutoClickService.getListPoint().logd()
            if (super.text > "1" && super.text.toInt() <= AutoClickService.getListPoint().size){
                val tempPoint = AutoClickService.getListPoint().get(super.text.toInt()-1)
                val tempTextPoint = super.text.toInt()

                AutoClickService.getListPoint().set(tempTextPoint-1, AutoClickService.getListPoint().get(super.text.toInt()-2))
                AutoClickService.getListPoint().set(tempTextPoint-2, tempPoint)
                AutoClickService.getListPoint().get(tempTextPoint-1).text = (super.text.toInt()).toString()
                AutoClickService.getListPoint().get(tempTextPoint-2).text = (super.text.toInt()-1).toString()

                TablePointsActivity.updateTable(tableLayout, inflater)
            }
            true
        }
    }

    override fun setTouchable(touchable: Boolean, wm:WindowManager){
        if(touchable){
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            view.isClickable = true
            nextPoint.params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            nextPoint.view.isClickable = true
        }
        else{
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            view.isClickable = false
            nextPoint.params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            nextPoint.view.isClickable = false
        }
        wm.updateViewLayout(view, params)
        wm.updateViewLayout(nextPoint.view, nextPoint.params)
    }



    override fun getCommand() : GestureDescription {
        "swipe from $xTouch $yTouch to ${nextPoint.xTouch} ${nextPoint.yTouch}".logd()
        val path = Path()
        path.moveTo(xTouch.toFloat() + getNavigationBar(), yTouch.toFloat())
        path.lineTo(nextPoint.xTouch.toFloat() + getNavigationBar(), nextPoint.yTouch.toFloat())
        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
        return builder.build()
    }

    override fun createHolderDialog(viewContent: View): AbstractViewHolderDialog {
        val holder = super.createHolderDialog(viewContent)
        return ExtendedSwipeDialog(holder, viewContent, this)
    }

    override fun createViewDialog(): View {
        val vContent = super.createViewDialog() as ViewGroup
        val vSwipePoint = LayoutInflater.from(vContent.context)
                .inflate(R.layout.swipe_dialog_elements, null)
        vContent.findViewById<LinearLayout>(R.id.content)
                .addView(vSwipePoint)
        return vContent
    }

    override fun swapPointOrientation() {
        super.swapPointOrientation()
        nextPoint.swapPointOrientation()
    }

    /**
     * Decorator for AbstractViewHolderDialog for SwipePoint
     * add new button to swipe SwipePoint
     */
    class ExtendedSwipeDialog(dialogHolder: AbstractViewHolderDialog, override val containerView: View,
                              val point: SwipePoint) : ExtendedViewHolder(dialogHolder), LayoutContainer {

        init{
            swipeBtn.setOnClickListener { //change position nextPoint and this point
                val xTemp = point.nextPoint.x
                val yTemp = point.nextPoint.y
                point.nextPoint.x = point.x
                point.nextPoint.y = point.y
                point.x = xTemp
                point.y = yTemp
                AutoClickService.getWM().updateViewLayout(point.view, point.params)
                AutoClickService.getWM().updateViewLayout(point.nextPoint.view, point.nextPoint.params)
                AutoClickService.getCanvas().invalidate()
                dialogHolder.dialog?.cancel()
            }
        }
    }
}