package com.easyfitness.bodymeasures

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.easyfitness.BtnClickListener
import com.easyfitness.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class BodyMeasureCursorAdapter(context: Context, c: Cursor?, flags: Int, mD: BtnClickListener?) :
    CursorAdapter(context, c, flags) {
    var mDeleteClickListener: BtnClickListener? = null
    private val mInflater: LayoutInflater
    private var mContext: Context? = null

    init {
        mContext = context
        mDeleteClickListener = mD
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val t0 = view.findViewById<TextView>(R.id.LIST_BODYMEASURE_ID)
        t0.setText(cursor.getString(0))

        val t1 = view.findViewById<TextView>(R.id.LIST_BODYMEASURE_DATE)
        val date: Date?
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
            date = dateFormat.parse(cursor.getString(1))

            //SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
            //dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
            //t1.setText(DateFormat.getDateInstance().format(date));
            val dateFormat3 =
                android.text.format.DateFormat.getDateFormat(mContext!!.getApplicationContext())
            dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"))
            t1.setText(dateFormat3.format(date))
        } catch (e: ParseException) {
            t1.setText("")
            e.printStackTrace()
        }

        val t2 = view.findViewById<TextView>(R.id.LIST_BODYMEASURE_WEIGHT)
        t2.setText(cursor.getString(3))

        val cdView = view.findViewById<CardView>(R.id.CARDVIEW)

        val mFirstColorOdd = 0
        if (cursor.getPosition() % 2 == mFirstColorOdd) {
            cdView.setBackgroundColor(
                context.getResources().getColor(R.color.record_background_even)
            )
        } else {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.background))
        }

        val deletImg = view.findViewById<ImageView>(R.id.deleteButton)
        deletImg.setTag(cursor.getLong(0))
        deletImg.setOnClickListener(View.OnClickListener { v: View? ->
            if (mDeleteClickListener != null) mDeleteClickListener!!.onBtnClick(v!!.getTag() as Long)
        })
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View? {
        return mInflater.inflate(R.layout.bodymeasure_row, parent, false)
    }
}
