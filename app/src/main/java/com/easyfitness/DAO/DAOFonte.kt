package com.easyfitness.DAO

import android.content.ContentValues
import android.content.Context
import com.easyfitness.GraphData
import com.easyfitness.utils.DateConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class DAOFonte(context: Context?) : DAORecord(context) {
    /**
     * @param pDate    Date
     * @param pMachine Machine name
     * Le Record a ajouter a la base
     */
    fun addBodyBuildingRecord(
        pDate: Date?,
        pMachine: String?,
        pSerie: Int,
        pRepetition: Int,
        pPoids: Float,
        pProfile: Profile?,
        pUnit: Int,
        pNote: String?,
        pTime: String?
    ): Long {
        return addRecord(
            pDate,
            pMachine,
            DAOMachine.TYPE_STRENGTH,
            pSerie,
            pRepetition,
            pPoids,
            pProfile,
            pUnit,
            pNote,
            pTime,
            0f,
            0,
            0,
            0
        )
    }

    /**
     * @param fonteList List of Fonte records
     */
    fun addBodyBuildingList(fonteList: MutableList<Fonte>) {
        for (fonte in fonteList) {
            addRecord(
                fonte.mDate,
                fonte.mExercise,
                DAOMachine.TYPE_STRENGTH,
                fonte.serie,
                fonte.repetition,
                fonte.poids,
                fonte.mProfile,
                fonte.unit,
                fonte.note,
                fonte.mTime,
                0f,
                0,
                0,
                0
            )
        }
    }

    // Getting single value
    fun getBodyBuildingRecord(id: Long): Fonte? {
        val selectQuery =
            "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id
        val valueList: MutableList<Fonte?>?

        valueList = getRecordsList(selectQuery)
        if (valueList.isEmpty()) return null
        else return valueList.get(0)
    }

    // Getting All Records
    private fun getRecordsList(pRequest: String): MutableList<Fonte?> {
        val valueList: MutableList<Fonte?> = ArrayList<Fonte?>()
        val db = this.getReadableDatabase()

        // Select All Query
        mCursor = null
        mCursor = db.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
            do {
                //Get Date
                var date: Date?
                try {
                    val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
                    date = dateFormat.parse(mCursor.getString(mCursor.getColumnIndex(DATE)))
                } catch (e: ParseException) {
                    e.printStackTrace()
                    date = Date()
                }

                //Get Profile
                val lDAOProfil = DAOProfil(mContext)
                val lProfile = lDAOProfil.getProfil(
                    mCursor.getLong(
                        mCursor.getColumnIndex(
                            PROFIL_KEY
                        )
                    )
                )

                var machine_key: Long = -1

                //Test is Machine exists. If not create it.
                val lDAOMachine = DAOMachine(mContext)
                if (mCursor.getString(mCursor.getColumnIndex(MACHINE_KEY)) == null) {
                    machine_key = lDAOMachine.addMachine(
                        mCursor.getString(
                            mCursor.getColumnIndex(
                                EXERCISE
                            )
                        ), "", DAOMachine.TYPE_STRENGTH, "", false, ""
                    )
                } else {
                    machine_key = mCursor.getLong(mCursor.getColumnIndex(MACHINE_KEY))
                }

                val value = Fonte(
                    date, mCursor.getString(mCursor.getColumnIndex(EXERCISE)),
                    mCursor.getInt(mCursor.getColumnIndex(SERIE)),
                    mCursor.getInt(mCursor.getColumnIndex(REPETITION)),
                    mCursor.getFloat(mCursor.getColumnIndex(WEIGHT)),
                    lProfile,
                    mCursor.getInt(mCursor.getColumnIndex(UNIT)),
                    mCursor.getString(mCursor.getColumnIndex(NOTES)),
                    machine_key,
                    mCursor.getString(mCursor.getColumnIndex(TIME))
                )

                value.setId(mCursor.getLong(mCursor.getColumnIndex(KEY)))

                // Adding value to list
                valueList.add(value)
            } while (mCursor.moveToNext())
        }
        // return value list
        return valueList
    }

    val allBodyBuildingRecords: MutableList<Fonte?>
        // Getting All Records
        get() {
            // Select All Query
            val selectQuery =
                ("SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME
                        + " WHERE " + TYPE + "=" + DAOMachine.TYPE_STRENGTH
                        + " ORDER BY " + DATE + " DESC," + KEY + " DESC")

            // return value list
            return getRecordsList(selectQuery)
        }

    // Getting All Records
    fun getAllBodyBuildingRecordsByProfileArray(pProfile: Profile): MutableList<Fonte?> {
        return getAllBodyBuildingRecordsByProfileArray(pProfile, -1)
    }

    private fun getAllBodyBuildingRecordsByProfileArray(
        pProfile: Profile,
        pNbRecords: Int
    ): MutableList<Fonte?> {
        val mTop: String?
        if (pNbRecords == -1) mTop = ""
        else mTop = " LIMIT " + pNbRecords


        // Select All Query
        val selectQuery = ("SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME
                + " WHERE " + PROFIL_KEY + "=" + pProfile.id
                + " AND " + TYPE + "=" + DAOMachine.TYPE_STRENGTH
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop)

        // Return value list
        return getRecordsList(selectQuery)
    }

    // Getting Function records
    fun getBodyBuildingFunctionRecords(
        pProfile: Profile, pMachine: String?,
        pFunction: Int
    ): MutableList<GraphData?> {
        var selectQuery: String? = null

        // TODO attention aux units de poids. Elles ne sont pas encore prise en compte ici.
        if (pFunction == SUM_FCT) {
            selectQuery = ("SELECT SUM(" + SERIE + "*" + REPETITION + "*"
                    + WEIGHT + "), " + DATE + " FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFIL_KEY + "=" + pProfile.id
                    + " GROUP BY " + DATE
                    + " ORDER BY date(" + DATE + ") ASC")
        } else if (pFunction == MAX5_FCT) {
            selectQuery = ("SELECT MAX(" + WEIGHT + ") , " + DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + REPETITION + ">=5"
                    + " AND " + PROFIL_KEY + "=" + pProfile.id
                    + " GROUP BY " + DATE
                    + " ORDER BY date(" + DATE + ") ASC")
        } else if (pFunction == MAX1_FCT) {
            selectQuery = ("SELECT MAX(" + WEIGHT + ") , " + DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + REPETITION + ">=1"
                    + " AND " + PROFIL_KEY + "=" + pProfile.id
                    + " GROUP BY " + DATE
                    + " ORDER BY date(" + DATE + ") ASC")
        } else if (pFunction == NBSERIE_FCT) {
            selectQuery = ("SELECT count(" + KEY + ") , " + DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFIL_KEY + "=" + pProfile.id
                    + " GROUP BY " + DATE
                    + " ORDER BY date(" + DATE + ") ASC")
        }

        // Formation de tableau de valeur
        val valueList: MutableList<GraphData?> = ArrayList<GraphData?>()
        val db = this.getReadableDatabase()

        mCursor = null
        mCursor = db.rawQuery(selectQuery!!, null)

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

                val value =
                    GraphData(DateConverter.nbDays(date.getTime().toDouble()), mCursor.getDouble(0))

                // Adding value to list
                valueList.add(value)
            } while (mCursor.moveToNext())
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

        val db = this.getReadableDatabase()
        mCursor = null

        // Select All Machines
        val selectQuery = ("SELECT SUM(" + SERIE + ") FROM " + TABLE_NAME
                + " WHERE " + DATE + "=\"" + lDate + "\" AND " + MACHINE_KEY + "=" + machine_key)
        mCursor = db.rawQuery(selectQuery, null)

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

        val db = this.getReadableDatabase()
        mCursor = null
        // Select All Machines
        val selectQuery =
            ("SELECT " + SERIE + ", " + WEIGHT + ", " + REPETITION + " FROM " + TABLE_NAME
                    + " WHERE " + DATE + "=\"" + lDate + "\" AND " + MACHINE_KEY + "=" + machine_key)
        mCursor = db.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            var i = 0
            do {
                val value = mCursor.getInt(0) * mCursor.getFloat(1) * mCursor.getInt(2)
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
        val db = this.getReadableDatabase()
        mCursor = null
        var lReturn = 0f

        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        val lDate = dateFormat.format(pDate)

        // Select All Machines
        val selectQuery =
            ("SELECT " + SERIE + ", " + WEIGHT + ", " + REPETITION + " FROM " + TABLE_NAME
                    + " WHERE " + DATE + "=\"" + lDate + "\"")
        mCursor = db.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            var i = 0
            do {
                val value = mCursor.getInt(0) * mCursor.getFloat(1) * mCursor.getInt(2)
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
        val db = this.getReadableDatabase()
        mCursor = null
        var w: Weight? = null

        // Select All Machines
        val selectQuery = ("SELECT MAX(" + WEIGHT + "), " + UNIT + " FROM " + TABLE_NAME
                + " WHERE " + PROFIL_KEY + "=" + p.id + " AND " + MACHINE_KEY + "=" + m.id)
        mCursor = db.rawQuery(selectQuery, null)

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
    fun getMin(p: Profile?, m: Machine): Weight? {
        val db = this.getReadableDatabase()
        mCursor = null
        var w: Weight? = null

        // Select All Machines
        val selectQuery = ("SELECT MIN(" + WEIGHT + "), " + UNIT + " FROM " + TABLE_NAME
                + " WHERE " + PROFIL_KEY + "=" + p!!.id + " AND " + MACHINE_KEY + "=" + m.id)
        mCursor = db.rawQuery(selectQuery, null)

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
    fun updateRecord(m: Fonte): Int {
        val db = this.getWritableDatabase()

        val value = ContentValues()

        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        value.put(DATE, dateFormat.format(m.getDate()))
        value.put(EXERCISE, m.getExercise())
        value.put(MACHINE_KEY, m.getExerciseKey())
        value.put(SERIE, m.serie)
        value.put(REPETITION, m.repetition)
        value.put(WEIGHT, m.poids)
        value.put(UNIT, m.unit)
        value.put(NOTES, m.note)
        value.put(PROFIL_KEY, m.getProfilKey())
        value.put(TIME, m.getTime())
        value.put(TYPE, DAOMachine.TYPE_STRENGTH)

        // updating row
        return db.update(
            TABLE_NAME, value, KEY + " = ?",
            arrayOf<String>(m.getId().toString())
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
            addBodyBuildingRecord(
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
            addBodyBuildingRecord(
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
        const val SUM_FCT: Int = 0
        const val MAX1_FCT: Int = 1
        const val MAX5_FCT: Int = 2
        const val NBSERIE_FCT: Int = 3

        private val TABLE_ARCHI =
            KEY + "," + DATE + "," + EXERCISE + "," + SERIE + "," + REPETITION + "," + WEIGHT + "," + UNIT + "," + PROFIL_KEY + "," + NOTES + "," + MACHINE_KEY + "," + TIME
    }
}
