package com.askerweb.autoclickerreplay.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.askerweb.autoclickerreplay.App;
import com.askerweb.autoclickerreplay.R;
import com.askerweb.autoclickerreplay.ktExt.Dimension;
import com.askerweb.autoclickerreplay.service.AutoClickService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.askerweb.autoclickerreplay.ktExt.UtilsApp.getContext;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.fab)
    FloatingActionButton actionButton;

    public static InterstitialAd interstitialAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.initActivityComponent(this);
        MobileAds.initialize(this);
        List<String> android_id = Collections.singletonList("B661284821DE7327318792508C54E72D");
        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder()
                        .setTestDeviceIds(android_id)
                        .build()
        );
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.Interstitial_Unit_Ad_ID));
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Toast.makeText(MainActivity.this,
                "onAdFailedToLoad() with error code: " + i,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded() {
                Toast.makeText(MainActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
            }
        });
        interstitialAd.loadAd(new AdRequest.Builder().build());
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
//        AutoClickService.requestAction(this, AutoClickService.ACTION_STOP);
    }
}
