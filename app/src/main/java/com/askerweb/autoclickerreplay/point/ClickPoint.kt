package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.context
import com.askerweb.autoclickerreplay.ktExt.getNavigationBar
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.askerweb.autoclickerreplay.service.AutoClickService.*
import com.google.gson.JsonObject


class ClickPoint : Point {

    constructor(builder: PointBuilder):super(builder)

    constructor(parcel: Parcel):super(parcel)

    constructor(json: JsonObject):super(json)

    override fun getCommand():GestureDescription? {
        val path = Path()
        path.moveTo(xTouch.toFloat() + getNavigationBar(), yTouch.toFloat())
        val builder = GestureDescription.Builder()
        return builder
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
    }

    override fun createTableView(tableLayout: TableLayout, inflater: LayoutInflater) {
        val tr = inflater.inflate(R.layout.table_row_for_table_setting_points, null) as TableRow
        val edNumberPoint = tr.findViewById<View>(R.id.numberPoint) as EditText
        edNumberPoint.setText(super.text)
        edNumberPoint.setOnFocusChangeListener { view: View, b: Boolean ->
            super.view.visibility = if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE
            if(edNumberPoint.text.toString() == "") {
                edNumberPoint.setText(super.text)
            }
        }
        edNumberPoint.addTextChangedListener{
            if(edNumberPoint.text.toString() != "") {
                val tempPoint = getListPoint().get(super.text.toInt()-1)
                val tempTextPoint = super.text.toInt()
                val edNumberPointCorrect = if(edNumberPoint.text.toString().toInt() > getListPoint().size)
                    getListPoint().size-1
                else
                    edNumberPoint.text.toString().toInt()-1

                getListPoint().set(super.text.toInt()-1, getListPoint().get(edNumberPointCorrect))
                getListPoint().set(edNumberPointCorrect, tempPoint)
                getListPoint().get(super.text.toInt()-1).text = tempTextPoint.toString()
                getListPoint().get(edNumberPointCorrect).text = (edNumberPointCorrect+1).toString()
                tableLayout.removeAllViews()
                val trHeading = inflater.inflate(R.layout.table_row_heading, null) as TableRow
                tableLayout.addView(trHeading)
                getListPoint().forEach {point ->
                    point.createTableView(tableLayout, inflater)
                }
            }
        }

        val tvSelectClass = tr.findViewById<View>(R.id.selectClass) as TextView
        tvSelectClass.setText("Click")

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
                getWM().updateViewLayout(super.view, super.params)
            }
        }

        val edYPoint = tr.findViewById<View>(R.id.yPoint) as EditText
        edYPoint.setText(super.y.toString())
        edYPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            super.view.visibility = if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE
            if(edYPoint.text.toString() == "") {
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
                getWM().updateViewLayout(super.view, super.params)
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

        val edDurationPoint = tr.findViewById<View>(R.id.durationPoint) as EditText
        edDurationPoint.setText(super.duration.toString())
        edDurationPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            super.view.visibility = if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE
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

        val edRepeatPoint = tr.findViewById<View>(R.id.repeatPoint) as EditText
        edRepeatPoint.setText(super.repeat.toString())
        edRepeatPoint.setOnFocusChangeListener{ view: View, b: Boolean ->
            super.view.visibility = if(super.view.visibility == View.GONE) View.VISIBLE else View.GONE
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
        tableLayout.addView(tr)
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(ClickPoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }
}