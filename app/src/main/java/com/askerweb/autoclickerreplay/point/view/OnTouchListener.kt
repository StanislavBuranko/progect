package com.askerweb.autoclickerreplay.point.view

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.askerweb.autoclickerreplay.ktExt.logd
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.pow
import kotlin.math.roundToInt

abstract class OnTouchListener(private val wm: WindowManager,
                               private var screenWidth: Int = -1,
                               private var screenHeight: Int = -1) : View.OnTouchListener {

    private var timerLongClick: Timer? = null

    var initialX = 0
    var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var isDrag = false
    private val startDragDistance: Int = 10

    open var x: Int = 0
    open var y: Int = 0

    open var updateView = {}

    open var calcNewPositionAndSet = { xDiff:Int, yDiff:Int, v:View ->
        //Calculate the X and Y coordinates of the view.
        val newX = initialX + xDiff
        val newY = initialY + yDiff

        x = if(canMoveX(newX, v.width)) newX else x
        y = if(canMoveY(newY, v.height)) newY else y
    }

    open fun canMoveX(newX:Int, size:Int) =
            ((screenWidth < 0 && screenHeight < 0) || ((screenWidth > -1 && screenHeight > -1) && (newX >= 0  && newX <= screenWidth - size)))

    open fun canMoveY(newY:Int, size:Int) =
            ((screenWidth < 0 && screenHeight < 0) || ((screenWidth > -1 && screenHeight > -1) && (newY >= 0  && newY <= screenHeight - size)))

    open var initPositionTouch = { x:Float, y:Float ->
        initialTouchX = x
        initialTouchY = y
    }

    private fun isDragging(event: MotionEvent): Boolean =
            (((event.rawX - initialTouchX).toDouble().pow(2.0)
                    + (event.rawY - initialTouchY).toDouble().pow(2.0))
                    > startDragDistance * startDragDistance)

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                //remember the initial position.
                initialX = x
                initialY = y

                //get the touch location
                initPositionTouch(event.rawX, event.rawY)

                v.isPressed = true
                isDrag = false
                timerLongClick = Timer()
                timerLongClick?.schedule(700){
                    v.handler.post { v.performLongClick() }
                }
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN ->{
                return true
            }
            MotionEvent.ACTION_POINTER_UP ->{
                return true
            }
            MotionEvent.ACTION_UP -> {
                timerLongClick?.cancel()
                v.isPressed = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDrag && isDragging(event)) {
                    isDrag = true
                }
                if(isDrag){
                    timerLongClick?.cancel()
                    val xDiff = (event.rawX - initialTouchX).roundToInt()
                    val yDiff = (event.rawY - initialTouchY).roundToInt()

                    calcNewPositionAndSet(xDiff, yDiff, v)

                    //Update the layout with new X & Y coordinates
                    updateView()
                }
                return true
            }
        }
        return false
    }

}

class ViewOverlayOnTouchListener(private val view: View, private val wm: WindowManager) : OnTouchListener(wm){
    override var updateView = {wm.updateViewLayout(view, view.layoutParams)}
    private val param = (view.layoutParams as WindowManager.LayoutParams)
    override var x: Int
        get() = param.x
        set(value) {
            param.x = value
        }
    override var y: Int
        get() = param.y
        set(value) {
            param.y = value
        }
}
