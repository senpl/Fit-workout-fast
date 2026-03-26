package com.easyfitness.programs

import android.annotation.SuppressLint
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
import androidx.core.content.res.ResourcesCompat.getColor
import com.easyfitness.BtnClickListener
import com.easyfitness.DAO.DAOExerciseInProgram
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.DAORecord
import com.easyfitness.R
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.UnitConverter
import java.text.DecimalFormat
import java.util.*

class RecordCursorAdapter internal constructor(private val mContext: Context, c: Cursor?, flags: Int, clickDelete: BtnClickListener?, clickCopy: BtnClickListener?) : CursorAdapter(mContext, c, flags) {
    private val mInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mFirstColorOdd = 0
    private val mDeleteClickListener: BtnClickListener? = clickDelete
    private val mCopyClickListener: BtnClickListener? = clickCopy
    @SuppressLint("SetTextI18n")
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val cdView: CardView = view.findViewById(R.id.CARDVIEW)
        val position = cursor.position
        if (position % 2 == mFirstColorOdd) {
            cdView.setBackgroundColor(getColor(context.resources,R.color.record_background_odd, context.theme))
        } else {
            cdView.setBackgroundColor(getColor(context.resources, R.color.record_background_even, context.theme))
        }

        /* Column display */
        val tDate = view.findViewById<TextView>(R.id.DATE_CELL)
        var date: Date?=null// = DateConverter.DBDateStrToDate("")
        val cursorDate = cursor.getColumnIndex(DAORecord.DATE)
        if(cursor.getColumnIndex(DAORecord.DATE) >= 0) {
            val dateString = cursor.getString(cursorDate)
            date = DateConverter.DBDateStrToDate(dateString)
        }
        tDate.text = DateConverter.dateToLocalDateStr(date, mContext)

        val tTime = view.findViewById<TextView>(R.id.TIME_CELL)
        val cursorTime=cursor.getColumnIndex(DAORecord.TIME)
        tTime.text = cursor.getString(cursorTime)

        val tExercise = view.findViewById<TextView>(R.id.MACHINE_CELL)
        val cursorExercise = cursor.getColumnIndex(DAOExerciseInProgram.EXERCISE)
        tExercise.text = cursor.getString(cursorExercise)
        val tSeries = view.findViewById<TextView>(R.id.SERIE_CELL)
        val tSeriesLabel = view.findViewById<TextView>(R.id.SERIE_LABEL)
        val tReps = view.findViewById<TextView>(R.id.REPETITION_CELL)
        val tRepsLabel = view.findViewById<TextView>(R.id.REP_LABEL)
        val tWeight = view.findViewById<TextView>(R.id.POIDS_CELL)
        val tWeightLabel = view.findViewById<TextView>(R.id.WEIGHT_LABEL)
        val tRepsLayout = view.findViewById<LinearLayout>(R.id.REP_LAYOUT)
        if (mCopyClickListener == null) {
            view.findViewById<View>(R.id.copyButton).visibility = View.GONE
        }

        /* Specific display */
        val cursorType = cursor.getColumnIndex(DAOExerciseInProgram.TYPE)
        when (cursor.getInt(cursorType)) {
            DAOMachine.TYPE_STRENGTH -> {
                // UI
                tSeriesLabel.text = mContext.getString(R.string.SerieLabel)
                tWeightLabel.text = mContext.getString(R.string.PoidsLabel)
                tRepsLabel.text = mContext.getString(R.string.RepetitionLabel_short)
                tRepsLayout.visibility = View.VISIBLE
                // Data
                val cursorSeries = cursor.getColumnIndex(DAOExerciseInProgram.SERIE)
                tSeries.text = cursor.getString(cursorSeries)
                val cursorRepetitions = cursor.getColumnIndex(DAOExerciseInProgram.REPETITION)
                tReps.text = cursor.getString(cursorRepetitions)
                var unit = mContext.getString(R.string.KgUnitLabel)
                val cursorWeight = cursor.getColumnIndex(DAOExerciseInProgram.WEIGHT)
                var poids = cursor.getFloat(cursorWeight)
                val cursorUnit = cursor.getColumnIndex(DAOExerciseInProgram.UNIT)
                if (cursor.getInt(cursorUnit) == UnitConverter.UNIT_LBS) {
                    poids = UnitConverter.KgtoLbs(poids)
                    unit = mContext.getString(R.string.LbsUnitLabel)
                }
                val numberFormat = DecimalFormat("#.##")
                tWeight.text = numberFormat.format(poids.toDouble()) + unit
            }
            DAOMachine.TYPE_STATIC -> {
                // UI
                tSeriesLabel.text = mContext.getString(R.string.SerieLabel)
                tWeightLabel.text = mContext.getString(R.string.PoidsLabel)
                tRepsLabel.text = mContext.getString(R.string.SecondsLabel_short)
                tRepsLayout.visibility = View.VISIBLE
                // Data
                val cursorSeries=cursor.getColumnIndex(DAOExerciseInProgram.SERIE)
                tSeries.text = cursor.getString(cursorSeries)
                val cursorSeconds=cursor.getColumnIndex(DAOExerciseInProgram.SECONDS)
                tReps.text = cursor.getString(cursorSeconds)
                var unit = mContext.getString(R.string.KgUnitLabel)
                val cursorWeight=cursor.getColumnIndex(DAOExerciseInProgram.WEIGHT)
                var poids = cursor.getFloat(cursorWeight)
                val cursorUnit = cursor.getColumnIndex(DAOExerciseInProgram.UNIT)
                if (cursor.getInt(cursorUnit) == UnitConverter.UNIT_LBS) {
                    poids = UnitConverter.KgtoLbs(poids)
                    unit = mContext.getString(R.string.LbsUnitLabel)
                }
                val numberFormat = DecimalFormat("#.##")
                tWeight.text = numberFormat.format(poids.toDouble()) + unit
            }
            DAOMachine.TYPE_CARDIO -> {
                tSeriesLabel.text = mContext.getString(R.string.DistanceLabel)
                tWeightLabel.text = mContext.getString(R.string.DurationLabel)
                tRepsLayout.visibility = View.GONE
                val cursorDistance = cursor.getColumnIndex(DAOExerciseInProgram.DISTANCE)
                var distance = cursor.getFloat(cursorDistance)
                var unit = mContext.getString(R.string.KmUnitLabel)
                val cursorDistanceUnit = cursor.getColumnIndex(DAOExerciseInProgram.DISTANCE_UNIT)
                if (cursor.getInt(cursorDistanceUnit) == UnitConverter.UNIT_MILES) {
                    distance = UnitConverter.KmToMiles(distance) // Always convert to KG
                    unit = mContext.getString(R.string.MilesUnitLabel)
                }
                val numberFormat = DecimalFormat("#.##")
                tSeries.text = numberFormat.format(distance.toDouble()) + unit
                val cursorDuration=cursor.getColumnIndex(DAOExerciseInProgram.DURATION)
                tWeight.text = DateConverter.durationToHoursMinutesSecondsStr(cursor.getInt(cursorDuration).toLong())
            }
        }

        val deleteImg = view.findViewById<ImageView>(R.id.deleteButton)
        val cursorKey = cursor.getColumnIndex(DAOExerciseInProgram.KEY)
        deleteImg.tag = cursor.getLong(cursorKey)
        deleteImg.setOnClickListener { v: View -> mDeleteClickListener?.onBtnClick(v.tag as Long) }
        val copyImg = view.findViewById<ImageView>(R.id.copyButton)
        copyImg.tag = cursor.getLong(cursorKey)
        copyImg.setOnClickListener { v: View -> mCopyClickListener?.onBtnClick(v.tag as Long) }
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return mInflater.inflate(R.layout.row_fonte, parent, false)
    }

    /**
     * @pColor : set colour odd for first row. Next colour will be even.
     * si 1 alors affiche la couleur Odd en premier. Sinon, a couleur Even.
     */
    fun setFirstColorOdd(pColor: Int) {
        mFirstColorOdd = pColor
    }

}
