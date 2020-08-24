package com.askerweb.autoclickerreplay.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.askerweb.autoclickerreplay.App;
import com.askerweb.autoclickerreplay.R;
import com.askerweb.autoclickerreplay.ktExt.LogExt;
import com.askerweb.autoclickerreplay.point.HomePoint;
import com.askerweb.autoclickerreplay.point.Point;
import com.askerweb.autoclickerreplay.point.PointCommand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

public class SimulateTouchAccessibilityService extends AccessibilityService {

    private static SimulateTouchAccessibilityService service;
    static CountDownTimer countDownTimer;

    Context appContext = App.appComponent.getAppContext();
    ArrayList<Point> listCommand;
    public static boolean isPlaying = false;
    public static int willExec = 0;
    int counterRepeatMacro=0;

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
            p.setCounterRepeat(p.getCounterRepeat());
            countDownTimerTv.cancel();
            allMSPoint = 0;
            if(AutoClickService.getListPoint() != null)
                for(int i = willExec; i < AutoClickService.getListPoint().size(); i++ ) {
                    allMSPoint = Math.toIntExact(allMSPoint + AutoClickService.getListPoint().get(i).getDelay()
                            + AutoClickService.getListPoint().get(i).getDuration() * AutoClickService.getListPoint().get(i).getRepeat());
                }
            int allMsPointTemp = allMSPoint;
            if (SimulateTouchAccessibilityService.isPlaying())
            countDownTimerTv = new CountDownTimer(allMsPointTemp, 100) {

                public void onTick(long millisUntilFinished) {
                    allMSPoint = allMSPoint - 100;
                    AutoClickService.tvTimer.setText(AutoClickService.getTimeCountDownTimer(allMSPoint));
                    //here you can have your logic to set text to edittext
                }

                public void onFinish() {
                    allMSPoint = 0;
                    if(AutoClickService.getListPoint() != null)
                        for (Point point : AutoClickService.getListPoint()) {
                            allMSPoint = Math.toIntExact(allMSPoint + (point.getDelay() + point.getDuration()) * point.getRepeat());
                        }
                    AutoClickService.tvTimer.setText(AutoClickService.getTimeCountDownTimer(allMSPoint));
                }
            }.start();
            requestContinue();
        }
    };



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
    
    public static Boolean isStartCounDownTimer = false;
    public synchronized static void execCommand(PointCommand command, GestureResultCallback callback){
        if(command != null){
            GestureDescription gd = command.getCommand();
            if(gd != null){
                service.dispatchGesture(gd, callback, null);
                if(!isStartCounDownTimer) {
                    startTimer();
                    isStartCounDownTimer = true;
                }
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
    static CountDownTimer countDownTimerTv;
    static int allMSPoint = 0;
    private static void startTimer(){
        allMSPoint = 0;
        for (Point point : AutoClickService.getListPoint()) {
            allMSPoint = Math.toIntExact(allMSPoint + (point.getDelay() + point.getDuration()) * point.getRepeat());
        }
        int allMsPointTemp = allMSPoint;
        countDownTimerTv = new CountDownTimer(allMsPointTemp, 100) {

            public void onTick(long millisUntilFinished) {
                allMSPoint = allMSPoint - 100;
                AutoClickService.tvTimer.setText(AutoClickService.getTimeCountDownTimer(allMSPoint));
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                allMSPoint = 0;
                for (Point point : AutoClickService.getListPoint()) {
                    allMSPoint = Math.toIntExact(allMSPoint + (point.getDelay() + point.getDuration()) * point.getRepeat());
                }
                AutoClickService.tvTimer.setText(AutoClickService.getTimeCountDownTimer(allMSPoint));
            }
        }.start();
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
    private boolean isStartTimer = false;


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
                if(isStartTimer) {
                    countDownTimer.cancel();
                    isStartTimer = false;
                }
                else {
                    Toast toast = Toast.makeText(this, R.string.error_listcomand_null, Toast.LENGTH_LONG);
                    toast.show();
                }
                isPlaying = false;
                stopSelf();
                break;
            case ACTION_COMPLETE:
                if(isPlaying) {
                    Point point = null;
                    if (counterRepeatMacro != 0 && listCommand.size() > 0) {
                        point = listCommand.get(willExec);
                        if (point.getCounterRepeat() == point.getRepeat()) {
                            point.setCounterRepeat(0);
                            willExec++;
                            if (willExec > listCommand.size()-1) {
                                if (counterRepeatMacro > 0) counterRepeatMacro--;
                                startTimer();
                                willExec = 0;
                            }
                            point = listCommand.get(willExec);
                        }
                    }
                    if (counterRepeatMacro != 0) {
                        Point finalPoint = point;

                        if (listCommand.size() > 0) {
                            isStartTimer = true;
                            countDownTimer = new CountDownTimer(finalPoint.getDelay(), 1000) {

                                public void onTick(long millisUntilFinished) {
                                    //here you can have your logic to set text to edittext
                                }

                                public void onFinish() {
                                    if (finalPoint.getClass() == HomePoint.class) {
                                        ((HomePoint) finalPoint).getCommandMain();
                                        finalPoint.setCounterRepeat(finalPoint.getCounterRepeat() + 1);
                                        requestContinue();
                                    }
                                    else
                                        SimulateTouchAccessibilityService.execCommand(finalPoint, getGestureCallback.apply(finalPoint));
                                }
                            }.start();
                        } else
                            AutoClickService.requestAction(appContext, AutoClickService.ACTION_STOP);
                    } else
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
                        startTimer();
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
