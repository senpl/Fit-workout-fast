package com.easyfitness.programs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.easyfitness.BtnClickListener
import com.easyfitness.DAO.*
import com.easyfitness.DAO.DAOMachine.*
import com.easyfitness.R
import com.easyfitness.SettingsFragment
import com.easyfitness.TimePickerDialogFragment
import com.easyfitness.databinding.TabProgramRunnerBinding
import com.easyfitness.machines.ExerciseDetailsPager
import com.easyfitness.machines.MachineCursorAdapter
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.ImageUtil
import com.easyfitness.utils.UnitConverter
import com.fitworkoutfast.MainActivity
import com.ikovac.timepickerwithseconds.MyTimePickerDialog
import com.ikovac.timepickerwithseconds.TimePicker
import com.onurkaganaldemir.ktoastlib.KToast
import com.pacific.timer.Rx2Timer
import timber.log.Timber
import java.io.IOException
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.*

class ProgramRunner : Fragment(R.layout.tab_program_runner) {
    private val progressScaleFix: Int = 3
    private lateinit var mainActivity: MainActivity
    private var lTableColor = 1
    private var machineListDialog: AlertDialog? = null
    private var selectedType = TYPE_FONTE
    private lateinit var daoProgram: DAOProgram
    private var programId: Long = 1
    private var currentExerciseOrder = 0  //start from 0
    private lateinit var exercisesFromProgram: List<ExerciseInProgram>
    private lateinit var daoRecord: DAORecord
    private lateinit var strengthRecordsDao: DAOFonte
    private lateinit var daoCardio: DAOCardio
    private lateinit var daoStatic: DAOStatic
    private lateinit var daoExerciseInProgram: DAOExerciseInProgram
    private lateinit var mDbMachine: DAOMachine
    private lateinit var swipeDetectorListener: SwipeDetectorListener
    private var restTimer: Rx2Timer? = null
    private lateinit var staticTimer: Rx2Timer
    private var staticTimerRunning: Boolean = false
    private var restTimerRunning: Boolean = false

    private var _binding: TabProgramRunnerBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabProgramRunnerBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialization of the database
        daoProgram = DAOProgram(context)
        daoRecord = DAORecord(context)
        strengthRecordsDao = DAOFonte(context)
        daoCardio = DAOCardio(context)
        daoStatic = DAOStatic(context)
        mDbMachine = DAOMachine(context)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val programs = daoProgram.allProgramsNames
        daoExerciseInProgram = DAOExerciseInProgram(requireContext())
        if (programs == null || programs.isEmpty()) {
            val profileId: Long? = (requireActivity() as MainActivity).currentProfile?.id
            val programsFragment = ProgramsFragment.newInstance("", profileId)
            Toast.makeText(context, R.string.add_program_first, Toast.LENGTH_LONG).show()
            requireActivity().supportFragmentManager.commit {
                addToBackStack(null)
                add(R.id.fragment_container, programsFragment)
            }
        } else {
            val programFirst = daoProgram.getRecord(programs[0])
            if (programFirst != null) {
                programId = requireContext().getSharedPreferences("currentProgram", Context.MODE_PRIVATE).getLong("currentProgram", programFirst.id)
                val tempPosition = requireContext().getSharedPreferences("currentProgramPosition", Context.MODE_PRIVATE).getInt("currentProgramPosition", 1)
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, programs)
                binding.programSelect.adapter = adapter
                if (tempPosition < programs.size) {
                    binding.programSelect.setSelection(tempPosition)
                }
                binding.programSelect.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>,
                                                view: View?, position: Int, id: Long) {
                        val program: Program? = daoProgram.getRecord(programs[position])
                        if (program != null ) {
                            programId = program.id
                            currentExerciseOrder = 0
                            exercisesFromProgram = daoExerciseInProgram.getAllExerciseInProgram(programId)
                            if(exercisesFromProgram.isNotEmpty()) {
                                binding.exerciseIndicator.initDots(exercisesFromProgram.size)
                                binding.exerciseInProgramNumber.text =
                                    exercisesFromProgram.size.toString()
                                binding.exerciseIndicator.setDotSelection(currentExerciseOrder)
                                binding.currentExerciseNumber.text = "1"
                                saveToPreference("currentProgram", programId)
                                saveToPreference("currentProgramPosition", position)
                                refreshData()
                                Toast.makeText(
                                    context,
                                    getString(R.string.program_selection) + " " + programs[position],
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val profileId: Long? = (requireActivity() as MainActivity).currentProfile?.id
                                val programsFragment = ProgramsFragment.newInstance("", profileId)
                                requireActivity().supportFragmentManager.commit {
                                    addToBackStack(null)
                                    add(R.id.fragment_container, programsFragment)
                                }
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }
        }
        swipeDetectorListener = SwipeDetectorListener(this)
        mDbMachine = DAOMachine(context)
        selectedType = TYPE_FONTE
        binding.imageExerciseThumb.setOnClickListener {
            val m = mDbMachine.getMachine(binding.exerciseEdit.text.toString())
            if (m != null) {
                val profileId: Long? = (requireActivity() as MainActivity).currentProfile?.id
                val machineDetailsFragment = ExerciseDetailsPager.newInstance(m.id, profileId!!)
                requireActivity().supportFragmentManager.commit {
                    addToBackStack(null)
                    add(R.id.fragment_container, machineDetailsFragment)
                }
            }
        }
        binding.nextExerciseArrow.setOnClickListener(clickArrows)
        binding.previousExerciseArrow.setOnClickListener(clickArrows)
        binding.addButton.setOnClickListener(clickAddButton)
        binding.failButton.setOnClickListener(clickFailButton)
        binding.exercisesListButton.setOnClickListener(onClickMachineListWithIcons)
        binding.durationEdit.setOnClickListener(clickDateEdit)
        binding.exerciseEdit.setOnKeyListener(checkExerciseExists)
        binding.exerciseEdit.onItemClickListener = onItemClickFilterList

        binding.restTimeEdit.onFocusChangeListener = restTimeEditChange
        restoreSharedParams()
        var weightUnit = UnitConverter.UNIT_KG
        try {
            weightUnit = sharedPreferences.getString(SettingsFragment.WEIGHT_UNIT_PARAM, "0")?.toInt()!!
        } catch (e: NumberFormatException) {
            Timber.d("Not important")
        }
        binding.unitShow.text = getString(R.string.kg)
        if (weightUnit == UnitConverter.UNIT_LBS)
            binding.unitShow.text = getString(R.string.Lbs)
        val distanceUnit: Int = try {
            sharedPreferences.getString(SettingsFragment.DISTANCE_UNIT_PARAM, "0")?.toInt()!!
        } catch (e: NumberFormatException) {
            UnitConverter.UNIT_KM
        }
        binding.unitDistanceSpinner.setSelection(distanceUnit)

        val poidsEditChange = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                binding.saveWeight.visibility = VISIBLE
            }
        }
        binding.poidsEdit.onFocusChangeListener = poidsEditChange
        binding.saveWeight.setOnClickListener {
            try {
                val weightToUpdate = binding.poidsEdit.text.toString()
                val weightTStore = weightToUpdate.toFloat()
                if (exercisesFromProgram.isNotEmpty()) {
                    daoExerciseInProgram.updateString(exercisesFromProgram[currentExerciseOrder], DAOExerciseInProgram.WEIGHT, weightTStore.toString())
                    Toast.makeText(context, getString(R.string.saved_into_program) + " " + weightTStore, Toast.LENGTH_SHORT).show()
                    binding.saveWeight.visibility = GONE
                }
            } catch (e: NumberFormatException) {
                Timber.d("Not saved")
            }
        }

        binding.notesInExercise.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (exercisesFromProgram.isNotEmpty()) {
                    updateNote()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val repsEditChange = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                binding.saveReps.visibility = VISIBLE
            }
        }
        binding.repsPicker.onFocusChangeListener = repsEditChange
        binding.saveReps.setOnClickListener {
            try {
                val repsToUpdate = binding.repsPicker.progress.toString()
                if (exercisesFromProgram.isNotEmpty()) {
                    daoExerciseInProgram.updateString(exercisesFromProgram[currentExerciseOrder], DAOExerciseInProgram.REPETITION, repsToUpdate)
                    Toast.makeText(context, getString(R.string.saved_into_program) + " " + repsToUpdate, Toast.LENGTH_SHORT).show()
                    binding.saveReps.visibility = GONE
                }
            } catch (e: NumberFormatException) {
                Timber.d("Not saved")
            }
        }

        binding.exerciseIndicator.onSelectListener = {
            chooseExercise(it)
        }

        if (requireContext().getSharedPreferences("swipeGesturesSwitch", Context.MODE_PRIVATE).getBoolean("swipeGesturesSwitch", true)) {
            binding.recordList.setOnTouchListener(swipeDetectorListener) //this is different view so require separate listener to work
            binding.tabProgramRunner.setOnTouchListener(swipeDetectorListener)
        }
    }

    private fun chooseExercise(selected: Int) {
        currentExerciseOrder = selected
        binding.currentExerciseNumber.text = (selected + 1).toString()
        refreshData()
    }


    fun nextExercise() {
        if (exercisesFromProgram.isNotEmpty() && currentExerciseOrder < exercisesFromProgram.size - 1) {
            currentExerciseOrder++
            binding.currentExerciseNumber.text = (currentExerciseOrder + 1).toString()
            binding.exerciseIndicator.setDotSelection(currentExerciseOrder)
            refreshData()
        }
    }

    fun previousExercise() {
        if (exercisesFromProgram.isNotEmpty() && currentExerciseOrder > 0) {
            currentExerciseOrder--
            binding.currentExerciseNumber.text = (currentExerciseOrder + 1).toString()
            binding.exerciseIndicator.setDotSelection(currentExerciseOrder)
            refreshData()
        }
    }

    fun saveToPreference(prefName: String?, prefLongToSet: Long?) {
        val sharedPref = requireContext().getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putLong(prefName, prefLongToSet!!)
        editor.apply()
    }

    fun saveToPreference(prefName: String?, prefIntToSet: Int?) {
        val sharedPref = requireContext().getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(prefName, prefIntToSet!!)
        editor.apply()
    }

    private val durationSet = MyTimePickerDialog.OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int, second: Int ->
        val strMinute: String = if (minute < 10) "0$minute" else minute.toString()
        val strHour: String = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay.toString()
        val strSecond: String = if (second < 10) "0$second" else second.toString()
        val date = "$strHour:$strMinute:$strSecond"
        binding.durationEdit.text = date
        hideKeyboard()
    }

    private val clickArrows = OnClickListener { v: View ->
        when (v.id) {
            R.id.nextExerciseArrow -> nextExercise()
            R.id.previousExerciseArrow -> previousExercise()
        }
    }
    private val checkExerciseExists = OnKeyListener { _: View?, _: Int, _: KeyEvent? ->
        val lMach = mDbMachine.getMachine(binding.exerciseEdit.text.toString())
        if (lMach != null) {
            changeExerciseTypeUI(lMach.type)
        }
        false
    }
    private val restTimeEditChange = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
        if (!hasFocus) {
            saveSharedParams()
        }
    }
    private val itemClickDeleteRecord = BtnClickListener { idToDelete: Long -> showDeleteDialog(idToDelete) }
    private val itemClickCopyRecord = BtnClickListener { id: Long ->
        val r: IRecord? = daoRecord.getRecord(id)
        if (r != null) {
            setCurrentMachine(r.exercise, r.type)
            when (r.type) {
                TYPE_FONTE -> {
                    val f = r as Fonte
                    binding.repsPicker.progress = f.repetition
                    binding.seriesEdit.setText(String.format("%d", f.serie))
                    val numberFormat = DecimalFormat("#.##")
                    var poids = f.poids
                    if (f.unit == UnitConverter.UNIT_LBS) {
                        poids = UnitConverter.KgtoLbs(poids)
                    }
                    binding.unitShow.text = f.unit.toString()
                    binding.poidsEdit.setText(numberFormat.format(poids))
                }
                TYPE_STATIC -> {
                    val f = r as StaticExercise
                    binding.secondsEdit.setText(String.format("%d", f.second))
                    binding.seriesEdit.setText(String.format("%d", f.serie))
                    val numberFormat = DecimalFormat("#.##")
                    binding.poidsEdit.setText(numberFormat.format(f.poids.toDouble()))
                }
                TYPE_CARDIO -> {
                    val c = r as Cardio
                    val numberFormat = DecimalFormat("#.##")
                    var distance = c.distance
                    if (c.distanceUnit == UnitConverter.UNIT_MILES) {
                        distance = UnitConverter.KmToMiles(c.distance)
                    }
                    binding.unitDistanceSpinner.setSelection(c.distanceUnit)
                    binding.distanceEdit.setText(numberFormat.format(distance.toDouble()))
                    binding.durationEdit.text = DateConverter.durationToHoursMinutesSecondsStr(c.duration)
                }
            }
            KToast.infoToast(mainActivity, getString(R.string.recordcopied), Gravity.BOTTOM, KToast.LENGTH_SHORT)
        }
    }


    private val restClickTimer = OnClickListener {
        restTimer?.restart()
    }


    private val clickStaticTimer = OnClickListener {
        staticTimerRunning = if (!staticTimerRunning) {
            if (staticTimer.isPause) {
                staticTimer.resume()
            } else {
                staticTimer.start()
            }
            true
        } else {
            staticTimer.pause()
            false
        }
    }

    private val clickStaticReset = OnClickListener {
        staticTimer.restart()
        staticTimerRunning = true
    }

    private val clickResetStaticTimer = OnLongClickListener {
        staticTimer.restart()
        staticTimerRunning = true
        true
    }

    @SuppressLint("SetTextI18n")
    private val clickAddButton = OnClickListener {
        if (exercisesFromProgram.isEmpty()) {
            KToast.warningToast(requireActivity(), resources.getText(R.string.emptyExercisesInProgram).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG)
            return@OnClickListener
        }
        if (binding.exerciseEdit.text.toString().isEmpty()) {
            KToast.warningToast(requireActivity(), resources.getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
            return@OnClickListener
        }
        val exerciseType: Int
        val lMachine = mDbMachine.getMachine(binding.exerciseEdit.text.toString())
        exerciseType = lMachine?.type ?: selectedType
        var restTime = 60
        try {
            restTime = binding.restTimeEdit.text.toString().toInt()
        } catch (e: NumberFormatException) {
            binding.restTimeEdit.setText("60")
        }
        val date = Date()
        val timeStr = DateConverter.currentTime()

        when (exerciseType) {
            TYPE_FONTE -> {
                if (binding.seriesEdit.text.toString().isEmpty() ||
                    binding.poidsEdit.text.toString().isEmpty()) {
                    KToast.warningToast(requireActivity(), resources.getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
                    return@OnClickListener
                }
                val tmpPoids = binding.poidsEdit.text.toString().replace(",".toRegex(), ".").toFloat()  /* Weight conversion */
                val unitPoids = UnitConverter.UNIT_KG // Kg
                strengthRecordsDao.addBodyBuildingRecord(date,
                    binding.exerciseEdit.text.toString(),
                    binding.seriesEdit.text.toString().toInt(),
                    binding.repsPicker.progress,
                    tmpPoids, // Always save in KG
                    getProfilFromMain(),
                    unitPoids, // Store Unit for future display
                    binding.notesInExercise.text.toString(), //Notes
                    timeStr
                )
            }
            TYPE_STATIC -> {
                if (binding.seriesEdit.text.toString().isEmpty() ||
                    binding.secondsEdit.text.toString().isEmpty() ||
                    binding.poidsEdit.text.toString().isEmpty()) {
                    KToast.warningToast(requireActivity(), resources.getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
                    return@OnClickListener
                }
                /* Weight conversion */
                val tmpPoids = binding.poidsEdit.text.toString().replace(",".toRegex(), ".").toFloat()
                val unitPoids = UnitConverter.UNIT_KG // Kg
                try {
                    restTime = binding.restTimeEdit.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    restTime = 0
                    binding.restTimeEdit.setText("0")
                }
                daoStatic.addStaticRecord(date,
                    binding.exerciseEdit.text.toString(),
                    binding.seriesEdit.text.toString().toInt(),
                    binding.secondsEdit.text.toString().toInt(),
                    tmpPoids,
                    getProfilFromMain(),
                    unitPoids, // Store Unit for future display
                    binding.notesInExercise.text.toString(), //Notes
                    timeStr)
            }
            TYPE_CARDIO -> {
                if (binding.durationEdit.text.toString().isEmpty() &&  // Only one is mandatory
                    binding.distanceEdit.text.toString().isEmpty()) {
                    KToast.warningToast(requireActivity(),
                        resources.getText(R.string.missinginfo).toString() + " Distance missing",
                        Gravity.BOTTOM, KToast.LENGTH_SHORT)
                    return@OnClickListener
                }
                var duration: Long
                try {
                    @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("HH:mm:ss")
                    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
                    val tmpDate = dateFormat.parse(binding.durationEdit.text.toString())
                    duration = tmpDate!!.time
                } catch (e: ParseException) {
                    e.printStackTrace()
                    duration = 0
                }
                var distance: Float = if (binding.distanceEdit.text.toString().isEmpty()) {
                    0f
                } else {
                    binding.distanceEdit.text.toString().replace(",".toRegex(), ".").toFloat()
                }
                var unitDistance = UnitConverter.UNIT_KM
                if (binding.unitDistanceSpinner.selectedItem.toString()
                    == context?.getString(R.string.MilesUnitLabel)) {
                    distance = UnitConverter.MilesToKm(distance) // Always convert to km
                    unitDistance = UnitConverter.UNIT_MILES
                }
                daoCardio.addCardioRecord(date,
                    timeStr,
                    binding.exerciseEdit.text.toString(),
                    distance,
                    duration,
                    getProfilFromMain(),
                    unitDistance)
            }
        }
        requireActivity().findViewById<View>(R.id.drawer_layout)?.requestFocus()
        hideKeyboard()
        lTableColor = (lTableColor + 1) % 2 // Change the color each time you add data
        refreshData()
        val adapter = ArrayAdapter(requireView().context,
            android.R.layout.simple_dropdown_item_1line, daoRecord.getAllMachines(profil))
        binding.exerciseEdit.setAdapter(adapter)
        // Launch Rest Countdown
        if (restTime != 0) {
            binding.restFillBackgroundProgress.visibility = VISIBLE
        }
        binding.exerciseIndicator[currentExerciseOrder].setBackgroundResource(R.drawable.green_button_background)
        runRest(restTime)
    }

    private val clickFailButton = OnClickListener {
        if (exercisesFromProgram.isNotEmpty()) {
            binding.exerciseIndicator[currentExerciseOrder].setBackgroundResource(R.drawable.red_button_background)
        }
    }

    private fun runRest(restTime: Int) {
        if (requireContext().getSharedPreferences("nextExerciseSwitch", Context.MODE_PRIVATE).getBoolean("nextExerciseSwitch", true)) {
            nextExercise()
        }
        if(restTime!=0) {
            binding.restFillBackgroundProgress.visibility = VISIBLE
            restTimerRunning = true
            binding.restFillBackgroundProgress.setDuration(restTime.toLong() * progressScaleFix)
            restTimer?.stop()
            restTimer = Rx2Timer.builder()
                .initialDelay(0)
                .take(restTime)
                .onEmit { count ->
                    binding.restFillBackgroundProgress.setProgress(count.toInt() * progressScaleFix)
                    if (count < 60) {
                        binding.countDown.text = getString(R.string.rest_counter, count)
                    } else {
                        val minutes: Int = ((count % 3600) / 60).toInt()
                        val seconds: Int = (count % 60).toInt()
                        binding.countDown.text = getString(R.string.rest_counter_minutes, minutes, seconds)
                    }
                }
                .onError { binding.countDown.text = getString(R.string.error) }
                .onComplete {
                    binding.countDown.text = getString(R.string.rest_finished)
                    binding.restFillBackgroundProgress.visibility = GONE
                    restTimerRunning = false
                    if (requireContext().getSharedPreferences("playRestSound", Context.MODE_PRIVATE).getBoolean("playRestSound", true)) {
                        val mediaPlayer = MediaPlayer()
                        try {
                            val myUri: Uri = Uri.parse(requireContext().getSharedPreferences("restSound", Context.MODE_PRIVATE).getString("restSound", RingtoneManager.getDefaultUri(R.raw.chime).toString()))
                            mediaPlayer.setDataSource(this.requireContext(), myUri)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        try {
                            mediaPlayer.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        mediaPlayer.start()
                    }
                }
                .build()
            restTimer?.start()
            binding.restFillBackgroundProgress.setOnClickListener(restClickTimer)
        }
    }

    private val onClickMachineListWithIcons = OnClickListener { v ->
        val oldCursor: Cursor
        if (machineListDialog != null && machineListDialog!!.isShowing) {        // In case the dialog is already open
            return@OnClickListener
        }
        val machineList = ListView(v.context)
        val c: Cursor? = mDbMachine.allMachines
        if (c == null || c.count == 0) {
            KToast.warningToast(requireActivity(), resources.getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
            machineList.adapter = null
        } else {
            if (machineList.adapter == null) {
                val mTableAdapter = MachineCursorAdapter(activity, c, 0, mDbMachine)
                machineList.adapter = mTableAdapter
            } else {
                val mTableAdapter = machineList.adapter as MachineCursorAdapter
                oldCursor = mTableAdapter.swapCursor(c)
                oldCursor?.close()
            }
            machineList.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
                val textView = view.findViewById<TextView>(R.id.LIST_MACHINE_ID)
                val machineID = textView.text.toString().toLong()
                val lMachineDb = DAOMachine(context)
                val lMachine = lMachineDb.getMachine(machineID)
                setCurrentExercise(lMachine.name)
                mainActivity.findViewById<View>(R.id.drawer_layout).requestFocus()
                hideKeyboard()
                if (machineListDialog!!.isShowing) {
                    machineListDialog!!.dismiss()
                }
            }
            val builder = AlertDialog.Builder(v.context)
            builder.setTitle(R.string.selectMachineDialogLabel)
            builder.setView(machineList)
            machineListDialog = builder.create()
            machineListDialog!!.show()
        }
    }
    private val onItemClickFilterList = OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long -> setCurrentExercise(binding.exerciseEdit.text.toString()) }

    //Required for cardio/duration
    private val clickDateEdit = OnClickListener { v: View ->
        when (v.id) {
            R.id.durationEdit -> showTimePicker(binding.durationEdit)
            R.id.exerciseEdit -> {
//                machineImage.setImageResource(R.drawable.ic_gym_bench_50dp)
                binding.minMaxLayout.visibility = GONE
            }
        }
    }

    private fun updateNote() {
        val previousNote = exercisesFromProgram[currentExerciseOrder].note
        if (binding.notesInExercise.text.toString() != previousNote) {
            daoExerciseInProgram.updateString(exercisesFromProgram[currentExerciseOrder],
                DAOExerciseInProgram.NOTES, binding.notesInExercise.text.toString())
            exercisesFromProgram[currentExerciseOrder].note = binding.notesInExercise.text.toString()
        }
    }

    private fun showDeleteDialog(idToDelete: Long) {
        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(resources.getText(R.string.areyousure).toString())
            .setCancelText(resources.getText(R.string.global_no).toString())
            .setConfirmText(resources.getText(R.string.global_yes).toString())
            .showCancelButton(true)
            .setConfirmClickListener { sDialog: SweetAlertDialog ->
                daoRecord.deleteRecord(idToDelete)
                updateRecordTable(binding.exerciseEdit.text.toString())
                KToast.infoToast(requireActivity(), resources.getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG)
                sDialog.dismissWithAnimation()
            }
            .show()
    }

    override fun onStart() {
        super.onStart()
        mainActivity = requireActivity() as MainActivity
    }

    val name: String?
        get() {
            return requireArguments().getString("name")
        }

    @SuppressLint("CommitTransaction")
    private fun showTimePicker(timeTextView: TextView?) {
        val tx = timeTextView?.text.toString()
        val hour: Int = try {
            tx.substring(0, 2).toInt()
        } catch (e: Exception) {
            0
        }
        val min: Int = try {
            tx.substring(3, 5).toInt()
        } catch (e: Exception) {
            0
        }
        val sec: Int = try {
            tx.substring(6).toInt()
        } catch (e: Exception) {
            0
        }
        if (timeTextView!!.id == R.id.durationEdit) {
            val mDurationFrag = TimePickerDialogFragment.newInstance(durationSet, hour, min, sec)
            val fm = requireActivity().supportFragmentManager
            mDurationFrag.show(fm.beginTransaction(), "dialog_time")
        }
    }

    val fragment: ProgramRunner
        get() = this

    private val profil: Profile?
        get() = mainActivity.currentProfile

    val machine: String
        get() = binding.exerciseEdit.text.toString()

    private fun setCurrentExercise(machineStr: String) {
        if (machineStr.isEmpty()) {
            binding.imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            binding.minMaxLayout.visibility = GONE
            return
        }
        val lMachine = mDbMachine.getMachine(machineStr)
        if (lMachine == null) {
            binding.exerciseEdit.setText("")
            binding.imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            changeExerciseTypeUI(TYPE_FONTE)
            return
        }
        // Update EditView
        binding.exerciseEdit.setText(lMachine.name)
        // Update exercise Image
        binding.imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
        val imgUtil = ImageUtil()
        ImageUtil.setThumb(binding.imageExerciseThumb, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one
        // Update Table
        updateRecordTable(lMachine.name)
        // Update display type
        changeExerciseTypeUI(lMachine.type)
        // Update last values
        updateLastRecord(lMachine)
    }

    @SuppressLint("SetTextI18n")
    private fun setRunningExercise(exercise: ExerciseInProgram) {
        // Update EditView
        binding.exerciseEdit.setText(exercise.exerciseName)
        // Update exercise Image
        when (exercise.type) {
            TYPE_CARDIO -> {
                binding.imageExerciseThumb.setImageResource(R.drawable.ic_training_white_50dp)
            }
            TYPE_STATIC -> {
                binding.imageExerciseThumb.setImageResource(R.drawable.ic_static)
                val staticPrepareTime = 5
                binding.staticFillBackgroundProgress.setDuration(((exercise.seconds + staticPrepareTime) * progressScaleFix).toLong())
                staticTimer = Rx2Timer.builder()
                    .initialDelay(0)
                    .take(exercise.seconds + staticPrepareTime)
                    .onEmit { count ->
                        binding.staticFillBackgroundProgress.setProgress(count.toInt() * progressScaleFix)
                        if (count < 60) {
                            binding.countDownStatic.text = getString(R.string.count_string, count)
                        } else {
                            val minutes: Int = ((count % 3600) / 60).toInt()
                            val seconds: Int = (count % 60).toInt()
                            binding.countDownStatic.text = getString(R.string.static_counter_minutes, minutes, seconds)
                        }
                    }
                    .onError { binding.countDownStatic.text = getString(R.string.error) }
                    .onComplete {
                        val staticFinishStr = getString(R.string.End) + " " + exercise.seconds.toString() + " " + getString(R.string.SecondsLabel_short)
                        binding.countDownStatic.text = staticFinishStr
                        if (requireContext().getSharedPreferences("playStaticExerciseFinishSound", Context.MODE_PRIVATE).getBoolean("playStaticExerciseFinishSound", true)) {
                            val mediaPlayer = MediaPlayer()
                            try {
                                val myUri: Uri = Uri.parse(requireContext().getSharedPreferences("staticSound", Context.MODE_PRIVATE).getString("staticSound", RingtoneManager.getDefaultUri(R.raw.chime).toString()))
                                mediaPlayer.setDataSource(this.requireContext(), myUri)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            try {
                                mediaPlayer.prepare()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            mediaPlayer.start()
                        }
                    }
                    .build()
            }
            else -> {
                binding.imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            }
        }
        val lMachine = mDbMachine.getMachine(exercise.exerciseName)
        if (lMachine != null) {
            val imgUtil = ImageUtil()
            ImageUtil.setThumb(binding.imageExerciseThumb, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one
        }
        changeExerciseTypeUI(exercise.type)
        updateRecordTable(exercise.exerciseName)
        binding.notesInExercise.setText(exercise.note)
        when (exercise.type) {
            TYPE_FONTE -> {
                binding.repsPicker.progress = exercise.repetition
                binding.seriesEdit.setText(exercise.serie.toString())
                binding.restTimeEdit.setText(exercise.secRest.toString())
                binding.poidsEdit.setText(exercise.poids.toString())
            }
            TYPE_CARDIO -> {
                binding.durationEdit.text = DateConverter.durationToHoursMinutesSecondsStr(exercise.duration)
                binding.distanceEdit.setText(exercise.distance.toString())
                binding.unitDistanceSpinner.setSelection(exercise.distanceUnit, false)
            }
            TYPE_STATIC -> {
                binding.seriesEdit.setText(exercise.serie.toString())
                binding.secondsEdit.setText(exercise.seconds.toString())
                binding.poidsEdit.setText(exercise.poids.toString())
                binding.restTimeEdit.setText(exercise.secRest.toString())
            }
        }
    }

    private fun setCurrentMachine(machineStr: String, exerciseType: Int) {
        if (machineStr.isEmpty()) {
            binding.imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            binding.minMaxLayout.visibility = GONE
            return
        }
        val lMachine = mDbMachine.getMachine(machineStr)
        if (lMachine == null) {
            binding.exerciseEdit.setText("")
            binding.imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            changeExerciseTypeUI(TYPE_FONTE)
            updateMinMax(null)
            return
        }

        binding.exerciseEdit.setText(lMachine.name)
        // Update exercise Image
        when (exerciseType) {
            TYPE_CARDIO -> {
                binding.imageExerciseThumb.setImageResource(R.drawable.ic_training_white_50dp)
            }
            TYPE_STATIC -> {
                binding.imageExerciseThumb.setImageResource(R.drawable.ic_static)
            }
            else -> {
                binding.imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            }
        }
        binding.imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
        val imgUtil = ImageUtil()
        ImageUtil.setThumb(binding.imageExerciseThumb, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one

        updateRecordTable(lMachine.name)
        changeExerciseTypeUI(exerciseType)

        updateMinMax(lMachine)
        updateLastRecord(lMachine)
    }

    @SuppressLint("SetTextI18n")
    private fun updateMinMax(m: Machine?) {
        var unitStr: String
        var weight: Float
        if (getProfilFromMain() != null && m != null) {
            if (m.type == TYPE_FONTE || m.type == TYPE_STATIC) {
                val minValue: Weight? = strengthRecordsDao.getMin(getProfilFromMain(), m)
                if (minValue != null) {
                    binding.minMaxLayout.visibility = VISIBLE
                    if (minValue.storedUnit == UnitConverter.UNIT_LBS) {
                        weight = UnitConverter.KgtoLbs(minValue.storedWeight)
                        unitStr = requireContext().getString(R.string.LbsUnitLabel)
                    } else {
                        weight = minValue.storedWeight
                        unitStr = requireContext().getString(R.string.KgUnitLabel)
                    }
                    val numberFormat = DecimalFormat("#.##")
                    binding.minText.text = numberFormat.format(weight.toDouble()) + " " + unitStr
                    val maxValue: Weight = strengthRecordsDao.getMax(getProfilFromMain(), m)
                    if (maxValue.storedUnit == UnitConverter.UNIT_LBS) {
                        weight = UnitConverter.KgtoLbs(maxValue.storedWeight)
                        unitStr = requireContext().getString(R.string.LbsUnitLabel)
                    } else {
                        weight = maxValue.storedWeight
                        unitStr = requireContext().getString(R.string.KgUnitLabel)
                    }
                    binding.maxText.text = numberFormat.format(weight.toDouble()) + " " + unitStr
                } else {
                    binding.minText.text = "-"
                    binding.maxText.text = "-"
                    binding.minMaxLayout.visibility = GONE
                }
            } else if (m.type == TYPE_CARDIO) {
                binding.minMaxLayout.visibility = GONE
            }
        } else {
            binding.minText.text = "-"
            binding.maxText.text = "-"
            binding.minMaxLayout.visibility = GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLastRecord(m: Machine) {
        val lLastRecord = daoRecord.getLastExerciseRecord(m.id, profil)
        // Default Values
        binding.seriesEdit.setText("1")
        binding.repsPicker.progress = 10
        binding.secondsEdit.setText("60")
        binding.poidsEdit.setText("50")
        binding.distanceEdit.setText("1")
        binding.durationEdit.text = "00:10:00"
        if (lLastRecord != null) {
            if (lLastRecord.type == TYPE_FONTE) {
                val lLastBodyBuildingRecord = lLastRecord as Fonte
                if (lLastBodyBuildingRecord.serie > 1) { //only show when more then one to keep interface clean
                    binding.serieCardView.visibility = VISIBLE
                }
                binding.seriesEdit.setText(lLastBodyBuildingRecord.serie.toString())
                binding.repsPicker.progress = lLastBodyBuildingRecord.repetition
                binding.unitShow.text = "kg"
                if (lLastBodyBuildingRecord.unit == UnitConverter.UNIT_LBS)
                    binding.unitShow.text = "Lbs"
                val numberFormat = DecimalFormat("#.##")
                if (lLastBodyBuildingRecord.unit == UnitConverter.UNIT_LBS) binding.poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastBodyBuildingRecord.poids).toDouble())) else binding.poidsEdit.setText(numberFormat.format(lLastBodyBuildingRecord.poids.toDouble()))
            } else if (lLastRecord.type == TYPE_CARDIO) {
                val lLastCardioRecord = lLastRecord as Cardio
                binding.durationEdit.text = DateConverter.durationToHoursMinutesSecondsStr(lLastCardioRecord.duration)
                binding.unitDistanceSpinner.setSelection(lLastCardioRecord.distanceUnit)
                val numberFormat = DecimalFormat("#.##")
                if (lLastCardioRecord.distanceUnit == UnitConverter.UNIT_MILES) binding.distanceEdit.setText(numberFormat.format(UnitConverter.KmToMiles(lLastCardioRecord.distance).toDouble())) else binding.distanceEdit.setText(numberFormat.format(lLastCardioRecord.distance.toDouble()))
            } else if (lLastRecord.type == TYPE_STATIC) {
                val lLastStaticRecord = lLastRecord as StaticExercise
                binding.seriesEdit.setText(lLastStaticRecord.serie.toString())
                binding.secondsEdit.setText(lLastStaticRecord.second.toString())
                binding.unitShow.text = "kg"
                if (lLastStaticRecord.unit == UnitConverter.UNIT_LBS)
                    binding.unitShow.text = "Lbs"
                val numberFormat = DecimalFormat("#.##")
                if (lLastStaticRecord.unit == UnitConverter.UNIT_LBS) binding.poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastStaticRecord.poids).toDouble())) else binding.poidsEdit.setText(numberFormat.format(lLastStaticRecord.poids.toDouble()))
            }
        }
    }

    private fun updateRecordTable(exerciseName: String) { // Records from records table
        mainActivity.currentMachine = exerciseName
        requireView().post {
            val c: Cursor?
            val oldCursor: Cursor
            //Get results
            val limitShowedResults = 10
            c = (daoRecord.getAllRecordByMachines(profil, exerciseName, limitShowedResults)
                ?: return@post)
            if (c.count == 0) {
                binding.recordList.adapter = null
            } else {
                if (binding.recordList.adapter == null) {
                    val mTableAdapter = RecordCursorAdapter(mainActivity, c, 0, itemClickDeleteRecord, itemClickCopyRecord)
                    mTableAdapter.setFirstColorOdd(lTableColor)
                    binding.recordList.adapter = mTableAdapter
                } else {
                    val mTableAdapter = binding.recordList.adapter as RecordCursorAdapter
                    mTableAdapter.setFirstColorOdd(lTableColor)
                    oldCursor = mTableAdapter.swapCursor(c)
                    oldCursor?.close()
                }
            }
        }
    }

    private fun getProfilFromMain(): Profile? {
        return mainActivity.currentProfile
    }

    @SuppressLint("SetTextI18n")
    private fun refreshData() {
        if (profil != null) {
            daoExerciseInProgram.setProfile(profil)
            if (exercisesFromProgram.isNotEmpty()) {
                val currentExercise = exercisesFromProgram[currentExerciseOrder]
                setRunningExercise(currentExercise)
                updateRecordTable(currentExercise.exerciseName)
            }
        }
    }

    private fun changeExerciseTypeUI(pType: Int) {
        binding.saveWeight.visibility = GONE
        binding.saveReps.visibility = GONE
        when (pType) {
            TYPE_CARDIO -> {
                binding.serieCardView.visibility = GONE
                binding.repetitionCardView.visibility = GONE
                binding.weightCardView.visibility = GONE
                binding.secondsCardView.visibility = GONE
                binding.distanceCardView.visibility = VISIBLE
                binding.durationCardView.visibility = VISIBLE
                binding.staticFillBackgroundProgress.visibility = GONE
                selectedType = TYPE_CARDIO
            }
            TYPE_STATIC -> {
                binding.serieCardView.visibility = GONE
                binding.repetitionCardView.visibility = GONE
                binding.secondsCardView.visibility = VISIBLE
                binding.weightCardView.visibility = VISIBLE
                binding.restTimeLayout.visibility = VISIBLE
                binding.distanceCardView.visibility = GONE
                binding.durationCardView.visibility = GONE
                binding.staticFillBackgroundProgress.visibility = VISIBLE
                binding.staticFillBackgroundProgress.setOnClickListener(clickStaticTimer)
                binding.staticFillBackgroundProgress.setOnLongClickListener(clickResetStaticTimer)
                binding.resetStaticTimerButton.setOnClickListener(clickStaticReset)
                selectedType = TYPE_STATIC
            }
            TYPE_FONTE -> {
                binding.serieCardView.visibility = GONE
                binding.repetitionCardView.visibility = VISIBLE
                binding.secondsCardView.visibility = GONE
                binding.weightCardView.visibility = VISIBLE
                binding.restTimeLayout.visibility = VISIBLE
                binding.distanceCardView.visibility = GONE
                binding.durationCardView.visibility = GONE
                binding.staticFillBackgroundProgress.visibility = GONE
                selectedType = TYPE_FONTE
            }
            else -> {
                binding.serieCardView.visibility = GONE
                binding.repetitionCardView.visibility = VISIBLE
                binding.secondsCardView.visibility = GONE
                binding.weightCardView.visibility = VISIBLE
                binding.restTimeLayout.visibility = VISIBLE
                binding.distanceCardView.visibility = GONE
                binding.durationCardView.visibility = GONE
                binding.staticFillBackgroundProgress.visibility = GONE
                selectedType = TYPE_FONTE
            }
        }
    }

    private fun saveSharedParams() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.putString("restTime", binding.restTimeEdit.text.toString())
//        editor?.putBoolean("restCheck", restTimeCheck.isChecked)
        editor?.putBoolean("showDetails", binding.restControlLayout.isShown)
        editor?.apply()
    }

    private fun restoreSharedParams() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        binding.restTimeEdit.setText(sharedPref?.getString("restTime", ""))
//        restTimeCheck.isChecked = sharedPref!!.getBoolean("restCheck", true)
    }

    private fun hideKeyboard() {
        try {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        } catch (ex: Exception) {
            Timber.d(ex, "EX %s", ex.message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): ProgramRunner {
            val f = ProgramRunner()
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.arguments = args
            return f
        }
    }
}
