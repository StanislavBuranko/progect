package com.askerweb.autoclickerreplay.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.loadMacroFromJson
import com.askerweb.autoclickerreplay.ktExt.saveMacroToJson
import com.askerweb.autoclickerreplay.service.AutoClickService
import kotlinx.android.synthetic.main.setting_layout.*;
import java.io.File

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_layout)
        save_btn.setOnClickListener {
            if(!AutoClickService.isRunning()){
                AutoClickService.start(this)
            }
            if(AutoClickService.isRunning() && AutoClickService.getListPoint().size > 0){
                val builder = AlertDialog.Builder(this, R.style.AppDialog)
                val contentView = LayoutInflater.from(this)
                        .inflate(R.layout.dialog_edit_text, null, false)
                val editNameFile = contentView.findViewById<EditText>(R.id.editText)
                editNameFile.setText(getString(R.string.untitled))
                val dialog = builder.setTitle(getString(R.string.title_save_script))
                        .setView(contentView)
                        .setPositiveButton(getString(R.string.save)) { _, _ ->
                            saveMacroToJson(AutoClickService.getListPoint(), editNameFile.text.toString())
                        }
                        .setNegativeButton(getString(R.string.cancel)){ d, _ ->
                            d.cancel()
                        }
                        .create()
                dialog.show()
            }
            else{
                Toast.makeText(this, R.string.toast_empty_script, Toast.LENGTH_LONG).show()
            }
        }
        load_btn.setOnClickListener { //TODO "refactor code here", comment code here
            AutoClickService.start(this)
            val dir = filesDir.listFiles()?.toList()
            val adapter = ScriptSavedAdapterFiles(dir!! as MutableList<File>, LayoutInflater.from(this))
            val dialog = AlertDialog.Builder(this,  R.style.AppDialog)
                    .setTitle(resources.getString(R.string.load_script))
                    .setAdapter(adapter){_,_->}
                    .create()
            dialog.show()

        }
    }

    class ScriptSavedAdapterFiles constructor(var listFiles:MutableList<File>,
                                              var inflater: LayoutInflater) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v = inflater.inflate(R.layout.list_files, parent, false)
            val vT = v.findViewById<TextView>(R.id.text)
            vT.text = getItem(position)
            val btn = v.findViewById<Button>(R.id.btn_delete)
            btn.setOnClickListener{
                val f = File("${inflater.context.filesDir}/${getItem(position)}")
                f.delete()
                listFiles.removeAt(position)
                notifyDataSetChanged()
            }
            v.setOnClickListener{
                AutoClickService.getListPoint().forEach{
                    it.detachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                }
                AutoClickService.getListPoint().clear()
                loadMacroFromJson(AutoClickService.getListPoint(), getItem(position))
                AutoClickService.getListPoint().forEach{
                    it.attachToWindow(AutoClickService.getWM(), AutoClickService.getCanvas())
                    AutoClickService.service.updateTouchListenerPoint(it)
                }
            }
            return v
        }

        override fun getCount() = listFiles.size

        override fun getItem(position: Int) = listFiles[position].nameWithoutExtension

        override fun getItemId(position: Int) = position.toLong()
    }
}

