package com.askerweb.autoclickerreplay.point.view

import com.askerweb.autoclickerreplay.point.view.AbstractViewHolderDialog

open class ExtendedViewHolder(private val dialogHolder: AbstractViewHolderDialog) : AbstractViewHolderDialog(){

    override var dialog
        get() = dialogHolder.dialog
        set(value) { dialogHolder.dialog = value}

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