package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import kotlinx.android.parcel.Parcelize

class SimplePoint:Point{

    init{
        view.setOnLongClickListener { false }
    }

    constructor(parcel: Parcel):super(parcel)

    constructor(builder: PointBuilder):super(builder)

    constructor(json:JsonObject):super(json)

    override fun getCommand():GestureDescription? {
        return null
    }


    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(SimplePoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }
}