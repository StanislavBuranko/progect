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

import butterknife.ButterKnife;

import static com.askerweb.autoclickerreplay.service.AutoClickService.getListPoint;

public class TablePointsActivity extends AppCompatActivity {

    Button save_change;
    List<Point> listCommand =  AutoClickService.getListPoint();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_setting_points);
        int i = 1;
        TableLayout tableLayout = (TableLayout) findViewById(R.id.table);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Point point : listCommand) {
            point.createTableView(tableLayout, inflater);
        }
        Button save_change = (Button) findViewById(R.id.save_change);
        save_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        AutoClickService.showViews();
    }
}
