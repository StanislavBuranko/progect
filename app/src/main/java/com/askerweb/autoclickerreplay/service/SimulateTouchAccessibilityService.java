package com.askerweb.autoclickerreplay.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.askerweb.autoclickerreplay.App;
import com.askerweb.autoclickerreplay.ktExt.LogExt;
import com.askerweb.autoclickerreplay.point.Point;
import com.askerweb.autoclickerreplay.point.PointCommand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

public class SimulateTouchAccessibilityService extends AccessibilityService {

    private static SimulateTouchAccessibilityService service;
    
    Context appContext = App.appComponent.getAppContext();

    public final static String ACTION_COMPLETE = "ACTION_COMPLETE_POINT";
    public final static String KEY_LIST_COMMAND = "listCommand";

    public static Function<Point, GestureResultCallback> getGestureCallback = (p)-> new GestureResultCallback() {
        @Override
        public void onCompleted(GestureDescription gestureDescription) {
            super.onCompleted(gestureDescription);
            p.setCounterRepeat(p.getCounterRepeat() + 1);
            requestContinue();
        }

        @Override
        public void onCancelled(GestureDescription gestureDescription) {
            super.onCancelled(gestureDescription);
            p.setCounterRepeat(p.getCounterRepeat() + 1);
            requestContinue();
        }
    };

    ArrayList<Point> listCommand;
    boolean isPlaying = false;
    int willExec = 0;
    int counterRepeatMacro=0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(LogExt.TAG, "onServiceConnected ServiceSimulateTouch");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
    }
    

    public synchronized static void execCommand(PointCommand command, GestureResultCallback callback){
        if(command != null){
            GestureDescription gd = command.getCommand();
            if(gd != null){
                service.dispatchGesture(gd, callback, null);
            }
        }
    }

    public synchronized static void execCommand(PointCommand command){
        execCommand(command,  new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(LogExt.TAG, "gesture completed");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(LogExt.TAG, "gesture cancelled ");
            }
        });
    }

    private static void requestAction(String action){
        Intent intent = new Intent(service.appContext, SimulateTouchAccessibilityService.class);
        intent.setAction(action);
        service.getApplicationContext().startService(intent);
    }



    public static void requestStart(List<Point> list){
        Intent intent = new Intent(service.appContext, SimulateTouchAccessibilityService.class)
                .setAction(AutoClickService.ACTION_START)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(KEY_LIST_COMMAND, (Serializable) list);
        service.getApplicationContext().startService(intent);
    }
    public static void requestStop(){
        requestAction(AutoClickService.ACTION_STOP);
    }
    public static void requestContinue() {
        requestAction(ACTION_COMPLETE);
    }

    public static boolean isPlaying() {
        return service != null && service.isPlaying;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() == null) return super.onStartCommand(intent, flags, startId);
        switch (intent.getAction()){
            case AutoClickService.ACTION_START:
                listCommand = (ArrayList<Point>) intent.getExtras().getSerializable(KEY_LIST_COMMAND);
                isPlaying = true;
                willExec = 0;
                counterRepeatMacro = AutoClickService.getParamRepeatMacro();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        requestContinue();
                    }
                }, 40);
                break;
            case AutoClickService.ACTION_STOP:
                isPlaying = false;
                stopSelf();
                break;
            case ACTION_COMPLETE:
                if(isPlaying){
                    Point point = null;
                    if(counterRepeatMacro != 0 && listCommand.size() > 0){
                        point = listCommand.get(willExec);
                        if(point.getCounterRepeat() == point.getRepeat()){
                            point.setCounterRepeat(0);
                            willExec++;
                            if(willExec > listCommand.size() - 1){
                                if(counterRepeatMacro > 0) counterRepeatMacro--;
                                willExec = 0;
                            }
                            point = listCommand.get(willExec);
                        }
                    }
                    if(counterRepeatMacro != 0)
                        SimulateTouchAccessibilityService.execCommand(point, getGestureCallback.apply(point));
                    else
                        AutoClickService.requestAction(appContext, AutoClickService.ACTION_STOP);
                }
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public static void continueComplete(){
        if(service.isPlaying){
            Point point = null;
            if(service.counterRepeatMacro != 0 && service.listCommand.size() > 0){
                point = service.listCommand.get(service.willExec);
                if(point.getCounterRepeat() == point.getRepeat()){
                    point.setCounterRepeat(0);
                    service.willExec++;
                    if(service.willExec > service.listCommand.size() - 1){
                        if(service.counterRepeatMacro > 0) service.counterRepeatMacro--;
                        service.willExec = 0;
                    }
                    point = service.listCommand.get(service.willExec);
                }
            }
            if(service.counterRepeatMacro != 0)
                SimulateTouchAccessibilityService.execCommand(point, getGestureCallback.apply(point));
            else
                AutoClickService.requestAction(service.appContext, AutoClickService.ACTION_STOP);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LogExt.TAG, "unbind ServiceSimulateTouch");
        service = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(LogExt.TAG, "destroy ServiceSimulateTouch");
        service = null;
        super.onDestroy();
    }

    @Override
    public void onInterrupt() {

    }
}
