@file:JvmName("UtilsApp")

package com.askerweb.autoclickerreplay.ktExt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.point.Point
import com.askerweb.autoclickerreplay.service.SimulateTouchAccessibilityService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.FileReader
import java.io.FileWriter


val context = App.appComponent.getAppContext()
val gson = App.appComponent.getGson()

fun checkPermissionOverlay(context: Context?) : Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

    Log.println(Log.ASSERT, "appAutoClicker", "permission overlay:" + Settings.canDrawOverlays(context))
    return Settings.canDrawOverlays(context)
}


fun isIntentAvailable(intent: Intent?, context: Context): Boolean {
    return intent != null && context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size > 0
}


fun checkAccessibilityPermission(context: Context?, service: Class<out AccessibilityService>) : Boolean {
    val am = context!!.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

    for (enabledService in enabledServices) {
        val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
        if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == service.name) return true
    }


    var accessibilityEnabled = 0
    val serviceName: String = context.packageName + "/" + SimulateTouchAccessibilityService::class.java.canonicalName
    try {
        accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED)
        Log.v(TAG, "accessibilityEnabled = $accessibilityEnabled")
    } catch (e: SettingNotFoundException) {
        Log.e(TAG, "Error finding setting, default accessibility to not found: "
                + e.message)
    }
    val mStringColonSplitter = SimpleStringSplitter(':')

    if (accessibilityEnabled == 1) {
        Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------")
        val settingValue = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (settingValue != null) {
            mStringColonSplitter.setString(settingValue)
            while (mStringColonSplitter.hasNext()) {
                val accessibilityService = mStringColonSplitter.next()
                Log.v(TAG, "-------------- > accessibilityService :: $accessibilityService $service")
                if (accessibilityService.equals(serviceName, ignoreCase = true)) {
                    Log.v(TAG, "We've found the correct setting - accessibility is switched on!")
                    return true
                }
            }
        }
    } else {
        Log.v(TAG, "***ACCESSIBILITY IS DISABLED***")
    }

    return false
}

fun checkAllPermission(context: Context?) : Boolean {
    var allPermission = true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Log.println(Log.ASSERT, "appAutoClicker", "permission overlay:" + checkPermissionOverlay(context))
        allPermission = checkPermissionOverlay(context)
    }
    Log.println(Log.ASSERT, "appAutoClicker", "accessibility simulateTouch:" + checkAccessibilityPermission(context, SimulateTouchAccessibilityService::class.java))
    allPermission = allPermission && checkAccessibilityPermission(context, SimulateTouchAccessibilityService::class.java)
    return allPermission
}

fun getWindowsTypeApplicationOverlay() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

const val standardOverlayFlags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

@JvmOverloads
fun getWindowsParameterLayout(_width:Float,
                                            _height: Float,
                                            gravity: Int = Gravity.START,
                                            _flags: Int = standardOverlayFlags,
                                            _types:Int = getWindowsTypeApplicationOverlay(),
                                            constDim: Boolean = true) : WindowManager.LayoutParams{
    val width =
            if(_width < 0 && constDim) _width.toInt() else Dimension.DP.convert(_width).toInt()
    val height =
            if(_height < 0 && constDim) _height.toInt() else  Dimension.DP.convert(_height).toInt()
    val params = WindowManager.LayoutParams(
            width,
            height,
            _types,
            _flags,
            PixelFormat.TRANSLUCENT)
    params.gravity = gravity
    params.x = 0
    params.y = 0
    return params
}

fun saveMacroToJson(points: List<Point>, nameMacro: String = "untitle.json"){
    val pointsJson = JsonArray()
    points.forEach{
        val json = it.toJson()
        pointsJson.add(json)
    }
    val saveJson = JsonObject()
    saveJson.add("points", pointsJson)
    FileWriter("${context.filesDir}/$nameMacro.json").use{writer ->
        gson.toJson(saveJson, writer)
    }
}

fun loadMacroFromJson(points: MutableList<Point>, nameMacro: String){
    var jsonObj:JsonObject? = null
    FileReader("${context.filesDir}/$nameMacro.json").use {
        val text = it.readText().trim()
                .replace("\\[", "")
                .replace("\\]", "")
        jsonObj = gson.fromJson(text, JsonObject::class.java)
    }
    val jsonArrayPoints =  jsonObj?.getAsJsonArray("points")
    jsonArrayPoints?.forEach {
        val jsonPoint = gson.fromJson(it.asString, JsonObject::class.java)
        val clazz = Class.forName(jsonPoint.get("class").asString) as Class<Point>
        val point:Point = Point.PointBuilder.invoke().buildFrom(clazz, jsonPoint)
        points.add(point)
    }
}

fun getDialogTitle(context: Context, text:String): View {
    val title = TextView(context)
    title.text = text
    title.setTextColor(context.resources.getColor(R.color.textColorDark))
    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, context.resources.getDimension(R.dimen.xsmall_text_size))
    title.typeface = Typeface.DEFAULT_BOLD
    title.setPadding(context.resources.getDimension(R.dimen.middle_size).toInt(),
            context.resources.getDimension(R.dimen.xxsmall_size).toInt(),
            0,
            context.resources.getDimension(R.dimen.xsmall_size).toInt())
    return title
}

enum class Dimension(private val type: Int){
    DP(TypedValue.COMPLEX_UNIT_DIP),
    SP(TypedValue.COMPLEX_UNIT_SP);
    @JvmOverloads fun convert(value: Float, metrics:DisplayMetrics? = displayMetrics):Float = TypedValue.applyDimension(type, value, metrics)
    companion object{
        @JvmField var displayMetrics:DisplayMetrics? = DisplayMetrics()
    }
}