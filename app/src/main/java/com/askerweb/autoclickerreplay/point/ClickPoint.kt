package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getNavigationBar
import com.google.gson.JsonObject
import java.util.zip.Inflater

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
        val tvNumberPoint = tr.findViewById<View>(R.id.numberPoint) as EditText
        tvNumberPoint.setText(super.text)

        val tvSelectClass = tr.findViewById<View>(R.id.selectClass) as TextView
        tvSelectClass.setText(javaClass.toString().substring(43))

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

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(ClickPoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }
}