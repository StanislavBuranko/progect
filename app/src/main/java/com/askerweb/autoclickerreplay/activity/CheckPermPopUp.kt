package com.askerweb.autoclickerreplay.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.askerweb.autoclickerreplay.service.AutoClickService.checkPermPopUP

class CheckPermPopUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermPopUP = true;
    }

    override fun onResume() {
        super.onResume()
        checkPermPopUP = true;
    }

}