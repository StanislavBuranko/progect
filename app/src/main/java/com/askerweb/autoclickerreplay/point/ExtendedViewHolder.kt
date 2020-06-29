package com.askerweb.autoclickerreplay.point

import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog

open class ExtendedViewHolder(private val dialogHolder: AbstractViewHolderDialog) : AbstractViewHolderDialog(){

    open val expandableSave = {

    }

    open val expandableUpdate = {

    }

    override fun saveEditDialog() {
        dialogHolder.saveEditDialog()
        expandableSave()
    }

    override fun updateViewDialogParam() {
        dialogHolder.updateViewDialogParam()
        expandableUpdate()
    }

    override fun requireSettingEdit() {
        dialogHolder.requireSettingEdit()
    }

    override fun isRequire(): Boolean {
        return dialogHolder.isRequire()
    }
}