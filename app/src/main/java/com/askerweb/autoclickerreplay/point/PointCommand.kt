package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import org.jetbrains.annotations.NotNull

interface PointCommand {
    fun getCommand():GestureDescription?
}