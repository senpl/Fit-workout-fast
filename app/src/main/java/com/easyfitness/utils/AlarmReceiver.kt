package com.easyfitness.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, arg1: Intent?) {
        val pattern = longArrayOf(
            0,  // Start immediately
            500, 500, 500, 500, 500
        )
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v!!.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            //deprecated in API 26
            if (v != null) {
                v.vibrate(pattern, -1)
            }
        }
    }
}
