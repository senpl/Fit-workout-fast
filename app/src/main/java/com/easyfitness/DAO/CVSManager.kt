//package com.easyfitness.DAO
//
//import android.content.Context
//import android.os.Environment
//import com.csvreader.CsvReader
//import com.csvreader.CsvWriter
//import com.easyfitness.DAO.bodymeasures.BodyMeasure
//import com.easyfitness.DAO.bodymeasures.BodyPart
//import com.easyfitness.DAO.bodymeasures.BodyPartExtensions
//import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure
//import com.easyfitness.DAO.bodymeasures.DAOBodyPart
//import com.easyfitness.DAO.cardio.DAOOldCardio
//import com.easyfitness.utils.DateConverter
//import com.easyfitness.utils.UnitConverter
//import java.io.File
//import java.io.FileNotFoundException
//import java.io.IOException
//import java.io.PrintWriter
//import java.nio.charset.Charset
//import java.text.DateFormat
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//// Uses http://javacsv.sourceforge.net/com/csvreader/CsvReader.html //
//class CVSManager(pContext: Context?) {
//    private var mContext: Context? = null
//
//    init {
//        mContext = pContext
//    }
//
//    fun exportDatabase(pProfile: Profile): Boolean {
//        val df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
//
//        /**First of all we check if the external storage of the device is available for writing.
//         * Remember that the external storage is not necessarily the sd card. Very often it is
//         * the device storage.
//         */
//        val state = Environment.getExternalStorageState()
//        if (Environment.MEDIA_MOUNTED != state) {
//            return false
//        } else {
//            val dateFormat = SimpleDateFormat("yyyy_MM_dd_H_m_s_", Locale.getDefault())
//            val date = Date()
//
//            //We use the FastNFitness directory for saving our .csv file.
//            val exportDir = Environment.getExternalStoragePublicDirectory(
//                "/FastnFitness/export/" + dateFormat.format(date) + pProfile.name
//            )
//            if (!exportDir.exists()) {
//                exportDir.mkdirs()
//            }
//
//            val printWriter: PrintWriter? = null
//            try {
//                exportFontes(exportDir, pProfile)
//                exportCardio(exportDir, pProfile)
//                exportIsometric(exportDir, pProfile)
//                //exportProfileWeight(exportDir, pProfile); No more Profile Weight in this version. Everything is in BodyMeasures
//                exportBodyMeasures(exportDir, pProfile)
//                exportExercise(exportDir, pProfile)
//                exportBodyParts(exportDir, pProfile)
//            } catch (e: Exception) {
//                //if there are any exceptions, return false
//                e.printStackTrace()
//                return false
//            } finally {
//                if (printWriter != null) printWriter.close()
//            }
//
//            //If there are no errors, return true.
//            return true
//        }
//    }
//
//    private fun exportFontes(exportDir: File, pProfile: Profile): Boolean {
//        try {
//            // FONTE
//            val dateFormat = SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault())
//            val date = Date()
//
//            val csvOutputFonte = CsvWriter(
//                exportDir.getPath() + "/" + "EF_" + pProfile.name + "_BodyBuilding_" + dateFormat.format(
//                    date
//                ) + ".csv", ',', Charset.forName("UTF-8")
//            )
//
//            /**This is our database connector class that reads the data from the database.
//             * The code of this class is omitted for brevity.
//             */
//            val dbcFonte = DAOFonte(mContext)
//            dbcFonte.open()
//
//            /**Let's read the first table of the database.
//             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
//             * containing all records of the table (all fields).
//             * The code of this class is omitted for brevity.
//             */
//            var records: MutableList<Fonte?>? = null
//            records = dbcFonte.getAllBodyBuildingRecordsByProfileArray(pProfile)
//
//            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
//            csvOutputFonte.write(TABLE_HEAD)
//            csvOutputFonte.write(ID_HEAD)
//            csvOutputFonte.write(DAOFonte.DATE)
//            csvOutputFonte.write(DAOFonte.TIME)
//            csvOutputFonte.write(DAOFonte.EXERCISE)
//            csvOutputFonte.write(DAOFonte.WEIGHT)
//            csvOutputFonte.write(DAOFonte.REPETITION)
//            csvOutputFonte.write(DAOFonte.SERIE)
//            csvOutputFonte.write(DAOFonte.PROFIL_KEY)
//            csvOutputFonte.write(DAOFonte.UNIT)
//            csvOutputFonte.write(DAOFonte.NOTES)
//            csvOutputFonte.write(DAORecord.Companion.TYPE)
//            csvOutputFonte.endRecord()
//
//            for (i in records.indices) {
//                csvOutputFonte.write(DAOFonte.TABLE_NAME)
//                csvOutputFonte.write(records.get(i)!!.getId().toString())
//
//                val dateRecord = records.get(i)!!.getDate()
//
//                csvOutputFonte.write(DateConverter.dateToDBDateStr(dateRecord))
//                csvOutputFonte.write(records.get(i)!!.time)
//                csvOutputFonte.write(records.get(i)!!.exercise)
//                csvOutputFonte.write(records.get(i)!!.poids.toString())
//                csvOutputFonte.write(records.get(i)!!.repetition.toString())
//                csvOutputFonte.write(records.get(i)!!.serie.toString())
//                if (records.get(i)!!.getProfil() != null) csvOutputFonte.write(
//                    records.get(i)!!.getProfil().id.toString()
//                )
//                else csvOutputFonte.write("-1")
//                csvOutputFonte.write(records.get(i)!!.unit.toString())
//                if (records.get(i)!!.note == null) csvOutputFonte.write("")
//                else csvOutputFonte.write(records.get(i)!!.note!!)
//                csvOutputFonte.write(DAOMachine.TYPE_STRENGTH.toString())
//                csvOutputFonte.endRecord()
//            }
//            csvOutputFonte.close()
//            dbcFonte.closeAll()
//        } catch (e: Exception) {
//            //if there are any exceptions, return false
//            e.printStackTrace()
//            return false
//        }
//        //If there are no errors, return true.
//        return true
//    }
//
//    private fun exportIsometric(exportDir: File, pProfile: Profile): Boolean {
//        try {
//            // FONTE
//            val dateFormat = SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault())
//            val date = Date()
//
//            val csvOutput = CsvWriter(
//                exportDir.getPath() + "/" + "EF_" + pProfile.name + "_Isometric_" + dateFormat.format(
//                    date
//                ) + ".csv", ',', Charset.forName("UTF-8")
//            )
//
//            /* This is our database connector class that reads the data from the database.
//             * The code of this class is omitted for brevity.
//             */
//            val daoStatic = DAOStatic(mContext)
//            daoStatic.open()
//
//            /*Let's read the first table of the database.
//             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
//             * containing all records of the table (all fields).
//             * The code of this class is omitted for brevity.
//             */
//            var records: MutableList<StaticExercise?>? = null
//            records = daoStatic.getAllStaticRecordsByProfileArray(pProfile)
//
//            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
//            csvOutput.write(TABLE_HEAD)
//            csvOutput.write(ID_HEAD)
//            csvOutput.write(DAORecord.Companion.DATE)
//            csvOutput.write(DAORecord.Companion.TIME)
//            csvOutput.write(DAORecord.Companion.EXERCISE)
//            csvOutput.write(DAORecord.Companion.WEIGHT)
//            csvOutput.write(DAORecord.Companion.SECONDS)
//            csvOutput.write(DAORecord.Companion.SERIE)
//            csvOutput.write(DAORecord.Companion.PROFIL_KEY)
//            csvOutput.write(DAORecord.Companion.UNIT)
//            csvOutput.write(DAORecord.Companion.NOTES)
//
//            csvOutput.endRecord()
//
//            for (i in records.indices) {
//                csvOutput.write(DAORecord.Companion.TABLE_NAME)
//                csvOutput.write(records.get(i)!!.getId().toString())
//
//                val dateRecord = records.get(i)!!.getDate()
//
//                csvOutput.write(DateConverter.dateToDBDateStr(dateRecord))
//                csvOutput.write(records.get(i)!!.time)
//                csvOutput.write(records.get(i)!!.exercise)
//                csvOutput.write(records.get(i)!!.poids.toString())
//                csvOutput.write(records.get(i)!!.second.toString())
//                csvOutput.write(records.get(i)!!.serie.toString())
//                if (records.get(i)!!.getProfil() != null) csvOutput.write(
//                    records.get(i)!!.getProfil().id.toString()
//                )
//                else csvOutput.write("-1")
//                csvOutput.write(records.get(i)!!.unit.toString())
//                if (records.get(i)!!.note == null) csvOutput.write("")
//                else csvOutput.write(records.get(i)!!.note)
//                csvOutput.write(DAOMachine.TYPE_STATIC.toString())
//                csvOutput.endRecord()
//            }
//            csvOutput.close()
//            daoStatic.closeAll()
//        } catch (e: Exception) {
//            //if there are any exceptions, return false
//            e.printStackTrace()
//            return false
//        }
//        //If there are no errors, return true.
//        return true
//    }
//
//    private fun exportProfileWeight(exportDir: File, pProfile: Profile): Boolean {
//        try {
//            // FONTE
//            val dateFormat = SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault())
//            val date = Date()
//
//            // use FileWriter constructor that specifies open for appending
//            val csvOutputWeight = CsvWriter(
//                exportDir.getPath() + "/" + "EF_" + pProfile.name + "_ProfilWeight_" + dateFormat.format(
//                    date
//                ) + ".csv", ',', Charset.forName("UTF-8")
//            )
//            val dbcWeight = DAOWeight(mContext)
//            dbcWeight.open()
//
//            val weightRecords: MutableList<ProfileWeight?>?
//            weightRecords = dbcWeight.getWeightList(pProfile)
//
//            csvOutputWeight.write(TABLE_HEAD)
//            csvOutputWeight.write(ID_HEAD)
//            csvOutputWeight.write(DAOWeight.POIDS)
//            csvOutputWeight.write(DAOWeight.DATE)
//            csvOutputWeight.endRecord()
//
//            for (i in weightRecords.indices) {
//                csvOutputWeight.write(DAOWeight.TABLE_NAME)
//                csvOutputWeight.write(weightRecords.get(i)!!.id.toString())
//                csvOutputWeight.write(weightRecords.get(i)!!.weight.toString())
//
//                val dateRecord = weightRecords.get(i)!!.date
//                csvOutputWeight.write(DateConverter.dateToDBDateStr(dateRecord))
//                csvOutputWeight.endRecord()
//            }
//            csvOutputWeight.close()
//            dbcWeight.close()
//        } catch (e: Exception) {
//            //if there are any exceptions, return false
//            e.printStackTrace()
//            return false
//        }
//        //If there are no errors, return true.
//        return true
//    }
//
//    private fun exportBodyMeasures(exportDir: File, pProfile: Profile): Boolean {
//        try {
//            val dateFormat = SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault())
//            val date = Date()
//
//            // use FileWriter constructor that specifies open for appending
//            val cvsOutput = CsvWriter(
//                exportDir.getPath() + "/" + "EF_" + pProfile.name + "_BodyMeasures_" + dateFormat.format(
//                    date
//                ) + ".csv", ',', Charset.forName("UTF-8")
//            )
//            val daoBodyMeasure = DAOBodyMeasure(mContext)
//            daoBodyMeasure.open()
//
//            val daoBodyPart = DAOBodyPart(mContext)
//
//            val bodyMeasures: MutableList<BodyMeasure?>
//            bodyMeasures = daoBodyMeasure.getBodyMeasuresList(pProfile)
//
//            cvsOutput.write(TABLE_HEAD)
//            cvsOutput.write(ID_HEAD)
//            cvsOutput.write(DAOBodyMeasure.Companion.DATE)
//            cvsOutput.write("bodypart_label")
//            cvsOutput.write(DAOBodyMeasure.Companion.MEASURE)
//            cvsOutput.write(DAOBodyMeasure.Companion.PROFIL_KEY)
//            cvsOutput.endRecord()
//
//            for (i in bodyMeasures.indices) {
//                cvsOutput.write(DAOBodyMeasure.Companion.TABLE_NAME)
//                cvsOutput.write(bodyMeasures.get(i)!!.id.toString())
//                val dateRecord = bodyMeasures.get(i)!!.date
//                cvsOutput.write(DateConverter.dateToDBDateStr(dateRecord))
//                val bp = daoBodyPart.getBodyPart(bodyMeasures.get(i)!!.bodyPartID.toLong())
//                cvsOutput.write(bp!!.getName(mContext) ) // Write the full name of the BodyPart
//                cvsOutput.write(bodyMeasures.get(i)!!.bodyMeasure.toString())
//                cvsOutput.write(bodyMeasures.get(i)!!.profileID.toString())
//
//                cvsOutput.endRecord()
//            }
//            cvsOutput.close()
//            daoBodyMeasure.close()
//        } catch (e: Exception) {
//            //if there are any exceptions, return false
//            e.printStackTrace()
//            return false
//        }
//        //If there are no errors, return true.
//        return true
//    }
//
//    private fun exportBodyParts(exportDir: File, pProfile: Profile): Boolean {
//        try {
//            val dateFormat = SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault())
//            val date = Date()
//
//            // use FileWriter constructor that specifies open for appending
//            val cvsOutput = CsvWriter(
//                exportDir.getPath() + "/" + "EF_" + pProfile.name + "_CustomBodyPart_" + dateFormat.format(
//                    date
//                ) + ".csv", ',', Charset.forName("UTF-8")
//            )
//            val daoBodyPart = DAOBodyPart(mContext)
//            daoBodyPart.open()
//
//
//            val bodyParts: MutableList<BodyPart>
//            bodyParts = daoBodyPart.getList()
//
//            cvsOutput.write(TABLE_HEAD)
//            cvsOutput.write(DAOBodyPart.Companion.KEY)
//            cvsOutput.write(DAOBodyPart.Companion.CUSTOM_NAME)
//            cvsOutput.write(DAOBodyPart.Companion.CUSTOM_PICTURE)
//            cvsOutput.endRecord()
//
//            for (bp in bodyParts) {
//                if (bp.bodyPartResKey == -1) { // Only custom BodyPart are exported
//                    cvsOutput.write(DAOBodyMeasure.Companion.TABLE_NAME)
//                    cvsOutput.write(bp.id.toString())
//                    cvsOutput.write(bp.getName(mContext))
//                    cvsOutput.write(bp.customPicture)
//                    cvsOutput.endRecord()
//                }
//            }
//            cvsOutput.close()
//            daoBodyPart.close()
//        } catch (e: Exception) {
//            //if there are any exceptions, return false
//            e.printStackTrace()
//            return false
//        }
//        //If there are no errors, return true.
//        return true
//    }
//
//    private fun exportCardio(exportDir: File, pProfile: Profile): Boolean {
//        try {
//            // CARDIO
//            val dateFormat = SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault())
//            val date = Date()
//
//            val csvOutput = CsvWriter(
//                exportDir.getPath() + "/" + "EF_" + pProfile.name + "_Cardio_" + dateFormat.format(
//                    date
//                ) + ".csv", ',', Charset.forName("UTF-8")
//            )
//
//            /**This is our database connector class that reads the data from the database.
//             * The code of this class is omitted for brevity.
//             */
//            val dbcCardio = DAOCardio(mContext)
//            dbcCardio.open()
//
//            /**Let's read the first table of the database.
//             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
//             * containing all records of the table (all fields).
//             * The code of this class is omitted for brevity.
//             */
//            var cardioRecords: MutableList<Cardio?>? = null
//            cardioRecords = dbcCardio.getAllCardioRecordsByProfile(pProfile)
//
//            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
//            csvOutput.write(TABLE_HEAD)
//            csvOutput.write(ID_HEAD)
//            csvOutput.write(DAORecord.Companion.DATE)
//            csvOutput.write(DAORecord.Companion.TIME)
//            csvOutput.write(DAORecord.Companion.EXERCISE)
//            csvOutput.write(DAORecord.Companion.DURATION)
//            csvOutput.write(DAORecord.Companion.DISTANCE)
//            csvOutput.write(DAORecord.Companion.DISTANCE_UNIT)
//            csvOutput.write(DAORecord.Companion.PROFIL_KEY)
//            csvOutput.write(DAORecord.Companion.TYPE)
//
//            csvOutput.endRecord()
//
//            for (i in cardioRecords.indices) {
//                csvOutput.write(DAORecord.Companion.TABLE_NAME)
//                csvOutput.write(cardioRecords.get(i)!!.getId().toString())
//
//                val dateRecord = cardioRecords.get(i)!!.getDate()
//
//                csvOutput.write(DateConverter.dateToDBDateStr(dateRecord))
//                csvOutput.write(cardioRecords.get(i)!!.time)
//                csvOutput.write(cardioRecords.get(i)!!.exercise)
//                csvOutput.write(cardioRecords.get(i)!!.duration.toString())
//                csvOutput.write(cardioRecords.get(i)!!.distance.toString())
//                csvOutput.write(cardioRecords.get(i)!!.distanceUnit.toString())
//                if (cardioRecords.get(i)!!
//                        .getProfil() != null
//                ) csvOutput.write(cardioRecords.get(i)!!.getProfil().id.toString())
//                else csvOutput.write("-1")
//                //write the record in the .csv file
//                csvOutput.write(DAOMachine.TYPE_CARDIO.toString())
//                csvOutput.endRecord()
//            }
//            csvOutput.close()
//            dbcCardio.close()
//        } catch (e: Exception) {
//            //if there are any exceptions, return false
//            e.printStackTrace()
//            return false
//        }
//        //If there are no errors, return true.
//        return true
//    }
//
//    private fun exportExercise(exportDir: File, pProfile: Profile): Boolean {
//        try {
//            // FONTE
//            val dateFormat = SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault())
//            val date = Date()
//
//            val csvOutput = CsvWriter(
//                exportDir.getPath() + "/" + "EF_" + pProfile.name + "_Exercises_" + dateFormat.format(
//                    date
//                ) + ".csv", ',', Charset.forName("UTF-8")
//            )
//
//            /**This is our database connector class that reads the data from the database.
//             * The code of this class is omitted for brevity.
//             */
//            val dbcMachine = DAOMachine(mContext)
//            dbcMachine.open()
//
//            /**Let's read the first table of the database.
//             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
//             * containing all records of the table (all fields).
//             * The code of this class is omitted for brevity.
//             */
//            var records: MutableList<Machine?>? = null
//            records = dbcMachine.allMachinesArray
//
//            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
//            csvOutput.write(TABLE_HEAD)
//            csvOutput.write(ID_HEAD)
//            csvOutput.write(DAOMachine.NAME)
//            csvOutput.write(DAOMachine.DESCRIPTION)
//            csvOutput.write(DAOMachine.TYPE)
//            csvOutput.write(DAOMachine.BODYPARTS)
//            csvOutput.write(DAOMachine.FAVORITES)
//            //csvOutput.write(DAOMachine.PICTURE_RES);
//            csvOutput.endRecord()
//
//            for (i in records.indices) {
//                csvOutput.write(DAOMachine.TABLE_NAME)
//                csvOutput.write(records.get(i)!!.id.toString())
//                csvOutput.write(records.get(i)!!.name)
//                csvOutput.write(records.get(i)!!.description!!)
//                csvOutput.write(records.get(i)!!.type.toString())
//                csvOutput.write(records.get(i)!!.bodyParts!!)
//                csvOutput.write(records.get(i)!!.favorite.toString())
//                //write the record in the .csv file
//                csvOutput.endRecord()
//            }
//            csvOutput.close()
//            dbcMachine.close()
//        } catch (e: Exception) {
//            //if there are any exceptions, return false
//            e.printStackTrace()
//            return false
//        }
//        //If there are no errors, return true.
//        return true
//    }
//
//    /*
//     * TODO : Renforcer cette fonction.     */
//    fun importDatabase(file: String?, pProfile: Profile): Boolean {
//        var ret = true
//
//        try {
//            val csvRecords = CsvReader(file, ',', Charset.forName("UTF-8"))
//
//            csvRecords.readHeaders()
//
//            val fonteList = ArrayList<Fonte?>()
//            val cardioList = ArrayList<Cardio?>()
//            val staticExerciseList = ArrayList<StaticExercise?>()
//
//            val dbcMachine = DAOMachine(mContext)
//
//            while (csvRecords.readRecord()) {
//                when (csvRecords.get(TABLE_HEAD)) {
//                    DAORecord.Companion.TABLE_NAME -> {
//                        val date: Date?
//                        date = DateConverter.DBDateStrToDate(csvRecords.get(DAOFonte.DATE))
//                        val time: String? = csvRecords.get(DAOFonte.TIME)
//                        val machine: String? = csvRecords.get(DAOFonte.EXERCISE)
//                        if (dbcMachine.getMachine(machine) != null) {
//                            if (dbcMachine.getMachine(machine)!!.type == DAOMachine.TYPE_STRENGTH) {
//                                val poids = csvRecords.get(DAOFonte.WEIGHT).toFloat()
//                                val repetition = csvRecords.get(DAOFonte.REPETITION).toInt()
//                                val serie = csvRecords.get(DAOFonte.SERIE).toInt()
//                                var unit = UnitConverter.UNIT_KG
//                                if (!csvRecords.get(DAOFonte.UNIT).isEmpty()) {
//                                    unit = csvRecords.get(DAOFonte.UNIT).toInt()
//                                }
//                                val notes: String? = csvRecords.get(DAOFonte.NOTES)
//
//                                val fonte = Fonte(
//                                    date,
//                                    machine,
//                                    serie,
//                                    repetition,
//                                    poids,
//                                    pProfile,
//                                    unit,
//                                    notes,
//                                    dbcMachine.getMachine(machine)!!.id,
//                                    time
//                                )
//                                fonteList.add(fonte)
//                            } else if (dbcMachine.getMachine(machine)!!.type == DAOMachine.TYPE_CARDIO) {
//                                val exercise: String? = csvRecords.get(DAORecord.Companion.EXERCISE)
//                                val distance =
//                                    csvRecords.get(DAORecord.Companion.DISTANCE)!!.toFloat()
//                                val duration = csvRecords.get(DAORecord.Companion.DURATION)!!.toInt()
//                                var distance_unit = UnitConverter.UNIT_KM
//                                if (!csvRecords.get(DAORecord.Companion.DISTANCE_UNIT)!!.isEmpty()) {
//                                    distance_unit =
//                                        csvRecords.get(DAORecord.Companion.DISTANCE_UNIT)!!.toInt()
//                                }
//                                val cardio = Cardio(
//                                    date,
//                                    exercise,
//                                    distance,
//                                    duration.toLong(),
//                                    pProfile,
//                                    time,
//                                    distance_unit
//                                )
//                                cardioList.add(cardio)
//                            } else if (dbcMachine.getMachine(machine)!!.type == DAOMachine.TYPE_STATIC) {
//                                val poids = csvRecords.get(DAORecord.Companion.WEIGHT)!!.toFloat()
//                                val second = csvRecords.get(DAORecord.Companion.SECONDS)!!.toInt()
//                                val serie = csvRecords.get(DAORecord.Companion.SERIE)!!.toInt()
//                                var unit = UnitConverter.UNIT_KG
//                                if (!csvRecords.get(DAORecord.Companion.UNIT)!!.isEmpty()) {
//                                    unit = csvRecords.get(DAORecord.Companion.UNIT)!!.toInt()
//                                }
//                                val staticExercise = StaticExercise(
//                                    date,
//                                    machine,
//                                    serie,
//                                    second,
//                                    poids,
//                                    pProfile,
//                                    unit,
//                                    dbcMachine.getMachine(machine)!!.id,
//                                    time
//                                )
//                                staticExerciseList.add(staticExercise)
//                            }
//                        } else {
//                            return false
//                        }
//                    }
//
//                    DAOOldCardio.Companion.TABLE_NAME -> {
//                        val dbcCardio = DAOCardio(mContext)
//                        dbcCardio.open()
//                        val date: Date?
//
//                        date =
//                            DateConverter.DBDateStrToDate(csvRecords.get(DAORecord.Companion.DATE))
//
//                        val exercice: String? = csvRecords.get(DAOOldCardio.Companion.EXERCICE)
//                        val distance = csvRecords.get(DAOOldCardio.Companion.DISTANCE)!!.toFloat()
//                        val duration = csvRecords.get(DAOOldCardio.Companion.DURATION)!!.toInt()
//                        dbcCardio.addCardioRecord(
//                            date,
//                            "",
//                            exercice,
//                            distance,
//                            duration.toLong(),
//                            pProfile,
//                            UnitConverter.UNIT_KM
//                        )
//                        dbcCardio.close()
//                    }
//
//                    DAOWeight.TABLE_NAME -> {
//                        val dbcWeight = DAOBodyMeasure(mContext)
//                        dbcWeight.open()
//                        val date: Date?
//                        date = DateConverter.DBDateStrToDate(csvRecords.get(DAOWeight.DATE))
//
//                        val poids = csvRecords.get(DAOWeight.POIDS)!!.toFloat()
//                        dbcWeight.addBodyMeasure(
//                            date,
//                            BodyPartExtensions.WEIGHT.toLong(),
//                            poids,
//                            pProfile.id
//                        )
//                    }
//
//                    DAOBodyMeasure.Companion.TABLE_NAME -> {
//                        val dbcBodyMeasure = DAOBodyMeasure(mContext)
//                        dbcBodyMeasure.open()
//                        val date: Date?
//                        date =
//                            DateConverter.DBDateStrToDate(csvRecords.get(DAOBodyMeasure.Companion.DATE))
//                        val bodyPartName = csvRecords.get("bodypart_label")
//                        val dbcBodyPart = DAOBodyPart(mContext)
//                        dbcBodyPart.open()
//                        val bodyParts: MutableList<BodyPart>
//                        bodyParts = dbcBodyPart.getList()
//                        for (bp in bodyParts) {
//                            if (bp.getName(mContext) == bodyPartName) {
//                                val measure =
//                                    csvRecords.get(DAOBodyMeasure.Companion.MEASURE).toFloat()
//                                dbcBodyMeasure.addBodyMeasure(
//                                    date,
//                                    bp.getId(),
//                                    measure,
//                                    pProfile.id
//                                )
//                                dbcBodyPart.close()
//                                break
//                            }
//                        }
//                    }
//
//                    DAOBodyPart.Companion.TABLE_NAME -> {
//                        val dbcBodyPart = DAOBodyPart(mContext)
//                        dbcBodyPart.open()
//                        val bodyPartId = -1
//                        val customName: String? = csvRecords.get(DAOBodyPart.Companion.CUSTOM_NAME)
//                        val customPicture: String? =
//                            csvRecords.get(DAOBodyPart.Companion.CUSTOM_PICTURE)
//                        dbcBodyPart.add(
//                            bodyPartId,
//                            customName,
//                            customPicture,
//                            0,
//                            BodyPartExtensions.TYPE_MUSCLE
//                        )
//                    }
//
//                    DAOProfil.Companion.TABLE_NAME -> {}
//                    DAOMachine.TABLE_NAME -> {
//                        val dbc = DAOMachine(mContext)
//                        val name = csvRecords.get(DAOMachine.NAME)
//                        val description = csvRecords.get(DAOMachine.DESCRIPTION)
//                        val type = csvRecords.get(DAOMachine.TYPE)!!.toInt()
//                        val favorite = csvRecords.get(DAOMachine.FAVORITES).toBoolean()
//                        val bodyParts = csvRecords.get(DAOMachine.BODYPARTS)
//
//                        // Check if this machine doesn't exist
//                        if (dbc.getMachine(name) == null) {
//                            dbc.addMachine(name, description, type, "", favorite, bodyParts)
//                        } else {
//                            val m = dbc.getMachine(name)
//                            m!!.description = description
//                            m.favorite = favorite
//                            m.bodyParts = bodyParts
//                            dbc.updateMachine(m)
//                        }
//                    }
//                }
//            }
//
//            csvRecords.close()
//
//            // In case of success
//            val dbcFonte = DAOFonte(mContext)
//            dbcFonte.addBodyBuildingList(fonteList)
//            val dbcCardio = DAOCardio(mContext)
//            dbcCardio.addCardioList(cardioList)
//            val dbcStatic = DAOStatic(mContext)
//            dbcStatic.addStaticList(staticExerciseList)
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//            ret = false
//        } catch (e: IOException) {
//            e.printStackTrace()
//            ret = false
//        }
//
//        return ret
//    }
//
//    companion object {
//        private const val TABLE_HEAD = "table"
//        private const val ID_HEAD = "id"
//    }
//}
