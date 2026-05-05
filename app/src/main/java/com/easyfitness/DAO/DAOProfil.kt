package com.easyfitness.DAO

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure
import com.easyfitness.utils.DateConverter
import java.util.Date

class DAOProfil(private val context: Context) : DAOBase(context) {
    private var mCursor: Cursor? = null


    /**
     * @param m DBOProfil Profile a ajouter a la base
     */
    fun addProfil(m: Profile) {
        // Check if profil already exists
        val check = getProfil(m.name)
        if (check != null) return

        val db = this.writableDatabase

        val value = ContentValues()

        value.put(CREATIONDATE, DateConverter.dateToDBDateStr(Date()))
        value.put(NAME, m.name)
        value.put(BIRTHDAY, DateConverter.dateToDBDateStr(m.birthday))
        value.put(SIZE, m.size)
        value.put(PHOTO, m.photo)
        value.put(GENDER, m.gender)

        db!!.insert(TABLE_NAME, null, value)

        close()
    }

    /**
     * @param pName String Nom du profil a ajouter a la base
     */
    fun addProfil(pName: String?) {
        // Check if profil already exists
        val check = getProfil(pName)
        if (check != null) return

        val db = this.writableDatabase

        val value = ContentValues()

        value.put(CREATIONDATE, DateConverter.dateToDBDateStr(Date()))
        value.put(NAME, pName)
        //value.put(DAOProfil.BIRTHDAY, DateConverter.dateToDBDateStr(m.getBirthday()));
        //value.put(DAOProfil.SIZE, 0);
        try {
            db!!.insert(TABLE_NAME, null, value)
        } catch (e: Exception) {
            //safely ignore as profile already exist
            println("Safely ignored profil creation")
            //throw new RuntimeException(e);
        }
        close()
    }

    /**
     * @param id long id of the Profile
     */
    fun getProfil(id: Long): Profile? {
        val db = this.readableDatabase
        if (mCursor != null) mCursor!!.close()
        mCursor = null
        mCursor = db!!.query(
            TABLE_NAME,
            arrayOf<String>(KEY, CREATIONDATE, NAME, SIZE, BIRTHDAY, PHOTO, GENDER),
            KEY + "=?",
            arrayOf<String>(id.toString()),
            null, null, null, null
        )
        if (mCursor != null && mCursor!!.getCount() > 0) {
            mCursor!!.moveToFirst()

            val value = Profile(
                mCursor!!.getLong(mCursor!!.getColumnIndex(KEY)),
                DateConverter.DBDateStrToDate(
                    mCursor!!.getString(
                        mCursor!!.getColumnIndex(
                            CREATIONDATE
                        )
                    )
                ),
                mCursor!!.getString(mCursor!!.getColumnIndex(NAME)),
                mCursor!!.getInt(mCursor!!.getColumnIndex(SIZE)),
                if (mCursor!!.getString(mCursor!!.getColumnIndex(BIRTHDAY)) != null) DateConverter.DBDateStrToDate(
                    mCursor!!.getString(
                        mCursor!!.getColumnIndex(
                            BIRTHDAY
                        )
                    )
                ) else Date(0),
                mCursor!!.getString(mCursor!!.getColumnIndex(PHOTO)),
                mCursor!!.getInt(mCursor!!.getColumnIndex(GENDER))
            )
            mCursor!!.close()
            close()

            // return value
            return value
        } else {
            mCursor!!.close()
            close()
            return null
        }
    }

    /**
     * @param name String name of the Profile
     */
    fun getProfil(name: String?): Profile? {
        val db = this.readableDatabase
        if (mCursor != null) mCursor!!.close()
        mCursor = null
        mCursor = db!!.query(
            TABLE_NAME,
            arrayOf<String>(KEY, CREATIONDATE, NAME, SIZE, BIRTHDAY, PHOTO, GENDER),
            NAME + "=?",
            arrayOf<String?>(name),
            null, null, null, null
        )
        if (mCursor != null && mCursor!!.getCount() > 0) {
            mCursor!!.moveToFirst()

            val value = Profile(
                mCursor!!.getLong(mCursor!!.getColumnIndex(KEY)),
                DateConverter.DBDateStrToDate(
                    mCursor!!.getString(
                        mCursor!!.getColumnIndex(
                            CREATIONDATE
                        )
                    )
                ),
                mCursor!!.getString(mCursor!!.getColumnIndex(NAME)),
                mCursor!!.getInt(mCursor!!.getColumnIndex(SIZE)),
                if (mCursor!!.getString(mCursor!!.getColumnIndex(BIRTHDAY)) != null) DateConverter.DBDateStrToDate(
                    mCursor!!.getString(
                        mCursor!!.getColumnIndex(
                            BIRTHDAY
                        )
                    )
                ) else Date(0),
                mCursor!!.getString(mCursor!!.getColumnIndex(PHOTO)),
                mCursor!!.getInt(mCursor!!.getColumnIndex(GENDER))
            )

            mCursor!!.close()
            close()

            // return value
            return value
        } else {
            close()
            return null
        }
    }

    // Getting All Profils
    fun getProfilsList(pRequest: String): MutableList<Profile?> {
        val valueList: MutableList<Profile?> = ArrayList<Profile?>()

        // Select All Query
        val db = this.readableDatabase
        mCursor = null
        mCursor = db!!.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (mCursor!!.moveToFirst()) {
            do {
                val value = Profile(
                    mCursor!!.getLong(mCursor!!.getColumnIndex(KEY)),
                    DateConverter.DBDateStrToDate(
                        mCursor!!.getString(
                            mCursor!!.getColumnIndex(
                                CREATIONDATE
                            )
                        )
                    ),
                    mCursor!!.getString(mCursor!!.getColumnIndex(NAME)),
                    mCursor!!.getInt(mCursor!!.getColumnIndex(SIZE)),
                    if (mCursor!!.getString(mCursor!!.getColumnIndex(BIRTHDAY)) != null) DateConverter.DBDateStrToDate(
                        mCursor!!.getString(
                            mCursor!!.getColumnIndex(
                                BIRTHDAY
                            )
                        )
                    ) else Date(0),
                    mCursor!!.getString(mCursor!!.getColumnIndex(PHOTO)),
                    mCursor!!.getInt(mCursor!!.getColumnIndex(GENDER))
                )

                // Adding value to list
                valueList.add(value)
            } while (mCursor!!.moveToNext())
        }

        close()
        // return value list
        return valueList
    }

    fun GetCursor(): Cursor? {
        return mCursor
    }

    val allProfils: MutableList<Profile?>
        // Getting All Profils
        get() {
            // Select All Query
            val selectQuery =
                "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + KEY + " DESC"

            // return value list
            return getProfilsList(selectQuery)
        }

    val top10Profils: MutableList<Profile?>
        // Getting Top 10 Profils
        get() {
            // Select All Query
            val selectQuery =
                "SELECT TOP 10 * FROM " + TABLE_NAME + " ORDER BY " + KEY + " DESC"

            // return value list
            return getProfilsList(selectQuery)
        }

    val allProfil: Array<String?>
        // Getting All Machines
        get() {
            val db = this.readableDatabase
//            mCursor = null

            // Select All Machines
            val selectQuery =
                "SELECT DISTINCT  " + NAME + " FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC"
            val mCursor = db!!.rawQuery(selectQuery, null)

            val size = mCursor!!.getCount()

            val valueList = arrayOfNulls<String>(size)

            // looping through all rows and adding to list
            if (mCursor!!.moveToFirst()) {
                var i = 0
                do {
                    val value = mCursor!!.getString(0)
                    valueList[i] = value
                    i++
                } while (mCursor!!.moveToNext())
            }

            close()

            // return value list
            return valueList
        }

    val lastProfil: Profile?
        // Getting last record
        get() {
            val db = this.readableDatabase
//            mCursor = null

            // Select All Machines
            val selectQuery =
                "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
            val mCursor = db!!.rawQuery(selectQuery, null)

            // looping through all rows and adding to list
            mCursor!!.moveToFirst()
            val value = mCursor!!.getString(0).toLong()

            val prof = this.getProfil(value)
            mCursor!!.close()
            close()

            // return value list
            return prof
        }

    // Updating single value
    fun updateProfile(m: Profile?): Int {
        val db = this.writableDatabase

        val value = ContentValues()
        value.put(CREATIONDATE, DateConverter.dateToDBDateStr(m!!.creationDate))
        value.put(NAME, m.name)
        value.put(BIRTHDAY, DateConverter.dateToDBDateStr(m.birthday))
        value.put(SIZE, m.size)
        value.put(PHOTO, m.photo)
        value.put(GENDER, m.gender)

        // updating row
        return db!!.update(
            TABLE_NAME, value, KEY + " = ?",
            arrayOf<String>(m.id.toString())
        )
    }

    // Deleting single Profile
    fun deleteProfil(m: Profile) {
        deleteProfil(m.id)
    }

    // Deleting single Profile
    fun deleteProfil(id: Long) {
        open()

        // Supprime les enregistrements de poids
        val mWeightDb = DAOWeight(context)
        val valueList = mWeightDb.getWeightList(getProfil(id)!!)
        for (i in valueList.indices) {
            mWeightDb.deleteMeasure(valueList.get(i)!!.id)
        }

        // Supprime les enregistrements de measure de body
        val mBodyDb = DAOBodyMeasure(context)
        val bodyMeasuresList = mBodyDb.getBodyMeasuresList(getProfil(id))
        for (i in bodyMeasuresList!!.indices) {
            mBodyDb.deleteMeasure(bodyMeasuresList.get(i)!!.id)
        }

        // Supprime le profile
        val db = this.writableDatabase
        db!!.delete(
            TABLE_NAME, KEY + " = ?",
            arrayOf<String>(id.toString())
        )

        close()
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

    /* DEBUG ONLY */
    fun populate() {
        val date = Date()
        val dateBirthday = DateConverter.getNewDate()
        var m = Profile(0, date, "Champignon", 120, dateBirthday, null, 0)
        this.addProfil(m)
        m = Profile(0, date, "Musclor", 150, dateBirthday, null, 0)
        this.addProfil(m)
    }

    companion object {
        // Contacts table name
        const val TABLE_NAME: String = "EFprofil"

        const val KEY: String = "_id"
        const val NAME: String = "name"
        const val CREATIONDATE: String = "creationdate"
        const val SIZE: String = "size"
        const val BIRTHDAY: String = "birthday"
        const val PHOTO: String = "photo"
        const val GENDER: String = "gender"

        val TABLE_CREATE: String =
            "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CREATIONDATE + " DATE, " + NAME + " TEXT, " + SIZE + " INTEGER, " + BIRTHDAY + " DATE, " + PHOTO + " TEXT, " + GENDER + " INTEGER);"

        val TABLE_DROP: String = "DROP TABLE IF EXISTS " + TABLE_NAME + ";"
    }
}
