package com.easyfitness.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object Keyboard {
    fun hide(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }

    fun show(context: Context, view: View) {
        if (view.requestFocus()) {
            view.postDelayed(Runnable {
                val inputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }, 300)
            /*InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);*/
        }
    }
}
