/*
 * MIT License
 *
 * Copyright (c) 2017 Jan Heinrich Reimer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.easyfitness.intro

import android.app.Activity
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.easyfitness.DAO.DAOProfil
import com.easyfitness.DAO.Profile
import com.easyfitness.DatePickerDialogFragment
import com.easyfitness.R
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.Gender
import com.heinrichreimersoftware.materialintro.app.SlideFragment
import com.onurkaganaldemir.ktoastlib.KToast

class NewProfileFragment : SlideFragment() {
    private var mName: EditText? = null
    private var mSize: EditText? = null
    private lateinit var mBirthday: TextView
    private lateinit var mBtCreate: Button
    private var mRbMale: RadioButton? = null
    private var mRbFemale: RadioButton? = null
    private var mRbOtherGender: RadioButton? = null
    private var mProfilCreated = false
    private val clickCreateButton = View.OnClickListener { v ->
        // Initialisation des objets DB
        val mDbProfils = DAOProfil(v.context)
        if (mName!!.text.toString().isEmpty()) {
            //Toast.makeText(getActivity().getBaseContext(), R.string.fillAllFields, Toast.LENGTH_SHORT).show();
            KToast.warningToast(requireActivity(), resources.getText(R.string.fillNameField).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
        } else {
            var size = 0
            try {
                if (mSize!!.text.toString().isNotEmpty()) {
                    size = java.lang.Double.valueOf(mSize!!.text.toString()).toInt()
                }
            } catch (ignored: NumberFormatException) {
            }
            var lGender = Gender.UNKNOWN
            if (mRbMale!!.isChecked) {
                lGender = Gender.MALE
            } else if (mRbFemale!!.isChecked) {
                lGender = Gender.FEMALE
            } else if (mRbOtherGender!!.isChecked) {
                lGender = Gender.OTHER
            }
            val p = Profile(mName!!.text.toString(), size, DateConverter.editToDate(mBirthday.text.toString()), lGender)
            // Create the new profil
            mDbProfils.addProfil(p)
            //Toast.makeText(getActivity().getBaseContext(), R.string.profileCreated, Toast.LENGTH_SHORT).show();
            SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(p.name)
                .setContentText(requireContext().resources.getText(R.string.profileCreated).toString())
                .setConfirmClickListener { nextSlide() }
                .show()
            mProfilCreated = true
        }
    }
    private var mDateFrag: DatePickerDialogFragment? = null
    private val dateSet = OnDateSetListener { _, year, month, day ->
        mBirthday.text = DateConverter.dateToString(year, month + 1, day)
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(mBirthday.windowToken, 0)
    }

    private fun showDatePickerFragment() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(dateSet)
        }
        val ft = requireActivity().fragmentManager.beginTransaction()
        mDateFrag!!.show(ft, "dialog")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.introfragment_newprofile, container, false)
        mName = view.findViewById(R.id.profileName)
        mSize = view.findViewById(R.id.profileSize)
        mBirthday = view.findViewById(R.id.profileBirthday)
        mBtCreate = view.findViewById(R.id.create_newprofil)
        mRbMale = view.findViewById(R.id.radioButtonMale)
        mRbFemale = view.findViewById(R.id.radioButtonFemale)
        mRbOtherGender = view.findViewById(R.id.radioButtonOtherGender)
        mBirthday.setOnClickListener { showDatePickerFragment() }

        /* Initialisation des boutons */mBtCreate.setOnClickListener(clickCreateButton)
        introActivity!!.addOnNavigationBlockedListener { position: Int, _: Int ->
            //Slide slide = getIntroActivity().getSlide(position);
            if (position == 4) {
                mBtCreate.callOnClick()
            }
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun canGoForward(): Boolean {
        return mProfilCreated
    }

    private val introActivity: MainIntroActivity?
        get() = if (activity is MainIntroActivity) {
            activity as MainIntroActivity?
        } else {
            throw IllegalStateException("SlideFragments must be attached to MainIntroActivity.")
        }

    companion object {
        fun newInstance(): NewProfileFragment {
            return NewProfileFragment()
        }
    }
}
