package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import android.app.AlertDialog
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.getWindowsParameterLayout
import com.askerweb.autoclickerreplay.point.view.PointCanvasView
import com.google.gson.JsonObject

class MultiPoint : Point {

    var wasDraw = false

    val path = Path()
    val points = arrayOf( PointBuilder.invoke()
            .position(x,y)
            .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.draw_point_click)!!)
            .build(SimplePoint::class.java));



    val panel = LinearLayout(App.getContext())
    val panelParam = getWindowsParameterLayout(
            WindowManager.LayoutParams.MATCH_PARENT.toFloat(),
            WindowManager.LayoutParams.MATCH_PARENT.toFloat(),
            Gravity.CENTER)

    override var drawableViewDefault: Drawable = ContextCompat.getDrawable(App.getContext(), R.drawable.point_solid)!!


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

    val layoutInflater: LayoutInflater? = null
    fun showDialog(){
        val builder = AlertDialog.Builder(App.getContext()).create()
        val editView = layoutInflater?.inflate(R.layout.dialog_multi_click_creator, null)
        builder.setView(editView)
        builder.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",{ _, _ ->
            Log.d("msgBuilderDialog", "allOk")
        })
        builder.show();
    }
    inner class DrawPathOnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when(event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_DOWN->{


                }
                MotionEvent.ACTION_POINTER_DOWN->{
                    points.plus(PointBuilder.invoke()
                            .position(event.rawX.toInt(),event.rawY.toInt())
                            .drawable(ContextCompat.getDrawable(App.getContext(), R.drawable.draw_point_click)!!)
                            .build(SimplePoint::class.java))
                    path.moveTo(x.toFloat(), y.toFloat())
                    Log.d("multi", "2");
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