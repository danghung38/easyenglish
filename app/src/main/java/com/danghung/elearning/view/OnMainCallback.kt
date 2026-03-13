package com.danghung.elearning.view

interface OnMainCallback {
    fun callBack(key: String, data: Any) {}
    fun showFragment(tag: String, data: Any?, isBacked: Boolean)
    fun showLoading(){

    }
    fun hideLoading(){

    }
}