package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject

class ClickPoint : Point {

    constructor(builder: PointBuilder):super(builder)

    constructor(parcel: Parcel):super(parcel)

    constructor(json: JsonObject):super(json)

    override fun getCommand():GestureDescription? {
        val path = Path()
        path.moveTo(xTouch.toFloat(), yTouch.toFloat())
        val builder = GestureDescription.Builder()
        return builder
                .addStroke(GestureDescription.StrokeDescription(path, delay, duration))
                .build()
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