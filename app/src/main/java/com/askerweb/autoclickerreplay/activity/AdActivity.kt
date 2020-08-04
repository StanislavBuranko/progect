package com.askerweb.autoclickerreplay.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.ktExt.logd
import com.google.android.gms.ads.InterstitialAd
import javax.inject.Inject

class AdActivity : AppCompatActivity() {

    @Inject
    lateinit var interstitialAd: InterstitialAd

    companion object{
        private lateinit var _instance:AdActivity

        @JvmStatic
        val instance
            get() = _instance
    }

    val closedInterstitialAd = Handler(Handler.Callback {
        "closedAd".logd()
        intent.putExtra("ad_request", "")
        moveTaskToBack(true)
        finish()
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        _instance = this
        App.appComponent.inject(_instance)
    }

    override fun onResume() {
        super.onResume()
        "onResume".logd()
        val ad = intent.getStringExtra("ad_request")
        "ad_request:$ad".logd()
        if (ad != null && ad.equals("true", ignoreCase = true)) {
            interstitialAd.show()
            "showed Ad".logd()
        }
        else{
            "show main activity".logd()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}