package com.easyfitness.DAO.cardio

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import com.easyfitness.DAO.DAOBase
import com.easyfitness.DAO.DAOProfil
import com.easyfitness.DAO.DAOUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

class DAOOldCardio(context: Context) : DAOBase(context) {
    private var mCursor: Cursor? = null
    private var mContext: Context = context

    // Getting All Records
    private fun getRecordsList(pRequest: String): MutableList<OldCardio?> {
        val valueList: MutableList<OldCardio?> = ArrayList<OldCardio?>()
        val db = this.readableDatabase

        // Select All Query
        mCursor = null
        mCursor = db!!.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (mCursor!!.moveToFirst()) {
            do {
                //Get Date
                var date: Date?
                try {
                    date = SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(
                        mCursor!!.getString(
                            mCursor!!.getColumnIndex(
                                DATE
                            )
                        )
                    )
                } catch (e: ParseException) {
                    e.printStackTrace()
                    date = Date()
                }

                // Get Profile
                val lDAOProfil = DAOProfil(mContext)
                val lProfile = lDAOProfil.getProfil(
                    mCursor!!.getLong(
                        mCursor!!.getColumnIndex(
                            PROFIL_KEY
                        )
                    )
                )

                val value = OldCardio(
                    date,
                    mCursor!!.getString(mCursor!!.getColumnIndex(EXERCICE)),
                    mCursor!!.getFloat(mCursor!!.getColumnIndex(DISTANCE)),
                    mCursor!!.getLong(mCursor!!.getColumnIndex(DURATION)),
                    lProfile
                )

                value.id=(mCursor!!.getString(mCursor!!.getColumnIndex(KEY)).toLong())

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

    val allRecords: MutableList<OldCardio?>
        // Getting All Records
        get() {
            // Select All Query
            val selectQuery =
                ("SELECT  * FROM " + TABLE_NAME + " ORDER BY "
                        + KEY + " DESC")

            // return value list
            return getRecordsList(selectQuery)
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

    fun tableExists(): Boolean {
        var isExist = true
        val res: Cursor?

        val db = this.readableDatabase
        try {
            res = db!!.rawQuery("SELECT * FROM " + TABLE_NAME, null)
            res.close()
        } catch (e: SQLiteException) {
            isExist = false
        }
        return isExist
    }

    fun dropTable(): Boolean {
        val db = this.readableDatabase
        db!!.execSQL(TABLE_DROP)
        return true
    }

    companion object {
        // Contacts table name
        const val TABLE_NAME: String = "EFcardio"

        const val KEY: String = "_id"
        const val DATE: String = "date"
        const val EXERCICE: String = "exercice"
        const val DISTANCE: String = "distance"
        const val DURATION: String = "duration"
        const val PROFIL_KEY: String = "profil_id"
        const val NOTES: String = "notes"
        const val DISTANCE_UNIT: String = "distance_unit"
        const val VITESSE: String = "vitesse"

        val TABLE_CREATE: String = ("CREATE TABLE " + TABLE_NAME
                + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATE + " DATE, "
                + EXERCICE + " TEXT, "
                + DISTANCE + " FLOAT, "
                + DURATION + " INTEGER, "
                + PROFIL_KEY + " INTEGER, "
                + NOTES + " TEXT, "
                + DISTANCE_UNIT + " TEXT, "
                + VITESSE + " FLOAT);")

        val TABLE_DROP: String = "DROP TABLE IF EXISTS " + TABLE_NAME + ";"
    }
}
