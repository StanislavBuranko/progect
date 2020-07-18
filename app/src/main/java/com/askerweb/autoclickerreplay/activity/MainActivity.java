package com.askerweb.autoclickerreplay.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.askerweb.autoclickerreplay.App;
import com.askerweb.autoclickerreplay.R;
import com.askerweb.autoclickerreplay.ktExt.Dimension;
import com.askerweb.autoclickerreplay.service.AutoClickService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.fab)
    FloatingActionButton actionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.initComponent(this);
        Dimension.displayMetrics = getResources().getDisplayMetrics();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        NavController navController = Navigation.findNavController(this, R.id.navigation_fragment);
        navController.addOnDestinationChangedListener((c,d,arg)->{
            actionButton.setVisibility(d.getId() == R.id.faq_fragment ? View.VISIBLE : View.GONE);
        });

        actionButton.setOnClickListener((v)->{
            AutoClickService.start(this);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AutoClickService.requestAction(this, AutoClickService.ACTION_STOP);
    }
}
