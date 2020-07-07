package com.askerweb.autoclickerreplay.activity

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.loadMacroFromJson
import com.askerweb.autoclickerreplay.ktExt.saveMacroToJson
import com.askerweb.autoclickerreplay.service.AutoClickService
import kotlinx.android.synthetic.main.setting_layout.*;

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_layout)
        save_btn.setOnClickListener {
            if(!AutoClickService.isRunning()){
                AutoClickService.start()
            }
            if(AutoClickService.isRunning() && AutoClickService.getListPoint().size > 0){
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
            else{
                Toast.makeText(this, R.string.toast_empty_script, Toast.LENGTH_LONG).show()
            }
        }
        load_btn.setOnClickListener { //TODO "refactor code here", comment code here
            AutoClickService.start()
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
            val dir = filesDir.listFiles()
            dir?.forEach {
                adapter.add(it.name.removeRange(it.name.indexOf('.') until it.name.length))
            }
            val dialog = AlertDialog.Builder(this)
                    .setTitle(resources.getString(R.string.load_script))
                    .setAdapter(adapter) { d, w->
                        AutoClickService.getListPoint().forEach{
                            it.detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                        }
                        AutoClickService.getListPoint().clear()
                        adapter.getItem(w)?.let {
                            it1 -> loadMacroFromJson(AutoClickService.getListPoint(), it1)
                        }
                        AutoClickService.getListPoint().forEach{
                            it.attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                            AutoClickService.service.updateTouchListenerPoint(it)
                        }
                    }
                    .create()
            dialog.show()

        }
    }
}

