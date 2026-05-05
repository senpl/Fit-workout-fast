package com.easyfitness.DAO

import android.content.ContentValues
import android.content.Context
import com.easyfitness.GraphData
import com.easyfitness.utils.DateConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class DAOStatic(context: Context) : DAORecord(context) {
    /**
     * @param pDate    Date
     * @param pMachine Machine name
     * Le Record a ajouter a la base
     */
    fun addStaticRecord(
        pDate: Date?,
        pMachine: String?,
        pSerie: Int,
        pSeconds: Int,
        pPoids: Float,
        pProfile: Profile?,
        pUnit: Int,
        pNote: String?,
        pTime: String?
    ): Long {
        return addRecord(
            pDate,
            pMachine,
            DAOMachine.TYPE_STATIC,
            pSerie,
            0,
            pPoids,
            pProfile,
            pUnit,
            pNote,
            pTime,
            0f,
            0,
            pSeconds,
            0
        )
    }

    fun addStaticList(staticExerciseList: MutableList<StaticExercise>) {
        for (staticExercise in staticExerciseList) {
            addRecord(
                staticExercise.mDate,
                staticExercise.exercise,
                DAOMachine.TYPE_CARDIO,
                staticExercise.serie,
                0,
                staticExercise.poids,
                staticExercise.profil,
                0,
                "",
                staticExercise.time,
                0f,
                0,
                staticExercise.second,
                0
            )
        }
    }

    // Getting single value
    fun getStaticRecord(id: Long): StaticExercise? {
        val selectQuery =
            "SELECT  " + TABLE_ARCHI + " FROM " + DAORecord.Companion.TABLE_NAME + " WHERE " + DAORecord.Companion.KEY + "=" + id
        val valueList: MutableList<StaticExercise?>?

        valueList = getRecordsList(selectQuery)
        if (valueList.isEmpty()) return null
        else return valueList.get(0)
    }

    // Getting All Records
    private fun getRecordsList(pRequest: String): MutableList<StaticExercise?> {
        val valueList: MutableList<StaticExercise?> = ArrayList<StaticExercise?>()
        val db = this.readableDatabase

        // Select All Query
//        mCursor = null
        val mCursor = db!!.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
            do {
                //Get Date
                var date: Date?
                try {
                    val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
                    date =
                        dateFormat.parse(mCursor.getString(mCursor.getColumnIndex(DAORecord.Companion.DATE)))
                } catch (e: ParseException) {
                    e.printStackTrace()
                    date = Date()
                }

                //Get Profile
                val lDAOProfil = DAOProfil(mContext)
                val lProfile =
                    lDAOProfil.getProfil(mCursor.getLong(mCursor.getColumnIndex(DAORecord.Companion.PROFIL_KEY)))

                var machine_key: Long = -1

                //Test is Machine exists. If not create it.
                val lDAOMachine = DAOMachine(mContext)
                if (mCursor.getString(mCursor.getColumnIndex(DAORecord.Companion.MACHINE_KEY)) == null) {
                    machine_key = lDAOMachine.addMachine(
                        mCursor.getString(mCursor.getColumnIndex(DAORecord.Companion.EXERCISE)),
                        "",
                        DAOMachine.TYPE_STATIC,
                        "",
                        false,
                        ""
                    )
                } else {
                    machine_key =
                        mCursor.getLong(mCursor.getColumnIndex(DAORecord.Companion.MACHINE_KEY))
                }

                val value = StaticExercise(
                    date, mCursor.getString(mCursor.getColumnIndex(DAORecord.Companion.EXERCISE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAORecord.Companion.SERIE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAORecord.Companion.SECONDS)),
                    mCursor.getFloat(mCursor.getColumnIndex(DAORecord.Companion.WEIGHT)),
                    lProfile,
                    mCursor.getInt(mCursor.getColumnIndex(DAORecord.Companion.UNIT)),
                    machine_key,
                    mCursor.getString(mCursor.getColumnIndex(DAORecord.Companion.TIME))
                )

                value.id=(mCursor.getLong(mCursor.getColumnIndex(DAORecord.Companion.KEY)))

                // Adding value to list
                valueList.add(value)
            } while (mCursor.moveToNext())
        }
        // return value list
        return valueList
    }

    val allStaticRecords: MutableList<StaticExercise?>
        // Getting All Records
        get() {
            // Select All Query
            val selectQuery =
                ("SELECT  " + TABLE_ARCHI + " FROM " + DAORecord.Companion.TABLE_NAME
                        + " WHERE " + DAORecord.Companion.TYPE + "=" + DAOMachine.TYPE_STATIC
                        + " ORDER BY " + DAORecord.Companion.DATE + " DESC," + DAORecord.Companion.KEY + " DESC")

            // return value list
            return getRecordsList(selectQuery)
        }

    // Getting All Records
    fun getAllStaticRecordsByProfileArray(pProfile: Profile): MutableList<StaticExercise?> {
        return getAllStaticRecordsByProfileArray(pProfile, -1)
    }

    private fun getAllStaticRecordsByProfileArray(
        pProfile: Profile,
        pNbRecords: Int
    ): MutableList<StaticExercise?> {
        val mTop: String?
        if (pNbRecords == -1) mTop = ""
        else mTop = " LIMIT " + pNbRecords


        // Select All Query
        val selectQuery = ("SELECT " + TABLE_ARCHI + " FROM " + DAORecord.Companion.TABLE_NAME
                + " WHERE " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                + " AND " + DAORecord.Companion.TYPE + "=" + DAOMachine.TYPE_STATIC
                + " ORDER BY " + DAORecord.Companion.DATE + " DESC," + DAORecord.Companion.KEY + " DESC" + mTop)

        // Return value list
        return getRecordsList(selectQuery)
    }

    // Getting Function records
    fun getStaticFunctionRecords(
        pProfile: Profile, pMachine: String?,
        pFunction: Int
    ): MutableList<GraphData?> {
        var selectQuery: String? = null

        // TODO attention aux units de poids. Elles ne sont pas encore prise en compte ici.
        if (pFunction == MAX_FCT) {
            selectQuery =
                ("SELECT MAX(" + DAORecord.Companion.WEIGHT + ") , " + DAORecord.Companion.SECONDS + " FROM "
                        + DAORecord.Companion.TABLE_NAME
                        + " WHERE " + DAORecord.Companion.EXERCISE + "=\"" + pMachine + "\""
                        + " AND " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                        + " GROUP BY " + DAORecord.Companion.SECONDS
                        + " ORDER BY " + DAORecord.Companion.SECONDS + " ASC")
        } else if (pFunction == NBSERIE_FCT) {
            selectQuery =
                ("SELECT count(" + DAORecord.Companion.KEY + ") , " + DAORecord.Companion.DATE + " FROM "
                        + DAORecord.Companion.TABLE_NAME
                        + " WHERE " + DAORecord.Companion.EXERCISE + "=\"" + pMachine + "\""
                        + " AND " + DAORecord.Companion.PROFIL_KEY + "=" + pProfile.id
                        + " GROUP BY " + DAORecord.Companion.DATE
                        + " ORDER BY date(" + DAORecord.Companion.DATE + ") ASC")
        }

        // Formation de tableau de valeur
        val valueList: MutableList<GraphData?> = ArrayList<GraphData?>()
        val db = this.readableDatabase

//        mCursor = null
        val mCursor = db!!.rawQuery(selectQuery!!, null)

        val i = 0.0

        // looping through all rows and adding to list
        if (pFunction == NBSERIE_FCT) {
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
        } else if (pFunction == MAX_FCT) {
            if (mCursor.moveToFirst()) {
                do {
                    val value = GraphData(mCursor.getDouble(1), mCursor.getDouble(0))
                    valueList.add(value)
                } while (mCursor.moveToNext())
            }
        }

        // return value list
        return valueList
    }

    /**
     * @return the number of series for this machine for this day
     */
    fun getNbSeries(pDate: Date, pMachine: String?): Int {
        var lReturn = 0

        //Test is Machine exists. If not create it.
        val lDAOMachine = DAOMachine(mContext)
        val machine_key = lDAOMachine.getMachine(pMachine)!!.id

        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        val lDate = dateFormat.format(pDate)

        val db = this.readableDatabase

        // Select All Machines
        val selectQuery =
            ("SELECT SUM(" + DAORecord.Companion.SERIE + ") FROM " + DAORecord.Companion.TABLE_NAME
                    + " WHERE " + DAORecord.Companion.DATE + "=\"" + lDate + "\" AND " + DAORecord.Companion.MACHINE_KEY + "=" + machine_key)
        val mCursor = db!!.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        mCursor.moveToFirst()
        try {
            lReturn = mCursor.getInt(0)
        } catch (e: NumberFormatException) {
            //Date date = new Date();
            lReturn = 0 // Return une valeur
        }

        close()

        // return value
        return lReturn
    }

    /**
     * @return the total weight for this machine for this day
     */
    fun getTotalWeightMachine(pDate: Date, pMachine: String?): Float {
        var lReturn = 0f

        //Test is Machine exists. If not create it.
        val lDAOMachine = DAOMachine(mContext)
        val machine_key = lDAOMachine.getMachine(pMachine)!!.id

        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        val lDate = dateFormat.format(pDate)

        val db = this.readableDatabase
        // Select All Machines
        val selectQuery =
            ("SELECT " + DAORecord.Companion.SERIE + ", " + DAORecord.Companion.WEIGHT + " FROM " + DAORecord.Companion.TABLE_NAME
                    + " WHERE " + DAORecord.Companion.DATE + "=\"" + lDate + "\" AND " + DAORecord.Companion.MACHINE_KEY + "=" + machine_key)
        val mCursor = db!!.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            var i = 0
            do {
                val value = mCursor.getInt(0) * mCursor.getFloat(1)
                lReturn += value
                i++
            } while (mCursor.moveToNext())
        }
        close()

        // return value
        return lReturn
    }


    /**
     * @return the total weight for this day
     */
    fun getTotalWeightSession(pDate: Date): Float {
        val db = this.readableDatabase
        var lReturn = 0f

        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        val lDate = dateFormat.format(pDate)

        // Select All Machines
        val selectQuery =
            ("SELECT " + DAORecord.Companion.SERIE + ", " + DAORecord.Companion.WEIGHT + " FROM " + DAORecord.Companion.TABLE_NAME
                    + " WHERE " + DAORecord.Companion.DATE + "=\"" + lDate + "\"")
        val mCursor = db!!.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            var i = 0
            do {
                val value = mCursor.getInt(0) * mCursor.getFloat(1)
                lReturn += value
                i++
            } while (mCursor.moveToNext())
        }
        close()

        // return value
        return lReturn
    }

    /**
     * @return Max weight for a profile p and a machine m
     */
    fun getMax(p: Profile, m: Machine): Weight? {
        val db = this.readableDatabase
        var w: Weight? = null

        // Select All Machines
        val selectQuery =
            ("SELECT MAX(" + DAORecord.Companion.WEIGHT + "), " + DAORecord.Companion.UNIT + " FROM " + DAORecord.Companion.TABLE_NAME
                    + " WHERE " + DAORecord.Companion.PROFIL_KEY + "=" + p.id + " AND " + DAORecord.Companion.MACHINE_KEY + "=" + m.id)
        val mCursor = db!!.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                w = Weight(mCursor.getFloat(0), mCursor.getInt(1))
            } while (mCursor.moveToNext())
        }
        close()

        // return value
        return w
    }

    /**
     * @return Min weight for a profile p and a machine m
     */
    fun getMin(p: Profile, m: Machine): Weight? {
        val db = this.readableDatabase
        var w: Weight? = null

        // Select All Machines
        val selectQuery =
            ("SELECT MIN(" + DAORecord.Companion.WEIGHT + "), " + DAORecord.Companion.UNIT + " FROM " + DAORecord.Companion.TABLE_NAME
                    + " WHERE " + DAORecord.Companion.PROFIL_KEY + "=" + p.id + " AND " + DAORecord.Companion.MACHINE_KEY + "=" + m.id)
        val mCursor = db!!.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                w = Weight(mCursor.getFloat(0), mCursor.getInt(1))
            } while (mCursor.moveToNext())
        }
        close()

        // return value
        return w
    }

    // Updating single value
    fun updateRecord(m: StaticExercise): Int {
        val db = this.writableDatabase

        val value = ContentValues()

        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        value.put(DAORecord.Companion.DATE, dateFormat.format(m.date))
        value.put(DAORecord.Companion.EXERCISE, m.exercise)
        value.put(DAORecord.Companion.MACHINE_KEY, m.exerciseKey)
        value.put(DAORecord.Companion.SERIE, m.serie)
        value.put(DAORecord.Companion.SECONDS, m.second)
        value.put(DAORecord.Companion.WEIGHT, m.poids)
        value.put(DAORecord.Companion.UNIT, m.unit)
        value.put(DAORecord.Companion.NOTES, m.note)
        value.put(DAORecord.Companion.PROFIL_KEY, m.profilKey)
        value.put(DAORecord.Companion.TIME, m.time)
        value.put(DAORecord.Companion.TYPE, DAOMachine.TYPE_STATIC)

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
            val machine = "Biceps"
            date.setDate(date.getDay() + i * 10)
            addStaticRecord(
                date,
                machine,
                i * 2,
                10 + i,
                (poids * i).toFloat(),
                mProfile,
                0,
                "",
                "12:34:56"
            )
        }

        date = Date()
        poids = 12

        for (i in 1..5) {
            val machine = "Dev Couche"
            date.setDate(date.getDay() + i * 10)
            addStaticRecord(
                date,
                machine,
                i * 2,
                10 + i,
                (poids * i).toFloat(),
                mProfile,
                0,
                "",
                "12:34:56"
            )
        }
    }

    companion object {
        const val MAX_FCT: Int = 1
        const val NBSERIE_FCT: Int = 2

        private val TABLE_ARCHI: String =
            DAORecord.Companion.KEY + "," + DAORecord.Companion.DATE + "," + DAORecord.Companion.EXERCISE + "," + DAORecord.Companion.SERIE + "," + DAORecord.Companion.SECONDS + "," + DAORecord.Companion.WEIGHT + "," + DAORecord.Companion.UNIT + "," + DAORecord.Companion.PROFIL_KEY + "," + DAORecord.Companion.NOTES + "," + DAORecord.Companion.MACHINE_KEY + "," + DAORecord.Companion.TIME
    }
}
