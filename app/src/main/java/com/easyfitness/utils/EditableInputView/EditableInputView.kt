package com.easyfitness.utils.EditableInputView

import android.app.Activity
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
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import com.easyfitness.R
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.Keyboard
import java.util.Calendar

open class EditableInputView : RelativeLayout, OnDateSetListener {
    protected var valueTextView: TextView? = null
    protected var editButton: View? = null
    protected var mConfirmClickListener: OnTextChangedListener? = null
    private var textViewInputType = InputType.TYPE_CLASS_NUMBER
    protected var mTitle: String? = ""

    /**
     * when CustomerDialogBuilder is used the OnTextChangedListener is not triggered
     */
    private var mCustomerDialogBuilder: CustomerDialogBuilder? = null

    private var mContext: Context? = null
    private var mActivateDialog = true
    private var mSuffix: String? = null
    private lateinit var mTextValue: String

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
        valueTextView!!.setText(
            DateConverter.dateToLocalDateStr(
                year,
                month,
                dayOfMonth,
                getContext()
            )
        )
        if (mConfirmClickListener != null) mConfirmClickListener!!.onTextChanged(this@EditableInputView)
    }

    protected fun init(context: Context, attrs: AttributeSet?) {
        //do setup work here
        mContext = context
        val rootView = inflate(context, R.layout.editableinput_view, this)
        valueTextView = rootView!!.findViewById<TextView>(R.id.valueTextView)
        editButton = rootView.findViewById<View>(R.id.editButton)
        mTextValue = ""
        mSuffix = ""

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.editableinput_view,
                0, 0
            )
            try {
                mTitle =
                    a.getString(R.styleable.editableinput_view_android_inputType) //editableinput_view_android_title ?? Not sure
                this.text = a.getString(R.styleable.editableinput_view_android_text).toString()
                valueTextView!!.setGravity(
                    a.getInt(
                        R.styleable.editableinput_view_android_gravity,
                        0
                    )
                )
                valueTextView!!.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    a.getDimension(R.styleable.editableinput_view_android_textSize, 0f)
                )
                valueTextView!!.setMaxLines(
                    a.getInt(
                        R.styleable.editableinput_view_android_maxLines,
                        1
                    )
                )
                valueTextView!!.setLines(a.getInt(R.styleable.editableinput_view_android_lines, 1))
                textViewInputType = a.getInt(
                    R.styleable.editableinput_view_android_inputType,
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                )
                valueTextView!!.setInputType(textViewInputType)
                if (a.getBoolean(R.styleable.editableinput_view_iconVisible, false)) {
                    editButton!!.setVisibility(VISIBLE)
                } else {
                    editButton!!.setVisibility(GONE)
                }
            } finally {
                a.recycle()
            }
        }

        valueTextView!!.setOnClickListener(OnClickListener { v: View? -> editDialog(v!!.getContext()) })

        editButton!!.setOnClickListener(OnClickListener { v: View? -> editDialog(v!!.getContext()) })
    }

    protected open fun editDialog(context: Context) {
        if (!mActivateDialog) return

        if (mCustomerDialogBuilder != null) {
            mCustomerDialogBuilder!!.customerDialogBuilder(this)!!.show()
        } else {
            if ((valueTextView!!.getInputType() and InputType.TYPE_CLASS_DATETIME) > 0) {
                val calendar = Calendar.getInstance()

                calendar.setTime(DateConverter.localDateStrToDate(this.text!!, getContext()))
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)

                val datePickerDialog = DatePickerDialog(
                    getContext(), this, year, month, day
                )
                datePickerDialog.show()
            } else {
                val editText = EditText(context)
                editText.setText(mTextValue)
                editText.setGravity(Gravity.CENTER)
                editText.setInputType(textViewInputType)
                editText.requestFocus()

                val linearLayout = LinearLayout(context.getApplicationContext())
                linearLayout.setOrientation(LinearLayout.VERTICAL)
                linearLayout.addView(editText)

                val dialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(mTitle)
                    .setCancelText(getContext().getString(R.string.global_cancel))
                    .setHideKeyBoardOnDismiss(true)
                    .setCancelClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                        editText.clearFocus()
                        Keyboard.hide(context, editText)
                        sDialog!!.dismissWithAnimation()
                    })
                    .setConfirmClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                        editText.clearFocus()
                        Keyboard.hide(context, editText)
                        this.text = editText.getText().toString()
                        sDialog!!.dismissWithAnimation()
                        if (mConfirmClickListener != null) mConfirmClickListener!!.onTextChanged(
                            this@EditableInputView
                        )
                    })
                dialog.setOnDismissListener(DialogInterface.OnDismissListener { sDialog: DialogInterface? ->
                    rootView!!.requestFocus()
                    val imm =
                        mContext!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
                    if (imm != null) imm.hideSoftInputFromWindow(rootView!!.getWindowToken(), 0)
                })
                //Keyboard.hide(context, editText);});
                dialog.setOnShowListener(OnShowListener { sDialog: DialogInterface? ->
                    editText.requestFocus()
                    Keyboard.show(context, editText)
                })

                dialog.setCustomView(linearLayout)
                dialog.show()
            }
        }
    }

    var text: String
        get() = mTextValue
        set(newValue) {
            mTextValue = newValue
            valueTextView!!.setText(newValue + mSuffix)
        }

    fun setHint(newValue: String?) {
        valueTextView!!.setHint(newValue)
    }

    fun setTextSuffix(newValue: String?) {
        mSuffix = newValue
    }

    val textView: TextView
        get() = valueTextView!!

    fun ActivateDialog(activate: Boolean) {
        mActivateDialog = activate
    }

    fun setOnTextChangeListener(listener: OnTextChangedListener?) {
        mConfirmClickListener = listener
    }

    fun setCustomDialogBuilder(customBuilder: CustomerDialogBuilder?) {
        mCustomerDialogBuilder = customBuilder
    }

    fun interface CustomerDialogBuilder {
        fun customerDialogBuilder(view: EditableInputView?): SweetAlertDialog?
    }

    fun interface OnTextChangedListener {
        fun onTextChanged(view: EditableInputView?)
    }
}
