package com.easyfitness.DAO.bodymeasures

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.easyfitness.DAO.DAOBase
import com.easyfitness.DAO.DAOUtils
import com.easyfitness.DAO.Profile
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class DAOBodyMeasure(context: Context) : DAOBase(context) {
    private val mProfile: Profile? = null
    var cursor: Cursor? = null
        private set

    /**
     * @param pDate           date of the weight measure
     * @param pBodymeasure_id id of the body part
     * @param pMeasure        body measure
     * @param pProfileID      profil associated with the measure
     */
    fun addBodyMeasure(pDate: Date, pBodymeasure_id: Long, pMeasure: Float, pProfileID: Long) {
        val db = this.writableDatabase

        val value = ContentValues()

        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)

        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))

        value.put(DATE, dateFormat.format(pDate))
        value.put(BODYPART_ID, pBodymeasure_id)
        value.put(MEASURE, pMeasure)
        value.put(PROFIL_KEY, pProfileID)

        db!!.insert(TABLE_NAME, null, value)
        db.close() // Closing database connection
    }

    // Getting single value
    private fun getMeasure(id: Long): BodyMeasure {
        val db = this.readableDatabase

        this.cursor = null
        this.cursor = db!!.query(
            TABLE_NAME,
            arrayOf<String>(KEY, DATE, BODYPART_ID, MEASURE, PROFIL_KEY),
            KEY + "=?",
            arrayOf<String>(id.toString()),
            null, null, null, null
        )
        if (this.cursor != null) cursor!!.moveToFirst()

        var date: Date?
        try {
            val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
            date = dateFormat.parse(cursor!!.getString(cursor!!.getColumnIndex(DATE)))
        } catch (e: ParseException) {
            e.printStackTrace()
            date = Date()
        }

        val value = BodyMeasure(
            cursor!!.getLong(cursor!!.getColumnIndex(KEY)),
            date,
            cursor!!.getInt(cursor!!.getColumnIndex(BODYPART_ID)),
            cursor!!.getFloat(cursor!!.getColumnIndex(MEASURE)),
            cursor!!.getLong(cursor!!.getColumnIndex(PROFIL_KEY))
        )

        db.close()

        // return value
        return value
    }

    // Getting All Measures
    private fun getMeasuresList(pRequest: String): MutableList<BodyMeasure?> {
        val valueList: MutableList<BodyMeasure?> = ArrayList<BodyMeasure?>()

        // Select All Query
        val db = this.readableDatabase
        this.cursor = null
        this.cursor = db!!.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (cursor!!.moveToFirst()) {
            do {
                var date: Date?
                try {
                    val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
                    date = dateFormat.parse(cursor!!.getString(1))
                } catch (e: ParseException) {
                    e.printStackTrace()
                    date = Date()
                }

                val value = BodyMeasure(
                    cursor!!.getLong(cursor!!.getColumnIndex(KEY)),
                    date,
                    cursor!!.getInt(cursor!!.getColumnIndex(BODYPART_ID)),
                    cursor!!.getFloat(cursor!!.getColumnIndex(MEASURE)),
                    cursor!!.getLong(cursor!!.getColumnIndex(PROFIL_KEY))
                )

                // Adding value to list
                valueList.add(value)
            } while (cursor!!.moveToNext())
        }

        // return value list
        return valueList
    }

    /**
     * Getting All Measures associated to a Body part for a specific Profile
     *
     * @param pBodyPartID
     * @param pProfile
     * @return List<BodyMeasure>
    </BodyMeasure> */
    fun getBodyPartMeasuresList(pBodyPartID: Long, pProfile: Profile): MutableList<BodyMeasure?> {
        // Select All Query
        val selectQuery =
            "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.id + " GROUP BY " + DATE + " ORDER BY date(" + DATE + ") DESC"

        // return value list
        return getMeasuresList(selectQuery)
    }

    /**
     * Getting All Measures associated to a Body part for a specific Profile
     *
     * @param pBodyPartID
     * @param pProfile
     * @return List<BodyMeasure>
    </BodyMeasure> */
    fun getBodyPartMeasuresListTop4(
        pBodyPartID: Long,
        pProfile: Profile?
    ): MutableList<BodyMeasure?>? {
        if (pProfile == null) return null

        // Select All Query
        val selectQuery =
            "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.id + " GROUP BY " + DATE + " ORDER BY date(" + DATE + ") DESC LIMIT 4;"

        // return value list
        return getMeasuresList(selectQuery)
    }

    /**
     * Getting All Measures for a specific Profile
     *
     * @param pProfile
     * @return List<BodyMeasure>
    </BodyMeasure> */
    fun getBodyMeasuresList(pProfile: Profile?): MutableList<BodyMeasure?>? {
        if (pProfile == null) return null

        // Select All Query
        val selectQuery =
            "SELECT * FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.id + " ORDER BY date(" + DATE + ") DESC"

        // return value list
        return getMeasuresList(selectQuery)
    }

    /**
     * Getting All Measures associated to a Body part for a specific Profile
     *
     * @param pBodyPartID
     * @param pProfile
     * @return List<BodyMeasure>
    </BodyMeasure> */
    fun getLastBodyMeasures(pBodyPartID: Long, pProfile: Profile): BodyMeasure? {
        // Select All Query
        val selectQuery =
            "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.id + " GROUP BY " + DATE + " ORDER BY date(" + DATE + ") DESC"

        val array = getMeasuresList(selectQuery)
        if (array.size <= 0) {
            return null
        }

        // return value list
        return getMeasuresList(selectQuery).get(0)
    }

    // Updating single value
    fun updateMeasure(m: BodyMeasure): Int {
        val db = this.writableDatabase

        val value = ContentValues()
        val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        val dateString = dateFormat.format(m.date)
        value.put(DATE, dateString)
        value.put(BODYPART_ID, m.bodyPartID)
        value.put(MEASURE, m.bodyMeasure)
        value.put(PROFIL_KEY, m.profileID)

        // updating row
        return db!!.update(
            TABLE_NAME, value, KEY + " = ?",
            arrayOf<String>(m.id.toString())
        )
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
            val countQuery = "SELECT * FROM " + TABLE_NAME
            open()
            val db = this.readableDatabase
            val cursor = db!!.rawQuery(countQuery, null)

            val value = cursor.getCount()
            cursor.close()
            close()

            // return count
            return value
        }

    fun populate() {
        val date = Date()
        val poids = 10

        for (i in 1..5) {
            date.setTime(date.getTime() + i * 1000 * 60 * 60 * 24 * 2)
            //addBodyMeasure(date, (float) i, mProfile);
        }
    }

    companion object {
        // Contacts table name
        const val TABLE_NAME: String = "EFbodymeasures"

        const val KEY: String = "_id"
        const val BODYPART_ID: String = "bodypart_id"
        const val MEASURE: String = "mesure"
        const val DATE: String = "date"
        const val UNIT: String = "unit"
        const val PROFIL_KEY: String = "profil_id"

        val TABLE_CREATE: String =
            "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " DATE, " + BODYPART_ID + " INTEGER, " + MEASURE + " REAL , " + PROFIL_KEY + " INTEGER, " + UNIT + " INTEGER);"

        val TABLE_DROP: String = "DROP TABLE IF EXISTS " + TABLE_NAME + ";"
    }
}


