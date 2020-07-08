package com.askerweb.autoclickerreplay.point;

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
import com.askerweb.autoclickerreplay.ktExt.UtilsApp;
import com.askerweb.autoclickerreplay.point.view.DataTouch;
import com.askerweb.autoclickerreplay.point.view.PointCanvasView;
import com.askerweb.autoclickerreplay.service.AutoClickService;
import com.askerweb.autoclickerreplay.service.SimulateTouchAccessibilityService;

import java.util.LinkedList;

public class RecordPoints {

    static CountDownTimer timer;
    static Integer i;
    static Boolean openRecordPanel = false;
    static float xDown, yDown;
    static Integer nMs = 0;
    static Boolean actionUp = false;
    static Boolean actionMove = false;
    static Boolean actionDown = false;
    static Boolean work = false;
    static Point point;
    static long nMsNow = 0;
    static Integer xMove = 0;
    static Integer yMove = 0;
    static Boolean pointMicroMove = false;
    static View recordPanel;

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

    public static   boolean timerStart(){
        timer = new CountDownTimer(9999 * 1000, 10) {
            @Override
            public void onTick(long l) {
                nMs += 10;
                Log.d("" + nMs, "");
            }

            @Override
            public void onFinish() {

            }
        };
        timerStart = true;
        return true;
    }

    public static boolean onTouch(MotionEvent event, WindowManager wm, LinkedList<Point> listCommando, PointCanvasView canvasView) {
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
                        DataTouch.setXUp((int) Math.round(event.getX()));
                        DataTouch.setYUp((int) Math.round(event.getY()));
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    actionMove = true;
                    actionUp = false;
                    xMove = Math.round(event.getX());
                    yMove = Math.round(event.getY());
                    break;
                case MotionEvent.ACTION_DOWN:
                    actionDown = true;
                    actionUp = false;
                    actionMove = false;
                    xDown = Math.round(event.getX());
                    yDown = Math.round(event.getY());
                    break;
            }
            if (actionUp == true && actionMove == false && actionDown == true) {

                actionUp = false;
                actionDown = false;

                nMsNow = nMs;
                point = Point.PointBuilder.invoke()
                        .position((int) xDown, (int) yDown)
                        .delay(nMsNow)
                        .text(String.format("%s", listCommando.size() + 1))
                        .build(ClickPoint.class);

                point.attachToWindow(wm, canvasView);
                listCommando.add(point);

                point.setDelay((long) 1);
                listCommandoNow.add(point);

                listCommando.forEach((c) -> c.setTouchable(false, wm));
                AutoClickService.updateLayoutFlagsOn();
                SimulateTouchAccessibilityService.requestStart(listCommandoNow);
                listCommandoNow.clear();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        AutoClickService.updateLayoutFlagsOff();
                    }
                }, 500);
                point.setDelay(nMs);
                nMs = 0;
            }
            else if (actionMove == true && actionUp == true && actionUp == true) {
                if (xDown - 75 <= xMove && xMove <= xDown + 75 && yDown - 75 <= yMove && yMove <= yDown + 75) {
                    Log.d("pointCAl", "xDown:" + xDown + " xMove:" + xMove + " yDown:" + yDown + " yMove:" + yMove);
                    nMsNow = nMs;
                    point = Point.PointBuilder.invoke()
                            .position((int) xMove, (int) yMove)
                            .delay(nMsNow)
                            .text(String.format("%s", listCommando.size() + 1))
                            .build(ClickPoint.class);

                    point.attachToWindow(wm, canvasView);
                    listCommando.add(point);

                    point.setDelay((long) 1);
                    listCommandoNow.add(point);

                    listCommando.forEach((c) -> c.setTouchable(false, wm));
                    AutoClickService.updateLayoutFlagsOn();
                    SimulateTouchAccessibilityService.requestStart(listCommandoNow);
                    listCommandoNow.clear();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            AutoClickService.updateLayoutFlagsOff();
                        }
                    }, 500);
                    point.setDelay(nMs);
                    nMs = 0;
                    pointMicroMove = true;
                }
                else {
                    nMsNow = nMs;
                    point = Point.PointBuilder.invoke()
                            .position((int) xDown, (int) yDown)
                            .delay(nMsNow)
                            .text(String.format("%s", listCommando.size() + 1))
                            .build(SwipePoint.class);

                    point.attachToWindow(wm, canvasView);
                    listCommando.add(point);

                    point.setDelay((long) 1);
                    listCommandoNow.add(point);

                    listCommando.forEach((c) -> c.setTouchable(false, wm));
                    AutoClickService.updateLayoutFlagsOn();
                    SimulateTouchAccessibilityService.requestStart(listCommandoNow);
                    listCommandoNow.clear();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            AutoClickService.updateLayoutFlagsOff();
                        }
                    }, 500);
                    point.setDelay(nMs);
                    nMs = 0;
                }
            }
        }
        else {}
            work = false;
        return false;
    }

}
