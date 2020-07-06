package com.askerweb.autoclickerreplay.point.view

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

class PointView constructor(context: Context) : FrameLayout(context) {
    private var textV: TextView = TextView(context)
    var text: String
        get() = textV.text as String
        set(value){
            textV.text = value
        }

    init {
        systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        textV.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        addView(textV)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }


}