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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RecordPoints {

    public static boolean creatPointStart;
    static CountDownTimer timer;
    static CountDownTimer timerForSwipe;
    static int xMove, yMove;
    public static ArrayList<Integer> coordinateYDown = new ArrayList<Integer>();
    public static ArrayList<Integer> coordinateXDown = new ArrayList<Integer>();
    public static ArrayList<Integer> coordinateYUp = new ArrayList<Integer>();
    public static ArrayList<Integer> coordinateXUp = new ArrayList<Integer>();
    static long nMs = 0;
    static long nMsNow = 0;
    static long nDurationMsNow = 0;
    static long nDurationMs = 0;
    static Boolean actionMove = false;
    static Boolean workCreatePoint = false;
    static SwipePoint swipePoint;
    static PinchPoint pinchPoint;
    static MultiPoint multiPoint;
    static int pointLocateHelper = 0;
    static Boolean timerForSwipeisStart = false;
    static boolean timerStart = false;

    static enum PointsCreate{
        Point,
        SwipePoint,
        PinchPoint,
        MultiPoint,
        PathPoint
    }

    static PointsCreate pointsCreate = PointsCreate.Point;

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

    public static void onTouch(MotionEvent event,
                               WindowManager wm,
                               List<Point> listCommando,
                               PointCanvasView canvasView,
                               float paramSizePoint) {
        boolean microMove = false;
        if(!timerStart)
            timerStart();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                nMs = 0;
                nDurationMs = 0;
                actionMove = false;
                coordinateXDown.clear();
                coordinateYDown.clear();
                coordinateYUp.clear();
                coordinateXUp.clear();
                coordinateXDown.add((int) event.getX());
                coordinateYDown.add((int) event.getY());
                Log.d("123321", "CreatPoint: " +coordinateXDown.get(coordinateXDown.size()-1)+" "+coordinateYDown.get(coordinateXDown.size()-1));
                if (timerForSwipeisStart)
                    timerForSwipe.cancel();
                timerForDurationStart();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                coordinateXDown.add((int) event.getX());
                coordinateYDown.add((int) event.getY());
                Log.d("123321", "CreatPoint: " +coordinateXDown.get(coordinateXDown.size()-1)+" "+coordinateYDown.get(coordinateXDown.size()-1));
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove = true;
                if(coordinateXDown.size() == 1) {
                    xMove = (int) event.getX();
                    yMove = (int) event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                coordinateXUp.add((int) event.getX());
                coordinateYUp.add((int) event.getY());
                boolean isPointCreate = false;
                if(coordinateXDown.size() == 1 && actionMove == true) {
                    if (coordinateXDown.get(coordinateXDown.size()-1) - 75 <= xMove
                            && xMove <= coordinateXDown.get(coordinateXDown.size()-1) + 75
                            && coordinateYDown.get(coordinateYDown.size()-1) - 75 <= yMove
                            && yMove <= coordinateYDown.get(coordinateYDown.size()-1) + 75) {
                        pointsCreate = PointsCreate.Point;
                        isPointCreate = true;
                    }
                    else if(actionMove && !isPointCreate)
                        pointsCreate = PointsCreate.SwipePoint;
                }
                //else if(coordinateXDown.size() == 2) {
                //    pointsCreate = PointsCreate.PinchPoint;
                //}
                //else if(coordinateXDown.size() > 2) {
                //    pointsCreate = PointsCreate.MultiPoint;
                //}
                else
                    pointsCreate = PointsCreate.Point;
                Log.d("1",""+coordinateXDown.size());

                if(!workCreatePoint) {
                    workCreatePoint = true;
                    creatPointStart = true;
                    AutoClickService.updateLayoutFlagsOn();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            CreatPoint(wm, listCommando, canvasView, paramSizePoint);
                        }
                    }, 100);
                    workCreatePoint = false;
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
                coordinateXUp.add((int) event.getX());
                coordinateYUp.add((int) event.getY());
                break;

        }

    }

    public static  void CreatPoint(WindowManager wm,
                                   List<Point> listCommando,
                                   PointCanvasView canvasView,
                                   float paramSizePoint)
    {
        Log.d("1",""+pointsCreate);
        if(paramSizePoint == 32)
            pointLocateHelper = 37;
        else if(paramSizePoint == 40)
            pointLocateHelper = 50;
        else if(paramSizePoint == 56)
            pointLocateHelper = 75;

        if(pointsCreate == PointsCreate.Point) {
            nMsNow = nMs;
            nDurationMsNow = nDurationMs;
            Log.d("123321", "CreatPoint: "+coordinateXDown.size());
            Point point = Point.PointBuilder.invoke()
                    .position((int) coordinateXDown.get(coordinateXDown.size()-1)-pointLocateHelper,
                            (int) coordinateYDown.get(coordinateXDown.size()-1)-pointLocateHelper)
                    .text(String.format("%s", listCommando.size() + 1))
                    .build(ClickPoint.class);

            point.attachToWindow(wm, canvasView);
            point.updateListener(wm, canvasView, AutoClickService.getParamBound());
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
            timerForDurationCancel();
        }
        if(pointsCreate == PointsCreate.SwipePoint) {
            nMsNow = nMs;
            nDurationMsNow = nDurationMs;
            Point point = (SwipePoint) Point.PointBuilder.invoke()
                    .position((int) coordinateXDown.get(coordinateXDown.size()-1)-pointLocateHelper,
                            (int) coordinateYDown.get(coordinateXDown.size()-1)-pointLocateHelper)
                    .delay(nMsNow).duration(nDurationMs)
                    .text(String.format("%s", listCommando.size() + 1))
                    .build(SwipePoint.class);
            swipePoint = (SwipePoint) point;
            swipePoint.getNextPoint().setX(coordinateXUp.get(coordinateXDown.size()-1)-pointLocateHelper);
            swipePoint.getNextPoint().setY(coordinateYUp.get(coordinateXDown.size()-1)-pointLocateHelper);

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
                    Log.d(LogExt.TAG, "gesture cancelled");
                }
            });

            point.setDelay(nMsNow);
            point.setDuration(nDurationMsNow);
            timerForDurationCancel();
        }
    }
}
