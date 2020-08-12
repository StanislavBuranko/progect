package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat.startActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.context
import com.askerweb.autoclickerreplay.ktExt.getDialogTitle
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
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
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(HomePoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }

    override fun createTableView(tableLayout: TableLayout, inflater: LayoutInflater) {
        val tr = inflater.inflate(R.layout.table_row_for_table_setting_points, null) as TableRow
        val tvNumberPoint = tr.findViewById<View>(R.id.numberPoint) as EditText
        tvNumberPoint.setText(super.text)

        val tvSelectClass = tr.findViewById<View>(R.id.selectClass) as TextView
        tvSelectClass.setText("HomePoint")

        val tvXPoint = tr.findViewById<View>(R.id.xPoint) as EditText
        tvXPoint.setText(super.x.toString())

        val tvYPoint = tr.findViewById<View>(R.id.yPoint) as EditText
        tvYPoint.setText(super.y.toString())

        val tvDelayPoint = tr.findViewById<View>(R.id.delayPoint) as EditText
        tvDelayPoint.setText(super.delay.toString())

        val tvDurationPoint = tr.findViewById<View>(R.id.durationPoint) as EditText
        tvDurationPoint.setText(super.duration.toString())

        val tvRepeatPoint = tr.findViewById<View>(R.id.repeatPoint) as EditText
        tvRepeatPoint.setText(super.repeat.toString())
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