package com.easyfitness.DAO

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.easyfitness.utils.DateConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class DAOWeight(context: Context) : DAOBase(context) {
    private var mProfile: Profile? = null
    private var mCursor: Cursor? = null

    fun setProfil(pProfile: Profile?) {
        mProfile = pProfile
    }

    /**
     * @param pDate    date of the weight measure
     * @param pWeight  weight
     * @param pProfile profil associated with the measure
     */
    fun addWeight(pDate: Date, pWeight: Float, pProfile: Profile) {
        val db = this.writableDatabase

        val value = ContentValues()

        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))

        value.put(DATE, dateFormat.format(pDate))
        value.put(POIDS, pWeight)
        value.put(PROFIL_KEY, pProfile.id)

        db!!.insert(TABLE_NAME, null, value)
        db.close() // Closing database connection
    }

    // Getting single value
    private fun getMeasure(id: Long): ProfileWeight {
        val db = this.readableDatabase

        mCursor = null
        mCursor = db!!.query(
            TABLE_NAME,
            arrayOf<String>(KEY, DATE, POIDS, PROFIL_KEY),
            KEY + "=?",
            arrayOf<String>(id.toString()),
            null, null, null, null
        )
        if (mCursor != null) mCursor!!.moveToFirst()

        val date: Date?
        date = DateConverter.DBDateStrToDate(mCursor!!.getString(1))

        val value = ProfileWeight(
            mCursor!!.getLong(0),
            date,
            mCursor!!.getFloat(2),
            mCursor!!.getLong(3)
        )

        db.close()

        // return value
        return value
    }

    val lastMeasure: ProfileWeight
        // Getting single value
        get() {
            val db = this.readableDatabase

            mCursor = null
            mCursor = db!!.query(
                TABLE_NAME,
                arrayOf<String>(
                    KEY,
                    DATE,
                    POIDS,
                    PROFIL_KEY
                ),
                PROFIL_KEY + "=?",
                arrayOf<String>(mProfile!!.id.toString()),
                null,
                null,
                DATE + " desc, " + KEY + " desc",
                null
            )

            if (mCursor != null) mCursor!!.moveToFirst()

            var date: Date?
            try {
                val dateFormat =
                    SimpleDateFormat(DAOUtils.DATE_FORMAT)
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
                date = dateFormat.parse(mCursor!!.getString(1))
            } catch (e: ParseException) {
                e.printStackTrace()
                date = Date()
            }

            val value = ProfileWeight(
                mCursor!!.getLong(0),
                date,
                mCursor!!.getFloat(2),
                mCursor!!.getLong(3)
            )

            db.close()

            // return value
            return value
        }

    // Getting All Measures
    private fun getMeasuresList(pRequest: String): MutableList<ProfileWeight?> {
        val valueList: MutableList<ProfileWeight?> = ArrayList<ProfileWeight?>()

        // Select All Query
        val db = this.readableDatabase
        mCursor = null
        mCursor = db!!.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (mCursor!!.moveToFirst()) {
            do {
                var date: Date?
                try {
                    val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
                    date = dateFormat.parse(mCursor!!.getString(1))
                } catch (e: ParseException) {
                    e.printStackTrace()
                    date = Date()
                }

                val value = ProfileWeight(
                    mCursor!!.getLong(0),
                    date,
                    mCursor!!.getFloat(2),
                    mCursor!!.getLong(3)
                )

                // Adding value to list
                valueList.add(value)
            } while (mCursor!!.moveToNext())
        }

        // return value list
        return valueList
    }

    fun GetCursor(): Cursor? {
        return mCursor
    }

    // Getting All Measures
    fun getWeightList(pProfile: Profile): MutableList<ProfileWeight?> {
        // Select All Query
        val selectQuery =
            "SELECT * FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.id + " GROUP BY " + DATE + " ORDER BY date(" + DATE + ") DESC"

        // return value list
        return getMeasuresList(selectQuery)
    }

    // Updating single value
    fun updateMeasure(m: ProfileWeight): Int {
        val db = this.writableDatabase

        val value = ContentValues()
        value.put(DATE, m.date.toString())
        value.put(POIDS, m.weight)
        value.put(PROFIL_KEY, m.profilId)

        // updating row
        return db!!.update(
            TABLE_NAME, value, KEY + " = ?",
            arrayOf<String>(m.id.toString())
        )
    }

    // Deleting single Measure
    fun deleteMeasure(m: ProfileWeight) {
        deleteMeasure(m.id)
    }

    // Deleting single Measure
    fun deleteMeasure(id: Long) {
        val db = this.writableDatabase
        db!!.delete(
            TABLE_NAME, KEY + " = ?",
            arrayOf<String>(id.toString())
        )
    }

    val count: Int
        // Getting Profils Count
        get() {
            val countQuery = "SELECT  * FROM " + TABLE_NAME
            open()
            val db = this.readableDatabase
            val cursor = db!!.rawQuery(countQuery, null)

            val value = cursor.getCount()
            cursor.close()
            close()

            // return count
            return value
        }

    val allRecords: MutableList<ProfileWeight?>
        get() {
            val selectQuery = "SELECT * FROM " + TABLE_NAME
            return getMeasuresList(selectQuery)
        }

    fun populate() {
        val date = Date()
        val poids = 10

        for (i in 1..5) {
            date.setTime(date.getTime() + i * 1000 * 60 * 60 * 24 * 2)
            addWeight(date, i.toFloat(), mProfile!!)
        }
    }

    companion object {
        // Contacts table name
        const val TABLE_NAME: String = "EFweight"

        const val KEY: String = "_id"
        const val POIDS: String = "poids"
        const val DATE: String = "date"
        const val PROFIL_KEY: String = "profil_id"

        @JvmField
        val TABLE_CREATE: String =
            "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " DATE, " + POIDS + " REAL , " + PROFIL_KEY + " INTEGER);"

        val TABLE_DROP: String = "DROP TABLE IF EXISTS " + TABLE_NAME + ";"
    }
}


