package com.easyfitness.DAO

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class DAOMachine(context: Context?) : DAOBase(context) {
    private val mProfile: Profile? = null
    var cursor: Cursor? = null
        private set

    /*
   public void setProfile(Profile pProfile) {
       mProfile = pProfile;
   }
*/
    /**
     * @param pName        le Record a ajouter a la base
     * @param pDescription
     * @param pType
     */
    fun addMachine(
        pName: String?,
        pDescription: String?,
        pType: Int,
        pPicture: String?,
        pFav: Boolean,
        pBodyParts: String?
    ): Long {
        var new_id: Long = -1

        val value = ContentValues()

        value.put(NAME, pName)
        value.put(DESCRIPTION, pDescription)
        value.put(TYPE, pType)
        value.put(PICTURE, pPicture)
        value.put(FAVORITES, pFav)
        value.put(BODYPARTS, pBodyParts)

        val db = this.getWritableDatabase()
        new_id = db.insert(TABLE_NAME, null, value)
        close()

        return new_id
    }

    // Getting single value
    fun getMachine(id: Long): Machine? {
        val db = this.getReadableDatabase()
        this.cursor = null
        this.cursor = db.query(
            TABLE_NAME,
            arrayOf<String>(KEY, NAME, DESCRIPTION, TYPE, BODYPARTS, PICTURE, FAVORITES),
            KEY + "=?",
            arrayOf<String>(id.toString()),
            null,
            null,
            null,
            null
        )
        if (this.cursor != null) cursor!!.moveToFirst()

        if (cursor!!.getCount() == 0) return null

        val value = Machine(
            cursor!!.getString(1),
            cursor!!.getString(2),
            cursor!!.getInt(3),
            cursor!!.getString(4),
            cursor!!.getString(5),
            cursor!!.getInt(6) == 1
        )

        value.id = cursor!!.getLong(0)
        // return value
        cursor!!.close()
        close()
        return value
    }

    // Getting single value
    fun getMachine(pName: String?): Machine? {
        val db = this.getReadableDatabase()
        this.cursor = null
        this.cursor = db.query(
            TABLE_NAME,
            arrayOf<String>(KEY, NAME, DESCRIPTION, TYPE, BODYPARTS, PICTURE, FAVORITES),
            NAME + "=?",
            arrayOf<String?>(pName),
            null,
            null,
            null,
            null
        )
        if (this.cursor != null) cursor!!.moveToFirst()

        if (cursor!!.getCount() == 0) return null

        val value = Machine(
            cursor!!.getString(1),
            cursor!!.getString(2),
            cursor!!.getInt(3),
            cursor!!.getString(4),
            cursor!!.getString(5),
            cursor!!.getInt(6) == 1
        )

        value.id = cursor!!.getLong(0)
        // return value
        cursor!!.close()
        close()
        return value
    }

    fun machineExists(name: String?): Boolean {
        val lMach = getMachine(name)
        return lMach != null
    }

    // Getting All Records
    private fun getMachineList(pRequest: String): java.util.ArrayList<Machine?> {
        val valueList = java.util.ArrayList<Machine?>()
        val db = this.getReadableDatabase()

        // Select All Query
        this.cursor = null
        this.cursor = db.rawQuery(pRequest, null)

        // looping through all rows and adding to list
        if (cursor!!.moveToFirst()) {
            do {
                val value = Machine(
                    cursor!!.getString(1),
                    cursor!!.getString(2),
                    cursor!!.getInt(3),
                    cursor!!.getString(4),
                    cursor!!.getString(5),
                    cursor!!.getInt(6) == 1
                )

                value.id = cursor!!.getLong(0)

                // Adding value to list
                valueList.add(value)
            } while (cursor!!.moveToNext())
        }
        // return value list
        return valueList
    }

    // Getting All Records
    private fun getMachineListCursor(pRequest: String): Cursor {
        val db = this.getReadableDatabase()

        // Select All Query
        return db.rawQuery(pRequest, null)
    }

    fun closeCursor() {
        cursor!!.close()
    }

    val allMachines: Cursor
        /**
         * @return List of Machine object ordered by Favorite and Name
         */
        get() {
            // Select All Query
            val selectQuery =
                ("SELECT  * FROM " + TABLE_NAME + " ORDER BY "
                        + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC")

            // return value list
            return getMachineListCursor(selectQuery)
        }

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    fun getAllMachines(type: Int): Cursor {
        // Select All Query
        var selectQuery = ""
        selectQuery = ("SELECT  * FROM " + TABLE_NAME + " WHERE " + TYPE + "=" + type + " ORDER BY "
                + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC")

        // return value list
        return getMachineListCursor(selectQuery)
    }

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    fun getFilteredMachines(filterString: CharSequence?): Cursor {
        // Select All Query
        // like '%"+inputText+"%'";
        val selectQuery =
            ("SELECT  * FROM " + TABLE_NAME + " WHERE " + NAME + " LIKE " + "'%" + filterString + "%' " + " ORDER BY "
                    + FAVORITES + " DESC," + NAME + " ASC")
        // return value list
        return getMachineListCursor(selectQuery)
    }


    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    fun deleteAllEmptyExercises() {
        val db = this.getWritableDatabase()
        db.delete(
            TABLE_NAME, NAME + " = ?",
            arrayOf<String>("")
        )
        db.close()
    }

    val allMachinesArray: ArrayList<Machine?>
        /**
         * @return List of Machine object ordered by Favorite and Name
         */
        get() {
// Select All Query
            val selectQuery =
                ("SELECT  * FROM " + TABLE_NAME + " ORDER BY "
                        + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC")

            // return value list
            return getMachineList(selectQuery)
        }

    /**
     * @param idList List of Machine IDs to be return
     * @return List of Machine object ordered by Favorite and Name
     */
    fun getAllMachines(idList: MutableList<Long?>): MutableList<Machine?> {
        var ids = idList.toString()
        ids = ids.replace('[', '(')
        ids = ids.replace(']', ')')

        // Select All Query
        val selectQuery =
            ("SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY + " in " + ids + " ORDER BY "
                    + FAVORITES + " DESC," + NAME + " COLLATE NOCASE ASC")

        // return value list
        return getMachineList(selectQuery)
    }

    val allMachinesName: Array<String?>
        // Getting All Machines
        get() {
            val db = this.getReadableDatabase()
            this.cursor = null

            // Select All Machines
            val selectQuery =
                ("SELECT DISTINCT  " + NAME + " FROM "
                        + TABLE_NAME + " ORDER BY " + NAME + " COLLATE NOCASE ASC")
            this.cursor = db.rawQuery(selectQuery, null)

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
            cursor!!.close()
            close()
            // return value list
            return valueList
        }

    // Updating single value
    fun updateMachine(m: Machine?): Int {
        val db = this.getWritableDatabase()

        val value = ContentValues()
        value.put(NAME, m?.name)
        value.put(DESCRIPTION, m?.description)
        value.put(TYPE, m?.type)
        value.put(BODYPARTS, m?.bodyParts)
        value.put(PICTURE, m?.picture)
        if (m?.favorite ==true) value.put(FAVORITES, 1)
        else
            value.put(FAVORITES, 0)

        // updating row
        return db.update(
            TABLE_NAME, value, KEY + " = ?",
            arrayOf<String>(m?.id.toString())
        )
    }

    // Deleting single Record
    fun delete(m: Machine?) {
        if (m != null) {
            val db = this.getWritableDatabase()
            db.delete(
                TABLE_NAME, KEY + " = ?",
                arrayOf<String>(m.id.toString())
            )
            db.close()
        }
    }

    // Deleting single Record
    fun delete(id: Long) {
        val db = this.getWritableDatabase()
        db.delete(TABLE_NAME, KEY + " = ?", arrayOf<String>(id.toString()))
        db.close()
    }

    val count: Int
        // Getting Profils Count
        get() {
            val countQuery = "SELECT  * FROM " + TABLE_NAME
            open()
            val db = this.getReadableDatabase()
            val cursor = db.rawQuery(countQuery, null)

            val value = cursor.getCount()

            cursor.close()
            close()

            // return count
            return value
        }

    fun populate() {
        addMachine("Dev Couche", "Developper couche : blabla ", TYPE_STRENGTH, "", true, "")
        addMachine("Biceps", "Developper couche : blabla ", TYPE_STRENGTH, "", false, "")
    }

    companion object {
        // Contacts table name
        const val TABLE_NAME: String = "EFmachines"

        const val KEY: String = "_id"
        const val NAME: String = "name"
        const val DESCRIPTION: String = "description"
        const val TYPE: String = "type"
        const val PICTURE: String = "picture"
        const val BODYPARTS: String = "bodyparts"
        const val FAVORITES: String =
            "favorites" // DEPRECATED - Specific DataBase created for this.


        const val TYPE_STRENGTH: Int = 0
        const val TYPE_CARDIO: Int = 1
        const val TYPE_STATIC: Int = 2

        @JvmField
        val TABLE_CREATE_5: String = ("CREATE TABLE " + TABLE_NAME
                + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
                + " TEXT, " + DESCRIPTION + " TEXT, " + TYPE + " INTEGER);")

        @JvmField
        val TABLE_CREATE: String = ("CREATE TABLE " + TABLE_NAME
                + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
                + " TEXT, " + DESCRIPTION + " TEXT, " + TYPE + " INTEGER, " + BODYPARTS + " TEXT, " + PICTURE + " TEXT, " + FAVORITES + " INTEGER);") //", " + PICTURE_RES + " INTEGER);";

        val TABLE_DROP: String = ("DROP TABLE IF EXISTS "
                + TABLE_NAME + ";")
    }
}
