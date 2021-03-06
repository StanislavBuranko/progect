package com.askerweb.autoclickerreplay.activity

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.ktExt.loadMacroFromJson
import com.askerweb.autoclickerreplay.ktExt.saveMacroToJson
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.setting_layout.*
import java.io.File
import java.io.FilenameFilter

class SettingActivity : AppCompatActivity() {


    companion object{
        private var _handlerBoughtAd:Handler? = null
        @JvmStatic val handlerBoughtAd
            get() = _handlerBoughtAd
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var isOpenLoadDialog = false
        var isOpenSaveDialog = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_layout)
        save_btn.setOnClickListener {
            if(!isOpenSaveDialog) {
                isOpenSaveDialog = true
                if (!AutoClickService.isAlive()) {
                    AutoClickService.start(this)
                }
                if (AutoClickService.isAlive() && AutoClickService.getListPoint().size > 0) {
                    AutoClickService.requestAction(this, AutoClickService.ACTION_HIDE_VIEWS)
                    val builder = AlertDialog.Builder(this, R.style.AppDialog)
                    val contentView = LayoutInflater.from(this)
                            .inflate(R.layout.dialog_edit_text, null, false)
                    val editNameFile = contentView.findViewById<EditText>(R.id.editText)
                    editNameFile.setText(getString(R.string.untitled))
                    val dialog = builder.setTitle(getString(R.string.title_save_script))
                            .setView(contentView)
                            .setPositiveButton(getString(R.string.save)) { d, _ ->
                                saveMacroToJson(AutoClickService.getListPoint(), editNameFile.text.toString())
                                d.cancel()
                                isOpenSaveDialog = false
                            }
                            .setNegativeButton(getString(R.string.cancel)) { d, _ ->
                                d.cancel()
                                isOpenSaveDialog = false
                            }
                            .setOnCancelListener {
                                isOpenSaveDialog = false
                                AutoClickService.requestAction(this, AutoClickService.ACTION_SHOW_VIEWS) }
                            .create()
                    dialog.show()
                } else {
                    Toast.makeText(this, R.string.toast_empty_script, Toast.LENGTH_LONG).show()
                    isOpenSaveDialog = false
                }
            }
        }
        load_btn.setOnClickListener {
            if(!isOpenLoadDialog) {
                isOpenLoadDialog = true
                AutoClickService.start(this)
                val dir = mutableListOf<File>(*filesDir.listFiles(FilenameFilter { file, s ->
                    return@FilenameFilter s.toLowerCase().endsWith(".json")
                }))
                
                if (dir.isNotEmpty()) {
                    AutoClickService.requestAction(this, AutoClickService.ACTION_HIDE_VIEWS)
                    val adapter = ScriptSavedAdapterFiles(dir, LayoutInflater.from(this))
                    val dialog = AlertDialog.Builder(this, R.style.AppDialog)
                            .setTitle(resources.getString(R.string.load_script))
                            .setAdapter(adapter) { _, _ -> isOpenLoadDialog = false}
                            .setOnCancelListener { isOpenLoadDialog = false
                                AutoClickService.requestAction(this, AutoClickService.ACTION_SHOW_VIEWS) }
                            .create()
                    adapter.callbackDelete = { if (dir.isEmpty()) dialog.cancel() }
                    adapter.callbackDownload = { dialog.cancel() }
                    dialog.show()
                } else {
                    Toast.makeText(this, R.string.toast_havent_saved_script, Toast.LENGTH_LONG).show()
                    isOpenLoadDialog = false
                }
            }
        }
        turn_off_ad.setOnClickListener {
            App.launchPay(this, getString(R.string.id_sku_turn_off_ad));
        }
        _handlerBoughtAd = Handler{
            adBanner.visibility = GONE
            adBanner.destroy()
            turn_off_ad.visibility = GONE
            true
        }
        if(App.isShowAd()) {
            adBanner.loadAd(AdRequest.Builder().build())
        }
        else{
            adBanner.visibility = GONE
            turn_off_ad.visibility = GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _handlerBoughtAd = null
    }

    class ScriptSavedAdapterFiles constructor(var listFiles:MutableList<File>,
                                              var inflater: LayoutInflater) : BaseAdapter() {

        var callbackDownload = {}
        var callbackDelete = {}

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val v = inflater.inflate(R.layout.list_files, parent, false)
            val vT = v.findViewById<TextView>(R.id.text)
            vT.text = getItem(position)
            val btn = v.findViewById<Button>(R.id.btn_delete)
            //delete button
            btn.setOnClickListener{
                val f = File("${inflater.context.filesDir}/${getItem(position)}.json")
                f.delete()
                listFiles.removeAt(position)
                notifyDataSetChanged()
                callbackDelete.invoke()
            }
            //choose download file
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
                callbackDownload.invoke()
            }
            return v
        }

        override fun getCount() = listFiles.size

        override fun getItem(position: Int) = listFiles[position].nameWithoutExtension

        override fun getItemId(position: Int) = position.toLong()
    }
}

