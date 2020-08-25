package com.askerweb.autoclickerreplay.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.service.AutoClickService.checkPermPopUP

class CheckPermPopUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermPopUP = true;
        checkPermPopUP.logd("checkActivityCreate")
    }

    override fun onResume() {
        super.onResume()
        val intent2 = Intent(this@CheckPermPopUp, MainActivity::class.java)
        intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent2)

        checkPermPopUP = true; checkPermPopUP.logd("checkActivity")
    }

    override fun onPause() {
        super.onPause()
        checkPermPopUP = true; checkPermPopUP.logd("checkActivity")
    }

    override fun onStart() {
        super.onStart()
        checkPermPopUP = true; checkPermPopUP.logd("checkActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
        checkPermPopUP = true; checkPermPopUP.logd("checkActivity")
    }


}