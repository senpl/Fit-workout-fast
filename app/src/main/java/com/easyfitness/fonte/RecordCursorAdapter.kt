package com.easyfitness.fonte

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.easyfitness.BtnClickListener
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.DAORecord
import com.easyfitness.R
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.UnitConverter
import java.text.DecimalFormat
import java.util.Date

class RecordCursorAdapter(
    context: Context,
    c: Cursor?,
    flags: Int,
    clickDelete: BtnClickListener?,
    clickCopy: BtnClickListener?
) : CursorAdapter(context, c, flags) {
    private val mInflater: LayoutInflater
    private var mFirstColorOdd = 0
    private var mContext: Context? = null
    private var mDeleteClickListener: BtnClickListener? = null
    private var mCopyClickListener: BtnClickListener? = null

    init {
        mContext = context
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mDeleteClickListener = clickDelete
        mCopyClickListener = clickCopy
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val cdView = view.findViewById<CardView>(R.id.CARDVIEW)

        val position = cursor.getPosition()

        if (position % 2 == mFirstColorOdd) {
            cdView.setBackgroundColor(
                context.getResources().getColor(R.color.record_background_odd)
            )
        } else {
            cdView.setBackgroundColor(
                context.getResources().getColor(R.color.record_background_even)
            )
        }

        /* Commun display */
        val tDate = view.findViewById<TextView>(R.id.DATE_CELL)
        val date: Date?
        val dateString = cursor.getString(cursor.getColumnIndex(DAORecord.DATE))
        date = DateConverter.DBDateStrToDate(dateString)
        tDate.setText(DateConverter.dateToLocalDateStr(date, mContext))

        val tTime = view.findViewById<TextView>(R.id.TIME_CELL)
        tTime.setText(cursor.getString(cursor.getColumnIndex(DAORecord.TIME)))

        val tExercise = view.findViewById<TextView>(R.id.MACHINE_CELL)
        tExercise.setText(cursor.getString(cursor.getColumnIndex(DAORecord.EXERCISE)))

        val tSerie = view.findViewById<TextView>(R.id.SERIE_CELL)
        val tSerieLabel = view.findViewById<TextView>(R.id.SERIE_LABEL)
        val tReps = view.findViewById<TextView>(R.id.REPETITION_CELL)
        val tRepsLabel = view.findViewById<TextView>(R.id.REP_LABEL)
        val tWeight = view.findViewById<TextView>(R.id.POIDS_CELL)
        val tWeightLabel = view.findViewById<TextView>(R.id.WEIGHT_LABEL)
        val tRepsLayout = view.findViewById<LinearLayout>(R.id.REP_LAYOUT)

        if (mCopyClickListener == null) {
            view.findViewById<View?>(R.id.copyButton).setVisibility(View.GONE)
        }

        /* Specific display */
        val recordType = cursor.getInt(cursor.getColumnIndex(DAORecord.TYPE))
        if (recordType == DAOMachine.TYPE_STRENGTH) {
            // UI
            tSerieLabel.setText(mContext!!.getString(R.string.SerieLabel))
            tWeightLabel.setText(mContext!!.getString(R.string.PoidsLabel))
            tRepsLabel.setText(mContext!!.getString(R.string.RepetitionLabel_short))
            tRepsLayout.setVisibility(View.VISIBLE)
            // Data
            tSerie.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SERIE)))
            tReps.setText(cursor.getString(cursor.getColumnIndex(DAORecord.REPETITION)))

            var unit = mContext!!.getString(R.string.KgUnitLabel)
            var poids = cursor.getFloat(cursor.getColumnIndex(DAORecord.WEIGHT))
            if (cursor.getInt(cursor.getColumnIndex(DAORecord.UNIT)) == UnitConverter.UNIT_LBS) {
                poids = UnitConverter.KgtoLbs(poids)
                unit = mContext!!.getString(R.string.LbsUnitLabel)
            }
            val numberFormat = DecimalFormat("#.##")
            tWeight.setText(numberFormat.format(poids.toDouble()) + unit)
        } else if (recordType == DAOMachine.TYPE_STATIC) {
            // UI
            tSerieLabel.setText(mContext!!.getString(R.string.SerieLabel))
            tWeightLabel.setText(mContext!!.getString(R.string.PoidsLabel))
            tRepsLabel.setText(mContext!!.getString(R.string.SecondsLabel_short))
            tRepsLayout.setVisibility(View.VISIBLE)
            // Data
            tSerie.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SERIE)))
            tReps.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SECONDS)))

            var unit = mContext!!.getString(R.string.KgUnitLabel)
            var poids = cursor.getFloat(cursor.getColumnIndex(DAORecord.WEIGHT))
            if (cursor.getInt(cursor.getColumnIndex(DAORecord.UNIT)) == UnitConverter.UNIT_LBS) {
                poids = UnitConverter.KgtoLbs(poids)
                unit = mContext!!.getString(R.string.LbsUnitLabel)
            }
            val numberFormat = DecimalFormat("#.##")
            tWeight.setText(numberFormat.format(poids.toDouble()) + unit)
        } else if (recordType == DAOMachine.TYPE_CARDIO) {
            tSerieLabel.setText(mContext!!.getString(R.string.DistanceLabel))
            tWeightLabel.setText(mContext!!.getString(R.string.DurationLabel))
            tRepsLayout.setVisibility(View.GONE)

            var distance = cursor.getFloat(cursor.getColumnIndex(DAORecord.DISTANCE))
            var unit = mContext!!.getString(R.string.KmUnitLabel)
            if (cursor.getInt(cursor.getColumnIndex(DAORecord.DISTANCE_UNIT)) == UnitConverter.UNIT_MILES) {
                distance = UnitConverter.KmToMiles(distance) // Always convert to KG
                unit = mContext!!.getString(R.string.MilesUnitLabel)
            }
            val numberFormat = DecimalFormat("#.##")
            tSerie.setText(numberFormat.format(distance.toDouble()) + unit)

            tWeight.setText(
                DateConverter.durationToHoursMinutesSecondsStr(
                    cursor.getInt(
                        cursor.getColumnIndex(
                            DAORecord.DURATION
                        )
                    ).toLong()
                )
            )
        }

        // Add separator if needed
        var separatorNeeded = false
        if (position == 0) {
            separatorNeeded = true
        } else {
            cursor.moveToPosition(position - 1)
            val datePreviousString = cursor.getString(cursor.getColumnIndex(DAORecord.DATE))
            if (datePreviousString.compareTo(dateString) != 0) {
                separatorNeeded = true
            }
            cursor.moveToPosition(position)
        }

        val t = view.findViewById<TextView>(R.id.SEPARATOR_CELL)
        if (separatorNeeded) {
            t.setText("- " + DateConverter.dateToLocalDateStr(date, mContext) + " -")
            t.setVisibility(View.VISIBLE)
        } else {
            t.setText("")
            t.setVisibility(View.GONE)
        }

        val deletImg = view.findViewById<ImageView>(R.id.deleteButton)
        deletImg.setTag(cursor.getLong(cursor.getColumnIndex(DAORecord.KEY)))
        deletImg.setOnClickListener(View.OnClickListener { v: View? ->
            if (mDeleteClickListener != null) mDeleteClickListener!!.onBtnClick(v!!.getTag() as Long)
        })

        val copyImg = view.findViewById<ImageView>(R.id.copyButton)
        copyImg.setTag(cursor.getLong(cursor.getColumnIndex(DAORecord.KEY)))
        copyImg.setOnClickListener(View.OnClickListener { v: View? ->
            if (mCopyClickListener != null) mCopyClickListener!!.onBtnClick(v!!.getTag() as Long)
        })
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View? {
        return mInflater.inflate(R.layout.row_fonte, parent, false)
    }

    /*
     * @pColor : si 1 alors affiche la couleur Odd en premier. Sinon, a couleur Even.
     */
    fun setFirstColorOdd(pColor: Int) {
        mFirstColorOdd = pColor
    }
}
