package gr.antoniom.chronometer

import android.content.Context
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.text.DecimalFormat
import kotlin.math.abs

/*
* The Android chronometer widget revised so as to count milliseconds
*/   class Chronometer @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : AppCompatTextView(context!!, attrs, defStyle) {
    private var mBase: Long = 0
    private var mVisible = false
    private var mStarted = false
    private var mRunning = false
    private var mPreciseClock = true
    var onChronometerTickListener: OnChronometerTickListener? = null
    var timeElapsed: Long = 0
        private set
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(m: Message) {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime())
                dispatchChronometerTick()
                if (mPreciseClock) sendMessageDelayed(Message.obtain(this,
                    TICK_WHAT), 100) else sendMessageDelayed(Message.obtain(this,
                    TICK_WHAT), 1000)
            }
        }
    }

    private fun init() {
        mBase = SystemClock.elapsedRealtime()
        updateText(mBase)
    }

    var base: Long
        get() = mBase
        set(base) {
            mBase = base
            dispatchChronometerTick()
            updateText(SystemClock.elapsedRealtime())
        }

    fun setPrecision(prec: Boolean) {
        mPreciseClock = prec
    }

    fun start() {
        //mBase = SystemClock.elapsedRealtime();
        mStarted = true
        updateRunning()
    }

    fun stop() {
        mStarted = false
        updateRunning()
    }

//    fun setStarted(started: Boolean) {
//        mStarted = started
//        updateRunning()
//    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mVisible = false
        updateRunning()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        mVisible = visibility == VISIBLE
        updateRunning()
    }

    @Synchronized
    private fun updateText(now: Long) {
        timeElapsed = now - mBase
        val df = DecimalFormat("00")
        val hours = (timeElapsed / (3600 * 1000)).toInt()
        var remaining = (timeElapsed % (3600 * 1000)).toInt()
        val minutes = remaining / (60 * 1000)
        remaining %= (60 * 1000)
        val seconds = remaining / 1000
//        remaining = remaining % 1000
        var milliseconds = 0
        if (mPreciseClock) {
            milliseconds = timeElapsed.toInt() % 1000 / 100
        }
        var text: String? = ""
        if (hours > 0) {
            text += df.format(abs(hours).toLong()) + ":"
        }
        text += df.format(abs(minutes).toLong()) + ":"
        text += df.format(abs(seconds).toLong())
        if (mPreciseClock) text += ":$milliseconds"
        setText(text)
    }

    private fun updateRunning() {
        val running = mVisible && mStarted
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime())
                dispatchChronometerTick()
                if (mPreciseClock) {
                    mHandler.sendMessageDelayed(Message.obtain(mHandler,
                        TICK_WHAT), 100)
                } else {
                    mHandler.sendMessageDelayed(Message.obtain(mHandler,
                        TICK_WHAT), 1000)
                }
            } else {
                mHandler.removeMessages(TICK_WHAT)
            }
            mRunning = running
        }
    }

    fun dispatchChronometerTick() {
        if (onChronometerTickListener != null) {
            onChronometerTickListener!!.onChronometerTick(this)
        }
    }

    interface OnChronometerTickListener {
        fun onChronometerTick(chronometer: Chronometer?)
    }

    companion object {
//        private const val TAG = "Chronometer"
        private const val TICK_WHAT = 2
    }

    init {
        init()
    }
}
