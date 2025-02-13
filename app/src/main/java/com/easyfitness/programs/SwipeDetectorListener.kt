package com.easyfitness.programs

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs

class SwipeDetectorListener(private val programRunner: ProgramRunner) : OnTouchListener {
    private var downX = 0f
    private var upX = 0f

    private fun onRightSwipe() {
        programRunner.nextExercise()
    }

    private fun onLeftSwipe() {
        programRunner.previousExercise()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                upX = event.x
                val deltaX = downX - upX
                if (abs(deltaX) > MIN_DISTANCE) {
                    // left or right
                    if (deltaX > 0) {
                        onRightSwipe()
                        return true
                    }
                    if (deltaX < 0) {
                        onLeftSwipe()
                        return true
                    }
                } else {
                    return false // We don't consume the event
                }
                return true
            }
            else -> return false
        }
    }

    companion object {
        const val MIN_DISTANCE = 290
    }
}
