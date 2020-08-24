@file:JvmName("UtilsApp")

package com.askerweb.autoclickerreplay.ktExt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import com.askerweb.autoclickerreplay.App
import com.askerweb.autoclickerreplay.R
import com.askerweb.autoclickerreplay.point.*
import com.askerweb.autoclickerreplay.service.AutoClickService
import com.askerweb.autoclickerreplay.service.AutoClickService.getWM
import com.askerweb.autoclickerreplay.service.AutoClickService.recordPanel
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

fun getParamOverlayFlags() : Int {
    return if (!isThereCutout())
        standardOverlayFlags
    else
        standardOverlayFlags
}

const val standardOverlayFlags =
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

const val standardOverlayFlagsForCutout =
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

fun yCutout() : Int{
    if(isThereCutout()){
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val idStatusBarHeight = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            return -context.resources.getDimensionPixelSize(idStatusBarHeight)
        }
    }
    return 0
}

fun xCutout() : Int{
    if(isThereCutout()){
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isRotatinNavigateRight()) {
                val idStatusBarHeight = context.resources.getIdentifier("status_bar_height", "dimen", "android")
                return -context.resources.getDimensionPixelSize(idStatusBarHeight)
            }
        }
    }
    return 0
}

fun getHeightNavaBar():Int {
    val idNavBarHeight: Int = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return context.resources.getDimensionPixelSize(idNavBarHeight)
}
@JvmOverloads
fun getWindowsParameterLayout(_width:Float,
                                            _height: Float,
                                            gravity: Int = Gravity.START,
                                            _flags: Int = getParamOverlayFlags(),
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

fun saveMacroToJson(points: List<com.askerweb.autoclickerreplay.point.Point>, nameMacro: String = "untitle.json"){
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

fun loadMacroFromJson(points: MutableList<com.askerweb.autoclickerreplay.point.Point>, nameMacro: String){
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
        val clazz = Class.forName(jsonPoint.get("class").asString) as Class<com.askerweb.autoclickerreplay.point.Point>
        val point: com.askerweb.autoclickerreplay.point.Point = com.askerweb.autoclickerreplay.point.Point.PointBuilder.invoke().buildFrom(clazz, jsonPoint)
        points.add(point)
    }
    AutoClickService.getTvTimer().setText(AutoClickService.getTime());
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

var cutoutParams = 0;
var cutoutParamsLandscape = 0;

/*fun getCutoutSizeYPoint() : Int{
    if(context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        val cutout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AutoClickService.getWM().defaultDisplay.cutout
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        cutoutParams = if(cutout?.boundingRects?.get(0)?.bottom == null) 0 else cutout?.boundingRects?.get(0)?.bottom!!

//        var windowInsets = Rect()
//        windowInsets.set(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom())
        return cutoutParams
    }
    return 0
}

fun getCutoutSizeXLandscape() : Int{
    if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        val cutout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AutoClickService.getWM().defaultDisplay.cutout
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        cutoutParamsLandscape = if(cutout?.boundingRects?.get(0)?.left == null) 0 else cutout?.boundingRects?.get(0)?.right!!
        cutout?.boundingRects?.get(0)?.right!!.logd("3221right")
        cutout?.boundingRects?.get(0)?.left!!.logd("3221left")
        cutout?.boundingRects?.get(0)?.bottom!!.logd("3221bottom")
        cutout?.boundingRects?.get(0)?.top!!.logd("3221top")
        return cutoutParamsLandscape
    }
    return 0
}

fun getCutoutSizeXPoint() : Int{
    if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        val cutout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AutoClickService.getWM().defaultDisplay.cutout
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        return if(cutout?.boundingRects?.get(0)?.bottom == null) 0 else cutoutParams
    }
    return 0
}*/

/*fun getCutoutSizeYPath(wm: WindowManager) : Int{
    if(context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        return cutoutParams
    }
    return 0
}*/

enum class Dimension(private val type: Int) {
    DP(TypedValue.COMPLEX_UNIT_DIP),
    SP(TypedValue.COMPLEX_UNIT_SP);

    @JvmOverloads
    fun convert(value: Float, metrics: DisplayMetrics? = displayMetrics): Float = TypedValue.applyDimension(type, value, metrics)

    companion object {
        @JvmField
        var displayMetrics: DisplayMetrics? = DisplayMetrics()
    }
}

fun inOpenStatusAndNavBarHeight() : OpenStatusAndNavBar {
    var heightRecordPanel = recordPanel.height
    Log.d("SizeWindow", "HeightRecordPanel: " + heightRecordPanel)

    val d = getWM().defaultDisplay
    val realDisplayMetrics = DisplayMetrics()
    d.getRealMetrics(realDisplayMetrics)
    val heigthRealDisplay = realDisplayMetrics.heightPixels
    Log.d("SizeWindow", "HeigthRealDisplay: $heigthRealDisplay")

    val idStatusBarHeight = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    var heightStatusBar = context.resources.getDimensionPixelSize(idStatusBarHeight)
    Log.d("SizeWindow", "HeightStatusbar: $heightStatusBar")

    val idNavBarHeight: Int = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    var heightNavBar = context.resources.getDimensionPixelSize(idNavBarHeight)
    Log.d("SizeWindow", "HeightStatusbar: $heightNavBar")

    if (heigthRealDisplay == heightRecordPanel)
        return OpenStatusAndNavBar.AllClose
    if (heigthRealDisplay == heightRecordPanel + heightStatusBar)
        return OpenStatusAndNavBar.StatusOpen
    if (heigthRealDisplay == heightRecordPanel + heightNavBar)
        return OpenStatusAndNavBar.NavOpen
    if (heigthRealDisplay == heightRecordPanel + heightNavBar + heightStatusBar)
        return OpenStatusAndNavBar.AllOpen

    return OpenStatusAndNavBar.AllOpen
}

fun inOpenStatusAndNavBarWidth() : OpenStatusAndNavBar {
    var heightRecordPanel = recordPanel.width
    Log.d("SizeWindow", "HeightRecordPanel: " + heightRecordPanel)

    val d = getWM().defaultDisplay
    val realDisplayMetrics = DisplayMetrics()
    d.getRealMetrics(realDisplayMetrics)
    val heigthRealDisplay = realDisplayMetrics.widthPixels
    Log.d("SizeWindow", "HeigthRealDisplay: $heigthRealDisplay")

    val idStatusBarHeight: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    var heightStatusBar = context.resources.getDimensionPixelSize(idStatusBarHeight)
    Log.d("SizeWindow", "HeightStatusbar: $heightStatusBar")

    val idNavBarHeight: Int = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    var heightNavBar = context.resources.getDimensionPixelSize(idNavBarHeight)
    Log.d("SizeWindow", "HeightStatusbar: $heightNavBar")

    if (heigthRealDisplay == heightRecordPanel)
        return OpenStatusAndNavBar.AllClose
    if (heigthRealDisplay == heightRecordPanel + heightStatusBar)
        return OpenStatusAndNavBar.StatusOpen
    if (heigthRealDisplay == heightRecordPanel + heightNavBar)
        return OpenStatusAndNavBar.NavOpen

    return OpenStatusAndNavBar.AllOpen
}

    enum class OpenStatusAndNavBar{
        AllClose,
        AllOpen,
        StatusOpen,
        NavOpen

    }

    fun isThereCutout() : Boolean{
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "Cutout: ${wm.defaultDisplay.cutout?.boundingRects?.get(0) != null}".logd("Cutout")
            return wm.defaultDisplay.cutout?.boundingRects?.get(0) != null
        }
        return false
    }

    fun isRotatinNavigateLeft(): Boolean {
        return getWM().defaultDisplay.rotation == Surface.ROTATION_270
    }

    fun isRotatinNavigateRight(): Boolean {
        return getWM().defaultDisplay.rotation == Surface.ROTATION_90
    }

    fun getNavigationBar(): Int {
        if (isRotatinNavigateLeft() && inOpenStatusAndNavBarWidth() == OpenStatusAndNavBar.AllOpen) {
            AutoClickService.getWM().defaultDisplay.cutout?.boundingRects?.get(0)?.left?.logd("123123")
            val resources: Resources = context.resources
            val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
                return resources.getDimensionPixelSize(resourceId)
        }
        return 0
    }