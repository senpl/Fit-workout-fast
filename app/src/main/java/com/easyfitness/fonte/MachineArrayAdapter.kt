package com.easyfitness.fonte

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.easyfitness.DAO.Machine
import com.easyfitness.R

class MachineArrayAdapter(data: ArrayList<Machine?>, context: Context) :
    ArrayAdapter<Machine?>(context, R.layout.bodypart_row, data), View.OnClickListener {
    var mContext: Context?
    private val lastPosition = -1

    init {
        this.mContext = context
    }

    override fun onClick(v: View) {
        val position = v.getTag() as Int
        val dataModel = getItem(position)
        //Snackbar.make(v, "Click:" + dataModel.getId(), Snackbar.LENGTH_SHORT);
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the data item for this position
        //BodyPart dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        var convertView = convertView
        val viewHolder: ViewHolder? // view lookup cache stored in tag

        val result: View?

        if (convertView == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(getContext())
            convertView = inflater.inflate(R.layout.simplemachinelist_row, parent, false)
            viewHolder.txtID = convertView.findViewById<TextView?>(R.id.LIST_MACHINE_ID)
            viewHolder.txtName = convertView.findViewById<TextView?>(R.id.LIST_MACHINE_NAME)

            //viewHolder.btFavorite = convertView.findViewById(R.id.LIST_MACHINE_FAVORITE);
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder?
            result = convertView
        }

        return convertView
    }

    // View lookup cache
    private class ViewHolder {
        var txtID: TextView? = null
        var txtName: TextView? = null
        var btFavorite: ImageView? = null
    }
}
