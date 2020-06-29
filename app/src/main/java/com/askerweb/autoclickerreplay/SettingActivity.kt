package com.askerweb.autoclickerreplay

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.askerweb.autoclickerreplay.ktExt.logd
import com.askerweb.autoclickerreplay.service.AutoClickService
import kotlinx.android.synthetic.main.setting_layout.*;

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_layout)
        save_btn.setOnClickListener {
            "setting activity save btn".logd()
            val builder = AlertDialog.Builder(this)
            val editNameFile = EditText(this)
            val layout  = LinearLayout(this)
            editNameFile.setText(getString(R.string.untitled))
            layout.addView(editNameFile)
            builder.setTitle(getString(R.string.title_save_script))
                    .setView(layout)
                    .setPositiveButton(getString(R.string.save)) { _, _ ->
                        saveMacroToJson(AutoClickService.getListPoint(), editNameFile.text.toString())
                    }
                    .setNegativeButton("Отмена"){ d, _ ->
                        d.cancel()
                    }
                    .create().show()
        }
        load_btn.setOnClickListener {
            AutoClickService.getListPoint().forEach{
                it.detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
            }
            AutoClickService.getListPoint().clear()
            loadMacroFromJson(AutoClickService.getListPoint(), "untitled")
            AutoClickService.getListPoint().forEach{
                it.attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                AutoClickService.service.updateTouchListenerPoint(it)
            }
        }
    }
}

