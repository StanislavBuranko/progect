package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getWindowsTypeApplicationOverlay
import com.askerweb.autoclickerreplay.point.Point.PointBuilder.Companion.invoke
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.askerweb.autoclickerreplay.service.AutoClickService.listCommando
import com.google.gson.JsonObject

class MultiPoint : Point {

    constructor(builder: PointBuilder): super(builder)

    constructor(parcel: Parcel):super(parcel){

    }

    constructor(json: JsonObject):super(json){

    }

    init{
        showDialog()
    }


    override fun getCommand(): GestureDescription? {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return PointBuilder.invoke().buildFrom(MultiPoint::class.java, parcel)
        }

        override fun newArray(size: Int): Array<MultiPoint?> {
            return arrayOfNulls(size)
        }
    }

    fun showDialog(){
        val viewContent: View = createViewDialog()
        val dialog = AlertDialog.Builder(view.context)
                .setTitle(view.context.getString(R.string.setting_point))
                .setView(viewContent)
                .setPositiveButton(R.string.save) { _, _ ->
                    val countListCommand: Int = listCommando.size;
                    val countEditText: Int? = view.findViewById<EditText>(R.id.editNumbMultiPoint).text.toString().toInt()
                    for(n in 1..countEditText!!) {
                        var point:Point? = null;
                        point = invoke()
                                .position(x+n*10, y+n*10)
                                .text(String.format("%s", countListCommand))
                                .build(ClickPoint::class.java)
                        point!!.attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                        listCommando.add(point)
                    }
                }
                .setNegativeButton(R.string.canel){ _, _ ->

                }
                .create()
        dialog.window?.setType(getWindowsTypeApplicationOverlay())
        dialog.show()
    }

    protected override fun createViewDialog():View{
        return LayoutInflater.from(view.context).inflate(R.layout.multi_point_dialog, null)
    }

    inner class DrawPathOnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when(event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_DOWN->{

                }
                MotionEvent.ACTION_POINTER_DOWN->{

                }
                MotionEvent.ACTION_UP->{

                }
                MotionEvent.ACTION_POINTER_UP->{

                }
            }
            return true
        }

    }

}