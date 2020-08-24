@file:JvmMultifileClass

package com.askerweb.autoclickerreplay.point

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TableLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.*
import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.askerweb.autoclickerreplay.point.view.PointOnTouchListener
import com.askerweb.autoclickerreplay.point.view.PointView
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.synthetic.main.dialog_setting_point.*
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Named
import kotlin.properties.Delegates

abstract class Point : PointCommand, Parcelable, Serializable{
    public var params: WindowManager.LayoutParams =
            getWindowsParameterLayout(32f,
                    32f,
                    Gravity.START or Gravity.TOP,
                    getParamOverlayFlags() or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

    val view: PointView = PointView(App.appComponent.getAppContext())

    val xTouch:Int
        get() = params.x + (params.width / 2)

    val yTouch:Int
        get() = params.y + (params.height / 2)

    var x:Int
        get() = params.x
        set(value){
            params.x = value
        }

    var y:Int
        get() = params.y
        set(value){
            params.y = value
        }

    var width:Int
        get() = params.width
        set(value){
            params.width = if (value < 0) value else Dimension.DP.convert(value.toFloat()).toInt()
        }

    var height:Int
        get() = params.height
        set(value){
            params.height = if(value < 0) value else Dimension.DP.convert(value.toFloat()).toInt()
        }

    open var text:String
        get() = view.text
        set(value){
            view.text = value
        }

    var delay by Delegates.notNull<Long>()
    var duration by Delegates.notNull<Long>()
    var repeat by Delegates.notNull<Int>()

    @IgnoredOnParcel @Transient
    var counterRepeat:Int = 0

    open val drawableViewDefault: Drawable = ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.point_click)!!

    @IgnoredOnParcel @Transient
    val gson: Gson = App.appComponent.getGson()

    @IgnoredOnParcel @Transient
    @Inject
    lateinit var appContext: Context

    @IgnoredOnParcel @Transient
    @Inject @field:[Named("ActivityContext")]
    lateinit var mainActivityContext:Context


    init{
        view.setOnLongClickListener {
            this.showEditDialog()
            true
        }
    }

    constructor(parcel: Parcel?){
        repeat = parcel?.readInt()!!
        delay = parcel.readLong()
        duration = parcel.readLong()
        val params =
                parcel.readParcelable<WindowManager.LayoutParams>(WindowManager.LayoutParams::class.java.classLoader)
        this.params.width = params?.width!!
        this.params.height = params.height
        this.params.x = params.x
        this.params.y = params.y
        text = parcel.readString()!!
    }

    constructor(json: JsonObject){
        repeat = json.get("repeat").asInt
        delay = json.get("delay").asLong
        duration = json.get("duration").asLong
        val params =
                gson.fromJson(json.get("params").asString, WindowManager.LayoutParams::class.java)
        this.params.width = params.width
        this.params.height = params.height
        this.params.x = params.x
        this.params.y = params.y
        text = json.get("text").asString
    }

    constructor(builder: PointBuilder) : this(builder.x, builder.y, builder.width,
            builder.height, builder.text, builder.delay, builder.duration, builder.repeat)

    constructor(x: Int, y: Int, width: Int, height: Int, text: String, delay: Long, duration: Long, repeat: Int){
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.text = text
        this.delay = delay
        this.duration = duration
        this.repeat = repeat
    }

    open fun updateListener(wm:WindowManager, canvas: PointCanvasView, bounds: Boolean){
        val l = PointOnTouchListener.create(this, wm, canvas, bounds);
        view.setOnTouchListener(l);
    }

    open fun updateViewLayout(wm: WindowManager, size:Float){
        params.width = Dimension.DP.convert(size).toInt()
        params.height = params.width
        wm.updateViewLayout(view, params)
    }

    open fun attachToWindow(wm: WindowManager, canvas: PointCanvasView){
        wm.addView(view, params)
        canvas.invalidate()
    }

    open fun detachToWindow(wm: WindowManager, canvas: PointCanvasView){
        wm.removeView(view)
        canvas.invalidate()
    }

    open fun setTouchable(touchable: Boolean, wm:WindowManager){
        if(touchable){
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            view.isClickable = true
        }
        else{
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            view.isClickable = false
        }
        wm.updateViewLayout(view, params)
    }
    open fun updateParamsFlags(){
        params.flags = getParamOverlayFlags() or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    }
    open fun setVisible(visible:Int){
        view.visibility = visible
    }

    fun isTouchable() =
            (params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) != WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE


    fun toJson(): String {
        val obj = toJsonObject()
        return gson.toJson(obj)
    }

    open fun toJsonObject():JsonObject{
        val obj = JsonObject()
        obj.addProperty("class", this::class.java.name)
        obj.addProperty("text", text)
        obj.addProperty("params", gson.toJson(params))
        obj.addProperty("delay", delay)
        obj.addProperty("duration", duration)
        obj.addProperty("repeat", repeat)
        return obj
    }

    open fun createTableView(tableLayout: TableLayout, inflater: LayoutInflater) {
    }



    open fun showEditDialog(){
        val viewContent: View = createViewDialog()
        val holder = createHolderDialog(viewContent)
        holder.updateViewDialogParam()
        val title = getDialogTitle(view.context, view.context.getString(R.string.setting_point))
        val dialog = AlertDialog.Builder(view.context, R.style.AppDialog)
                .setCustomTitle(title)
                .setView(viewContent)
                .setPositiveButton(R.string.save) { _, _ ->
                    holder.saveEditDialog()
                    AutoClickService.getCanvas()?.invalidate()
                    AutoClickService.getTvTimer().setText(AutoClickService.getTime())
                }.create()
        holder.dialog = dialog
        dialog.window?.setType(getWindowsTypeApplicationOverlay())
        dialog.show()
        holder.saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    }

    protected open fun createHolderDialog(viewContent:View): AbstractViewHolderDialog {
        return PointHolderDialogEdit(viewContent, this)
    }

    protected open fun createViewDialog():View{
        return LayoutInflater.from(ContextThemeWrapper(App.activityComponent.getActivityContext(), R.style.AppDialogGradient))
                .inflate(R.layout.dialog_setting_point, null, false)
    }

    class PointHolderDialogEdit(override val containerView:View, private val point: Point) :
            AbstractViewHolderDialog(), LayoutContainer {

        init{

            btn_duplicate.setOnClickListener{
                // Duplicate this point
                AutoClickService.requestAction(point.appContext, AutoClickService.ACTION_DUPLICATE_POINT, AutoClickService.KEY_POINT, point)
                dialog?.cancel()
            }

            btn_delete.setOnClickListener{
                // Delete this point
                AutoClickService.requestAction(point.appContext, AutoClickService.ACTION_DELETE_POINT, AutoClickService.KEY_POINT, point)
                dialog?.cancel()
            }

            editDelay.doAfterTextChanged{
                requireSettingEdit()
            }

            editDuration.doAfterTextChanged{
                requireSettingEdit()
            }

            editRepeat.doAfterTextChanged{
                requireSettingEdit()
            }


            editDelay.addTextChangedListener {
                if (editDelay.text.toString() != "")
                    if (editDelay.text.toString().toInt() < 0)
                        editDelay.setText((0).toString())
                    else if (editDelay.text.toString().toInt() > 9999999)
                        editDelay.setText((9999999).toString())
                editDelay.setSelection(editDelay.text.length)
            }

            editDuration.addTextChangedListener {
                if (editDuration.text.toString() != "")
                    if (editDuration.text.toString().toInt() < 0)
                        editDuration.setText((0).toString())
                    else if (editDuration.text.toString().toInt() > 60000)
                        editDuration.setText((60000).toString())
                editDuration.setSelection(editDuration.text.length)
            }

            editRepeat.addTextChangedListener {
                if (editRepeat.text.toString() != "")
                    if (editRepeat.text.toString().toInt() < 0)
                        editRepeat.setText((0).toString())
                    else if (editRepeat.text.toString().toInt() > 999999)
                        editRepeat.setText((999999).toString())
                editRepeat.setSelection(editRepeat.text.length)
            }

        }

        override fun updateViewDialogParam(){
            editDelay.setText("${point.delay}")
            editDuration.setText("${point.duration}")
            editRepeat.setText("${point.repeat}")
        }

        override fun saveEditDialog(){
            point.delay = if(editDelay.text.toString() != "") editDelay.text.toString().toLong() else 0
            point.duration = if(editDuration.text.toString() != "") editDuration.text.toString().toLong() else 0
            point.repeat = if(editRepeat.text.toString() != "") editRepeat.text.toString().toInt() else 0
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

    companion object Factory{

        @JvmStatic fun <T:Point> newPoint(clazz:Class<out T>): T{
            return clazz.newInstance()
        }
        @JvmStatic fun <T:Point> newPoint(clazz:Class<out T>, builder: PointBuilder): T{
            return clazz.getConstructor(PointBuilder::class.java).newInstance(builder)
        }
        @JvmStatic fun <T:Point> newPoint(clazz:Class<out T>, json:JsonObject): T{
            return clazz.getConstructor(JsonObject::class.java).newInstance(json)
        }
        @JvmStatic fun <T:Point> newPoint(clazz:Class<out T>, parcel: Parcel?): T{
            return clazz.getConstructor(Parcel::class.java).newInstance(parcel)
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(repeat)
        dest?.writeLong(delay)
        dest?.writeLong(duration)
        dest?.writeParcelable(params, flags)
        dest?.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    open fun swapPointOrientation() {
        val temp = params.x
        x = params.y + xCutoutPathHelper()
        y = temp
    }

    open fun swapPointOrientationLandscapeToLandscape() {
    }

    class PointBuilder private constructor(var factory:(Class<out Point>, PointBuilder)->Point){
        var x:Int = 0;
        var y:Int = 0;
        var width:Int = getSetting(KEY_SIZE_POINT, defaultSizePoint)
                ?: defaultSizePoint
        var height:Int = width
        var text:String = ""
        var delay:Long = 0L
        var duration:Long = 10L
        var repeat:Int = 1
        var drawable: Drawable? = null

        var doAfter = { p:Point ->
            App.activityComponent.inject(p)
            p.view.background = if (drawable != null) drawable else p.drawableViewDefault
        }

        fun position(x:Int, y:Int):PointBuilder{
            this.x = x
            this.y = y
            return this
        }

        fun text(text:String):PointBuilder {
            this.text = text
            return this
        }

        fun delay(delay:Long):PointBuilder{
            this.delay = delay
            return this
        }

        fun duration(duration:Long):PointBuilder{
            this.duration = duration
            return this
        }

        fun repeat(repeat:Int):PointBuilder{
            this.repeat = repeat
            return this
        }

        fun size(width:Int, height:Int){
            this.width = width
            this.height = height
        }

        fun drawable(drawable: Drawable):PointBuilder{
            this.drawable = drawable
            return this
        }

        fun build(clazz: Class<out Point>):Point {
            return with(factory(clazz, this))
            {
                doAfter(this)
                this
            }
        }

        fun buildFrom(clazz: Class<out Point>, parcel: Parcel?):Point{
            return with(newPoint(clazz, parcel)){
                doAfter(this)
                this
            }
        }

        fun buildFrom(clazz: Class<out Point>, json: JsonObject):Point{
            return with(newPoint(clazz, json)){
                doAfter(this)
                this
            }
        }

        companion object {
           @JvmStatic fun invoke() = PointBuilder(Factory::newPoint)
        }
    }
}