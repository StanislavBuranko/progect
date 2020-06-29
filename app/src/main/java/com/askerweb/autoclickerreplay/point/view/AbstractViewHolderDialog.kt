package com.askerweb.autoclickerreplay.point.view

import android.app.Dialog
import android.widget.Button

abstract class AbstractViewHolderDialog(var saveButton: Button? = null, var dialog: Dialog? = null) {
    abstract fun updateViewDialogParam()
    /**
     * Saves result changed properties in dialog
     */
    abstract fun saveEditDialog()
    /**
     * turn on/off save button if fields is not valid
     */
    abstract fun requireSettingEdit()
    /**
     * This method check to validating fields
     * @return true if all fields was require
     */
    abstract fun isRequire():Boolean
}