package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.KeyEvent.KEYCODE_BACK
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.context
import com.askerweb.autoclickerreplay.ktExt.getDialogTitle
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.gson.JsonObject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_setting_point.*

class HomePoint : Point {

    constructor(builder: PointBuilder):super(builder)

    constructor(parcel: Parcel):super(parcel)

    constructor(json: JsonObject):super(json)

    fun getCommandMain() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(context,startMain, Bundle.EMPTY)
        /*simulateKey(KEYCODE_BACK)*/
        //val drive: AndroidDriver = driver.pressKey(new KeyEvent(AndroidKey.BACK));
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(HomePoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }

    /*fun simulateKey(KeyCode: Int) {
        object : Thread() {
            override fun run() {
                try {
                    val inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyCode)
                } catch (e: Exception) {

                }
            }
        }.start()
    }*/

    override val drawableViewDefault = ContextCompat.getDrawable(App.appComponent.getAppContext(), R.drawable.draw_point_home)!!

    override fun createTableView(tableLayout: TableLayout, inflater: LayoutInflater) {
        val tr = inflater.inflate(R.layout.table_row_for_table_setting_points, null) as TableRow

        val edXPoint = tr.findViewById<View>(R.id.xPoint) as EditText
        edXPoint.setText(super.x.toString())
        edXPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            super.view.visibility = if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE
            if(edXPoint.text.toString() == ""){
                edXPoint.setText(super.params.x.toString())
                edXPoint.setSelection(edXPoint.text.length)
            }
        }
        edXPoint.addTextChangedListener{
            if(edXPoint.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edXPoint.text.toString().toInt() > display.width) {
                    edXPoint.setText(display.width.toString())
                    super.params.x = edXPoint.text.toString().toInt()
                }
                else {super.params.x = edXPoint.text.toString().toInt()}
                AutoClickService.getWM().updateViewLayout(super.view, super.params)
            }
        }

        val edYPoint = tr.findViewById<View>(R.id.yPoint) as EditText
        edYPoint.setText(super.y.toString())
        edYPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            super.view.visibility = if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE
            if(edYPoint.text.toString() == ""){
                edYPoint.setText(super.y.toString())
                edYPoint.setSelection(edXPoint.text.length)
            }

        }
        edYPoint.addTextChangedListener{
            if(edYPoint.text.toString() != "") {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                if(edYPoint.text.toString().toInt() > display.height) {
                    edYPoint.setText(display.height.toString())
                    super.params.y = edYPoint.text.toString().toInt()
                }
                else {super.params.y = edYPoint.text.toString().toInt()}
                AutoClickService.getWM().updateViewLayout(super.view, super.params)
            }
        }

        val edDelayPoint = tr.findViewById<View>(R.id.delayPoint) as EditText
        edDelayPoint.setText(super.delay.toString())
        edDelayPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            super.view.visibility = if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE
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


        val linearLayoutHideShowRow = tr.findViewById<View>(R.id.linearLayoutButtonShowHideRow)
        val imageViewHideShowRow = linearLayoutHideShowRow.findViewById<View>(R.id.butttonHideShowRow) as Button
        imageViewHideShowRow.setBackgroundResource(R.drawable.ic_disable_minimal)

        val linearLayoutTypePoint = tr.findViewById<View>(R.id.linearLayoutTypePoint)
        val imageViewTypePoint = linearLayoutTypePoint.findViewById<View>(R.id.imageType) as ImageView
        imageViewTypePoint.setBackgroundResource(R.drawable.ic_home_point)

        tr.removeAllViews()
        tr.addView(linearLayoutHideShowRow)
        tr.addView(linearLayoutTypePoint)
        tr.addView(edXPoint)
        tr.addView(edYPoint)
        tr.addView(edDelayPoint)
        tableLayout.addView(tr)
    }

    override fun getCommand(): GestureDescription? {
        TODO("Not yet implemented")
    }

    override open fun createHolderDialog(viewContent:View): AbstractViewHolderDialog {
        return PointHolderDialogEdit(viewContent, this)
    }

    override open fun createViewDialog():View{
        return LayoutInflater.from(ContextThemeWrapper(App.activityComponent.getActivityContext(), R.style.AppDialogGradient))
                .inflate(R.layout.dialog_setting_home_point, null, false)
    }

    override fun showEditDialog(){
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
                }.create()
        holder.dialog = dialog
        dialog.window?.setType(getWindowsTypeApplicationOverlay())
        dialog.show()
        holder.saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    }

    class PointHolderDialogEdit(override val containerView:View, private val point: Point) :
            AbstractViewHolderDialog(), LayoutContainer {

        init{

            btn_duplicate.setOnClickListener{
                // Duplicate this point
                AutoClickService.requestAction(point.appContext ,AutoClickService.ACTION_DUPLICATE_POINT, AutoClickService.KEY_POINT, point)
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

            editDelay.addTextChangedListener {
                if (editDelay.text.toString() != "")
                    if (editDelay.text.toString().toInt() < 0)
                        editDelay.setText((0).toString())
                    else if (editDelay.text.toString().toInt() > 9999999)
                        editDelay.setText((9999999).toString())
                editDelay.setSelection(editDelay.text.length)
            }
        }

        override fun updateViewDialogParam(){
            editDelay.setText("${point.delay}")
        }

        override fun saveEditDialog(){
            point.delay = editDelay.text.toString().toLong()
        }

        override fun requireSettingEdit(){
            saveButton?.isEnabled = isRequire()
        }

        override fun isRequire():Boolean{
            val delayRequire = editDelay.text.isNotEmpty() &&
                    Integer.parseInt(editDelay.text.toString()) >= 0
            return delayRequire
        }
    }
}