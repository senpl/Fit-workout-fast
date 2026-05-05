/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2013 Ivan Kovac  navratnanos@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ikovac.timepickerwithseconds.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.easyfitness.R
import java.text.DateFormat
import java.util.Calendar

//import com.ikovac.timepickerwithseconds.R;
/**
 * A dialog that prompts the user for the time of day using a [TimePicker].
 */
class MyTimePickerDialog(
    context: Context,
    theme: Int,
    callBack: OnTimeSetListener?,
    hourOfDay: Int, minute: Int, seconds: Int, is24HourView: Boolean
) : AlertDialog(context, theme), DialogInterface.OnClickListener, TimePicker.OnTimeChangedListener {
    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    fun interface OnTimeSetListener {
        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute The minute that was set.
         */
        fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int)
    }

    private val mTimePicker: TimePicker
    private val mCallback: OnTimeSetListener?
    private val mCalendar: Calendar
    private val mDateFormat: DateFormat?

    var mInitialHourOfDay: Int
    var mInitialMinute: Int
    var mInitialSeconds: Int
    var mIs24HourView: Boolean

    /**
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    constructor(
        context: Context,
        callBack: OnTimeSetListener?,
        hourOfDay: Int, minute: Int, seconds: Int, is24HourView: Boolean
    ) : this(
        context, 0,
        callBack, hourOfDay, minute, seconds, is24HourView
    )

    /**
     * @param context Parent.
     * @param theme the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        mCallback = callBack
        mInitialHourOfDay = hourOfDay
        mInitialMinute = minute
        mInitialSeconds = seconds
        mIs24HourView = is24HourView

        mDateFormat = android.text.format.DateFormat.getTimeFormat(context)
        mCalendar = Calendar.getInstance()
        updateTitle(mInitialHourOfDay, mInitialMinute, mInitialSeconds)

        setButton(context.getText(android.R.string.dialog_alert_title), this)
        setButton2(
            context.getText(android.R.string.cancel),
            null as DialogInterface.OnClickListener?
        )

        //setIcon(android.R.drawable.ic_dialog_time);
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(
            R.layout.time_picker_dialog,
            null
        )
        setView(view)

        mTimePicker = view.findViewById<View?>(R.id.timePicker) as TimePicker
        // initialize state
        mTimePicker.currentHour = mInitialHourOfDay
        mTimePicker.currentMinute = mInitialMinute
        mTimePicker.setCurrentSecond(mInitialSeconds)
        mTimePicker.setIs24HourView(mIs24HourView)
        mTimePicker.setOnTimeChangedListener(this)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        if (mCallback != null) {
            mTimePicker.clearFocus()
            mCallback.onTimeSet(
                mTimePicker, mTimePicker.currentHour,
                mTimePicker.currentMinute, mTimePicker.currentSeconds
            )
        }
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int) {
        updateTitle(hourOfDay, minute, seconds)
    }

    fun updateTime(hourOfDay: Int, minutOfHour: Int, seconds: Int) {
        mTimePicker.currentHour = hourOfDay
        mTimePicker.currentMinute = minutOfHour
        mTimePicker.setCurrentSecond(seconds)
    }

    private fun updateTitle(hour: Int, minute: Int, seconds: Int) {
        val sHour = String.format("%02d", hour)
        val sMin = String.format("%02d", minute)
        val sSec = String.format("%02d", seconds)
        setTitle(sHour + ":" + sMin + ":" + sSec)
    }

    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putInt(HOUR, mTimePicker.currentHour)
        state.putInt(MINUTE, mTimePicker.currentMinute)
        state.putInt(SECONDS, mTimePicker.currentSeconds)
        state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView())
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val hour = savedInstanceState.getInt(HOUR)
        val minute = savedInstanceState.getInt(MINUTE)
        val seconds = savedInstanceState.getInt(SECONDS)
        mTimePicker.currentHour = hour
        mTimePicker.currentMinute = minute
        mTimePicker.setCurrentSecond(seconds)
        mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR))
        mTimePicker.setOnTimeChangedListener(this)
        updateTitle(hour, minute, seconds)
    }


    companion object {
        private const val HOUR = "hour"
        private const val MINUTE = "minute"
        private const val SECONDS = "seconds"
        private const val IS_24_HOUR = "is24hour"
    }
}
