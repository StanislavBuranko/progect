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

import com.askerweb.autoclickerreplay.App;
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
import java.util.Timer;
import java.util.TimerTask;

public class RecordPoints {

    static CountDownTimer timer;
    static CountDownTimer timerForSwipe;
    static int xDown, yDown, xMove, yMove, xUp, yUp;
    static int xDown2, yDown2, xMove2, yMove2, xUp2, yUp2;
    static ArrayList<Integer> coordinateYDown = new ArrayList<Integer>();
    static ArrayList<Integer> coordinateXDown = new ArrayList<Integer>();
    static ArrayList<Integer> coordinateYUp = new ArrayList<Integer>();
    static ArrayList<Integer> coordinateXUp = new ArrayList<Integer>();
    static int numbElementInCoordinateXYDown = 0;
    static int numbElementInCoordinateXYUp = 0;
    static long nMs = 0;
    static long nMsNow = 0;
    static long nDurationMsNow = 0;
    static long nDurationMs = 0;
    static Boolean actionUp = false;
    static Boolean anyClick = false;
    static Boolean actionMove = false;
    static Boolean actionDown = false;
    static Boolean counterMoreOne = false;
    static Boolean work = false;
    static SwipePoint swipePoint;
    static PinchPoint pinchPoint;
    static MultiPoint multiPoint;
    static int pointLocateHelper = 0;
    static Boolean timerForSwipeisStart = false;
    static int delayHollder = 400;
    static int pointerCount = 0;
    static public LinkedList<Point> listCommandoNow = new LinkedList<>();

    static boolean timerStart = false;
    static boolean recordPanelInitialization  = false;

    static View.OnLayoutChangeListener listenerChangeLayout = null;

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

    enum PointsCreate{
        Point,
        SwipePoint,
        PinchPoint,
        MultiPoint,
        PathPoint
    }

    static PointsCreate pointsCreate = PointsCreate.Point;

    public static void onTouch(MotionEvent event, WindowManager wm, List<Point> listCommando, PointCanvasView canvasView, float paramSizePoint) {
        boolean microMove = false;
        if(!timerStart)
            timerStart();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                nMs = 0;
                nDurationMs = 0;
                actionMove = false;
                coordinateYUp.clear();
                coordinateXUp.clear();
                coordinateXDown.add((int) event.getX());
                coordinateYDown.add((int) event.getY());
                if (timerForSwipeisStart)
                    timerForSwipe.cancel();
                timerForDurationStart();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                coordinateXDown.add((int) event.getX());
                coordinateYDown.add((int) event.getY());
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
                else if(coordinateXDown.size() == 2) {
                    pointsCreate = PointsCreate.PinchPoint;
                }
                else if(coordinateXDown.size() > 2) {
                    pointsCreate = PointsCreate.MultiPoint;
                }
                else
                    pointsCreate = PointsCreate.Point;
                Log.d("1",""+coordinateXDown.size());
                AutoClickService.updateLayoutFlagsOn();
                CreatPoint(wm,listCommando,canvasView,paramSizePoint);
                coordinateXDown.clear();
                coordinateYDown.clear();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                coordinateXUp.add((int) event.getX());
                coordinateYUp.add((int) event.getY());
                break;

        }

    }

    public static  void CreatPoint(WindowManager wm, List<Point> listCommando, PointCanvasView canvasView, float paramSizePoint)
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
                    Log.d(LogExt.TAG, "gesture cancelled ");
                }
            });

            point.setDelay(nMsNow);
            point.setDuration(nDurationMsNow);
            timerForDurationCancel();
        }
        if(pointsCreate == PointsCreate.PinchPoint) {
            nMsNow = nMs;
            nDurationMsNow = nDurationMs;
            Point point = (PinchPoint) Point.PointBuilder.invoke()
                    .position(500,
                            750)
                    .delay(nMsNow).duration(nDurationMsNow)
                    .text(String.format("%s", listCommando.size() + 1))
                    .build(PinchPoint.class);
            pinchPoint = (PinchPoint) point;

/*
            pinchPoint.getFirstPoint().setX((int) coordinateXDown.get(coordinateXDown.size()-2)-pointLocateHelper);
            pinchPoint.getFirstPoint().setY((int) coordinateYDown.get(coordinateYDown.size()-2)-pointLocateHelper);

            pinchPoint.getSecondPoint().setX((int) coordinateXDown.get(coordinateXDown.size()-1)-pointLocateHelper);
            pinchPoint.getSecondPoint().setY((int) coordinateYDown.get(coordinateYDown.size()-1)-pointLocateHelper);
            Log.d("13211", "CreatPoint: " + coordinateXDown.get(coordinateXDown.size()-2));
            Log.d("13211", "CreatPoint: " + coordinateXDown.get(coordinateXDown.size()-1));
            Log.d("13211", "CreatPoint: "+Math.abs(coordinateXDown.get(coordinateXDown.size()-2) - coordinateXDown.get(coordinateXDown.size()-1)));
            point.setX(Math.abs(coordinateXDown.get(coordinateXDown.size()-2) - coordinateXDown.get(coordinateXDown.size()-1)));
            point.setY(Math.abs(coordinateYDown.get(coordinateYDown.size()-2) - coordinateYDown.get(coordinateYDown.size()-1)));
*/

            pinchPoint.setTypePinch(PinchPoint.PinchDirection.OUT);

            point.attachToWindow(wm, canvasView);
            point.updateListener(wm, canvasView, AutoClickService.getParamBound());
            point.setTouchable(false, wm);


            listCommando.add(point);

            point.setDelay((long) 1);
            point.setDuration((long) 1);

            /*SimulateTouchAccessibilityService.execCommand(point, new AccessibilityService.GestureResultCallback() {
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
*/
            point.setDelay(nMsNow);
            point.setDuration(nDurationMsNow);
            timerForDurationCancel();
        }
    }

            // onTouch method for recordPanel
    /*public static void onTouch(MotionEvent event, WindowManager wm, List<Point> listCommando, PointCanvasView canvasView, float paramSizePoint) {
        int pointerIndex = event.getActionIndex();
        if(paramSizePoint == 32)
            pointLocateHelper = 37;
        else if(paramSizePoint == 40)
            pointLocateHelper = 50;
        else if(paramSizePoint == 56)
            pointLocateHelper = 75;

        if(!timerStart)
            timerStart();
        if(!work) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (timerForSwipeisStart)
                        timerForSwipe.cancel();
                    xDown = Math.round(event.getX());
                    yDown = Math.round(event.getY());
                    timerForDurationStart();
                    nDurationMs = 0;
                    numbElementInCoordinateXYDown = 0;
                    numbElementInCoordinateXYUp = 0;
                    actionDown = true;
                    actionUp = false;
                    actionMove = false;
                    pointerCount = 1;
                    counterMoreOne = false;
                    AutoClickService.updateLayoutFlagsOn();
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    xDown2 = (int) Math.round(event.getX());
                    yDown2 = (int) Math.round(event.getY());
                    anyClick = true;
                    coordinateXDown[numbElementInCoordinateXYDown] = (int) Math.round(event.getX());
                    coordinateYDown[numbElementInCoordinateXYDown] = (int) Math.round(event.getY());
                    Log.d("123321",""+coordinateXDown[numbElementInCoordinateXYDown]);
                    Log.d("123321",""+numbElementInCoordinateXYDown);
                    numbElementInCoordinateXYDown++;
                    pointerCount++;
                    break;
                case MotionEvent.ACTION_MOVE:
                    actionMove = true;
                    actionUp = false;
                    xMove = Math.round(event.getX());
                    yMove = Math.round(event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    if (actionUp != true) {
                        actionUp = true;
                        xUp = (int) Math.round(event.getX());
                        yUp = (int) Math.round(event.getY());
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    coordinateXUp[numbElementInCoordinateXYUp] = (int) Math.round(event.getX());
                    coordinateYUp[numbElementInCoordinateXYUp] = (int) Math.round(event.getY());
                    numbElementInCoordinateXYUp += 1;
                    counterMoreOne = true;
                    break;
            }

            if (actionUp == true && actionMove == false && actionDown == true && pointerCount == 1) {
                actionUp = false;
                actionDown = false;
                nMsNow = nMs;
                nDurationMsNow = nDurationMs;
                Point point = Point.PointBuilder.invoke()
                        .position((int) xDown-pointLocateHelper, (int) yDown-pointLocateHelper)
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
                nMs = 0;
                nDurationMs = 0;

                timerForDurationCancel();
                actionDown = false;
                actionUp = false;
                actionMove = false;
            }
            else if (actionMove == true && actionUp == true && actionUp == true && pointerCount == 1) {
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
            else if (actionUp == true && actionDown == true && pointerCount == 2 && counterMoreOne) {

                nMsNow = nMs;
                Point point = (PinchPoint) Point.PointBuilder.invoke()
                        .position((int) 100, (int) 100)
                        .delay(nMsNow).duration(nDurationMsNow)
                        .text(String.format("%s", listCommando.size() + 1))
                        .build(PinchPoint.class);
                pinchPoint = (PinchPoint) point;
                pinchPoint.getFirstPoint().setX(coordinateXDown[numbElementInCoordinateXYDown - 1] - pointLocateHelper);
                pinchPoint.getFirstPoint().setY(coordinateYDown[numbElementInCoordinateXYDown - 1] - pointLocateHelper);
                for (int i : coordinateXDown) {
                    try {
                        Log.d("123",""+coordinateXDown[i]);
                        Log.d("123",""+i);
                    }
                    catch (Throwable t){}
                }

                pinchPoint.getSecondPoint().setX(xDown - pointLocateHelper);
                pinchPoint.getSecondPoint().setY(yDown - pointLocateHelper);
                pinchPoint.setTypePinch(PinchPoint.PinchDirection.OUT);

                point.attachToWindow(wm, canvasView);
                point.updateListener(wm, canvasView, AutoClickService.getParamBound());
                point.setTouchable(false, wm);


                listCommando.add(point);

                point.setDelay((long) 1);
                point.setDuration((long) 1);

                SimulateTouchAccessibilityService.execCommand(pinchPoint, new AccessibilityService.GestureResultCallback() {
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
                pointerCount = 0;
                counterMoreOne = false;
            }
            else if (actionUp == true && actionDown == true && pointerCount == 2 && counterMoreOne) {
                nMsNow = nMs;
                Point point = (PinchPoint) Point.PointBuilder.invoke()
                        .position((int) 100, (int) 100)
                        .delay(nMsNow).duration(nDurationMsNow)
                        .text(String.format("%s", listCommando.size() + 1))
                        .build(PinchPoint.class);
                pinchPoint = (PinchPoint) point;
                pinchPoint.getFirstPoint().setX(xDown - pointLocateHelper);
                pinchPoint.getFirstPoint().setY(yDown - pointLocateHelper);
                for (int i : coordinateXDown) {
                    try {
                        Log.d("123",""+coordinateXDown[i]);
                        Log.d("123",""+i);
                    }
                    catch (Throwable t){}
                }

                pinchPoint.getSecondPoint().setX(xDown2 - pointLocateHelper);
                pinchPoint.getSecondPoint().setY(yDown2 - pointLocateHelper);
                pinchPoint.setTypePinch(PinchPoint.PinchDirection.OUT);

                point.attachToWindow(wm, canvasView);
                point.updateListener(wm, canvasView, AutoClickService.getParamBound());
                point.setTouchable(false, wm);


                listCommando.add(point);

                point.setDelay((long) 1);
                point.setDuration((long) 1);

                SimulateTouchAccessibilityService.execCommand(pinchPoint, new AccessibilityService.GestureResultCallback() {
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
                pointerCount = 0;
                counterMoreOne = false;
            }
            else if (actionUp == true && actionDown == true && pointerCount >= 2 && counterMoreOne) {
                nMsNow = nMs;
                Log.d(123+"", "onTouch: "+multiPoint.getPoints());
                for (int i  = 0 ; i < 5; i++) {
                    multiPoint.createPointsForRecordPanel(i,coordinateXDown[i], coordinateYDown[i]);
                }

                multiPoint.attachToWindow(wm, canvasView);
                multiPoint.updateListener(wm, canvasView, AutoClickService.getParamBound());
                multiPoint.setTouchable(false, wm);


                listCommando.add(multiPoint);

                multiPoint.setDelay((long) 1);
                multiPoint.setDuration((long) 1);

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

                multiPoint.setDelay(nMsNow);
                multiPoint.setDuration(nDurationMsNow);
                nMs = 0;
                nDurationMs = 0;

                timerForDurationCancel();
                actionDown = false;
                actionUp = false;
                actionMove = false;
                pointerCount = 0;
                counterMoreOne = false;
            }
        }
        else {}
            work = false;
    }*/

}
