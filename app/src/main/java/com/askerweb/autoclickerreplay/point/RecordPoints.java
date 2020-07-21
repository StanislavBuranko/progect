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
    static Integer i;
    static Boolean openRecordPanel = false;
    static float xDown, yDown;
    static Integer nMs = 0;
    static Integer nForDurationMs = 0;
    static Boolean actionUp = false;
    static Boolean actionMove = false;
    static Boolean actionDown = false;
    static Boolean work = false;
    static Point point;
    static SwipePoint swipePoint;
    static long nMsNow = 0;
    static Integer xMove = 0;
    static Integer yMove = 0;
    static Boolean pointMicroMove = false;
    static View recordPanel;
    static Boolean timerForSwipeisStart = false;
    static int delayHollder = 500;

    static public LinkedList<Point> listCommandoNow = new LinkedList<>();

    public static final WindowManager.LayoutParams paramsRecordPanelFlagsOff =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.CENTER);
    public static final WindowManager.LayoutParams paramsRecordPanelFlagsOn =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.CENTER,  WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    static boolean timerStart = false;
    static boolean recordPanelInitialization  = false;

    static public  boolean recordPanelInitialization() {
        recordPanel = LayoutInflater.from(AutoClickService.getService()).inflate(R.layout.record_panel, null);
        recordPanelInitialization = true;
        return  true;
    }

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
    public static void timerCancel(){
        timer.cancel();
    }

    public static void timerForDurationStart(){
        timerForSwipe = new CountDownTimer(9999 * 1000, 10) {
            @Override
            public void onTick(long l) {
                nForDurationMs += 10;
            }

            @Override
            public void onFinish() {

            }
        }.start();
        timerStart = true;
    }
    public static void timerForDurationCancel(){
        timerForSwipe.cancel();
        nForDurationMs = 0;
    }

    public static void onTouch(MotionEvent event, WindowManager wm, List<Point> listCommando, PointCanvasView canvasView) {
        int xUp = 0;
        int yUp = 0;
        if(!recordPanelInitialization)
            recordPanelInitialization();
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
                    nForDurationMs = 0;
                    break;
            }
            if (actionUp == true && actionMove == false && actionDown == true) {
                actionUp = false;
                actionDown = false;
                nMsNow = nMs;
                point = Point.PointBuilder.invoke()
                        .position((int) xDown-75, (int) yDown-75)
                        .delay(nMsNow).duration(nForDurationMs)
                        .text(String.format("%s", listCommando.size() + 1))
                        .build(ClickPoint.class);

                point.attachToWindow(wm, canvasView);
                listCommando.add(point);

                point.setTouchable(false, wm);

                point.setDelay((long) 1);
                listCommandoNow.add(point);

                AutoClickService.updateLayoutFlagsOn();
                SimulateTouchAccessibilityService.requestStart(listCommandoNow);
                listCommandoNow.clear();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        AutoClickService.updateLayoutFlagsOff();
                    }
                }, delayHollder);
                point.setDelay(nMsNow);
                nMs = 0;
                nForDurationMs = 0;
                timerForDurationCancel();
            }
            else if (actionMove == true && actionUp == true && actionUp == true) {
                boolean isWorkSwipe = false;
                if (xDown - 75 <= xMove && xMove <= xDown + 75 && yDown - 75 <= yMove && yMove <= yDown + 75) {
                    nMsNow = nMs;
                    point = Point.PointBuilder.invoke()
                            .position((int) xDown-75, (int) yDown-75)
                            .delay(nMsNow)
                            .text(String.format("%s", listCommando.size() + 1))
                            .build(ClickPoint.class);

                    point.attachToWindow(wm, canvasView);
                    listCommando.add(point);

                    point.setTouchable(false, wm);


                    point.setDelay((long) 1);
                    listCommandoNow.add(point);

                    point.setTouchable(false, wm);


                    AutoClickService.updateLayoutFlagsOn();

                    SimulateTouchAccessibilityService.requestStart(listCommandoNow);
                    listCommandoNow.clear();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            AutoClickService.updateLayoutFlagsOff();
                        }
                    }, delayHollder);
                    point.setDelay(nMsNow);
                    nMs = 0;
                    timerForDurationCancel();

                }
                else if (!isWorkSwipe){
                    isWorkSwipe = true;
                    nMsNow = nMs;
                    point = Point.PointBuilder.invoke()
                            .position((int) xDown-75, (int) yDown-75)
                            .delay(nMsNow).duration(nForDurationMs)
                            .text(String.format("%s", listCommando.size() + 1))
                            .build(SwipePoint.class);
                    swipePoint = (SwipePoint) point;
                    swipePoint.getNextPoint().setX(xUp-75);
                    swipePoint.getNextPoint().setY(yUp-75);

                    point.attachToWindow(wm, canvasView);

                    point.setTouchable(false, wm);


                    listCommando.add(point);

                    point.setDelay((long) 1);
                    listCommandoNow.add(point);

                    AutoClickService.updateLayoutFlagsOn();

                    Log.d("123qwe",""+point.getDuration());

                    SimulateTouchAccessibilityService.requestStart(listCommandoNow);
                    listCommandoNow.clear();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            AutoClickService.updateLayoutFlagsOff();
                        }
                    }, delayHollder);


                    point.setDelay(nMsNow);
                    nMs = 0;
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
