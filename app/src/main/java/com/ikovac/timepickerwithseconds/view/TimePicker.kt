/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2013 Ivan Kovac navratnanos@gmail.com
 * updated by senpl
 *
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

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import com.easyfitness.R
import java.text.DateFormatSymbols
import java.util.Calendar

/**
 * A view for selecting the time of day, in either 24 hour or AM/PM mode.
 *
 * The hour, each minute digit, each seconds digit, and AM/PM (if applicable) can be conrolled by
 * vertical spinners.
 *
 * The hour can be entered by keyboard input.  Entering in two digit hours
 * can be accomplished by hitting two digits within a timeout of about a
 * second (e.g. '1' then '2' to select 12).
 *
 * The minutes can be entered by entering single digits.
 * The seconds can be entered by entering single digits.
 *
 * Under AM/PM mode, the user can hit 'a', 'A", 'p' or 'P' to pick.
 *
 * For a dialog using this view, see [TimePickerDialog].
 */
class TimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    // state
    private var mCurrentHour = 0 // 0-23
    private var mCurrentMinute = 0 // 0-59

    /**
     * @return The current minute.
     */
    var currentSeconds: Int = 0 // 0-59
        private set
    private var mIs24HourView = false
    private var mIsAm: Boolean

    // ui components
    private val mHourPicker: NumberPicker
    private val mMinutePicker: NumberPicker
    private val mSecondPicker: NumberPicker
    private var mAmPmButton: Button? = null
    private val mAmText: String?
    private val mPmText: String?

    // callbacks
    private var mOnTimeChangedListener: OnTimeChangedListener? = null

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    fun interface OnTimeChangedListener {
        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         * @param seconds The current second.
         */
        fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int)
    }

    init {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //        int timeWidget=R.layout.time_picker_widget;
        inflater.inflate(
            android.R.layout.activity_list_item,
            this,  // we are the parent
            true
        )

        // hour
        mHourPicker = findViewById<NumberPicker>(R.id.hour)
        mHourPicker.setOnValueChangedListener(OnValueChangeListener { picker: NumberPicker?, oldVal: Int, newVal: Int ->
            mCurrentHour = newVal
            if (!mIs24HourView) {
// adjust from [1-12] to [0-11] internally, with the times
// written "12:xx" being the start of the half-day
                if (mCurrentHour == 12) {
                    mCurrentHour = 0
                }
                if (!mIsAm) {
// PM means 12 hours later than nominal
                    mCurrentHour += 12
                }
            }
            onTimeChanged()
        })

        // digits of minute
        mMinutePicker = findViewById<NumberPicker>(R.id.minute)
        mMinutePicker.setMinValue(0)
        mMinutePicker.setMaxValue(59)
        mMinutePicker.setFormatter(TWO_DIGIT_FORMATTER)
        mMinutePicker.setOnValueChangedListener(OnValueChangeListener { spinner: NumberPicker?, oldVal: Int, newVal: Int ->
            mCurrentMinute = newVal
            onTimeChanged()
        })

        // digits of seconds
        mSecondPicker = findViewById<NumberPicker>(R.id.seconds)
        mSecondPicker.setMinValue(0)
        mSecondPicker.setMaxValue(59)
        mSecondPicker.setFormatter(TWO_DIGIT_FORMATTER)
        mSecondPicker.setOnValueChangedListener(OnValueChangeListener { picker: NumberPicker?, oldVal: Int, newVal: Int ->
            this.currentSeconds = newVal
            onTimeChanged()
        })

        // am/pm
        mAmPmButton = findViewById<Button?>(R.id.amPm)

        // now that the hour/minute picker objects have been initialized, set
        // the hour range properly based on the 12/24 hour display mode.
        configurePickerRanges()

        // initialize to current time
        val cal = Calendar.getInstance()
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER)

        // by default we're not in 24 hour mode
        cal.get(Calendar.HOUR_OF_DAY).also { this.currentHour = it }
        cal.get(Calendar.MINUTE).also { this.currentMinute = it }
        setCurrentSecond(cal.get(Calendar.SECOND))

        mIsAm = (mCurrentHour < 12)

        /* Get the localized am/pm strings and use them in the spinner */
        val dfs = DateFormatSymbols()
        val dfsAmPm = dfs.getAmPmStrings()
        mAmText = dfsAmPm[Calendar.AM]
        mPmText = dfsAmPm[Calendar.PM]
        mAmPmButton!!.setText(if (mIsAm) mAmText else mPmText)
        mAmPmButton!!.setOnClickListener(OnClickListener { v: View? ->
            requestFocus()
            if (mIsAm) {
                // Currently AM switching to PM

                if (mCurrentHour < 12) {
                    mCurrentHour += 12
                }
            } else {
                // Currently PM switching to AM

                if (mCurrentHour >= 12) {
                    mCurrentHour -= 12
                }
            }
            mIsAm = !mIsAm
            mAmPmButton!!.setText(if (mIsAm) mAmText else mPmText)
            onTimeChanged()
        })

        if (!isEnabled()) {
            setEnabled(false)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mMinutePicker.setEnabled(enabled)
        mHourPicker.setEnabled(enabled)
        mAmPmButton!!.setEnabled(enabled)
    }

    /**
     * Used to save / restore state of time picker
     */
    public class SavedState : BaseSavedState {
        val hour: Int
        val minute: Int

        constructor(superState: Parcelable?, hour: Int, minute: Int) : super(superState) {
            this.hour = hour
            this.minute = minute
        }

        private constructor(`in`: Parcel) : super(`in`) {
            this.hour = `in`.readInt()
            this.minute = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(this.hour)
            dest.writeInt(this.minute)
        }

        companion object {
            @JvmField
            val CREATOR
                    : Parcelable.Creator<SavedState?> = object : Parcelable.Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls<SavedState>(size)
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, mCurrentHour, mCurrentMinute)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.getSuperState())
        this.currentHour = ss.hour
        this.currentMinute = ss.minute
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     * @param onTimeChangedListener the callback, should not be null.
     */
    fun setOnTimeChangedListener(onTimeChangedListener: OnTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener
    }

    var currentHour: Int
        /**
         * @return The current hour (0-23).
         */
        get() = mCurrentHour
        /**
         * Set the current hour.
         */
        set(currentHour) {
            this.mCurrentHour = currentHour
            updateHourDisplay()
        }

    /**
     * Set whether in 24 hour or AM/PM mode.
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    fun setIs24HourView(is24HourView: Boolean) {
        if (mIs24HourView !== is24HourView) {
            mIs24HourView = is24HourView
            configurePickerRanges()
            updateHourDisplay()
        }
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    fun is24HourView(): Boolean {
        return mIs24HourView
    }

    var currentMinute: Int
        /**
         * @return The current minute.
         */
        get() = mCurrentMinute
        /**
         * Set the current minute (0-59).
         */
        set(currentMinute) {
            this.mCurrentMinute = currentMinute
            updateMinuteDisplay()
        }

    /**
     * Set the current second (0-59).
     */
    fun setCurrentSecond(currentSecond: Int) {
        this.currentSeconds = currentSecond
        updateSecondsDisplay()
    }

    override fun getBaseline(): Int {
        return mHourPicker.getBaseline()
    }

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private fun updateHourDisplay() {
        var currentHour = mCurrentHour
        if (!mIs24HourView) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour > 12) currentHour -= 12
            else if (currentHour == 0) currentHour = 12
        }
        mHourPicker.setValue(currentHour)
        mIsAm = mCurrentHour < 12
        mAmPmButton!!.setText(if (mIsAm) mAmText else mPmText)
        onTimeChanged()
    }

    private fun configurePickerRanges() {
        if (mIs24HourView) {
            mHourPicker.setMinValue(0)
            mHourPicker.setMaxValue(23)
            mHourPicker.setFormatter(TWO_DIGIT_FORMATTER)
            mAmPmButton!!.setVisibility(GONE)
        } else {
            mHourPicker.setMinValue(1)
            mHourPicker.setMaxValue(12)
            mHourPicker.setFormatter(null)
            mAmPmButton!!.setVisibility(VISIBLE)
        }
    }

    private fun onTimeChanged() {
        mOnTimeChangedListener!!.onTimeChanged(
            this,
            this.currentHour,
            this.currentMinute,
            this.currentSeconds
        )
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private fun updateMinuteDisplay() {
        mMinutePicker.setValue(mCurrentMinute)
        mOnTimeChangedListener!!.onTimeChanged(
            this,
            this.currentHour,
            this.currentMinute,
            this.currentSeconds
        )
    }

    /**
     * Set the state of the spinners appropriate to the current second.
     */
    private fun updateSecondsDisplay() {
        mSecondPicker.setValue(this.currentSeconds)
        mOnTimeChangedListener!!.onTimeChanged(
            this,
            this.currentHour,
            this.currentMinute,
            this.currentSeconds
        )
    }

    companion object {
        /**
         * A no-op callback used in the constructor to avoid null checks
         * later in the code.
         */
        private val NO_OP_CHANGE_LISTENER =
            TimePicker.OnTimeChangedListener { view: TimePicker?, hourOfDay: Int, minute: Int, seconds: Int -> }

        val TWO_DIGIT_FORMATTER: NumberPicker.Formatter =
            NumberPicker.Formatter { value: Int -> String.format("%02d", value) }
    }
}
