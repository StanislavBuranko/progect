package com.askerweb.autoclickerreplay.service;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.askerweb.autoclickerreplay.App;
import com.askerweb.autoclickerreplay.R;
import com.askerweb.autoclickerreplay.activity.AdActivity;
import com.askerweb.autoclickerreplay.activity.CheckPermPopUp;
import com.askerweb.autoclickerreplay.activity.MainActivity;
import com.askerweb.autoclickerreplay.ktExt.Dimension;
import com.askerweb.autoclickerreplay.ktExt.LogExt;
import com.askerweb.autoclickerreplay.ktExt.SettingExt;
import com.askerweb.autoclickerreplay.ktExt.UtilsApp;
import com.askerweb.autoclickerreplay.point.HomePoint;
import com.askerweb.autoclickerreplay.point.ClickPoint;
import com.askerweb.autoclickerreplay.point.MultiPoint;
import com.askerweb.autoclickerreplay.point.PathPoint;
import com.askerweb.autoclickerreplay.point.PinchPoint;
import com.askerweb.autoclickerreplay.point.Point;
import com.askerweb.autoclickerreplay.point.RecordPoints;
import com.askerweb.autoclickerreplay.point.SwipePoint;
import com.askerweb.autoclickerreplay.point.view.PointCanvasView;
import com.askerweb.autoclickerreplay.point.view.ViewOverlayOnTouchListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import butterknife.ViewCollections;

import static com.askerweb.autoclickerreplay.ktExt.MiuiCheckPermission.getMiuiVersion;
import static com.askerweb.autoclickerreplay.ktExt.SettingExt.KEY_SHARE_BUTTON_ON;
import static com.askerweb.autoclickerreplay.ktExt.SettingExt.KEY_TIMER_ON;
import static com.askerweb.autoclickerreplay.ktExt.SettingExt.defaultShareButtonOn;
import static com.askerweb.autoclickerreplay.ktExt.SettingExt.defaultTimerOn;
import static com.askerweb.autoclickerreplay.ktExt.SettingExt.getSetting;
import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.getNavBar;
import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.getParamOverlayFlags;
import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.getWindowsTypeApplicationOverlay;
import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.inOpenStatusAndNavBarWidth;
import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.isRotatinNavigateRight;
import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.isThereCutout;
import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.standardOverlayFlags;
import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.standardOverlayFlagsForCutout;

@SuppressLint("ClickableViewAccessibility")
public class AutoClickService extends Service implements View.OnTouchListener{

    public static AutoClickService service = null;
    
    static WindowManager wm = null;
    private Unbinder unbindControlPanel = null;
    private View controlPanel;
    static View timerPanel;
    public static TextView tvTimer;
    public static View recordPanel;
    static PointCanvasView canvasView;
    private RecordPoints recordPoints;

    @BindView(R.id.group_control)
    View group_control;
    @BindViews({R.id.start_pause, R.id.remove_point, R.id.add_point, R.id.record_points_start_pause, R.id.settings_table_points, R.id.setting, R.id.close, R.id.shareApp})
    List<View> controls;

    @Inject
    public List<Point> listCommands;

    @Inject
    public InterstitialAd interstitialAd;

    public Boolean paramBoundsOn;
    public Boolean paramTimerOn;
    public Boolean paramHideShareButtonOn;
    public Integer paramRepeatMacro;
    public Integer paramSizePoint;
    public Integer paramSizeControl;
    private Boolean openRecordPanel = false;
    private Integer allMSPoint = 0;
    private Boolean isInitalView = false;
    public static boolean checkPermPopUP = false;
    private boolean isActionStart = false;


    public static final WindowManager.LayoutParams paramsTimerPanel =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL | Gravity.TOP, standardOverlayFlagsForCutout);
    public static final WindowManager.LayoutParams paramsControlPanel =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    Gravity.START, standardOverlayFlags);
    public static final WindowManager.LayoutParams paramsRecordPanelFlagsOff =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.CENTER);
    public static WindowManager.LayoutParams paramsCanvas =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.CENTER);
    public static final WindowManager.LayoutParams paramsRecordPanelFlagsOn =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.CENTER,  WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    int lastOrientation = 1;
    public int orientGradus = 0;
    // update listener after change orientation
    public final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isAlive() && SimulateTouchAccessibilityService.isPlaying()){
                //startPauseCommand();
            }
            if(getResources().getConfiguration().orientation != lastOrientation) {
                listCommands.forEach(AutoClickService.this::swapPointOrientation);
                listCommands.forEach(AutoClickService.this::updateFlagsParams);
            }
            else if(getResources().getConfiguration().orientation == lastOrientation && isThereCutout()){
                listCommands.forEach(point -> {
                    point.swapPointOrientationLandscapeToLandscape();
                });
            }
            lastOrientation = getResources().getConfiguration().orientation;
            AutoClickService.getCanvas().invalidate();
        }
    };




    public final static String KEY_POINT = "point";

    public final static String ACTION_STOP = "ACTION_STOP";
    public final static String ACTION_START = "ACTION_START";
    public final static String ACTION_UPDATE_SETTING = "ACTION_UPDATE_SETTING";
    public final static String ACTION_DUPLICATE_POINT = "ACTION_DUPLICATE_POINT";
    public final static String ACTION_DELETE_POINT = "ACTION_DELETE_POINT";
    public final static String ACTION_HIDE_VIEWS = "ACTION_HIDE_VIEWS";
    public final static String ACTION_SHOW_VIEWS = "ACTION_SHOW_VIEWS";

    public final static String ACTIVITY_SETTING = "com.askerweb.autoclicker.setting";
    public final static String ACTIVITY_SETTING_TABLE = "com.askerweb.autoclicker.settingPoints";

    public static long startCount = 0;

    @Override
    public void onCreate() {
        lastOrientation = getResources().getConfiguration().orientation;
        Log.d("OverlayFlags", "onCreate: "+ getParamOverlayFlags());
        super.onCreate();
        updateSetting();
        service = this;
        App.initServiceComponent(service);
        App.serviceComponent.inject(service);
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(new AdRequest.Builder().build());
                AdActivity.getInstance()
                        .getClosedInterstitialAd()
                        .sendEmptyMessage(0);
                runMacroAfterAd();
            }
        });
        initView();
        recordPoints = new RecordPoints();
        //start listing change orientation
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(receiver, intentFilter);

    }

    private void initView(){
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        recordPanel = LayoutInflater.from(this).inflate(R.layout.record_panel, null);
        recordPanel.setOnTouchListener(this);
        wm.addView(recordPanel, paramsRecordPanelFlagsOn);

        controlPanel = LayoutInflater.from(this).inflate(R.layout.control_panel_service, null);
        controlPanel.setLayoutParams(paramsControlPanel);
        controlPanel.setOnTouchListener(new ViewOverlayOnTouchListener(controlPanel, wm));
        wm.addView(controlPanel, paramsControlPanel);

        timerPanel = LayoutInflater.from(this).inflate(R.layout.timer_panel_service, null);
        timerPanel.setLayoutParams(paramsTimerPanel);
        timerPanel.setOnTouchListener(new ViewOverlayOnTouchListener(timerPanel, wm));
        wm.addView(timerPanel, paramsTimerPanel);

        if(AutoClickService.getParamTimer() == true)
            timerPanel.setVisibility(View.VISIBLE);
        else
            timerPanel.setVisibility(View.GONE);

        canvasView = new PointCanvasView(this);
        canvasView.points = listCommands;
        paramsCanvas.flags |=  WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        canvasView.setLayoutParams(paramsCanvas);
        // update listener after change orientation
        canvasView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                listCommands.forEach(AutoClickService.this::updateTouchListenerPoint)
        );
        wm.addView(canvasView, paramsCanvas);

        unbindControlPanel = ButterKnife.bind(this, controlPanel);
        //unbindControlPanel = ButterKnife.bind(this, timerPanel);

        setControlSize();

        tvTimer = (TextView) timerPanel.findViewById(R.id.tvTimer);
        tvTimer.setText(getTime());
        //on start button
        controlPanel.findViewById(R.id.start_pause).setOnTouchListener((v, event)->{
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    startPauseCommand();
                    break;
            }
            return true;
        });
        //on close button
        controlPanel.findViewById(R.id.close).setOnTouchListener((v, event)->{
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    closeService();
                    break;
            }
            return true;
        });
        isInitalView = true;
        if(paramHideShareButtonOn == true)
            group_control.findViewById(R.id.shareApp).setVisibility(View.GONE);
    }

    public static String getTime(){
        Integer allMs = 0;
        for (Point point : getListPoint()) {
            allMs = Math.toIntExact(allMs + (point.getDelay() + point.getDuration()) * point.getRepeat());
        }
        if (allMs != 0) {
            String min = ""+(allMs / 60000);
            String second = ""+(allMs % 60000) / 1000;
            String ms = ""+(allMs % 60000) % 1000;
            if (Integer.parseInt(min) < 10)
                min = "0"+min;
            if (Integer.parseInt(second) < 10)
                second = "0"+second;
            if(Integer.parseInt(ms) > 10){
                ms = Math.round(Integer.parseInt(ms)/10) + "";
                if(Integer.parseInt(ms) == 10) {
                    ms = "0";
                    second = (Integer.parseInt(second)+1)+"";
                }
            }
            return min + ":" + second + ":" + ms + "ms";
        }
        return "00:00:0 ms";
    }

    public static String getTimeCountDownTimer(int allMs){
        if (allMs != 0) {
            String min = ""+(allMs / 60000);
            String second = ""+(allMs % 60000) / 1000;
            String ms = ""+(allMs % 60000) % 1000;
            if (Integer.parseInt(min) < 10)
                min = "0"+min;
            if (Integer.parseInt(second) < 10)
                second = "0"+second;
            if(Integer.parseInt(ms) > 10){
                ms = Math.round(Integer.parseInt(ms)/10) + "";
                if(Integer.parseInt(ms) == 10) {
                    ms = "0";
                    second = (Integer.parseInt(second)+1)+"";
                }
            }
            return min + ":" + second + ":" + ms + "ms";
        }
        return "00:00:0 ms";
    }

    public static void start(Context context){
        if(isAlive()) return;
        Intent service = new Intent(context, AutoClickService.class);
        context.startService(service);
    }

    public static boolean isAlive(){
        return service != null;
    }


    @Override
    public void onDestroy() {
        service = null;
        unregisterReceiver(receiver);
        unbindControlPanel.unbind();
        for (Point a : listCommands) {
            a.detachToWindow(wm, canvasView);
        }
        wm.removeView(timerPanel);
        wm.removeView(controlPanel);
        wm.removeView(recordPanel);
        wm.removeView(canvasView);
        listCommands.clear();
        super.onDestroy();
    }

    public static boolean getParamBound(){
        return service.paramBoundsOn;
    }

    public int getCounterRunMacro(){
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("counterRunMacro", 0);
    }

    public int incCounterRunMacro(){
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("counterRunMacro", getCounterRunMacro() + 1).apply();
        return getCounterRunMacro();
    }


    public static boolean getParamTimer(){ return service.paramTimerOn; }

    public static  TextView getTvTimer(){
        return tvTimer;
    }

    public static int getParamRepeatMacro(){
        return service.paramRepeatMacro;
    }

    public static int getParamSizePoint(){
        return service.paramSizePoint;
    }

    public static int getParamSizeControl(){
        return service.paramSizeControl;
    }

    public static List<Point> getListPoint(){
        return service.listCommands;
    }

    public static View getControlPanel(){
        return service.controlPanel;
    }

    public static WindowManager getWM(){
        return service.wm;
    }

    public static PointCanvasView getCanvas(){
        return service.canvasView;
    }

    public static AutoClickService getService(){
        return service;
    }

//    @OnClick(R.id.start_pause)
    public void startPauseCommand(){
        if(openRecordPanel){
            cancelRecord();
        }
        String action = SimulateTouchAccessibilityService.isPlaying() ? ACTION_STOP : ACTION_START;
        requestAction(this, action);
    }

    @OnClick(R.id.add_point)
    public void addPoint() {
        Point point = Point.PointBuilder.invoke()
                .position(canvasView.getWidth()/2, canvasView.getHeight()/2)
                .text(String.format("%s", listCommands.size() + 1))
                .build(ClickPoint.class);
        point.attachToWindow(wm,canvasView);
        updateTouchListenerPoint(point);
        listCommands.add(point);
        AutoClickService.getTvTimer().setText(AutoClickService.getTime());
    }

    @OnLongClick(R.id.add_point)
    public boolean addPointExtend() {
        List<Class<? extends Point>> listTypes = new ArrayList<>();
        listTypes.add(ClickPoint.class);
        listTypes.add(SwipePoint.class);
        listTypes.add(PinchPoint.class);
        listTypes.add(PathPoint.class);
        listTypes.add(MultiPoint.class);
        listTypes.add(HomePoint.class);
        View title = UtilsApp.getDialogTitle(this, getString(R.string.sel_type_goal));
        TypePointAdapter adapter = new TypePointAdapter(new ContextThemeWrapper(this, R.style.AppDialog), listTypes);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setAdapter(adapter, (dialog, which) -> {
                    Point point = Point.PointBuilder.invoke()
                        .position(canvasView.getWidth()/2, canvasView.getHeight()/2)
                        .text(String.format("%s", listCommands.size() + 1))
                        .build(listTypes.get(which));
                    point.attachToWindow(wm, canvasView);
                    updateTouchListenerPoint(point);
                    listCommands.add(point);
                    AutoClickService.getTvTimer().setText(AutoClickService.getTime());
                })
                .setCustomTitle(title);
        Dialog d = builder.create();
        d.getWindow().setType(getWindowsTypeApplicationOverlay());
        d.show();
        return true;
    }



    static class TypePointAdapter extends ArrayAdapter<String> {

        List<Class<? extends Point>> listTypes;
        LayoutInflater inflater;

        public TypePointAdapter(@NonNull Context context, List<Class<? extends Point>> types) {
            super(context, 0);
            listTypes = types;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return listTypes.size();
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return listTypes.get(position).getName();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = inflater.inflate(R.layout.list_type_point, parent, false);
            TextView tv = v.findViewById(R.id.text);
            tv.setText(listTypes.get(position).getSimpleName());
            ImageView img = v.findViewById(R.id.pic);
            Class clazz = listTypes.get(position);
            if(clazz.isAssignableFrom(SwipePoint.class)){
                img.setImageResource(R.drawable.ic_swap);
            }
            else if(clazz.isAssignableFrom(ClickPoint.class)){
                img.setImageResource(R.drawable.ic_point);
            }
            else if(clazz.isAssignableFrom(PinchPoint.class)){
                img.setImageResource(R.drawable.ic_pinch);
            }
            else if(clazz.isAssignableFrom(PathPoint.class)){
                img.setImageResource(R.drawable.ic_path_point);
            }
            else if(clazz.isAssignableFrom(MultiPoint.class)){
                img.setImageResource(R.drawable.ic_multi_point);
            }
            else if(clazz.isAssignableFrom(HomePoint.class)){
                img.setImageResource(R.drawable.ic_home_point);
            }
            return v;
        }
    }

    @OnClick(R.id.remove_point)
    public void removePoint(){
        if(listCommands.size() > 0){
            listCommands.remove(listCommands.size() - 1)
                    .detachToWindow(wm, canvasView);
        }
        AutoClickService.getTvTimer().setText(AutoClickService.getTime());
    }

    @OnClick(R.id.setting)
    public void showSetting(){
        Intent intent = new Intent(ACTIVITY_SETTING);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @OnClick(R.id.settings_table_points)
    public void showSettingPoints(){
        if(listCommands.size() != 0 ) {
            Intent intent = new Intent(ACTIVITY_SETTING_TABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        else {
            Toast toast = Toast.makeText(this, R.string.error_listcomand_null, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @OnClick(R.id.shareApp)
    public void shareApp(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_TEXT, R.string.link_app_play_market);
        startActivity(intent);
    }


//    @OnClick(R.id.expand)
    public void showHidePanel() {
        Log.v("appAutoClicker", "expand click");
        group_control.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int from = group_control.getHeight(),
                to = group_control.getHeight() == 0 ? group_control.getMeasuredHeight() : 0;
        ValueAnimator slideAnimator = ValueAnimator.ofInt(from, to);
        slideAnimator.addUpdateListener((anim) -> {
            group_control.getLayoutParams().height = (Integer) anim.getAnimatedValue();
            group_control.requestLayout();
        });
        slideAnimator.setDuration(1200);
        slideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        slideAnimator.start();
    }

    public void setControlSize(){
        ViewCollections.set(controls, (view, value, index) -> {
            value = value != null ? (int) Dimension.DP.convert(value) : 0;
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            layoutParams.width = value;
        }, paramSizeControl);
        wm.updateViewLayout(controlPanel, paramsControlPanel);
    }


    @OnClick(R.id.close)
    public void closeService() {
        if(SimulateTouchAccessibilityService.isPlaying()){
            requestAction(this, ACTION_STOP);
        }
        if(openRecordPanel)
            cancelRecord();
        stopSelf();
    }

    public static void hideViews(){
        timerPanel.setVisibility(View.GONE);
        AutoClickService.getControlPanel().setVisibility(View.GONE);
        AutoClickService.getListPoint().forEach((c)->c.setVisible(View.GONE));
        canvasView.invalidate();
    }

    public static void showViews() {
        if(AutoClickService.getParamTimer())
            timerPanel.setVisibility(View.VISIBLE);
        AutoClickService.getControlPanel().setVisibility(View.VISIBLE);
        AutoClickService.getListPoint().forEach((c)->c.setVisible(View.VISIBLE));
        canvasView.invalidate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null || intent.getAction() == null) return super.onStartCommand(intent, flags, startId);
        switch (intent.getAction()){
            case ACTION_STOP:
                SimulateTouchAccessibilityService.isPlaying = false;
                listCommands.forEach((c)->c.setTouchable(true, wm));
                controlPanel.findViewById(R.id.start_pause)
                        .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_play));
                group_control.setVisibility(View.VISIBLE);
                SimulateTouchAccessibilityService.requestStop();
                AutoClickService.getTvTimer().setText(AutoClickService.getTime());
                SimulateTouchAccessibilityService.isStartCounDownTimer = false;
                isActionStart = false;
                break;
            case ACTION_START:
                if(!isActionStart) {
                    inOpenStatusAndNavBarWidth();
                    isActionStart = true;
                    int startCount = incCounterRunMacro();
                    if (App.isShowAd() && interstitialAd.isLoaded() && startCount >= 2) {
                        if (getMiuiVersion() != 0) {
                            ifMiui();
                        } else {
                            showAdBeforeRunMacro();
                        }
                    } else {
                        runMacro();
                    }
                }
                break;
            case ACTION_HIDE_VIEWS:
                hideViews();
                break;
            case ACTION_SHOW_VIEWS:
                showViews();
                break;
            case ACTION_UPDATE_SETTING: //update after change setting
                updateSetting();
                paramsCanvas = UtilsApp.getWindowsParameterLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
                paramsCanvas.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                canvasView.setLayoutParams(paramsCanvas);
                wm.updateViewLayout(canvasView, paramsCanvas);
                listCommands.forEach(this::updatePoint);
                setControlSize();
                break;
            case ACTION_DUPLICATE_POINT:
                duplicatePoint(Objects.requireNonNull(intent.getParcelableExtra(KEY_POINT)));
                break;
            case ACTION_DELETE_POINT:
                deletePoint(Objects.requireNonNull(intent.getParcelableExtra(KEY_POINT)));
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void ifMiui(){
        Intent intent = new Intent(this, CheckPermPopUp.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Log.d("checkActivity2", "ifMiui: "+ checkPermPopUP);
            if (checkPermPopUP) {
                checkPermPopUP = false;
                showAdBeforeRunMacro();
            } else {
                DialogInterface.OnClickListener click = (dialogInterface, i) -> {
                    dialogInterface.cancel();
                };

                AlertDialog dialog = new AlertDialog.Builder(getApplicationContext())
                        .setTitle(getApplicationContext().getString(R.string.error_pop_up))
                        .setMessage(R.string.pop_up_info)
                        .setPositiveButton(R.string.ok, click)
                        .create();
                dialog.getWindow().setType(getWindowsTypeApplicationOverlay());
                dialog.show();
            }
            isActionStart = false;
        }, 2000);
    }

    private void showAdBeforeRunMacro(){
        hideViews();
        Intent intent2 = new Intent(getApplicationContext(), AdActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent2.putExtra("ad_request", "true");
        startActivity(intent2);
    }

    private void runMacro(){
        listCommands.forEach((c)->c.setTouchable(false,wm));
        controlPanel.findViewById(R.id.start_pause)
                .setBackground(ContextCompat.getDrawable(AutoClickService.this, R.drawable.ic_pause));
        group_control.setVisibility(View.GONE);
        allMSPoint = 0;

        SimulateTouchAccessibilityService.requestStart(listCommands);

    }

    private void runMacroAfterAd(){
        showViews();
        runMacro();
    }

    private void duplicatePoint(Point point){
        point.setText(String.format("%s", listCommands.size() + 1));
        point.attachToWindow(wm, canvasView);
        updateTouchListenerPoint(point);
        listCommands.add(point);
        AutoClickService.getTvTimer().setText(AutoClickService.getTime());
    }

    private void deletePoint(Point point){
        int index  = Collections.binarySearch(listCommands, point,
                (u1,u2)-> u1.getText().compareTo(u2.getText()));
        if(index > -1){
            Point p = listCommands.remove(index);
            reindexListCommand();
            p.detachToWindow(wm, canvasView);
        }
        AutoClickService.getTvTimer().setText(AutoClickService.getTime());
    }

    private void reindexListCommand(){
        AtomicInteger i = new AtomicInteger(1);
        listCommands.forEach((p)-> p.setText(String.valueOf(i.getAndIncrement())));
    }

    void updateSetting(){
        paramBoundsOn = Optional
                .ofNullable(getSetting(SettingExt.KEY_BOUNDS_ON, SettingExt.defaultBoundsOn))
                .orElse(SettingExt.defaultBoundsOn);
        paramRepeatMacro = Optional
                .ofNullable(getSetting(SettingExt.KEY_REPEAT, SettingExt.defaultRepeat))
                .orElse(SettingExt.defaultRepeat);
        paramSizePoint = Optional
                .ofNullable(getSetting(getString(R.string.key_preference_size_point), SettingExt.defaultSizePoint))
                .orElse(SettingExt.defaultSizePoint);
        paramSizeControl = Optional
                .ofNullable(getSetting(getString(R.string.key_preference_size_control_panel), SettingExt.defaultSizeControl))
                .orElse(SettingExt.defaultSizeControl);
        paramTimerOn = Optional
                .ofNullable(getSetting(SettingExt.KEY_TIMER_ON, SettingExt.defaultTimerOn))
                .orElse(SettingExt.defaultTimerOn);
        paramHideShareButtonOn = Optional
                .ofNullable(getSetting(SettingExt.KEY_SHARE_BUTTON_ON, SettingExt.defaultShareButtonOn))
                .orElse(SettingExt.defaultShareButtonOn);

        if(isInitalView){
            AutoClickService.getListPoint().forEach(point ->{
                point.updateParamsFlags();
            });

            if(!paramHideShareButtonOn)
                group_control.findViewById(R.id.shareApp).setVisibility(View.GONE);
            else
                group_control.findViewById(R.id.shareApp).setVisibility(View.VISIBLE);

            if (AutoClickService.getParamTimer() == true) {
                timerPanel.setVisibility(View.VISIBLE);
            } else
                timerPanel.setVisibility(View.GONE);
        }
        Log.d("paramTimerOn", "updateSetting: " + paramTimerOn);
        LogExt.logd(paramSizeControl);
        LogExt.logd("d:"+ SettingExt.defaultSizeControl);

    }

    void updatePoint(@NotNull Point c){
        updateViewLayoutPoint(c);
        updateTouchListenerPoint(c);
    }

    void updateViewLayoutPoint(@NotNull Point point){
        point.updateViewLayout(wm, paramSizePoint);
        canvasView.invalidate();
    }

    void swapPointOrientation(Point p){
        p.swapPointOrientation();
        p.updateViewLayout(wm, paramSizePoint);
        canvasView.invalidate();
    }

    void updateFlagsParams(Point p){
        p.updateParamsFlags();
        p.updateViewLayout(wm, paramSizePoint);
        canvasView.invalidate();
    }

    public void updateTouchListenerPoint(@NotNull Point point){
        point.updateListener(wm, canvasView, paramBoundsOn);
    }

    public static void requestAction(Context context, String action){
        Intent intent = new Intent(context, AutoClickService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    public static void requestAction(Context context, String action, String key, Parcelable parcelable){
        Intent intent = new Intent(context, AutoClickService.class);
        intent.setAction(action);
        intent.putExtra(key, parcelable);
        context.startService(intent);
    }

    @OnClick(R.id.record_points_start_pause)
    public void recordPoints(){
        if(!openRecordPanel) {
            openRecord();
        }
        else {
            cancelRecord();
        }
    }


    public void openRecord(){
        recordPoints.timerStart();
        openRecordPanel = true;
        wm.updateViewLayout(recordPanel, paramsRecordPanelFlagsOff);
        listCommands.forEach((c) -> c.setTouchable(false, wm));
        paramRepeatMacro = Optional
                .ofNullable(getSetting(SettingExt.KEY_REPEAT, 1))
                .orElse(1);
        controlPanel.findViewById(R.id.record_points_start_pause)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_radio_button));
    }

    public void cancelRecord(){
        recordPoints.timerStart();
        paramRepeatMacro = Optional
                .ofNullable(getSetting(SettingExt.KEY_REPEAT, SettingExt.defaultRepeat))
                .orElse(SettingExt.defaultRepeat);
        wm.updateViewLayout(recordPanel, paramsRecordPanelFlagsOn);
        listCommands.forEach((c) -> c.setTouchable(true, wm));
        openRecordPanel = false;
        controlPanel.findViewById(R.id.record_points_start_pause)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.ic_radio_button_off));
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        recordPoints.onTouch(event,wm, listCommands, canvasView, paramSizePoint);
        return true;
    }

    public static void updateLayoutFlagsOn(){
        wm.updateViewLayout(recordPanel, paramsRecordPanelFlagsOn);
    }
    public static void updateLayoutFlagsOff(){
        wm.updateViewLayout(recordPanel, paramsRecordPanelFlagsOff);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
