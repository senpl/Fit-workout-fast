package com.easyfitness.utils.EditableInputView

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.text.InputType
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import com.easyfitness.R
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.Keyboard
import java.util.Calendar
import java.util.Date

class EditableInputViewWithDate : EditableInputView, OnDateSetListener {
    var date: Date? = null
        private set
    private var dateEditView: TextView? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        date = DateConverter.dateToDate(year, month, dayOfMonth)
        if (dateEditView != null && date != null) dateEditView!!.setText(
            DateConverter.dateToLocalDateStr(
                date!!,
                getContext()
            )
        )
    }

    private val editableInputViewWithDate: EditableInputViewWithDate
        get() = this

    override fun editDialog(context: Context) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val editDate = TextView(getContext())
        date = DateConverter.newDate
        editDate.setLayoutParams(params)
        editDate.text =(DateConverter.dateToLocalDateStr(date!!, getContext()))
        editDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        editDate.setGravity(Gravity.CENTER)
        editDate.setOnClickListener(OnClickListener { view: View? ->
            val calendar = Calendar.getInstance()
            calendar.setTime(DateConverter.newDate)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            val datePickerDialog = DatePickerDialog(
                getContext(),
                this.editableInputViewWithDate, year, month, day
            )
            dateEditView = editDate
            datePickerDialog.show()
        })

        val editText = EditText(context)
        if (text.contentEquals("-")) {
            editText.setText("")
            editText.setHint("Enter value here")
        } else {
            editText.setText(text)
        }
        editText.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        editText.setGravity(Gravity.CENTER)
        editText.setLayoutParams(params)
        editText.requestFocus()
        editText.selectAll()

        val linearLayout = LinearLayout(getContext().getApplicationContext())

        linearLayout.setLayoutParams(params)
        linearLayout.setOrientation(LinearLayout.VERTICAL)
        linearLayout.addView(editDate)
        linearLayout.addView(editText)

        val dialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(mTitle)
            .showCancelButton(true)
            .setCancelClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                editText.clearFocus()
                Keyboard.hide(context, editText)
                sDialog!!.dismissWithAnimation()
            })
            .setCancelText(getContext().getString(R.string.global_cancel))
            .setConfirmText(getContext().getString(R.string.AddLabel))
            .setConfirmClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                Keyboard.hide(sDialog!!.getContext(), editText)
                text=(editText.getText().toString())
                mConfirmClickListener?.onTextChanged(this@EditableInputViewWithDate)
                sDialog.dismissWithAnimation()
            })
        dialog.setCustomView(linearLayout)
        dialog.setOnShowListener(OnShowListener { sDialog: DialogInterface? ->
            Keyboard.show(
                getContext(),
                editText
            )
        })
        dialog.show()
    }
}
