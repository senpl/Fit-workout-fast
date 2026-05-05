package com.easyfitness.bodymeasures

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.easyfitness.DAO.Profile
import com.easyfitness.DAO.bodymeasures.BodyPart
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure
import com.easyfitness.R
import com.easyfitness.graph.MiniDateGraph
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.ImageUtil
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry

class BodyPartListAdapter(data: ArrayList<BodyPart?>, context: Context) :
    ArrayAdapter<BodyPart?>(context, R.layout.bodypart_row, data), View.OnClickListener {
    var mContext: Context?

    override fun onClick(v: View?) {
        /*
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        BodyPart dataModel = (BodyPart) object;*/
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the data item for this position
        var convertView = convertView
        val dataModel = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        val viewHolder: ViewHolder // view lookup cache stored in tag

        if (convertView == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(getContext())
            convertView = inflater.inflate(R.layout.bodypart_row, parent, false)
            viewHolder.txtID = convertView.findViewById<TextView>(R.id.LIST_BODYPART_ID)
            viewHolder.txtName = convertView.findViewById<TextView>(R.id.LIST_BODYPART)
            viewHolder.txtLastMeasure =
                convertView.findViewById<TextView>(R.id.LIST_BODYPART_LASTRECORD)
            viewHolder.logo = convertView.findViewById<ImageView>(R.id.LIST_BODYPART_LOGO)
            viewHolder.miniGraph = MiniDateGraph(
                getContext(),
                convertView.findViewById<LineChart?>(R.id.LIST_BODYPART_MINIGRAPH),
                ""
            )

            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.getTag() as ViewHolder
        }

        //Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        //result.startAnimation(animation);
        //lastPosition = position;
        viewHolder.txtID!!.setText(dataModel!!.getId().toString())
        viewHolder.txtName!!.setText(dataModel.getName(getContext()))

        if (dataModel.getLastMeasure() != null) viewHolder.txtLastMeasure!!.setText(
            dataModel.getLastMeasure().getBodyMeasure().toString()
        )
        else viewHolder.txtLastMeasure!!.setText("-")
        if (dataModel.getCustomPicture() != "") {
            ImageUtil.setPic(viewHolder.logo, dataModel.getCustomPicture())
        } else {
            if (dataModel.getBodyPartResKey() != -1) viewHolder.logo!!.setImageDrawable(
                dataModel.getPicture(
                    getContext()
                )
            )
            else viewHolder.logo!!.setImageDrawable(null) // Remove the image, Custom is not managed yet
        }

        convertView.post(Runnable {
            val mDbBodyMeasure = DAOBodyMeasure(getContext())
            val valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(
                dataModel.getId(),
                this.profile
            )
            if (valueList != null) {
                // Recupere les enregistrements
                if (valueList.size < 1) {
//                    viewHolder.miniGraph!!.getChart().clear()
                } else {
                    val yVals = ArrayList<Entry?>()

                    if (valueList.size > 0) {
                        for (i in valueList.indices.reversed()) {
                            val value = Entry(
                                DateConverter.nbDays(
                                    valueList.get(i)!!.getDate().getTime().toDouble()
                                ).toFloat(), valueList.get(i)!!.getBodyMeasure()
                            )
                            yVals.add(value)
                        }

                        viewHolder.miniGraph!!.draw(yVals)
                    }
                }
            }
        }
        )

        // Return the completed view to render on screen
        return convertView
    }

    private var profile: Profile? = null

    init {
        this.mContext = context
    }

    // View lookup cache
    private class ViewHolder {
        var txtID: TextView? = null
        var txtName: TextView? = null
        var txtLastMeasure: TextView? = null
        var logo: ImageView? = null
        var miniGraph: MiniDateGraph? = null
    }
}
