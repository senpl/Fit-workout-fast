package com.easyfitness.DAO

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.easyfitness.DAO.DAOExerciseInProgram.Companion.addInitialExercise
import com.easyfitness.DAO.DAOProgram.Companion.addInitialProgram
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure
import com.easyfitness.DAO.bodymeasures.DAOBodyPart
import com.easyfitness.utils.UnitConverter
import java.io.File

class DatabaseHelper private constructor(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DAORecord.Companion.TABLE_CREATE) // Covers Fonte and Cardio and Static
        db.execSQL(DAOProfil.Companion.TABLE_CREATE)
        db.execSQL(DAOWeight.TABLE_CREATE)
        db.execSQL(DAOMachine.TABLE_CREATE)
        db.execSQL(DAOBodyMeasure.Companion.TABLE_CREATE)
        db.execSQL(DAOBodyPart.Companion.TABLE_CREATE)
        initBodyPartTable(db)
        db.execSQL(DAOProgram.TABLE_CREATE)
        db.execSQL(DAOExerciseInProgram.TABLE_CREATE)
        val defaultProgramName = "training program default"
        addInitialProgram(db, defaultProgramName)
        addExampleExercises(db)
    }

    override fun onUpgrade(
        db: SQLiteDatabase, oldVersion: Int,
        newVersion: Int
    ) {
        var upgradeTo = oldVersion + 1
        while (upgradeTo <= newVersion) {
            when (upgradeTo) {
                1, 2 -> db.execSQL(DAORecord.Companion.TABLE_CREATE)
                3 -> {
                    db.execSQL(DAORecord.Companion.TABLE_DROP)
                    db.execSQL(DAORecord.Companion.TABLE_CREATE)
                }

                4 -> {
//                    db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.NOTES + " TEXT")
//                    db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.UNIT + " INTEGER DEFAULT 0")
                }

                5 -> {
//                    db.execSQL(DAOMachine.TABLE_CREATE_5)
//                    db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.MACHINE_KEY + " INTEGER")
                }

                6 -> if (!isFieldExist(
                        db,
                        DAOMachine.TABLE_NAME,
                        DAOMachine.BODYPARTS
                    )
                )  // Easyfitness 0.9 : Probleme d'upgrade
                    db.execSQL("ALTER TABLE " + DAOMachine.TABLE_NAME + " ADD COLUMN " + DAOMachine.BODYPARTS + " TEXT")

                7 -> db.execSQL("ALTER TABLE " + DAOMachine.TABLE_NAME + " ADD COLUMN " + DAOMachine.PICTURE + " TEXT")
//                8 -> db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.TIME + " TEXT")
                9 -> db.execSQL(DAOBodyMeasure.Companion.TABLE_CREATE)
                10 -> db.execSQL("ALTER TABLE " + DAOMachine.TABLE_NAME + " ADD COLUMN " + DAOMachine.FAVORITES + " INTEGER")
//                11 -> {
//                    // Renomme la table FONTE en table temporaire
//                    db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " RENAME TO tmp_table_name")
//                    // Cree la nouvelle table FONTE
//                    db.execSQL(DAOFonte.TABLE_CREATE)
//                    // Copie les infos de l'ancienne vers la nouvelle
//                    db.execSQL("INSERT INTO " + DAOFonte.TABLE_NAME + " SELECT * FROM tmp_table_name")
//                }

                12 ->                     // Delete old table table
                    db.execSQL("DROP TABLE IF EXISTS tmp_table_name")

                13 -> {
                    // Update profile database
                    db.execSQL("ALTER TABLE " + DAOProfil.Companion.TABLE_NAME + " ADD COLUMN " + DAOProfil.Companion.SIZE + " INTEGER")
                    db.execSQL("ALTER TABLE " + DAOProfil.Companion.TABLE_NAME + " ADD COLUMN " + DAOProfil.Companion.BIRTHDAY + " DATE")
                }

                14 -> db.execSQL("ALTER TABLE " + DAOProfil.Companion.TABLE_NAME + " ADD COLUMN " + DAOProfil.Companion.PHOTO + " TEXT")
                15 -> {
                    // Merge of Cardio DB and Fonte DB
                    db.execSQL("ALTER TABLE " + DAORecord.Companion.TABLE_NAME + " ADD COLUMN " + DAORecord.Companion.DISTANCE + " REAL")
                    db.execSQL("ALTER TABLE " + DAORecord.Companion.TABLE_NAME + " ADD COLUMN " + DAORecord.Companion.DURATION + " INTEGER")
                    db.execSQL("ALTER TABLE " + DAORecord.Companion.TABLE_NAME + " ADD COLUMN " + DAORecord.Companion.TYPE + " INTEGER DEFAULT " + DAOMachine.TYPE_STRENGTH)
                }

                16 -> {
                    // Merge of Cardio DB and Fonte DB
                    db.execSQL("ALTER TABLE " + DAOBodyMeasure.Companion.TABLE_NAME + " ADD COLUMN " + DAOBodyMeasure.Companion.UNIT + " INTEGER")
                    migrateWeightTable(db)
                }

                17 -> db.execSQL("ALTER TABLE " + DAOProfil.Companion.TABLE_NAME + " ADD COLUMN " + DAOProfil.Companion.GENDER + " INTEGER")
                18 -> db.execSQL("ALTER TABLE " + DAORecord.Companion.TABLE_NAME + " ADD COLUMN " + DAORecord.Companion.SECONDS + " INTEGER DEFAULT 0")
                19 -> db.execSQL("ALTER TABLE " + DAORecord.Companion.TABLE_NAME + " ADD COLUMN " + DAORecord.Companion.DISTANCE_UNIT + " INTEGER DEFAULT 0")
                20 -> {
                    db.execSQL(DAOBodyPart.Companion.TABLE_CREATE)
                    initBodyPartTable(db)
                }

                21 -> {
                    db.execSQL(DAOProgram.TABLE_CREATE)
                    db.execSQL(DAOExerciseInProgram.TABLE_CREATE)
                    val defaultProgramName = "training program default"
                    addInitialProgram(db, defaultProgramName)
                }

                24 -> addExampleExercises(db)
            }
            upgradeTo++
        }
    }

    override fun onDowngrade(
        db: SQLiteDatabase?, oldVersion: Int,
        newVersion: Int
    ) {
        var upgradeTo = oldVersion - 1
        while (upgradeTo >= newVersion) {
            when (upgradeTo) {
                2 -> {}
                3 -> {}
                4 -> {}
                5 -> {}
            }
            upgradeTo--
        }
    }

    // This method will return if your table exist a field or not
    private fun isFieldExist(db: SQLiteDatabase, tableName: String?, fieldName: String?): Boolean {
        var isExist = true
        val res: Cursor?

        try {
            res = db.rawQuery("SELECT " + fieldName + " FROM " + tableName, null)
            res.close()
        } catch (e: SQLiteException) {
            isExist = false
        }

        return isExist
    }

    fun tableExists(db: SQLiteDatabase, tableName: String?): Boolean {
        var isExist = true
        val res: Cursor?

        try {
            res = db.rawQuery("SELECT * FROM " + tableName, null)
            res.close()
        } catch (e: SQLiteException) {
            isExist = false
        }
        return isExist
    }

    fun checkIfRecordExist(
        db: SQLiteDatabase?,
        nameOfTable: String?,
        columnName: String?,
        columnValue: String?
    ): Boolean {
        return DatabaseUtils.longForQuery(
            db,
            "select count(*) from " + nameOfTable + " where " + columnName + "=? limit 1",
            arrayOf<String?>(columnValue)
        ) > 0
    }

    private fun migrateWeightTable(db: SQLiteDatabase) {
        val valueList: MutableList<ProfileWeight?> = ArrayList<ProfileWeight?>()
        // Select All Query
        val selectQuery = "SELECT * FROM " + DAOWeight.TABLE_NAME
        //SQLiteDatabase db = this.getWritableDatabase();
        val mCursor: Cursor?
        mCursor = db.rawQuery(selectQuery, null)

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                val value = ContentValues()

                //                value.put(DAOBodyMeasure.DATE, mCursor.getString(mCursor.getColumnIndex(DAOWeight.DATE)));
//                value.put(DAOBodyMeasure.BODYPART_ID, BodyPartExtensions.WEIGHT);
//                value.put(DAOBodyMeasure.MEASURE, mCursor.getFloat(mCursor.getColumnIndex(DAOWeight.POIDS)));
//                value.put(DAOBodyMeasure.PROFIL_KEY, mCursor.getLong(mCursor.getColumnIndex(DAOWeight.PROFIL_KEY)));
                db.insert(DAOBodyMeasure.Companion.TABLE_NAME, null, value)
            } while (mCursor.moveToNext())
            mCursor.close()
            //db.close(); // Closing database connection
        }
    }

    fun initBodyPartTable(db: SQLiteDatabase) {
        var display_order = 0

        addInitialBodyPart(
            db,
            BodyPartExtensions.LEFTBICEPS.toLong(),
            "",
            "",
            display_order++,
            BodyPartExtensions.TYPE_MUSCLE
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.RIGHTBICEPS.toLong(),
            "",
            "",
            display_order++,
            BodyPartExtensions.TYPE_MUSCLE
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.PECTORAUX.toLong(),
            "",
            "",
            display_order++,
            BodyPartExtensions.TYPE_MUSCLE
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.WAIST.toLong(),
            "",
            "",
            display_order++,
            BodyPartExtensions.TYPE_MUSCLE
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.BEHIND.toLong(),
            "",
            "",
            display_order++,
            BodyPartExtensions.TYPE_MUSCLE
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.LEFTTHIGH.toLong(),
            "",
            "",
            display_order++,
            BodyPartExtensions.TYPE_MUSCLE
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.RIGHTTHIGH.toLong(),
            "",
            "",
            display_order++,
            BodyPartExtensions.TYPE_MUSCLE
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.LEFTCALVES.toLong(),
            "",
            "",
            display_order++,
            BodyPartExtensions.TYPE_MUSCLE
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.RIGHTCALVES.toLong(),
            "",
            "",
            display_order++,
            BodyPartExtensions.TYPE_MUSCLE
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.WEIGHT.toLong(),
            "",
            "",
            0,
            BodyPartExtensions.TYPE_WEIGHT
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.MUSCLES.toLong(),
            "",
            "",
            0,
            BodyPartExtensions.TYPE_WEIGHT
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.WATER.toLong(),
            "",
            "",
            0,
            BodyPartExtensions.TYPE_WEIGHT
        )
        addInitialBodyPart(
            db,
            BodyPartExtensions.FAT.toLong(),
            "",
            "",
            0,
            BodyPartExtensions.TYPE_WEIGHT
        )
    }

    fun addInitialBodyPart(
        db: SQLiteDatabase,
        pKey: Long,
        pCustomName: String?,
        pCustomPicture: String?,
        pDisplay: Int,
        pType: Int
    ) {
        //SQLiteDatabase db = this.getWritableDatabase();

        val value = ContentValues()

        value.put(DAOBodyPart.Companion.KEY, pKey)
        value.put(DAOBodyPart.Companion.BODYPART_RESID, pKey)
        value.put(DAOBodyPart.Companion.CUSTOM_NAME, pCustomName)
        value.put(DAOBodyPart.Companion.CUSTOM_PICTURE, pCustomPicture)
        value.put(DAOBodyPart.Companion.DISPLAY_ORDER, pDisplay)
        value.put(DAOBodyPart.Companion.TYPE, pType)

        db.insert(DAOBodyPart.Companion.TABLE_NAME, null, value)
    }

    companion object {
        const val DATABASE_VERSION: Int = 24
        private const val OLD09_DATABASE_NAME = "easyfitness"
        private const val DATABASE_NAME = "easyfitness.db"
        private var sInstance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx

            if (sInstance == null) {
                sInstance = DatabaseHelper(context)
            }
            return sInstance!!
        }

        fun renameOldDatabase(activity: Activity) {
            val oldDatabaseFile = activity.getDatabasePath(OLD09_DATABASE_NAME)
            if (oldDatabaseFile.exists()) {
                val newDatabaseFile = File(oldDatabaseFile.getParentFile(), DATABASE_NAME)
                oldDatabaseFile.renameTo(newDatabaseFile)
            }
        }

        private fun addExampleExercises(db: SQLiteDatabase) {
            val newProgramName = "Low Back Muscle Strain"
            val programId: Long // To store the ID of the newly inserted program
            try {
                programId = addInitialProgram(db, newProgramName)
                if (programId != -1L) {
                    println("DB Upgrade Case 24: Successfully obtained program ID: " + programId + " for '" + newProgramName + "'.")
                    addInitialExercise(
                        db,
                        1,
                        programId,
                        10,
                        "Knee Sway",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        15,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=NXEcEAHzSNg&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=4&t=91s",
                        120
                    )
                    addInitialExercise(
                        db,
                        2,
                        programId,
                        10,
                        "Knee to Chest",
                        DAOMachine.TYPE_STATIC,
                        1,
                        15,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        20,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=NXEcEAHzSNg&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=4&t=120s",
                        214
                    )
                    addInitialExercise(
                        db,
                        3,
                        programId,
                        10,
                        "Knee to Chest",
                        DAOMachine.TYPE_STATIC,
                        1,
                        15,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        20,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=NXEcEAHzSNg&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=4&t=120s",
                        214
                    )
                    addInitialExercise(
                        db,
                        4,
                        programId,
                        10,
                        "Cat Cow",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=NXEcEAHzSNg&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=4&t=214s",
                        265
                    )
                    addInitialExercise(
                        db,
                        5,
                        programId,
                        10,
                        "Child's Pose with Reach",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=NXEcEAHzSNg&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=4&t=265s",
                        280
                    )
                    println("DB Upgrade Case 24: Successfully added exercises for program ID: " + programId)
                } else {
                    System.err.println("DB Upgrade Case 24: Failed to insert or retrieve ID for program '" + newProgramName + "'. Exercises will not be added.")
                }
                val newProgramName2 = "Low Back And Core Pain prevention with ball"
                val programId2 = addInitialProgram(db, newProgramName2)
                if (programId2 != -1L) {
                    println("DB Upgrade Case 24: Successfully obtained program ID: " + programId2 + " for '" + newProgramName2 + "'.")
                    addInitialExercise(
                        db,
                        1,
                        programId2,
                        10,
                        "Back extension on ball",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        15,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=ggUwvc-UDcM&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=25&t=36s",
                        106
                    )
                    addInitialExercise(
                        db,
                        2,
                        programId2,
                        10,
                        "Back extension on ball",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        15,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=ggUwvc-UDcM&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=25&t=36s",
                        106
                    )
                    addInitialExercise(
                        db,
                        3,
                        programId2,
                        10,
                        "Opposite Arm/Leg Lift",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        15,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "as many as comfortable",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=ggUwvc-UDcM&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=25&t=106s",
                        154
                    )
                    addInitialExercise(
                        db,
                        4,
                        programId2,
                        10,
                        "Plank on Elbows",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        15,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "as many as comfortable",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=ggUwvc-UDcM&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=25&t=154s",
                        238
                    )
                    addInitialExercise(
                        db,
                        5,
                        programId2,
                        10,
                        "Roll Out",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "as far and as many as comfortable",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=ggUwvc-UDcM&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=25&t=238s",
                        296
                    )
                    addInitialExercise(
                        db,
                        6,
                        programId2,
                        10,
                        "Curl Up",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "as far and as many as comfortable",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=ggUwvc-UDcM&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=25&t=296s",
                        280
                    )
                    addInitialExercise(
                        db,
                        7,
                        programId2,
                        10,
                        "Side Crunch",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "stay safe when you need support leg use it",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=ggUwvc-UDcM&list=PLQ3ggWrvWXyAGnvqnGrGW54Q_icij6ESg&index=25&t=357s",
                        452
                    )
                    println("DB Upgrade Case 24: Successfully added exercises for program ID: " + programId2)
                } else {
                    System.err.println("DB Upgrade Case 24: Failed to insert or retrieve ID for program '" + newProgramName2 + "'. Exercises will not be added.")
                }
                val newProgramName3 = "Recovery breathing and better sleep"
                val programId3 = addInitialProgram(db, newProgramName3)
                if (programId3 != -1L) {
                    println("DB Upgrade Case 24: Successfully obtained program ID: " + programId3 + " for '" + newProgramName3 + "'.")
                    addInitialExercise(
                        db,
                        1,
                        programId3,
                        10,
                        "Breath Awareness",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=04Z4t9udlmo&list=PLQ3ggWrvWXyBG09cIQCkWzdP2IkaPydVt&index=1&t=130s",
                        244
                    )
                    addInitialExercise(
                        db,
                        2,
                        programId3,
                        10,
                        "Inhale through Nose, Exhale through Mouth",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=04Z4t9udlmo&list=PLQ3ggWrvWXyBG09cIQCkWzdP2IkaPydVt&index=1&t=244s",
                        250
                    )
                    addInitialExercise(
                        db,
                        3,
                        programId3,
                        10,
                        "Accentuate Breath in upper part of lungs",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=04Z4t9udlmo&list=PLQ3ggWrvWXyBG09cIQCkWzdP2IkaPydVt&index=1&t=250s",
                        282
                    )
                    addInitialExercise(
                        db,
                        4,
                        programId3,
                        10,
                        "Accentuate Breath in lower part of lungs",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://youtu.be/04Z4t9udlmo?list=PLQ3ggWrvWXyBG09cIQCkWzdP2IkaPydVt&t=282",
                        318
                    )
                    addInitialExercise(
                        db,
                        5,
                        programId3,
                        10,
                        "Accentuate Breath in side and back part of lungs",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://youtu.be/04Z4t9udlmo?list=PLQ3ggWrvWXyBG09cIQCkWzdP2IkaPydVt&t=318",
                        350
                    )
                    addInitialExercise(
                        db,
                        6,
                        programId3,
                        10,
                        "Vary Breathing Pace fast inhale slow exhale",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=04Z4t9udlmo&list=PLQ3ggWrvWXyBG09cIQCkWzdP2IkaPydVt&index=1&t=350s",
                        428
                    )
                    addInitialExercise(
                        db,
                        7,
                        programId3,
                        10,
                        "Vary Breathing Pace slow inhale fast exhale",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=04Z4t9udlmo&list=PLQ3ggWrvWXyBG09cIQCkWzdP2IkaPydVt&index=1&t=350s",
                        428
                    )
                    println("DB Upgrade Case 24: Successfully added exercises for program ID: " + programId3)
                } else {
                    System.err.println("DB Upgrade Case 24: Failed to insert or retrieve ID for program '" + newProgramName3 + "'. Exercises will not be added.")
                }
                val newProgramName4 = "Ankle Mobility, Flexibility and Strength"
                val programId4 = addInitialProgram(db, newProgramName4)
                if (programId4 != -1L) {
                    println("DB Upgrade Case 24: Successfully obtained program ID: " + programId4 + " for '" + newProgramName4 + "'.")
                    addInitialExercise(
                        db,
                        1,
                        programId4,
                        5,
                        "Massage pain in ankle",
                        DAOMachine.TYPE_STATIC,
                        1,
                        1,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        30,
                        30,
                        UnitConverter.UNIT_KM,
                        "https://youtu.be/jWGNwgQgBFk?list=PL23bUbC-jqMRypUnbrKD_wo98JuQRKRcg&t=99",
                        105
                    )
                    addInitialExercise(
                        db,
                        2,
                        programId4,
                        5,
                        "Ankle side move with resistance(band)",
                        DAOMachine.TYPE_STATIC,
                        1,
                        1,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        30,
                        30,
                        UnitConverter.UNIT_KM,
                        "https://youtu.be/jWGNwgQgBFk?list=PL23bUbC-jqMRypUnbrKD_wo98JuQRKRcg&t=304",
                        320
                    )
                    addInitialExercise(
                        db,
                        3,
                        programId4,
                        5,
                        "Calf Stretch Right",
                        DAOMachine.TYPE_STATIC,
                        1,
                        1,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        30,
                        30,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=67s",
                        105
                    )
                    addInitialExercise(
                        db,
                        4,
                        programId4,
                        5,
                        "Calf Stretch Left",
                        DAOMachine.TYPE_STATIC,
                        1,
                        1,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        30,
                        30,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=67s",
                        105
                    )
                    addInitialExercise(
                        db,
                        5,
                        programId4,
                        5,
                        "Calf Stretch Right",
                        DAOMachine.TYPE_STATIC,
                        1,
                        1,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        30,
                        30,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=67s",
                        105
                    )
                    addInitialExercise(
                        db,
                        6,
                        programId4,
                        5,
                        "Calf Stretch Left",
                        DAOMachine.TYPE_STATIC,
                        1,
                        1,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        30,
                        30,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=67s",
                        105
                    )
                    addInitialExercise(
                        db,
                        7,
                        programId4,
                        10,
                        "Ankle Alphabet",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        2,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "Try 3 repetitions",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=105s",
                        126
                    )
                    addInitialExercise(
                        db,
                        8,
                        programId4,
                        10,
                        "Heel Toe Raise",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        20,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=126s",
                        150
                    )
                    addInitialExercise(
                        db,
                        9,
                        programId4,
                        10,
                        "Heel Toe Raise finger inward",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        20,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "hold wall if needed",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://youtu.be/Q9Z1xze9VkA?list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&t=150",
                        157
                    )
                    addInitialExercise(
                        db,
                        10,
                        programId4,
                        10,
                        "Heel Toe Raise finger out",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        20,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "hold wall if needed",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://youtu.be/Q9Z1xze9VkA?list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&t=157",
                        192
                    )
                    addInitialExercise(
                        db,
                        11,
                        programId4,
                        10,
                        "Single injured leg balance with eyes open",
                        DAOMachine.TYPE_STATIC,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "hold wall if needed",
                        "0",
                        0.0f,
                        10,
                        30,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=192s",
                        208
                    )
                    addInitialExercise(
                        db,
                        12,
                        programId4,
                        10,
                        "Single injured leg balance with eyes closed",
                        DAOMachine.TYPE_STATIC,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "hold wall if needed",
                        "0",
                        0.0f,
                        10,
                        10,
                        UnitConverter.UNIT_KM,
                        "https://youtu.be/Q9Z1xze9VkA?list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&t=208",
                        251
                    )
                    addInitialExercise(
                        db,
                        13,
                        programId4,
                        10,
                        "3-way Lunge Right",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=251s",
                        278
                    )
                    addInitialExercise(
                        db,
                        14,
                        programId4,
                        10,
                        "3-way Lunge Left",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=251s",
                        278
                    )
                    addInitialExercise(
                        db,
                        15,
                        programId4,
                        10,
                        "3-way Lunge Right",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=251s",
                        278
                    )
                    addInitialExercise(
                        db,
                        16,
                        programId4,
                        10,
                        "3-way Lunge Left",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        10,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://www.youtube.com/watch?v=Q9Z1xze9VkA&list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&index=11&t=251s",
                        278
                    )
                    addInitialExercise(
                        db,
                        17,
                        programId4,
                        10,
                        "Side step squats",
                        DAOMachine.TYPE_STRENGTH,
                        1,
                        20,
                        0.0f,
                        1,
                        UnitConverter.UNIT_KG,
                        "",
                        "0",
                        0.0f,
                        10,
                        0,
                        UnitConverter.UNIT_KM,
                        "https://youtu.be/Q9Z1xze9VkA?list=PLQ3ggWrvWXyCLMZu_FE8n3b3danhqWdAY&t=278",
                        345
                    )
                    println("DB Upgrade Case 24: Successfully added exercises for program ID: " + programId4)
                } else {
                    System.err.println("DB Upgrade Case 24: Failed to insert or retrieve ID for program '" + newProgramName4 + "'. Exercises will not be added.")
                }
            } catch (e: SQLException) {
                System.err.println("DB Upgrade Case 24: SQLException during program/exercise addition for '" + newProgramName + "': " + e.message)
            }
        }
    }
}
