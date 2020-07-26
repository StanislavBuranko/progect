package com.askerweb.autoclickerreplay.point;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.askerweb.autoclickerreplay.R;
import com.askerweb.autoclickerreplay.ktExt.LogExt;
import com.askerweb.autoclickerreplay.ktExt.UtilsApp;
import com.askerweb.autoclickerreplay.point.view.PointCanvasView;
import com.askerweb.autoclickerreplay.service.AutoClickService;
import com.askerweb.autoclickerreplay.service.SimulateTouchAccessibilityService;

import java.util.LinkedList;
import java.util.List;

public class RecordPoints {

    static CountDownTimer timer;
    static CountDownTimer timerForSwipe;
    static int xDown, yDown, xMove, yMove, xUp, yUp;
    static long nMs = 0;
    static long nMsNow = 0;
    static long nDurationMsNow = 0;
    static long nDurationMs = 0;
    static Boolean actionUp = false;
    static Boolean actionMove = false;
    static Boolean actionDown = false;
    static Boolean work = false;
    static SwipePoint swipePoint;
    static int pointLocateHelper = 0;
    static Boolean timerForSwipeisStart = false;
    static int delayHollder = 400;

    static public LinkedList<Point> listCommandoNow = new LinkedList<>();

    static boolean timerStart = false;
    static boolean recordPanelInitialization  = false;

    // start timer for delay
    public static void timerStart(){
        timer = new CountDownTimer(9999 * 1000, 10) {
            @Override
            public void onTick(long l) {
                nMs += 10;
            }

            @Override
            public void onFinish() {

            }
        }.start();
        timerStart = true;
    }
    // canel timer for delay
    public static void timerCancel(){
        timer.cancel();
    }
    // start timer for duration
    public static void timerForDurationStart(){
        timerForSwipe = new CountDownTimer(9999 * 1000, 10) {
            @Override
            public void onTick(long l) {
                nDurationMs += 10;
            }

            @Override
            public void onFinish() {

            }
        }.start();
        timerStart = true;
    }
    // canel timer for duration
    public static void timerForDurationCancel(){
        timerForSwipe.cancel();
        nDurationMs = 0;
    }
    // onTouch method for recordPanel
    public static void onTouch(MotionEvent event, WindowManager wm, List<Point> listCommando, PointCanvasView canvasView, float paramSizePoint) {
        if(paramSizePoint == 32)
            pointLocateHelper = 37;
        else if(paramSizePoint == 40)
            pointLocateHelper = 50;
        else if(paramSizePoint == 56)
            pointLocateHelper = 75;

        if(!timerStart)
            timerStart();
        if(!work) {
            work = true;
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (actionUp != true) {
                        actionUp = true;
                        xUp = (int) Math.round(event.getX());
                        yUp = (int) Math.round(event.getY());
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    actionMove = true;
                    actionUp = false;
                    xMove = Math.round(event.getX());
                    yMove = Math.round(event.getY());
                    break;
                case MotionEvent.ACTION_DOWN:
                    if(timerForSwipeisStart)
                        timerForSwipe.cancel();

                    actionDown = true;
                    actionUp = false;
                    actionMove = false;
                    xDown = Math.round(event.getX());
                    yDown = Math.round(event.getY());
                    timerForDurationStart();
                    nDurationMs = 0;
                    AutoClickService.updateLayoutFlagsOn();
                    break;
            }
            if (actionUp == true && actionMove == false && actionDown == true) {
                actionUp = false;
                actionDown = false;
                nMsNow = nMs;
                nDurationMsNow = nDurationMs;
                Point point = Point.PointBuilder.invoke()
                        .position((int) xDown-pointLocateHelper, (int) yDown-pointLocateHelper)
                        .text(String.format("%s", listCommando.size() + 1))
                        .build(ClickPoint.class);

                point.attachToWindow(wm, canvasView);
                point.updateListener(wm,canvasView, AutoClickService.getParamBound());
                point.setTouchable(false, wm);

                listCommando.add(point);

                point.setDelay((long) 1);
                point.setDuration((long) 1);

                SimulateTouchAccessibilityService.execCommand(point, new AccessibilityService.GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);
                        AutoClickService.updateLayoutFlagsOff();
                        Log.d(LogExt.TAG, "gesture completed");
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                        AutoClickService.updateLayoutFlagsOff();
                        Log.d(LogExt.TAG, "gesture cancelled ");
                    }
                });

                point.setDelay(nMsNow);
                point.setDuration(nDurationMsNow);
                nMs = 0;
                nDurationMs = 0;

                timerForDurationCancel();
                actionDown = false;
                actionUp = false;
                actionMove = false;
            }
            else if (actionMove == true && actionUp == true && actionUp == true) {
                boolean isWorkSwipe = false;
                if (xDown - 75 <= xMove && xMove <= xDown + 75 && yDown - 75 <= yMove && yMove <= yDown + 75) {
                    nMsNow = nMs;
                    nDurationMsNow = nDurationMs;
                    Point point = Point.PointBuilder.invoke()
                            .position((int) xDown-pointLocateHelper, (int) yDown-pointLocateHelper)
                            .delay(nMsNow)
                            .text(String.format("%s", listCommando.size() + 1))
                            .build(ClickPoint.class);

                    point.attachToWindow(wm, canvasView);
                    point.updateListener(wm,canvasView, AutoClickService.getParamBound());
                    point.setTouchable(false, wm);

                    listCommando.add(point);

                    point.setDelay((long) 1);
                    point.setDuration((long) 1);

                    SimulateTouchAccessibilityService.execCommand(point, new AccessibilityService.GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            super.onCompleted(gestureDescription);
                            AutoClickService.updateLayoutFlagsOff();
                            Log.d(LogExt.TAG, "gesture completed");
                        }

                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            super.onCancelled(gestureDescription);
                            AutoClickService.updateLayoutFlagsOff();
                            Log.d(LogExt.TAG, "gesture cancelled ");
                        }
                    });

                    point.setDelay(nMsNow);
                    point.setDuration(nDurationMsNow);
                    nMs = 0;
                    nDurationMs = 0;

                    timerForDurationCancel();
                    actionDown = false;
                    actionUp = false;
                    actionMove = false;
                }
                else if (!isWorkSwipe){
                    isWorkSwipe = true;
                    nMsNow = nMs;
                    Point point = (SwipePoint) Point.PointBuilder.invoke()
                            .position((int) xDown-pointLocateHelper, (int) yDown-pointLocateHelper)
                            .delay(nMsNow).duration(nDurationMs)
                            .text(String.format("%s", listCommando.size() + 1))
                            .build(SwipePoint.class);
                    swipePoint = (SwipePoint) point;
                    swipePoint.getNextPoint().setX(xUp-pointLocateHelper);
                    swipePoint.getNextPoint().setY(yUp-pointLocateHelper);

                    point.attachToWindow(wm, canvasView);
                    point.updateListener(wm,canvasView, AutoClickService.getParamBound());
                    point.setTouchable(false, wm);


                    listCommando.add(point);

                    point.setDelay((long) 1);
                    point.setDuration((long) 1);

                    SimulateTouchAccessibilityService.execCommand(point, new AccessibilityService.GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            super.onCompleted(gestureDescription);
                            AutoClickService.updateLayoutFlagsOff();
                            Log.d(LogExt.TAG, "gesture completed");
                        }

                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            super.onCancelled(gestureDescription);
                            AutoClickService.updateLayoutFlagsOff();
                            Log.d(LogExt.TAG, "gesture cancelled ");
                        }
                    });

                    point.setDelay(nMsNow);
                    point.setDuration(nDurationMsNow);
                    nMs = 0;
                    nDurationMs = 0;

                    timerForDurationCancel();
                    actionDown = false;
                    actionUp = false;
                    actionMove = false;
                    isWorkSwipe = false;
                }
            }
        }
        else {}
            work = false;
    }

}
