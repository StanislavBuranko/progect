package com.askerweb.autoclickerreplay.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.askerweb.autoclickerreplay.service.AutoClickService.checkPermPopUP

class ChekPermPopUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermPopUP = true;
    }

}