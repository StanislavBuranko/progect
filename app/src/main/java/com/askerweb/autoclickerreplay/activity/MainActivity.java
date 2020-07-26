package com.askerweb.autoclickerreplay.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.askerweb.autoclickerreplay.App;
import com.askerweb.autoclickerreplay.R;
import com.askerweb.autoclickerreplay.ktExt.Dimension;
import com.askerweb.autoclickerreplay.ktExt.LogExt;
import com.askerweb.autoclickerreplay.service.AutoClickService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.getContext;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.fab)
    FloatingActionButton actionButton;

    @Inject
    public InterstitialAd interstitialAd;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.initActivityComponent(this);
        App.activityComponent.inject(this);
        interstitialAd.loadAd(new AdRequest.Builder().build());
        Dimension.displayMetrics = getResources().getDisplayMetrics();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        NavController navController = Navigation.findNavController(this, R.id.navigation_fragment);
        navController.addOnDestinationChangedListener((c,d,arg)->{
            actionButton.setVisibility(d.getId() == R.id.faq_fragment ? View.VISIBLE : View.GONE);
        });

        actionButton.setOnClickListener((v)->{
            AutoClickService.start(this);
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        String ad = getIntent().getStringExtra("ad_request");
        if(ad != null && !ad.isEmpty()){
            interstitialAd.show();
            getIntent().putExtra("ad_request", "");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LogExt.logd("onDestroy");
        super.onDestroy();
    }

}



