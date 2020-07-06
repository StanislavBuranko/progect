package com.askerweb.autoclickerreplay.services;

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
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.askerweb.autoclickerreplay.App;
import com.askerweb.autoclickerreplay.Dimension;
import com.askerweb.autoclickerreplay.LogExt;
import com.askerweb.autoclickerreplay.MainActivity;
import com.askerweb.autoclickerreplay.R;
import com.askerweb.autoclickerreplay.SettingExt;
import com.askerweb.autoclickerreplay.UtilsApp;
import com.askerweb.autoclickerreplay.point.ClickPoint;
import com.askerweb.autoclickerreplay.point.PinchPoint;
import com.askerweb.autoclickerreplay.point.Point;
import com.askerweb.autoclickerreplay.point.PointCanvasView;
import com.askerweb.autoclickerreplay.point.SwipePoint;
import com.askerweb.autoclickerreplay.point.ViewOverlayOnTouchListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import butterknife.ViewCollections;

import static java.lang.Thread.sleep;

@SuppressLint("ClickableViewAccessibility")
public class AutoClickService extends Service implements View.OnTouchListener {

    public static AutoClickService service = null;

    WindowManager wm = null;
    Unbinder unbindControlPanel = null;
    View controlPanel;
    View recordPanel;
    PointCanvasView canvasView;

    @BindView(R.id.group_control)
    View group_control;
    @BindViews({R.id.start_pause, R.id.remove_point, R.id.add_point, R.id.setting, R.id.close})
    List<View> controls;

    public LinkedList<Point> listCommando = new LinkedList<>();
    public Gson gson = new GsonBuilder().create();

    public Boolean paramBoundsOn;
    public Integer paramRepeatMacro;
    public Integer paramSizePoint;
    public Integer paramSizeControl;


    public static final WindowManager.LayoutParams paramsControlPanel =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    public static final WindowManager.LayoutParams paramsRecordPanel =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    public static final WindowManager.LayoutParams paramsCanvas =
            UtilsApp.getWindowsParameterLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.CENTER);


    // update listener after change orientation
    public final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            listCommando.forEach(AutoClickService.this::swipePointOrientation);
        }
    };


    public final static String KEY_POINT = "point";

    public final static String ACTION_STOP = "ACTION_STOP";
    public final static String ACTION_START = "ACTION_START";
    public final static String ACTION_UPDATE_SETTING = "ACTION_UPDATE_SETTING";
    public final static String ACTION_DUPLICATE_POINT = "ACTION_DUPLICATE_POINT";
    public final static String ACTION_DELETE_POINT = "ACTION_DELETE_POINT";

    public final static String ACTIVITY_SETTING = "com.askerweb.autoclicker.setting";


    @Override
    public void onCreate() {
        super.onCreate();
        LogExt.logd("OnCreate", "AutoClickUpdateListener");
        updateSetting();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        controlPanel = LayoutInflater.from(this).inflate(R.layout.control_panel_service, null);
        controlPanel.setLayoutParams(paramsControlPanel);
        controlPanel.setOnTouchListener(new ViewOverlayOnTouchListener(controlPanel, wm));
        wm.addView(controlPanel, paramsControlPanel);

        canvasView = new PointCanvasView(this, listCommando);
        paramsCanvas.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        canvasView.setLayoutParams(paramsCanvas);
        // update listener after change orientation
        canvasView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                listCommando.forEach(AutoClickService.this::updateTouchListenerPoint)
        );
        wm.addView(canvasView, paramsCanvas);

        unbindControlPanel = ButterKnife.bind(this, controlPanel);

        setControlSize();

        controlPanel.findViewById(R.id.start_pause).setOnTouchListener((v, event)->{
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    startPauseCommand();
                    break;
            }
            return true;
        });
        controlPanel.findViewById(R.id.close).setOnTouchListener((v, event)->{
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    closeService();
                    break;
            }
            return true;
        });

        //start listing change orientation
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(receiver, intentFilter);
        service = this;
    }



    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        unbindControlPanel.unbind();
        for (Point a : listCommando) {
            a.detachToWindow(wm, canvasView);
        }
        wm.removeView(controlPanel);
        wm.removeView(canvasView);
        super.onDestroy();
    }

    public static Gson getGson(){
        return service.gson;
    }

    public static boolean getParamBound(){
        return service.paramBoundsOn;
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

    public static LinkedList<Point> getListPoint(){
        return service.listCommando;
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
        String action = SimulateTouchAccessibilityService.isPlaying() ? ACTION_STOP : ACTION_START;
        requestAction(action);
    }

    @OnClick(R.id.add_point)
    public void addPoint() {
        Point point = Point.PointBuilder.invoke()
                .position(canvasView.getWidth()/2, canvasView.getHeight()/2)
                .text(String.format("%s", listCommando.size() + 1))
                .build(ClickPoint.class);
        point.attachToWindow(wm,canvasView);
        updateTouchListenerPoint(point);
        listCommando.add(point);
    }

    @OnLongClick(R.id.add_point)
    public boolean addPointExtend() {
        List<Class<? extends Point>> listTypes = new ArrayList<>();
        listTypes.add(ClickPoint.class);
        listTypes.add(SwipePoint.class);
        listTypes.add(PinchPoint.class);
        TypePointAdapter adapter = new TypePointAdapter(App.getContext(), listTypes);
        AlertDialog.Builder builder = new AlertDialog.Builder(App.getContext())
                .setAdapter(adapter, (dialog, which) -> {
                    Point point = Point.PointBuilder.invoke()
                        .position(canvasView.getWidth()/2, canvasView.getHeight()/2)
                        .text(String.format("%s", listCommando.size() + 1))
                        .build(listTypes.get(which));
                    point.attachToWindow(wm, canvasView);
                    updateTouchListenerPoint(point);
                    listCommando.add(point);
                })
                .setTitle("Выберите вид цели");
        Dialog d = builder.create();
        d.getWindow().setType(UtilsApp.getWindowsTypeApplicationOverlay());
        d.show();
        return true;
    }




    class TypePointAdapter extends ArrayAdapter<String> {

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
                img.setImageResource(R.drawable.ic_swipe_point);
            }
            else if(clazz.isAssignableFrom(ClickPoint.class)){
                img.setImageResource(R.drawable.ic_click_point);
            }
            else if(clazz.isAssignableFrom(PinchPoint.class)){
                img.setImageResource(R.drawable.ic_pinch_point);
            }
            return v;
        }
    }

    @OnClick(R.id.remove_point)
    public void removePoint(){
        if(listCommando.size() > 0){
            listCommando.remove(listCommando.size() - 1)
                    .detachToWindow(wm, canvasView);
        }
    }

    @OnClick(R.id.setting)
    public void showSetting(){
        Intent intent = new Intent(ACTIVITY_SETTING);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
        Log.v("appAutoClicker", "destroy");
        if(SimulateTouchAccessibilityService.isPlaying()){
            requestAction(ACTION_STOP);
        }
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null || intent.getAction() == null) return super.onStartCommand(intent, flags, startId);
        switch (intent.getAction()){
            case ACTION_STOP:
                listCommando.forEach((c)->c.setTouchable(true, wm));
                controlPanel.findViewById(R.id.start_pause)
                        .setBackground(ContextCompat.getDrawable(App.getContext(), android.R.drawable.ic_media_play));
                group_control.setVisibility(View.VISIBLE);
                SimulateTouchAccessibilityService.requestStop();
                break;
            case ACTION_START:
                listCommando.forEach((c)->c.setTouchable(false,wm));
                controlPanel.findViewById(R.id.start_pause)
                        .setBackground(ContextCompat.getDrawable(App.getContext(), android.R.drawable.ic_media_pause));
                group_control.setVisibility(View.GONE);
                SimulateTouchAccessibilityService.requestStart(listCommando);
                LogExt.logd("after start");
                break;
            case ACTION_UPDATE_SETTING: //update after change setting
                updateSetting();
                listCommando.forEach(this::updatePoint);
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

    private void duplicatePoint(Point point) {
        point.setText(String.format("%s", listCommando.size() + 1));
        point.attachToWindow(wm, canvasView);
        updateTouchListenerPoint(point);
        listCommando.add(point);
    }

    private void deletePoint(Point point){
        int index  = Collections.binarySearch(listCommando, point,
                (u1,u2)-> u1.getText().compareTo(u2.getText()));
        if(index > -1){
            Point p = listCommando.remove(index);
            reindexListCommand();
            p.detachToWindow(wm, canvasView);
        }
    }

    private void reindexListCommand(){
        AtomicInteger i = new AtomicInteger(1);
        listCommando.forEach((p)-> p.setText(String.valueOf(i.getAndIncrement())));
    }

    void updateSetting(){
        paramBoundsOn = Optional
                .ofNullable(SettingExt.getSetting(SettingExt.KEY_BOUNDS_ON, SettingExt.defaultBoundsOn))
                .orElse(SettingExt.defaultBoundsOn);
        paramRepeatMacro = Optional
                .ofNullable(SettingExt.getSetting(SettingExt.KEY_REPEAT, SettingExt.defaultRepeat))
                .orElse(SettingExt.defaultRepeat);
        paramSizePoint = Optional
                .ofNullable(SettingExt.getSetting(getString(R.string.key_preference_size_point), SettingExt.defaultSizePoint))
                .orElse(SettingExt.defaultSizePoint);
        paramSizeControl = Optional
                .ofNullable(SettingExt.getSetting(getString(R.string.key_preference_size_control_panel), SettingExt.defaultSizeControl))
                .orElse(SettingExt.defaultSizeControl);
    }

    void updatePoint(@NotNull Point c){
        updateViewLayoutPoint(c);
        updateTouchListenerPoint(c);
    }

    void updateViewLayoutPoint(@NotNull Point point){
        point.updateViewLayout(wm, paramSizePoint);
        canvasView.invalidate();
    }

    void swipePointOrientation(Point p){
        View v = p.getView();
        WindowManager.LayoutParams params =
                (WindowManager.LayoutParams)v.getLayoutParams();
        int temp = params.x;
        params.x = (params.y);
        params.y = temp;
        wm.updateViewLayout(v, params);
    }

    public void updateTouchListenerPoint(@NotNull Point point){
        point.updateListener(wm, canvasView, paramBoundsOn);
    }

    public static void requestAction(String action){
        Intent intent = new Intent(App.getContext(), AutoClickService.class);
        intent.setAction(action);
        App.getContext().startService(intent);
    }

    public static void requestAction(String action, String key, Parcelable parcelable){
        Intent intent = new Intent(App.getContext(), AutoClickService.class);
        intent.setAction(action);
        intent.putExtra(key, parcelable);
        App.getContext().startService(intent);
    }

    boolean openPanel = false;
    @OnClick(R.id.record_points)
    public void recordPoints(){
        /* Intent service = new Intent(this, RecordService.class);
        this.startService(service);

       */

        if(!openPanel) {
            recordPanel = LayoutInflater.from(this).inflate(R.layout.record_panel, null);
            wm.addView(recordPanel, paramsRecordPanel);
            recordPanel.setOnTouchListener(this);

            wm.removeView(controlPanel);
            wm.removeView(canvasView);
            wm.addView(controlPanel, paramsControlPanel);
            wm.addView(canvasView, paramsCanvas);
            openPanel = true;
        }
        else {
            openPanel = false;
            wm.removeView(recordPanel);
        }


    }

    float xDown, yDown;

    boolean actionUp = false;
    boolean actionMove = false;
    boolean actionDown = false;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            if(actionUp != true) {
                actionUp = true;
                ClassInfoForUpTouch.yUp = (int) Math.round(event.getY());
                ClassInfoForUpTouch.xUp = (int) Math.round(event.getX());

            }
            break;
        case MotionEvent.ACTION_MOVE:
            actionMove = true;
            actionUp = false;
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
            Point point = Point.PointBuilder.invoke()
                    .position((int) xDown, (int) yDown)
                    .text(String.format("%s", listCommando.size() + 1))
                    .build(ClickPoint.class);
            point.attachToWindow(wm, canvasView);
            updateTouchListenerPoint(point);
            listCommando.add(point);
            actionUp = false;
            actionDown = false;
        }
        else if (actionMove == true && actionUp == true && actionUp == true) {
            Point point = Point.PointBuilder.invoke()
                    .position((int)xDown,(int)yDown)
                    .text(String.format("%s", listCommando.size() + 1))
                    .build(SwipePoint.class);
            point.attachToWindow(wm, canvasView);
            updateTouchListenerPoint(point);
            listCommando.add(point);

        }
        return false;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
