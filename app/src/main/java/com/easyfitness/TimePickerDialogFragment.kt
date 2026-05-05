package com.easyfitness

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.ikovac.timepickerwithseconds.view.MyTimePickerDialog

//@SuppressLint("ValidFragment")
class TimePickerDialogFragment : DialogFragment() {
    private var onTimeSetListener: MyTimePickerDialog.OnTimeSetListener? = null
//    private val Hours = 0
//    private val Minutes = 0
//    private val Seconds = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = getArguments()
        val hour = bundle!!.getInt("HOUR")
        val min = bundle.getInt("MINUTE")
        val sec = bundle.getInt("SECOND")

        // Create a new instance of TimePickerDialog and return it
        return MyTimePickerDialog(requireActivity(), onTimeSetListener, hour, min, sec, true)
    }

    private fun setOnTimeSetListener(listener: MyTimePickerDialog.OnTimeSetListener?) {
        this.onTimeSetListener = listener
    }

    companion object {
        fun newInstance(
            onTimeSetListener: MyTimePickerDialog.OnTimeSetListener?,
            hour: Int,
            min: Int,
            sec: Int
        ): TimePickerDialogFragment {
            val pickerFragment = TimePickerDialogFragment()
            pickerFragment.setOnTimeSetListener(onTimeSetListener)

            //Pass the date in a bundle.
            val bundle = Bundle()
            bundle.putInt("HOUR", hour)
            bundle.putInt("MINUTE", min)
            bundle.putInt("SECOND", sec)
            pickerFragment.setArguments(bundle)
            return pickerFragment
        }
    }
}
