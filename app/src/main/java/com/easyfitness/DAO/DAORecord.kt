package com.easyfitness.DAO

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.easyfitness.R
import com.easyfitness.utils.DateConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

open class DAORecord(protected var mContext: Context) : DAOBase(mContext) {
    protected var mProfile: Profile? = null
    var cursor: Cursor? = null
        protected set

    fun setProfile(pProfile: Profile?) {
        mProfile = pProfile
    }

    val count: Int
        // Getting Count
        get() {
            val countQuery =
                "SELECT " + KEY + " FROM " + TABLE_NAME
            open()
            val db = this.readableDatabase
            val cursor = db!!.rawQuery(countQuery, null)

            val value = cursor.getCount()
            cursor.close()
            close()

            // return count
            return value
        }

    /**
     * @param pDate    Date
     * @param pMachine Machine name
     * @return id of the added record, -1 if error
     */
    fun addRecord(
        pDate: Date?,
        pMachine: String?,
        pType: Int,
        pSerie: Int,
        pRepetition: Int,
        pPoids: Float,
        pProfile: Profile?,
        pUnit: Int,
        pNote: String?,
        pTime: String?,
        pDistance: Float,
        pDuration: Long,
        pSeconds: Int,
        distance_unit: Int
    ): Long {
        val value = ContentValues()
        var new_id: Long = -1
        var machine_key: Long = -1

        //Test is Machine exists. If not create it.
        val lDAOMachine = DAOMachine(mContext)
        if (!lDAOMachine.machineExists(pMachine)) {
            machine_key = lDAOMachine.addMachine(pMachine, "", pType, "", false, "")
        } else {
            machine_key = lDAOMachine.getMachine(pMachine)!!.id
        }

        value.put(DATE, DateConverter.dateToDBDateStr(pDate))
        value.put(EXERCISE, pMachine)
        value.put(SERIE, pSerie)
        value.put(REPETITION, pRepetition)
        value.put(WEIGHT, pPoids)
        value.put(PROFIL_KEY, pProfile?.id)
        value.put(UNIT, pUnit)
        value.put(NOTES, pNote)
        value.put(MACHINE_KEY, machine_key)
        value.put(TIME, pTime)
        value.put(DISTANCE, pDistance)
        value.put(DURATION, pDuration)
        value.put(TYPE, pType)
        value.put(SECONDS, pSeconds)
        value.put(DISTANCE_UNIT, distance_unit)

        val db = open()
        new_id = db!!.insert(TABLE_NAME, null, value)
        close()

        return new_id
    }

    // Deleting single Record
    fun deleteRecord(id: Long) {
        val db = this.writableDatabase
        db!!.delete(TABLE_NAME, KEY + " = ?", arrayOf<String>(id.toString()))
        db.close()
    }

    // Getting single value
    open fun getRecord(id: Long): IRecord? {
        val selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id

        this.cursor = getRecordsListCursor(selectQuery)
        if (cursor!!.moveToFirst()) {
            //Get Date
            var date: Date?
            try {
                val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
                date = dateFormat.parse(cursor!!.getString(cursor!!.getColumnIndex(DATE)))
            } catch (e: ParseException) {
                e.printStackTrace()
                date = Date()
            }

            //Get Profile
            val lDAOProfil = DAOProfil(mContext)
            val lProfile =
                lDAOProfil.getProfil(cursor!!.getLong(cursor!!.getColumnIndex(PROFIL_KEY)))

            var machine_key: Long = -1

            //Test is Machine exists. If not create it.
            val lDAOMachine = DAOMachine(mContext)
            if (cursor!!.getString(cursor!!.getColumnIndex(MACHINE_KEY)) == null) {
                machine_key = lDAOMachine.addMachine(
                    cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
                    "",
                    DAOMachine.TYPE_STRENGTH,
                    "",
                    false,
                    ""
                )
            } else {
                machine_key = cursor!!.getLong(cursor!!.getColumnIndex(MACHINE_KEY))
            }

            var value: IRecord? = null

            if (cursor!!.getInt(cursor!!.getColumnIndex(TYPE)) == DAOMachine.TYPE_STRENGTH) {
                value = Fonte(
                    date,
                    cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(SERIE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(REPETITION)),
                    cursor!!.getFloat(cursor!!.getColumnIndex(WEIGHT)),
                    lProfile,
                    cursor!!.getInt(cursor!!.getColumnIndex(UNIT)),
                    cursor!!.getString(cursor!!.getColumnIndex(NOTES)),
                    machine_key,
                    cursor!!.getString(cursor!!.getColumnIndex(TIME))
                )
            } else if (cursor!!.getInt(cursor!!.getColumnIndex(TYPE)) == DAOMachine.TYPE_STATIC) {
                value = StaticExercise(
                    date,
                    cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(SERIE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(SECONDS)),
                    cursor!!.getFloat(cursor!!.getColumnIndex(WEIGHT)),
                    lProfile,
                    cursor!!.getInt(cursor!!.getColumnIndex(UNIT)),
                    machine_key,
                    cursor!!.getString(cursor!!.getColumnIndex(TIME))
                )
            } else {
                value = Cardio(
                    date,
                    cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
                    cursor!!.getFloat(cursor!!.getColumnIndex(DISTANCE)),
                    cursor!!.getLong(cursor!!.getColumnIndex(DURATION)),
                    lProfile,
                    cursor!!.getString(cursor!!.getColumnIndex(TIME)),
                    cursor!!.getInt(cursor!!.getColumnIndex(DISTANCE_UNIT))
                )
            }

            value.id=(cursor!!.getLong(cursor!!.getColumnIndex(KEY)))
            return value
        } else {
            return null
        }
    }

    // Get all record for one Machine
    fun getAllRecordByMachines(pProfile: Profile, pMachines: String?): Cursor {
        return getAllRecordByMachines(pProfile, pMachines, -1)
    }

    fun getAllRecordByMachines(pProfile: Profile, pMachines: String?, pNbRecords: Int): Cursor {
        val mTop: String?
        if (pNbRecords == -1) mTop = ""
        else mTop = " LIMIT " + pNbRecords

        // Select All Query
        val selectQuery = ("SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachines + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.id
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop)

        // return value list
        return getRecordsListCursor(selectQuery)
    }

    // Getting All Records
    fun getAllRecordsByProfile(pProfile: Profile): Cursor {
        return getAllRecordsByProfile(pProfile, -1)
    }

    /**
     * @param pProfile   record associated to one profile
     * @param pNbRecords max number of records requested
     * @return pNbRecords number of records for a specified pProfile
     */
    private fun getAllRecordsByProfile(pProfile: Profile, pNbRecords: Int): Cursor {
        val mTop: String?
        if (pNbRecords == -1) mTop = ""
        else mTop = " LIMIT " + pNbRecords

        // Select All Query
        val selectQuery = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + PROFIL_KEY + "=" + pProfile.id +
                " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop

        // Return value list
        return getRecordsListCursor(selectQuery)
    }

    // Getting All Records
    private fun getRecordsListCursor(pRequest: String): Cursor {
        val db = this.readableDatabase

        // Select All Query

        // return value list
        return db!!.rawQuery(pRequest, null)
    }

    val allMachinesStrList: MutableList<String?>
        // Getting All Machines
        get() = getAllMachinesStrList(null)

    // Getting All Machines
    fun getAllMachinesStrList(pProfile: Profile?): MutableList<String?> {
        val db = this.readableDatabase
        this.cursor = null
        val selectQuery = if (pProfile == null) {
            ("SELECT DISTINCT " + EXERCISE + " FROM "
                + TABLE_NAME + " ORDER BY " + EXERCISE + " ASC")
        } else {
            ("SELECT DISTINCT " + EXERCISE + " FROM "
                + TABLE_NAME + "  WHERE " + PROFIL_KEY + "=" + pProfile.id + " ORDER BY " + EXERCISE + " ASC")
        }
        this.cursor = db!!.rawQuery(selectQuery, null)

        val size = cursor!!.getCount()

        val valueList: MutableList<String?> = ArrayList<String?>(size)

        // looping through all rows and adding to list
        if (cursor!!.moveToFirst()) {
            var i = 0
            do {
                valueList.add(cursor!!.getString(0))
                i++
            } while (cursor!!.moveToNext())
        }
        close()
        // return value list
        return valueList
    }

    // Getting All Machines
    open fun getAllMachines(pProfile: Profile): Array<String?> {
        val db = this.readableDatabase
        this.cursor = null

        // Select All Machines
        val selectQuery = ("SELECT DISTINCT " + EXERCISE + " FROM "
                + TABLE_NAME + "  WHERE " + PROFIL_KEY + "=" + pProfile.id + " ORDER BY " + EXERCISE + " ASC")
        this.cursor = db!!.rawQuery(selectQuery, null)

        val size = cursor!!.getCount()

        val valueList = arrayOfNulls<String>(size)

        // looping through all rows and adding to list
        if (cursor!!.moveToFirst()) {
            var i = 0
            do {
                val value = cursor!!.getString(0)
                valueList[i] = value
                i++
            } while (cursor!!.moveToNext())
        }
        close()
        // return value list
        return valueList
    }

    val allMachines: Array<String?>
        // Getting All Machines
        get() {
            val db = this.readableDatabase
            this.cursor = null

            // Select All Machines
            val selectQuery =
                ("SELECT DISTINCT  " + EXERCISE + " FROM "
                        + TABLE_NAME + " ORDER BY " + EXERCISE + " ASC")
            this.cursor = db!!.rawQuery(selectQuery, null)

            val size = cursor!!.getCount()

            val valueList = arrayOfNulls<String>(size)

            // looping through all rows and adding to list
            if (cursor!!.moveToFirst()) {
                var i = 0
                do {
                    val value = cursor!!.getString(0)
                    valueList[i] = value
                    i++
                } while (cursor!!.moveToNext())
            }
            close()
            // return value list
            return valueList
        }

    // Getting All Dates
    fun getAllDatesList(pProfile: Profile?, pMachine: Machine?): MutableList<String?> {
        val db = this.readableDatabase

        this.cursor = null

        // Select All Machines
        var selectQuery = "SELECT DISTINCT " + DATE + " FROM " + TABLE_NAME
        if (pMachine != null) {
            selectQuery += " WHERE " + MACHINE_KEY + "=" + pMachine.id
            if (pProfile != null) selectQuery += " AND " + PROFIL_KEY + "=" + pProfile.id // pProfile should never be null but depending on how the activity is resuming it happen. to be fixed
        } else {
            if (pProfile != null) selectQuery += " WHERE " + PROFIL_KEY + "=" + pProfile.id // pProfile should never be null but depending on how the activity is resuming it happen. to be fixed
        }
        selectQuery += " ORDER BY " + DATE + " DESC"

        this.cursor = db!!.rawQuery(selectQuery, null)
        val size = cursor!!.getCount()

        val valueList: MutableList<String?> = ArrayList<String?>(size)

        // looping through all rows and adding to list
        if (cursor!!.moveToFirst()) {
            do {
                var i = 0

                //String date;
                //date = mCursor.getString(0);
                // Change Date format
                //date = date.substring(0, 3) + "-" + date.substring(5, 6) + "-"
                var date: Date?
                try {
                    val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
                    date = dateFormat.parse(cursor!!.getString(0))
                } catch (e: ParseException) {
                    e.printStackTrace()
                    date = Date()
                }

                val dateFormat3 =
                    android.text.format.DateFormat.getDateFormat(mContext?.getApplicationContext())
                dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"))
                valueList.add(dateFormat3.format(date))
                i++
            } while (cursor!!.moveToNext())
        }

        close()

        // return value list
        return valueList
    }

    fun getTop3DatesRecords(pProfile: Profile?): Cursor? {
        var selectQuery: String? = null

        if (pProfile == null) return null

        selectQuery = ("SELECT * FROM " + TABLE_NAME
                + " WHERE " + PROFIL_KEY + "=" + pProfile.id
                + " AND " + DATE + " IN (SELECT DISTINCT " + DATE + " FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.id + " ORDER BY " + DATE + " DESC LIMIT 3)"
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC")

        return getRecordsListCursor(selectQuery)
    }

    // Getting Filtered records
    fun getFilteredRecords(pProfile: Profile, pMachine: String?, pDate: String?): Cursor {
        var lfilterMachine = true
        var lfilterDate = true
        var selectQuery: String? = null

        if (pMachine == null || pMachine.isEmpty() || pMachine == mContext?.getResources()
                !!.getText(R.string.all).toString()
        ) {
            lfilterMachine = false
        }

        if (pDate == null || pDate.isEmpty() || pDate == mContext!!.getResources()
                .getText(R.string.all).toString()
        ) {
            lfilterDate = false
        }

        if (lfilterMachine && lfilterDate) {
            selectQuery = ("SELECT * FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine
                    + "\" AND " + DATE + "=\"" + pDate
                    + "\" AND " + PROFIL_KEY + "=" + pProfile.id
                    + " ORDER BY " + DATE + " DESC," + KEY + " DESC")
        } else if (!lfilterMachine && lfilterDate) {
            selectQuery = ("SELECT * FROM " + TABLE_NAME
                    + " WHERE " + DATE + "=\"" + pDate
                    + "\" AND " + PROFIL_KEY + "=" + pProfile.id
                    + " ORDER BY " + DATE + " DESC," + KEY + " DESC")
        } else if (lfilterMachine) {
            selectQuery = ("SELECT * FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine
                    + "\" AND " + PROFIL_KEY + "=" + pProfile.id
                    + " ORDER BY " + DATE + " DESC," + KEY + " DESC")
        } else {
            selectQuery = ("SELECT * FROM " + TABLE_NAME
                    + " WHERE " + PROFIL_KEY + "=" + pProfile.id
                    + " ORDER BY " + DATE + " DESC," + KEY + " DESC")
        }

        // return value list
        return getRecordsListCursor(selectQuery)
    }

    /**
     * @return the last record for a profile p
     */
    fun getLastRecord(pProfile: Profile): IRecord? {
        val db = this.readableDatabase
        this.cursor = null
        var lReturn: IRecord? = null

        // Select All Machines
        /*
        String selectQuery = "SELECT " + KEY + " FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " AND " + DATE + "=(SELECT MAX(" + DATE + ") FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + ");";
*/
        val selectQuery = ("SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                + " WHERE " + PROFIL_KEY + "=" + pProfile.id)
        this.cursor = db!!.rawQuery(selectQuery, null)

        // looping through only the first rows.
        if (cursor!!.moveToFirst()) {
            try {
                val value = cursor!!.getLong(0)
                lReturn = getRecord(value)
            } catch (e: NumberFormatException) {
                lReturn = null // Return une valeur
            }
        }

        close()

        // return value list
        return lReturn
    }


    /**
     * @return the last record for a profile p
     */
    fun getLastExerciseRecord(machineID: Long, p: Profile?): IRecord? {
        val db = this.readableDatabase
        this.cursor = null
        var lReturn: IRecord? = null

        val selectQuery: String?
        if (p == null) {
            selectQuery = ("SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                    + " WHERE " + MACHINE_KEY + "=" + machineID)
        } else {
            selectQuery = ("SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                    + " WHERE " + MACHINE_KEY + "=" + machineID + " AND " + PROFIL_KEY + "=" + p.id)
        }
        this.cursor = db!!.rawQuery(selectQuery, null)

        // looping through only the first rows.
        if (cursor!!.moveToFirst()) {
            try {
                val value = cursor!!.getLong(0)
                lReturn = this.getRecord(value)
            } catch (e: NumberFormatException) {
                lReturn = null // Return une valeur
            }
        }

        close()

        // return value list
        return lReturn
    }

    // Get all record for one Machine
    fun getAllRecordByMachinesArray(pProfile: Profile, pMachines: String?): MutableList<IRecord?> {
        return getAllRecordByMachinesArray(pProfile, pMachines, -1)
    }

    private fun getAllRecordByMachinesArray(
        pProfile: Profile,
        pMachines: String?,
        pNbRecords: Int
    ): MutableList<IRecord?> {
        val mTop: String?
        if (pNbRecords == -1) mTop = ""
        else mTop = " LIMIT " + pNbRecords

        // Select All Query
        val selectQuery = ("SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachines + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.id
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop)

        // return value list
        return getRecordsList(selectQuery)
    }

    // Getting All Records
    private fun getRecordsList(pRequest: String): MutableList<IRecord?> {
        val valueList: MutableList<IRecord?> = ArrayList<IRecord?>()
        val db = this.readableDatabase

        // Select All Query
        this.cursor = null
        this.cursor = db!!.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (cursor!!.moveToFirst() && cursor!!.getCount() > 0) {
            do {
                //Get Date
                var date: Date?
                try {
                    val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
                    date =
                        dateFormat.parse(cursor!!.getString(cursor!!.getColumnIndex(DATE)))
                } catch (e: ParseException) {
                    e.printStackTrace()
                    date = Date()
                }

                //Get Profile
                val lDAOProfil = DAOProfil(mContext)
                val lProfile = lDAOProfil.getProfil(
                    cursor!!.getLong(
                        cursor!!.getColumnIndex(PROFIL_KEY)
                    )
                )

                var machine_key: Long = -1

                //Test if machine_key is properly fill. If not add it.
                val lDAOMachine = DAOMachine(mContext)
                if (cursor!!.getString(cursor!!.getColumnIndex(MACHINE_KEY)) == null) {
                    machine_key = lDAOMachine.addMachine(
                        cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)), "", cursor!!.getInt(
                            cursor!!.getColumnIndex(TYPE)
                        ), "", false, ""
                    )
                } else {
                    machine_key = cursor!!.getLong(cursor!!.getColumnIndex(MACHINE_KEY))
                }

                var value: IRecord? = null

                if (cursor!!.getInt(cursor!!.getColumnIndex(TYPE)) == DAOMachine.TYPE_STRENGTH) {
                    value = Fonte(
                        date,
                        cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
                        cursor!!.getInt(cursor!!.getColumnIndex(SERIE)),
                        cursor!!.getInt(cursor!!.getColumnIndex(REPETITION)),
                        cursor!!.getFloat(cursor!!.getColumnIndex(WEIGHT)),
                        lProfile,
                        cursor!!.getInt(cursor!!.getColumnIndex(UNIT)),
                        cursor!!.getString(cursor!!.getColumnIndex(NOTES)),
                        machine_key,
                        cursor!!.getString(cursor!!.getColumnIndex(TIME))
                    )
                } else if (cursor!!.getInt(cursor!!.getColumnIndex(TYPE)) == DAOMachine.TYPE_STATIC) {
                    value = StaticExercise(
                        date,
                        cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
                        cursor!!.getInt(cursor!!.getColumnIndex(SERIE)),
                        cursor!!.getInt(cursor!!.getColumnIndex(SECONDS)),
                        cursor!!.getFloat(cursor!!.getColumnIndex(WEIGHT)),
                        lProfile,
                        cursor!!.getInt(cursor!!.getColumnIndex(UNIT)),
                        machine_key,
                        cursor!!.getString(cursor!!.getColumnIndex(TIME))
                    )
                } else {
                    value = Cardio(
                        date,
                        cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
                        cursor!!.getFloat(cursor!!.getColumnIndex(DISTANCE)),
                        cursor!!.getLong(cursor!!.getColumnIndex(DURATION)),
                        lProfile,
                        cursor!!.getString(cursor!!.getColumnIndex(TIME)),
                        cursor!!.getInt(cursor!!.getColumnIndex(DISTANCE_UNIT))
                    )
                }

                value.id=(cursor!!.getLong(cursor!!.getColumnIndex(KEY)))

                // Adding value to list
                valueList.add(value)
            } while (cursor!!.moveToNext())
        }
        // return value list
        return valueList
    }

    // Updating single value
    fun updateRecord(m: IRecord): Int {
        val db = this.readableDatabase

        val value = ContentValues()

        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        value.put(DATE, dateFormat.format(m.date))
        value.put(EXERCISE, m.exercise)
        value.put(PROFIL_KEY, m.profilKey)
        value.put(TIME, m.time)
        value.put(TYPE, m.type)
        value.put(MACHINE_KEY, m.exerciseKey)

        // updating row
        return db!!.update(
            TABLE_NAME, value, KEY + " = ?",
            arrayOf<String>(m.id.toString())
        )
    }

    fun closeCursor() {
        if (this.cursor != null) cursor!!.close()
    }

    fun closeAll() {
        if (this.cursor != null) cursor!!.close()
        close()
    }

    companion object {
        // Contacts table name
        const val TABLE_NAME: String = "EFfontes"

        const val KEY: String = "_id"
        const val DATE: String = "date"
        const val TIME: String = "time"
        const val EXERCISE: String = "machine"
        const val PROFIL_KEY: String = "profil_id"
        const val MACHINE_KEY: String = "machine_id"
        const val NOTES: String = "notes"
        const val TYPE: String = "type"

        // Specific to BodyBuilding
        const val SERIE: String = "serie"
        const val REPETITION: String = "repetition"
        const val WEIGHT: String = "poids"
        const val UNIT: String = "unit" // 0:kg 1:lbs

        // Specific to Cardio
        const val DISTANCE: String = "distance"
        const val DURATION: String = "duration"
        const val DISTANCE_UNIT: String = "distance_unit" // 0:km 1:mi

        // Specific to STATIC
        const val SECONDS: String = "seconds"

        val TABLE_CREATE: String = ("CREATE TABLE " + TABLE_NAME
                + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE
                + " DATE, " + EXERCISE + " TEXT, " + SERIE + " INTEGER, "
                + REPETITION + " INTEGER, " + WEIGHT + " REAL, " + PROFIL_KEY
                + " INTEGER, " + UNIT + " INTEGER, " + NOTES + " TEXT, " + MACHINE_KEY
                + " INTEGER," + TIME + " TEXT," + DISTANCE + " REAL, " + DURATION + " TEXT, " + TYPE + " INTEGER, " + SECONDS + " INTEGER, " + DISTANCE_UNIT + " INTEGER);")

        val TABLE_DROP: String = ("DROP TABLE IF EXISTS "
                + TABLE_NAME + ";")
    }
}
