package com.askerweb.autoclickerreplay.point

import android.accessibilityservice.GestureDescription
import org.jetbrains.annotations.NotNull

interface PointCommand {
    /**
     * @return complete [GestureDescription] for dispatch gesture
     */
    fun getCommand():GestureDescription?
}