package com.easyfitness

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class CustomSpinnerAdapter : ArrayAdapter<SpinnerItem?> {
    private val context: Context
    private val layoutResID: Int
    private val spinnerData: MutableList<SpinnerItem>

    constructor(
        context: Context, layoutResourceID: Int,
        textViewResourceId: Int, spinnerDataList: MutableList<SpinnerItem>
    ) : super(context, layoutResourceID, textViewResourceId, spinnerDataList) {
        this.context = context
        this.layoutResID = layoutResourceID
        this.spinnerData = spinnerDataList
    }

    constructor(
        context: Context, layoutResourceID: Int,
        spinnerDataList: MutableList<SpinnerItem>
    ) : super(context, layoutResourceID, spinnerDataList) {
        this.context = context
        this.layoutResID = layoutResourceID
        this.spinnerData = spinnerDataList
    }


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getCustomView(position, convertView, parent)
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }


    fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var row = convertView
        val holder: SpinnerHolder

        if (row == null) {
            val inflater = (context as Activity).getLayoutInflater()

            row = inflater.inflate(layoutResID, parent, false)
            holder = SpinnerHolder()

            holder.userImage = row.findViewById<ImageView>(R.id.left_pic)
            holder.name = row.findViewById<TextView>(R.id.text_main_name)
            holder.email = row.findViewById<TextView>(R.id.sub_text_email)

            row.setTag(holder)
        } else {
            holder = row.getTag() as SpinnerHolder
        }

        val spinnerItem = spinnerData.get(position)

        holder.userImage!!.setImageDrawable(
            row.getResources().getDrawable(spinnerItem.drawableResID)
        )
        holder.name!!.setText(spinnerItem.name)
        holder.email!!.setText(spinnerItem.email)

        return row
    }

    private class SpinnerHolder {
        var userImage: ImageView? = null
        var name: TextView? = null
        var email: TextView? = null
    }
}
