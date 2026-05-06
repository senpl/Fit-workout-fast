package com.easyfitness.utils

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import java.util.Calendar
import java.util.Date

class SweetAlertDialogWithDate : SweetAlertDialog, OnDateSetListener {
    private lateinit var date: Date
    private var dateEditView: TextView? = null
    private var editText: EditText? = null
    private var linearLayout: LinearLayout? = null

    private val view: View? = null
    private val viewGroup: ViewGroup? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, alertType: Int) : super(context, alertType)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        super.setCustomView(linearLayout)
    }

    private fun init() {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        dateEditView = TextView(getContext().getApplicationContext())

        date = DateConverter.newDate
        dateEditView!!.setText(DateConverter.dateToLocalDateStr(date, getContext()))
        dateEditView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        dateEditView!!.setGravity(Gravity.CENTER)
        dateEditView!!.setLayoutParams(params)
        dateEditView!!.setOnClickListener(View.OnClickListener { view: View? ->
            val calendar = Calendar.getInstance()
            calendar.setTime(DateConverter.newDate)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            val datePickerDialog = DatePickerDialog(getContext(), this, year, month, day)
            datePickerDialog.show()
        })

        //editText = view.findViewById(R.id.valueEditText);
        editText = EditText(getContext().getApplicationContext())
        editText!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        //editText.setTextColor(getContext().getColor(R.color.text_color));
        editText!!.setText("")
        editText!!.setHint("Enter value here")
        editText!!.setLayoutParams(params)
        editText!!.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        editText!!.setGravity(Gravity.CENTER)

        editText!!.requestFocus()
        editText!!.selectAll()

        linearLayout = LinearLayout(getContext().getApplicationContext())
        linearLayout!!.setLayoutParams(params)
        linearLayout!!.setOrientation(LinearLayout.VERTICAL)
        linearLayout!!.addView(dateEditView)
        linearLayout!!.addView(editText)
    }

    fun GetDate(): Date? {
        return date
    }

    fun GetDateString(): String {
        return dateEditView!!.getText().toString()
    }

    fun GetText(): String {
        return editText!!.getText().toString()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        date = DateConverter.dateToDate(year, month, dayOfMonth)
        if (dateEditView != null) dateEditView!!.setText(
            DateConverter.dateToLocalDateStr(
                date,
                getContext().getApplicationContext()
            )
        )
    }
}
