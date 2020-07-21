package com.askerweb.autoclickerreplay.point.view

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.askerweb.autoclickerreplay.R

class PointView constructor(context: Context) : FrameLayout(context) {
    private var textV: TextView = TextView(context)
    var text: String
        get() = textV.text as String
        set(value){
            textV.text = value
        }

    var textVisible
        get() = textV.visibility == View.GONE
        set(value){
            textV.visibility = if(value) View.VISIBLE else View.GONE
        }

    init {
        systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        addView(textV)
        textV.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        val param = FrameLayout.LayoutParams(textV.layoutParams)
        param.gravity = Gravity.CENTER
        param.width = ViewGroup.LayoutParams.WRAP_CONTENT
        param.height = ViewGroup.LayoutParams.WRAP_CONTENT
        textV.textSize = resources.getDimension(R.dimen.xsmall_text_size)
        textV.setTextColor(resources.getColor(R.color.blueSanMarino))
        textV.typeface = Typeface.createFromAsset(context.assets, "fonts/Roboto-Bold.ttf")
        textV.layoutParams = param
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }


}