package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.*
import com.askerweb.autoclickerreplay.point.view.*
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_setting_point.*
import java.util.*
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin


class PathPoint : Point {

    public var coordinateXMove: Array<Float> = arrayOf()
    public var coordinateYMove: Array<Float> = arrayOf()
    var wasDraw = false

    var path = Path()
    val endPoint = PointBuilder.invoke()
            .position(x,y)
            .drawable(ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.draw_point_path_end)!!)
            .build(SimplePoint::class.java)

    val panel = LinearLayout(AutoClickService.getService().applicationContext)
    var panelParam = getWindowsParameterLayout(
            WindowManager.LayoutParams.MATCH_PARENT.toFloat(),
            WindowManager.LayoutParams.MATCH_PARENT.toFloat(),
            Gravity.CENTER)


    //override val drawableViewDefault = ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.draw_point_path_start)!!

    init{
        panelParam.flags = panelParam.flags or getParamOverlayFlags() or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
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

    override fun swapPointOrientation() {
        if(wasDraw == true) {
            path.reset()
            var temp = coordinateXMove
            coordinateXMove = coordinateYMove
            coordinateYMove = temp
            path.moveTo(coordinateXMove[0], coordinateYMove[0])
            for (n in 1..coordinateXMove.size - 1) {
                path.lineTo(coordinateXMove[n], coordinateYMove[n])
            }
            x = coordinateXMove.first().toInt() - pointLocateHelper + xCutoutPathHelper()
            y = coordinateYMove.first().toInt() - yCutout() - pointLocateHelper
            xCutoutPathHelper().logd("proverkaCut")
            getNavigationBar().logd("proverkaNav")
            endPoint.x = coordinateXMove.last().toInt() - pointLocateHelper + xCutoutPathHelper()
            endPoint.y = coordinateYMove.last().toInt() - yCutout() - pointLocateHelper
            AutoClickService.getCanvas().invalidate()
        }
    }

    override fun swapPointOrientationLandscapeToLandscape() {
        swapPointOrientation()
        swapPointOrientation()
        updateViewLayout(AutoClickService.getWM(), AutoClickService.getParamSizePoint().toFloat())
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
        AutoClickService.getCanvas().invalidate()
    }

    override fun updateParamsFlags(){
        super.params.flags = getParamOverlayFlags() or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        endPoint.updateParamsFlags()
    }

    override fun updateListener(wm: WindowManager, canvas: PointCanvasView, bounds: Boolean) {
        val l = PathOnTouchListener.create(this, wm, canvas, bounds);
        endPoint.view.setOnTouchListener(l);
        super.view.setOnTouchListener(l);
        panel.setOnTouchListener(DrawPathOnTouchListener())
    }

    override fun createTableView(tableLayout: TableLayout, inflater: LayoutInflater) {
        val tr = inflater.inflate(R.layout.table_row_for_table_setting_points, null) as TableRow

        val linearLayout = tr.findViewById<View>(R.id.linearLayoutTypePoint)
        val imageView = linearLayout.findViewById<View>(R.id.imageType) as ImageView
        imageView.setBackgroundResource(R.drawable.ic_path_point)

        val edXPoint = tr.findViewById<View>(R.id.xPoint) as EditText
        edXPoint.setText(super.params.x.toString())
        edXPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if(edXPoint.text.toString() == ""){
                edXPoint.setText(super.params.x.toString())
            }
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
        }

        val edYPoint = tr.findViewById<View>(R.id.yPoint) as EditText
        edYPoint.setText(super.params.y.toString())
        edYPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if(edYPoint.text.toString() == ""){
                edYPoint.setText(super.y.toString())
            }

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
        }

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

        val edDurationPoint = tr.findViewById<View>(R.id.durationPoint) as EditText
        edDurationPoint.setText(super.duration.toString())
        edDurationPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
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
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
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
        imagePoint.setBackgroundResource(R.drawable.draw_point_path_end)


        val edXPointEnd = trEnd.findViewById(R.id.xPoint) as EditText
        edXPointEnd.setText(endPoint.params.x.toString())
        edXPointEnd.setOnFocusChangeListener{ view: View, b: Boolean ->
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if(edXPointEnd.text.toString() == "") {
                edXPointEnd.setText(endPoint.params.x.toString())
            }
        }
        edXPointEnd.addTextChangedListener{
            if(edXPointEnd.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edXPointEnd.text.toString().toInt() > display.width) {
                    edXPointEnd.setText(display.width.toString())
                    edXPointEnd.setSelection(edXPointEnd.text.length)
                    endPoint.params.x = edXPointEnd.text.toString().toInt()
                }
                else {endPoint.params.x = edXPointEnd.text.toString().toInt()}
                AutoClickService.getWM().updateViewLayout(endPoint.view, endPoint.params)
            }
        }

        val edYPointEnd = trEnd.findViewById(R.id.yPoint) as EditText
        edYPointEnd.setText(endPoint.y.toString())
        edYPointEnd.setOnFocusChangeListener{ view: View, b: Boolean ->
            setVisible(if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE)
            if(edYPointEnd.text.toString() == ""){
                edYPointEnd.setText(endPoint.params.y.toString())
            }
        }
        edYPointEnd.addTextChangedListener{
            if(edYPointEnd.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edYPointEnd.text.toString().toInt() > display.height) {
                    edYPointEnd.setText(display.height.toString())
                    edYPointEnd.setSelection(edYPointEnd.text.length)
                    endPoint.params.y = edYPointEnd.text.toString().toInt()
                }
                else {endPoint.params.y = edYPointEnd.text.toString().toInt()}
                AutoClickService.getWM().updateViewLayout(endPoint.view, endPoint.params)
            }
        }
        trEnd.visibility = View.GONE
        tableLayout.addView(trEnd)
        val buttonShowHideRow = tr.findViewById<View>(R.id.butttonHideShowRow) as Button
        buttonShowHideRow.setBackgroundResource(R.drawable.ic_close_minimal)
        buttonShowHideRow.setOnClickListener {
            trEnd.visibility = if (trEnd.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            if(trEnd.visibility == View.VISIBLE)
                buttonShowHideRow.setBackgroundResource(R.drawable.ic_open_minimal)
            else
                buttonShowHideRow.setBackgroundResource(R.drawable.ic_close_minimal)
        }
    }

    override fun getCommand(): GestureDescription? {
        val builder = GestureDescription.Builder()
        path.offset(getNavigationBar().toFloat() + xCutoutPathHelper(), -yCutout().toFloat())
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, super.duration))
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
                    if(AutoClickService.getParamSizePoint() == context.resources.getStringArray(R.array.arr_size_point_values)[0].toInt())
                        pointLocateHelper = 37;
                    else if(AutoClickService.getParamSizePoint() == context.resources.getStringArray(R.array.arr_size_point_values)[1].toInt())
                        pointLocateHelper = 50;
                    else if(AutoClickService.getParamSizePoint() == context.resources.getStringArray(R.array.arr_size_point_values)[2].toInt())
                        pointLocateHelper = 75;
                    x = event.getX().toInt() - pointLocateHelper +  xCutoutPathHelper()
                    y = event.getY().toInt() - pointLocateHelper - yCutout()
                    path.moveTo(event.getX(), event.getY())
                    coordinateXMove += event.getX()
                    coordinateYMove += event.getY()
                }
                MotionEvent.ACTION_UP->{
                    detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                    wasDraw = true
                    if(coordinateXMove.size == 1) {
                        path.lineTo(coordinateXMove.last() + 100, coordinateYMove.last() + 100)
                        coordinateXMove += coordinateXMove.last() + 100
                        coordinateYMove += coordinateYMove.last() + 100
                    }
                    endPoint.x = coordinateXMove.last().toInt() - pointLocateHelper + xCutoutPathHelper()
                    endPoint.y = coordinateYMove.last().toInt() - pointLocateHelper - yCutout()
                    attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                    updateListener(AutoClickService.getWM(), AutoClickService.getCanvas(),AutoClickService.getParamBound())
                }
                MotionEvent.ACTION_MOVE-> {
                    coordinateXMove += event.getX()
                    coordinateYMove += event.getY()
                    path.lineTo(event.getX(), event.getY())
                    AutoClickService.getCanvas().invalidate()
                }
            }
            return true
        }
    }

    fun initArrowDraw(p:Path, size:Float):Path{
        return with(p){
            lineTo(-size, -size)
            lineTo(35f, 0f)
            lineTo(-35f, 35f)
            close()
            this
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