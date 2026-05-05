package com.easyfitness.DAO.bodymeasures

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.easyfitness.DAO.DAOBase

class DAOBodyPart(context: Context) : DAOBase(context) {
    var cursor: Cursor? = null
        private set

    fun add(
        pBodyPartId: Int,
        pCustomName: String?,
        pCustomPicture: String?,
        pDisplay: Int,
        pType: Int
    ): Long {
        var new_id: Long = -1
        val db = this.writableDatabase

        val value = ContentValues()

        value.put(BODYPART_RESID, pBodyPartId)
        value.put(CUSTOM_NAME, pCustomName)
        value.put(CUSTOM_PICTURE, pCustomPicture)
        value.put(DISPLAY_ORDER, pDisplay)
        value.put(TYPE, pType)

        new_id = db!!.insert(TABLE_NAME, null, value)
        db.close() // Closing database connection
        return new_id
    }

    // Getting single value
    fun getBodyPart(id: Long): BodyPart? {
        val db = this.writableDatabase

        this.cursor = null
        this.cursor = db!!.query(
            TABLE_NAME,
            arrayOf<String>(KEY, BODYPART_RESID, CUSTOM_NAME, CUSTOM_PICTURE, DISPLAY_ORDER, TYPE),
            KEY + "=?",
            arrayOf<String>(id.toString()),
            null, null, null, null
        )
        var value: BodyPart? = null
        if (this.cursor != null && cursor!!.getCount() != 0) {
            cursor!!.moveToFirst()

            value = BodyPart(
                cursor!!.getLong(cursor!!.getColumnIndex(KEY)),
                cursor!!.getInt(cursor!!.getColumnIndex(BODYPART_RESID)),
                cursor!!.getString(cursor!!.getColumnIndex(CUSTOM_NAME)),
                cursor!!.getString(cursor!!.getColumnIndex(CUSTOM_PICTURE)),
                cursor!!.getInt(cursor!!.getColumnIndex(DISPLAY_ORDER)),
                cursor!!.getInt(cursor!!.getColumnIndex(TYPE))
            )
        }

        db.close()

        // return value
        return value
    }

    fun getBodyPartfromBodyPartKey(bodyPartKey: Long): BodyPart? {
        val db = this.writableDatabase

        this.cursor = null
        this.cursor = db!!.query(
            TABLE_NAME,
            arrayOf<String>(KEY, BODYPART_RESID, CUSTOM_NAME, CUSTOM_PICTURE, DISPLAY_ORDER, TYPE),
            BODYPART_RESID + "=?",
            arrayOf<String>(bodyPartKey.toString()),
            null, null, null, null
        )
        var value: BodyPart? = null
        if (this.cursor != null && cursor!!.getCount() != 0) {
            cursor!!.moveToFirst()

            value = BodyPart(
                cursor!!.getLong(cursor!!.getColumnIndex(KEY)),
                cursor!!.getInt(cursor!!.getColumnIndex(BODYPART_RESID)),
                cursor!!.getString(cursor!!.getColumnIndex(CUSTOM_NAME)),
                cursor!!.getString(cursor!!.getColumnIndex(CUSTOM_PICTURE)),
                cursor!!.getInt(cursor!!.getColumnIndex(DISPLAY_ORDER)),
                cursor!!.getInt(cursor!!.getColumnIndex(TYPE))
            )
        }

        db.close()

        // return value
        return value
    }

    val list: MutableList<BodyPart?>
        // Getting All Measures
        get() = getList("SELECT * FROM " + TABLE_NAME + " ORDER BY " + DISPLAY_ORDER + " ASC")

    val musclesList: MutableList<BodyPart?>
        // Getting All Measures
        get() = getList("SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE + "=" + BodyPartExtensions.TYPE_MUSCLE + " ORDER BY " + DISPLAY_ORDER + " ASC")

    // Getting All Measures
    private fun getList(pRequest: String): MutableList<BodyPart?> {
        val valueList: MutableList<BodyPart?> = ArrayList<BodyPart?>()

        // Select All Query
        val db = this.readableDatabase
        this.cursor = null
        this.cursor = db!!.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (cursor!!.moveToFirst()) {
            do {
                val value = BodyPart(
                    cursor!!.getLong(cursor!!.getColumnIndex(KEY)),
                    cursor!!.getInt(cursor!!.getColumnIndex(BODYPART_RESID)),
                    cursor!!.getString(cursor!!.getColumnIndex(CUSTOM_NAME)),
                    cursor!!.getString(cursor!!.getColumnIndex(CUSTOM_PICTURE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(DISPLAY_ORDER)),
                    cursor!!.getInt(cursor!!.getColumnIndex(TYPE))
                )

                // Adding value to list
                valueList.add(value)
            } while (cursor!!.moveToNext())
        }

        // return value list
        return valueList
    }

    // Updating single value
    fun update(m: BodyPart): Int {
        val db = this.writableDatabase

        val value = ContentValues()
        value.put(BODYPART_RESID, m.bodyPartResKey)
        value.put(CUSTOM_NAME, m.customName)
        value.put(CUSTOM_PICTURE, m.customPicture)
        value.put(DISPLAY_ORDER, m.displayOrder)
        value.put(TYPE, m.type)

        // updating row
        return db!!.update(
            TABLE_NAME, value, KEY + " = ?",
            arrayOf<String>(m.id.toString())
        )
    }

    // Deleting single Measure
    fun delete(id: Long) {
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

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    fun deleteAllEmptyBodyPart() {
        val db = this.writableDatabase
        db!!.delete(
            TABLE_NAME, BODYPART_RESID + "=? " + " AND " + CUSTOM_NAME + "=?",
            arrayOf<String>("-1", "")
        )
        db.close()
    }

    companion object {
        // Contacts table name
        const val TABLE_NAME: String = "EFbodyparts"

        const val KEY: String = "_id"
        const val BODYPART_RESID: String = "bodypart_id"
        const val CUSTOM_NAME: String = "custom_name"
        const val CUSTOM_PICTURE: String = "custom_picture"
        const val DISPLAY_ORDER: String = "display_order"
        const val TYPE: String = "type" // Muscles or Body weight

        val TABLE_CREATE: String =
            "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + BODYPART_RESID + " INTEGER, " + CUSTOM_NAME + " STRING, " + CUSTOM_PICTURE + " STRING, " + DISPLAY_ORDER + " INTEGER, " + TYPE + " INTEGER);"

        val TABLE_DROP: String = "DROP TABLE IF EXISTS " + TABLE_NAME + ";"
    }
}


