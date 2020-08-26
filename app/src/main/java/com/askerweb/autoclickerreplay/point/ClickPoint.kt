package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.activity.TablePointsActivity
import com.askerweb.autoclickerreplay.activity.TablePointsActivity.updateTable
import com.askerweb.autoclickerreplay.ktExt.*
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.askerweb.autoclickerreplay.service.AutoClickService.*
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.list_files.view.*
import kotlin.math.log


class ClickPoint : Point {

    constructor(builder: PointBuilder):super(builder)

    constructor(parcel: Parcel):super(parcel)

    constructor(json: JsonObject):super(json)

    override fun getCommand():GestureDescription? {
        val path = Path()
        path.moveTo(xTouch.toFloat() + getNavigationBar() + randomizePoint(), yTouch.toFloat() + randomizePoint())
        val builder = GestureDescription.Builder()
        return builder
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
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
    }

    fun imageTypePoint(tr: TableRow){
            val linearLayout = tr.findViewById<View>(R.id.linearLayoutTypePoint)
            val imageView = linearLayout.findViewById<View>(R.id.imageType) as ImageView
            imageView.setBackgroundResource(R.drawable.ic_point)
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
        if(super.text.toInt() == AutoClickService.getListPoint().size)
            imageViewButtonDown.setBackgroundResource(R.drawable.ic_arrow_down_disable)
    }

    fun imageBtnUp(tr: TableRow) {
        val linearLayoutButtonUp = tr.findViewById<View>(R.id.linerLayoutUpPoint)
        val imageViewButtonUp = linearLayoutButtonUp.findViewById<View>(R.id.butttonUpPoint) as Button
        if(super.text == "1")
            imageViewButtonUp.setBackgroundResource(R.drawable.ic_arrow_up_disable)
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