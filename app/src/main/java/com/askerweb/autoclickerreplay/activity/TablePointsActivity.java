package com.askerweb.autoclickerreplay.activity;

import android.app.LauncherActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.askerweb.autoclickerreplay.R;
import com.askerweb.autoclickerreplay.point.ClickPoint;
import com.askerweb.autoclickerreplay.point.HomePoint;
import com.askerweb.autoclickerreplay.point.MultiPoint;
import com.askerweb.autoclickerreplay.point.PathPoint;
import com.askerweb.autoclickerreplay.point.PinchPoint;
import com.askerweb.autoclickerreplay.point.Point;
import com.askerweb.autoclickerreplay.point.SimplePoint;
import com.askerweb.autoclickerreplay.point.SwipePoint;
import com.askerweb.autoclickerreplay.service.AutoClickService;

import java.nio.file.Path;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.ButterKnife;

import static com.askerweb.autoclickerreplay.service.AutoClickService.getListPoint;

public class TablePointsActivity extends AppCompatActivity {

    Button save_change;
    List<Point> listCommand =  AutoClickService.getListPoint();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoClickService.hideViews();
        AutoClickService.getListPoint().forEach(point -> {
            point.setTouchable(false, AutoClickService.getWM());
        });

        setContentView(R.layout.activity_table_setting_points);
        int i = 1;
        TableLayout tableLayout = (TableLayout) findViewById(R.id.table);
        LayoutInflater inflater = LayoutInflater.from(this);
        updateTable(tableLayout, inflater);

    }

    @Override
    protected void onPause() {
        super.onPause();
        AutoClickService.showViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AutoClickService.showViews();
        AutoClickService.getListPoint().forEach(point -> {
            point.setTouchable(true, AutoClickService.getWM());
        });
        AutoClickService.getTvTimer().setText(AutoClickService.getTime());
    }

    public static void updateTable(TableLayout tableLayout, LayoutInflater inflater){
        tableLayout.removeAllViews();
        TableRow trHeading = (TableRow) inflater.inflate(R.layout.table_row_heading, null);
        tableLayout.addView(trHeading);
        for (Point point : AutoClickService.getListPoint()) {
            point.setVisible(View.GONE);
            point.createTableView(tableLayout, inflater);
        }
    }
}
