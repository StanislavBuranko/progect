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
    static int xMove, yMove, xMove2, yMove2;
    public static ArrayList<Integer> coordinateYDown = new ArrayList<Integer>();
    public static ArrayList<Integer> coordinateXDown = new ArrayList<Integer>();
    public static ArrayList<Integer> coordinateYUp = new ArrayList<Integer>();
    public static ArrayList<Integer> coordinateXUp = new ArrayList<Integer>();
    static long nMs = 0;
    static long nMsNow = 0;
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
        int numbPointerUp = 0;
        if(!timerStart)
            timerStart();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                xMove = 0;
                xMove2 = 0;
                yMove = 0;
                yMove2 = 0;
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
                coordinateXDown.add((int) event.getX(event.getActionIndex()));
                coordinateYDown.add((int) event.getY(event.getActionIndex()));
                Log.d("123321", "CreatPoint: " +coordinateXDown.get(coordinateXDown.size()-1)+" "+coordinateYDown.get(coordinateXDown.size()-1));
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove = true;
                xMove = (int) event.getX(0);
                yMove = (int) event.getY(0);
                if(event.getPointerCount() != 1) {
                    xMove2 = (int) event.getX(1);
                    yMove2 = (int) event.getY(1);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d("123321123", "onTouch: xDown:"+coordinateXDown.get(0)+" yDown:"+coordinateYDown.get(0)+" xMove:"+xMove+" yMove:"+yMove);
                if(coordinateXDown.get(0) - 75 <= xMove
                        && xMove <= coordinateXDown.get(0) + 75
                        && coordinateYDown.get(0) - 75 <= yMove
                        && yMove <= coordinateYDown.get(0) + 75
                        && coordinateXDown.get(1) - 75 <= xMove2
                        && xMove2 <= coordinateXDown.get(1) + 75
                        && coordinateYDown.get(1) - 75 <= yMove2
                        && yMove2 <= coordinateYDown.get(1) + 75
                        && coordinateXDown.size() >= 2)
                {
                    microMove = true;
                }
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
                else if(coordinateXDown.size() == 2 && actionMove == true && microMove == false) {
                    pointsCreate = PointsCreate.PinchPoint;
                }
                else if(coordinateXDown.size() > 2 || microMove == true && coordinateXDown.size() == 2 && actionMove == true) {
                    pointsCreate = PointsCreate.MultiPoint;
                }
                else
                    pointsCreate = PointsCreate.Point;
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
                                   float paramSizePoint) {
        if(paramSizePoint == 32)
            pointLocateHelper = 37;
        else if(paramSizePoint == 40)
            pointLocateHelper = 50;
        else if(paramSizePoint == 56)
            pointLocateHelper = 75;

        if(pointsCreate == PointsCreate.Point) {
            nMsNow = nMs;
            Log.d("123321", "CreatPoint: "+coordinateXDown.size());
            Point point = Point.PointBuilder.invoke()
                    .position((int) coordinateXDown.get(coordinateXDown.size()-1)-pointLocateHelper,
                            (int) coordinateYDown.get(coordinateXDown.size()-1)-pointLocateHelper)
                    .delay(nMsNow).duration(nDurationMs)
                    .text(String.format("%s", listCommando.size() + 1))
                    .build(ClickPoint.class);

            point.attachToWindow(wm, canvasView);
            point.updateListener(wm, canvasView, AutoClickService.getParamBound());
            point.setTouchable(false, wm);

            listCommando.add(point);

            point.setDelay((long) 1);

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
            timerForDurationCancel();
        }
        if(pointsCreate == PointsCreate.SwipePoint) {
            nMsNow = nMs;
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
            timerForDurationCancel();
        }
        if(pointsCreate == PointsCreate.PinchPoint) {
            nMsNow = nMs;
            Point point = (PinchPoint) Point.PointBuilder.invoke()
                    .position(500,
                            750)
                    .delay(nMsNow).duration(nDurationMs)
                    .text(String.format("%s", listCommando.size() + 1))
                    .build(PinchPoint.class);
            pinchPoint = (PinchPoint) point;

            if(coordinateYDown.get(0) > coordinateYDown.get(1)) {
                pinchPoint.getFirstPoint().setX(coordinateXDown.get(0));
                pinchPoint.getFirstPoint().setY(coordinateYDown.get(0));
                pinchPoint.getSecondPoint().setX(coordinateXDown.get(1));
                pinchPoint.getSecondPoint().setY(coordinateYDown.get(1));
                if(pinchPoint.getFirstPoint().getX() >= xMove && pinchPoint.getFirstPoint().getY() <= yMove)
                    pinchPoint.setTypePinch(PinchPoint.PinchDirection.OUT);
                else
                    pinchPoint.setTypePinch(PinchPoint.PinchDirection.IN);
            }
            else {
                pinchPoint.getFirstPoint().setX(coordinateXDown.get(1));
                pinchPoint.getFirstPoint().setY(coordinateYDown.get(1));
                pinchPoint.getSecondPoint().setX(coordinateXDown.get(0));
                pinchPoint.getSecondPoint().setY(coordinateYDown.get(0));

                if(pinchPoint.getFirstPoint().getX() >= xMove2 && pinchPoint.getFirstPoint().getY() <= yMove2)
                    pinchPoint.setTypePinch(PinchPoint.PinchDirection.OUT);
                else
                    pinchPoint.setTypePinch(PinchPoint.PinchDirection.IN);
            }

            point.setX((pinchPoint.getFirstPoint().getX()+pinchPoint.getSecondPoint().getX())/2);
            point.setY((pinchPoint.getFirstPoint().getY()+pinchPoint.getSecondPoint().getY())/2);


            point.attachToWindow(wm, canvasView);
            point.updateListener(wm, canvasView, AutoClickService.getParamBound());
            point.setTouchable(false, wm);

            listCommando.add(point);

            point.setDelay((long) 1);

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
            timerForDurationCancel();
        }
        if(pointsCreate == PointsCreate.MultiPoint) {
            nMsNow = nMs;
            Point point = (MultiPoint) Point.PointBuilder.invoke()
                    .position((int) coordinateXDown.get(coordinateXDown.size() - 1) - pointLocateHelper,
                            (int) coordinateYDown.get(coordinateXDown.size() - 1) - pointLocateHelper)
                    .delay(nMsNow).duration(nDurationMs)
                    .text(String.format("%s", listCommando.size() + 1))
                    .build(MultiPoint.class);

            multiPoint = (MultiPoint) point;
            Log.d("1233211", "CreatPoint: ");
            multiPoint.createPointsForRecordPanelChangeElemenent(0, coordinateXDown.get(0) - pointLocateHelper, coordinateYDown.get(0) - pointLocateHelper);
            multiPoint.createPointsForRecordPanelChangeElemenent(1, coordinateXDown.get(1) - pointLocateHelper, coordinateYDown.get(1) - pointLocateHelper);
            if (coordinateXDown.size() > 10)
                for (int i = 2; i < 10; i++) {
                    multiPoint.createPointsForRecordPanel(coordinateXDown.get(i) - pointLocateHelper,
                            coordinateYDown.get(i) - pointLocateHelper);
                }
            else
                for (int i = 2; i < coordinateXDown.size(); i++) {
                    multiPoint.createPointsForRecordPanel(coordinateXDown.get(i) - pointLocateHelper,
                            coordinateYDown.get(i) - pointLocateHelper);
                }
            multiPoint.attachToWindow(wm, canvasView);
            multiPoint.updateListener(wm, canvasView, AutoClickService.getParamBound());
            multiPoint.setTouchable(false, wm);

            listCommando.add(point);

            multiPoint.setDelay((long) 1);

            SimulateTouchAccessibilityService.execCommand(multiPoint, new AccessibilityService.GestureResultCallback() {
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

            multiPoint.setDelayRecord((int) nMsNow);
            timerForDurationCancel();
        }
    }
}
