package com.easyfitness

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import gr.antoniom.chronometer.Chronometer

class ChronoDialogbox(var c: Activity) : Dialog(c), View.OnClickListener {
    var d: Dialog? = null
    var startstop: Button? = null
    var exit: Button? = null
    var reset: Button? = null
    var chrono: Chronometer? = null
    var strCurrentTime: String = ""
    var startTime: Long = 0
    var stopTime: Long = 0
    private var chronoStarted = false
    private var chronoResetted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(c.getResources().getString(R.string.ChronometerLabel)) //ChronometerLabel
        setContentView(R.layout.dialog_chrono)
        this.setCanceledOnTouchOutside(false) // make it modal

        startstop = findViewById<Button>(R.id.btn_startstop)
        exit = findViewById<Button>(R.id.btn_exit)
        reset = findViewById<Button>(R.id.btn_reset)
        chrono = findViewById<Chronometer>(R.id.chronoValue)

        startstop!!.setOnClickListener(this)
        exit!!.setOnClickListener(this)
        reset!!.setOnClickListener(this)
        chrono!!.base = SystemClock.elapsedRealtime()
        chrono!!.start()
        startTime = SystemClock.elapsedRealtime()
        chronoStarted = true

        startstop!!.setText("Stop")
    }

    override fun onClick(v: View) {
        val id = v.getId()
        if (id == R.id.btn_startstop) {
            if (chronoStarted) {
                chrono!!.stop()
                stopTime = SystemClock.elapsedRealtime()
                chronoStarted = false
                startstop!!.setText("Start")
            } else {
                if (chronoResetted) {
                    startTime = SystemClock.elapsedRealtime()
                    chrono!!.base = startTime
                } else {
                    startTime = SystemClock.elapsedRealtime() - (stopTime - startTime)
                    chrono!!.base = startTime
                }
                chrono!!.start()
                chronoStarted = true
                startstop!!.setText("Stop")
            }
            chronoResetted = false
        } else if (id == R.id.btn_reset) {
            startTime = SystemClock.elapsedRealtime()
            chrono!!.base = startTime
            chrono!!.setText("00:00:0")
            chronoResetted = true
        } else if (id == R.id.btn_exit) {
            chrono!!.stop()
            chronoStarted = false
            chrono!!.setText("00:00:0")
            startstop!!.setText("Start")
            dismiss()
        }
    }
}
