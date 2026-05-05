package com.easyfitness.DAO

import android.content.ContentValues
import android.content.Context
import com.easyfitness.GraphData
import com.easyfitness.R
import com.easyfitness.utils.DateConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class DAOCardio(context: Context) : DAORecord(context) {
    init {
        mContext = context
    }

    /**
     * @param pDate
     * @param pTime
     * @param pMachine
     * @param pDistance
     * @param pDuration
     * @param pProfile
     * @return
     */
    fun addCardioRecord(
        pDate: Date?,
        pTime: String?,
        pMachine: String?,
        pDistance: Float,
        pDuration: Long,
        pProfile: Profile?,
        pDistanceUnit: Int
    ): Long {
        return addRecord(
            pDate,
            pMachine,
            DAOMachine.TYPE_CARDIO,
            0,
            0,
            0f,
            pProfile,
            0,
            "",
            pTime,
            pDistance,
            pDuration,
            0,
            pDistanceUnit
        )
    }

    fun addCardioList(cardioList: MutableList<Cardio>) {
        for (cardio in cardioList) {
            addRecord(
                cardio.mDate,
                cardio.exercise,
                DAOMachine.TYPE_CARDIO,
                0,
                0,
                0f,
                cardio.profil,
                0,
                "",
                cardio.time,
                cardio.distance,
                cardio.duration,
                0,
                cardio.distanceUnit
            )
        }
    }

    override fun getRecord(id: Long): Cardio? {
        val selectQuery = ("SELECT  " + TABLE_ARCHI + " FROM " + DAORecord.Companion.TABLE_NAME
                + " WHERE " + DAORecord.Companion.KEY + "=" + id)
        val valueList: MutableList<Cardio?>?

        valueList = getRecordsList(selectQuery)
        if (valueList.isEmpty()) return null
        else return valueList.get(0)
    }

    // Getting All Records
    private fun getRecordsList(pRequest: String): MutableList<Cardio?> {
        val valueList: MutableList<Cardio?> = ArrayList<Cardio?>()
        val db = this.readableDatabase

        // Select All Query
//        val mCursor = null
        var mCursor = db!!.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                //Get Date
                val date: Date?
                date =
                    DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DAORecord.Companion.DATE)))

                //Get Profile
                val lDAOProfil = DAOProfil(mContext)
                val lProfile =
                    lDAOProfil.getProfil(mCursor.getLong(mCursor.getColumnIndex(PROFIL_KEY)))

                val value = Cardio(
                    date,
                    mCursor.getString(mCursor.getColumnIndex(EXERCISE)),
                    mCursor.getFloat(mCursor.getColumnIndex(DISTANCE)),
                    mCursor.getLong(mCursor.getColumnIndex(DURATION)),
                    lProfile,
                    mCursor.getString(mCursor.getColumnIndex(TIME)),
                    mCursor.getInt(mCursor.getColumnIndex(DISTANCE_UNIT))
                )

                value.id=(
                    mCursor.getString(mCursor.getColumnIndex(DAORecord.Companion.KEY)).toLong()
                )

                // Adding value to list
                valueList.add(value)
            } while (mCursor.moveToNext())
        }
        // return value list
        return valueList
    }

    val allRecords: MutableList<Cardio?>
        // Getting All Records
        get() {
            // Select All Query
            val selectQuery =
                ("SELECT " + TABLE_ARCHI + " FROM " + DAORecord.Companion.TABLE_NAME
                        + " ORDER BY " + DAORecord.Companion.KEY + " DESC")

            // return value list
            return getRecordsList(selectQuery)
        }

    // Getting All Records
    fun getAllCardioRecordsByProfile(pProfile: Profile): MutableList<Cardio?> {
        // Select All Query
        val selectQuery = ("SELECT " + TABLE_ARCHI + " FROM " + DAORecord.Companion.TABLE_NAME
                + " WHERE " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                + " AND " + DAORecord.Companion.TYPE + "=" + DAOMachine.TYPE_CARDIO
                + " ORDER BY " + DAORecord.Companion.KEY + " DESC")

        // return value list
        return getRecordsList(selectQuery)
    }

    // Getting Top 10 Records
    fun getTop10Records(pProfile: Profile): MutableList<Cardio?> {
        // Select All Query
        val selectQuery = ("SELECT TOP 10 * FROM " + DAORecord.Companion.TABLE_NAME
                + " WHERE " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                + " AND " + DAORecord.Companion.TYPE + "=" + DAOMachine.TYPE_CARDIO
                + " ORDER BY " + DAORecord.Companion.KEY + " DESC")

        // return value list
        return getRecordsList(selectQuery)
    }

    // Getting Function records
    fun getFunctionRecords(
        pProfile: Profile, pMachine: String?,
        pFunction: Int
    ): MutableList<GraphData?> {
        var lfilterMachine = true
        val lfilterFunction = true
        var selectQuery: String? = null

        if (pMachine == null || pMachine.isEmpty() || pMachine == mContext?.getResources()
                !!.getText(R.string.all).toString()
        ) {
            lfilterMachine = false
        }

        if (pFunction == DISTANCE_FCT) {
            selectQuery =
                ("SELECT SUM(" + DAORecord.Companion.DISTANCE + "), " + DAORecord.Companion.DATE + " FROM " + DAORecord.Companion.TABLE_NAME
                        + " WHERE " + DAORecord.Companion.EXERCISE + "=\"" + pMachine + "\""
                        + " AND " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                        + " GROUP BY " + DAORecord.Companion.DATE
                        + " ORDER BY date(" + DAORecord.Companion.DATE + ") ASC")
        } else if (pFunction == DURATION_FCT) {
            selectQuery =
                ("SELECT SUM(" + DAORecord.Companion.DURATION + ") , " + DAORecord.Companion.DATE + " FROM "
                        + DAORecord.Companion.TABLE_NAME
                        + " WHERE " + DAORecord.Companion.EXERCISE + "=\"" + pMachine + "\""
                        + " AND " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                        + " GROUP BY " + DAORecord.Companion.DATE
                        + " ORDER BY date(" + DAORecord.Companion.DATE + ") ASC")
        } else if (pFunction == SPEED_FCT) {
            selectQuery =
                ("SELECT SUM(" + DAORecord.Companion.DISTANCE + ") / SUM(" + DAORecord.Companion.DURATION + ")," + DAORecord.Companion.DATE + " FROM "
                        + DAORecord.Companion.TABLE_NAME
                        + " WHERE " + DAORecord.Companion.EXERCISE + "=\"" + pMachine + "\""
                        + " AND " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                        + " GROUP BY " + DAORecord.Companion.DATE
                        + " ORDER BY date(" + DAORecord.Companion.DATE + ") ASC")
        } else if (pFunction == MAXDISTANCE_FCT) {
            selectQuery =
                ("SELECT MAX(" + DAORecord.Companion.DISTANCE + ") , " + DAORecord.Companion.DATE + " FROM "
                        + DAORecord.Companion.TABLE_NAME
                        + " WHERE " + DAORecord.Companion.EXERCISE + "=\"" + pMachine + "\""
                        + " AND " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                        + " GROUP BY " + DAORecord.Companion.DATE
                        + " ORDER BY date(" + DAORecord.Companion.DATE + ") ASC")
        }

        // case "MEAN" : selectQuery = "SELECT SUM("+ SERIE + "*" + REPETITION +
        // "*" + WEIGHT +") FROM " + TABLE_NAME + " WHERE " + EXERCISE + "=\"" +
        // pMachine + "\" AND " + DATE + "=\"" + pDate + "\" ORDER BY " + KEY +
        // " DESC";
        // break;

        // Formation de tableau de valeur
        val valueList: MutableList<GraphData?> = ArrayList<GraphData?>()
        val db = this.readableDatabase
        val mCursor = db!!.rawQuery(selectQuery!!, null)

        val i = 0.0

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                var date: Date?
                try {
                    val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
                    date = dateFormat.parse(mCursor.getString(1))
                } catch (e: ParseException) {
                    e.printStackTrace()
                    date = Date()
                }

                val value = GraphData(
                    DateConverter.nbDays(date.getTime().toDouble()),
                    mCursor.getDouble(0)
                )

                // Adding value to list
                valueList.add(value)
            } while (mCursor.moveToNext())
        }

        // return value list
        return valueList
    }

    // Getting All Machines
    override fun getAllMachines(pProfile: Profile): Array<String?> {
        val db = this.readableDatabase

        // Select All Machines
        val selectQuery =
            ("SELECT DISTINCT  " + DAORecord.Companion.EXERCISE + " FROM " + DAORecord.Companion.TABLE_NAME
                    + " WHERE " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                    + " AND " + DAORecord.Companion.TYPE + "=" + DAOMachine.TYPE_CARDIO
                    + " ORDER BY " + DAORecord.Companion.EXERCISE + " COLLATE NOCASE ASC")
        val mCursor = db!!.rawQuery(selectQuery, null)

        val size = mCursor.getCount()

        val valueList = arrayOfNulls<String>(size)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            var i = 0
            do {
                val value = mCursor.getString(mCursor.getColumnIndex(DAORecord.Companion.EXERCISE))
                valueList[i] = value
                i++
            } while (mCursor.moveToNext())
        }
        close()
        // return value list
        return valueList
    }

    // Get all record for one Exercise
    fun getAllCardioRecordByMachines(pProfile: Profile, pExercise: String?): MutableList<Cardio?> {
        // Select All Query
        val selectQuery = ("SELECT * FROM " + DAORecord.Companion.TABLE_NAME
                + " WHERE " + DAORecord.Companion.EXERCISE + "=\"" + pExercise + "\""
                + " AND " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                + " ORDER BY " + DAORecord.Companion.KEY + " DESC")

        // return value list
        return getRecordsList(selectQuery)
    }

    // Updating single value
    fun updateRecord(pProfile: Profile, m: Cardio): Int {
        val db = this.writableDatabase

        val value = ContentValues()
        value.put(DAORecord.Companion.DATE, m.date.toString())
        value.put(DAORecord.Companion.EXERCISE, m.exercise)
        value.put(DAORecord.Companion.MACHINE_KEY, m.exerciseKey)
        value.put(DAORecord.Companion.DISTANCE, m.distance)
        value.put(DAORecord.Companion.DURATION, m.duration)
        value.put(DAORecord.Companion.PROFIL_KEY, pProfile.id)
        value.put(DAORecord.Companion.DISTANCE_UNIT, m.distanceUnit)

        // updating row
        return db!!.update(
            DAORecord.Companion.TABLE_NAME, value, DAORecord.Companion.KEY + " = ?",
            arrayOf<String>(m.id.toString())
        )
    }

    fun populate() {
        // DBORecord(long id, Date pDate, String pMachine, int pSerie, int
        // pRepetition, int pPoids)
        var date = Date()
        var poids = 10

        for (i in 1..5) {
            val machine = "Tapis"
            date.setDate(date.getDay() + i * 10)
            addCardioRecord(
                date,
                "00:00",
                machine,
                i.toFloat() * 20,
                (120000 * i).toLong(),
                mProfile,
                0
            )
        }

        date = Date()
        poids = 12

        for (i in 1..5) {
            val machine = "Rameur"
            date.setDate(date.getDay() + i * 10)
            addCardioRecord(date, "00:00", machine, 0f, (120000 * i * 3).toLong(), mProfile, 0)
        }
    }

    companion object {
        const val DISTANCE_FCT: Int = 0
        const val DURATION_FCT: Int = 1
        const val SPEED_FCT: Int = 2
        const val MAXDURATION_FCT: Int = 3
        const val MAXDISTANCE_FCT: Int = 4
        const val NBSERIE_FCT: Int = 5

        private const val OLD_TABLE_NAME = "EFcardio"

        private val TABLE_ARCHI: String =
            DAORecord.Companion.KEY + "," + DAORecord.Companion.DATE + "," + DAORecord.Companion.EXERCISE + "," + DAORecord.Companion.DISTANCE + "," + DAORecord.Companion.DURATION + "," + DAORecord.Companion.PROFIL_KEY + "," + DAORecord.Companion.TIME + "," + DAORecord.Companion.DISTANCE_UNIT
    }
}
