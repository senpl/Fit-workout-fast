package com.easyfitness.programs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easyfitness.*
import com.easyfitness.DAO.*
import com.easyfitness.DAO.DAOMachine.*
import com.easyfitness.databinding.TabProgramWithExercisesBinding
import com.easyfitness.machines.ExerciseDetailsPager
import com.easyfitness.machines.MachineCursorAdapter
import com.fitworkoutfast.MainActivity
import com.easyfitness.utils.BtnOnPostiomClickListener
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.ImageUtil
import com.easyfitness.utils.UnitConverter
import com.ikovac.timepickerwithseconds.view.MyTimePickerDialog
import com.onurkaganaldemir.ktoastlib.KToast
import timber.log.Timber
import java.lang.Integer.parseInt
import java.util.*
import androidx.core.view.isGone
import androidx.core.content.edit
import com.easyfitness.DAO.DAOMachine.Companion.TYPE_CARDIO
import com.easyfitness.DAO.DAOMachine.Companion.TYPE_STATIC
import com.easyfitness.DAO.DAOMachine.Companion.TYPE_STRENGTH
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExercisesInProgramFragment : Fragment(R.layout.tab_program_with_exercises) {
    private lateinit var mainActivity: MainActivity
    private var lTableColor = 1
    private var machineListDialog: AlertDialog? = null
    private var selectedType = TYPE_STRENGTH
    private lateinit var daoProgram: DAOProgram
    private var programId: Long = 1
    var programs: MutableList<String>? = null
    private var exercisesList: MutableList<ExerciseInProgram> = ArrayList<ExerciseInProgram>().toMutableList()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var _binding: TabProgramWithExercisesBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabProgramWithExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        daoProgram = DAOProgram(context)
        programs = daoProgram.allProgramsNames
        daoExerciseInProgram = DAOExerciseInProgram(requireContext())
        if (programs == null || programs!!.isEmpty()) {
            val profileId: Long = (requireActivity() as MainActivity).currentProfile!!.id
            val programsFragment = ProgramsFragment.newInstance("", profileId)
            Toast.makeText(context, R.string.add_program_first, Toast.LENGTH_LONG).show()
            requireActivity().supportFragmentManager.commit {
                addToBackStack(null)
                add(R.id.fragment_container, programsFragment)
            }
        } else {
            programId = daoProgram.getRecord(programs!![0])!!.id
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, programs!!)
            binding.programSelect.adapter = adapter
            exercisesList = daoExerciseInProgram.getAllExerciseInProgram(programId).toMutableList()
            binding.exercisesRecycler.adapter = ExerciseInProgramAdapter(requireContext(), exercisesList, itemClickDeleteRecord, null)
            binding.exercisesRecycler.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

            binding.programSelect.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View?, position: Int, id: Long) {
                    programId = daoProgram.getRecord(programs!![position])!!.id
                    refreshData()
                    Toast.makeText(context, getString(R.string.program_selection) + " " + programs!![position], Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
        binding.detailsLayout.visibility = View.VISIBLE
        binding.addButton.setOnClickListener(clickAddButton)
        binding.machineListButton.setOnClickListener(onClickMachineListWithIcons) //onClickMachineList
        binding.seriesEdit.onFocusChangeListener = touchRazEdit
        binding.repetitionEdit.onFocusChangeListener = touchRazEdit
        binding.poidsEdit.onFocusChangeListener = touchRazEdit
        binding.distanceEdit.onFocusChangeListener = touchRazEdit
        binding.durationEdit.setOnClickListener(clickDateEdit)
        binding.secondsEdit.onFocusChangeListener = touchRazEdit
        binding.exerciseEdit.setOnKeyListener(checkExerciseExists)
        binding.exerciseEdit.onFocusChangeListener = touchRazEdit
        binding.exerciseEdit.onItemClickListener = onItemClickFilterList
        binding.restTimeEdit.onFocusChangeListener = restTimeEditChange
        binding.youtubeStartUrl.onFocusChangeListener = urlEditChange
        binding.youtubeEndUrl.onFocusChangeListener = urlEditChange
        binding.restTimeCheck.setOnCheckedChangeListener(restTimeCheckChange)
        binding.bodybuildingSelector.setOnClickListener(clickExerciseTypeSelector)
        binding.cardioSelector.setOnClickListener(clickExerciseTypeSelector)
        binding.staticExerciseSelector.setOnClickListener(clickExerciseTypeSelector)
        restoreSharedParams()
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        var weightUnit: Int
        try {
            weightUnit = sharedPreferences?.getString(SettingsFragment.WEIGHT_UNIT_PARAM, "0")?.toInt()!!
        } catch (_: Exception) {
            Timber.d("Conversion Not important")
            weightUnit = UnitConverter.UNIT_KG
        }
        binding.unitSpinner.setSelection(weightUnit)
        val distanceUnit: Int = try {
             sharedPreferences?.getString(SettingsFragment.DISTANCE_UNIT_PARAM, "0")?.toInt()!!
        } catch (_: Exception) {
            UnitConverter.UNIT_KM
        }
        binding.unitDistanceSpinner.setSelection(distanceUnit)
        // Initialization of the database
        mDbMachine = DAOMachine(context)
        selectedType = TYPE_STRENGTH
        binding.exerciseImage.setOnClickListener {
            val m = mDbMachine.getMachine(binding.exerciseEdit.text.toString())
            if (m != null) {
                val profileId: Long = (requireActivity() as MainActivity).currentProfile!!.id
                val machineDetailsFragment = ExerciseDetailsPager.newInstance(m.id, profileId)
                requireActivity().supportFragmentManager.commit {
                    addToBackStack(null)
                    add(R.id.fragment_container, machineDetailsFragment)
                }
            }
        }

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, dragged: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = dragged.getBindingAdapterPosition()
                val toPosition = target.getBindingAdapterPosition()
                val listOfExercises: List<ExerciseInProgram> = exercisesList
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(listOfExercises, i, i + 1)
                        val order1: Long = listOfExercises[i].order
                        val order2: Long = listOfExercises[i + 1].order
                        listOfExercises[i].order = order2
                        listOfExercises[i + 1].order = order1
                        daoExerciseInProgram.updateString(listOfExercises[i], DAOExerciseInProgram.ORDER_EXECUTION, order2.toString())
                        daoExerciseInProgram.updateString(listOfExercises[i + 1], DAOExerciseInProgram.ORDER_EXECUTION, order1.toString())
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(listOfExercises, i, i - 1)
                        val order1: Long = listOfExercises[i].order
                        val order2: Long = listOfExercises[i - 1].order
                        listOfExercises[i].order = order2
                        listOfExercises[i - 1].order = order1
                        daoExerciseInProgram.updateString(listOfExercises[i], DAOExerciseInProgram.ORDER_EXECUTION, order2.toString())
                        daoExerciseInProgram.updateString(listOfExercises[i - 1], DAOExerciseInProgram.ORDER_EXECUTION, order1.toString())
                    }
                }
                binding.exercisesRecycler.adapter!!.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        touchHelper.attachToRecyclerView(binding.exercisesRecycler)
        linearLayoutManager = LinearLayoutManager(requireContext())
        binding.exercisesRecycler.layoutManager = linearLayoutManager
    }


    private val durationSet = MyTimePickerDialog.OnTimeSetListener { _ , hourOfDay: Int, minute: Int, second: Int ->
        val strMinute: String = if (minute < 10) "0$minute" else minute.toString()
        val strHour: String = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay.toString()
        val strSecond: String = if (second < 10) "0$second" else second.toString()
        val date = "$strHour:$strMinute:$strSecond"
        binding.durationEdit.text = date
        hideKeyboard()
    }
    private lateinit var daoExerciseInProgram: DAOExerciseInProgram
    private lateinit var mDbMachine: DAOMachine
    private val clickExerciseTypeSelector = View.OnClickListener { v: View ->
        when (v.id) {
            R.id.staticExerciseSelector -> changeExerciseTypeUI(TYPE_STATIC, true)
            R.id.cardioSelector -> changeExerciseTypeUI(TYPE_CARDIO, true)
            R.id.bodybuildingSelector -> changeExerciseTypeUI(TYPE_STRENGTH, true)
            else -> changeExerciseTypeUI(TYPE_STRENGTH, true)
        }
    }
    private val checkExerciseExists = View.OnKeyListener { _: View?, _: Int, _: KeyEvent? ->
        val lMach = mDbMachine.getMachine(binding.exerciseEdit.text.toString())
        if (lMach == null) {
            showExerciseTypeSelector(true)
        } else {
            changeExerciseTypeUI(lMach.type, false)
        }
        false
    }
    private val restTimeEditChange = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
        if (!hasFocus) {
            saveSharedParams()
        }
    }
    private val urlEditChange = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
        if (!hasFocus) {
            saveSharedVideo()
        }
    }
    private val restTimeCheckChange = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, _: Boolean -> saveSharedParams() }
    private val itemClickDeleteRecord = BtnOnPostiomClickListener { idToDelete: Long, positionOnList: Int -> showDeleteDialog(idToDelete, positionOnList) }

    @SuppressLint("SetTextI18n")
    private val clickAddButton = View.OnClickListener {
        if (binding.exerciseEdit.text.toString().isEmpty()) {
            KToast.warningToast(requireActivity(), resources.getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
            return@OnClickListener
        }
        val exerciseType: Int = selectedType
        var restTime = 60
        try {
            restTime = binding.restTimeEdit.text.toString().toInt()
        } catch (_: NumberFormatException) {
            binding.restTimeEdit.setText("60")
        }
        val videoUrl=binding.youtubeStartUrl.text.toString()
        var videoSeconds = 50
        if(binding.youtubeEndUrl.text.toString().contains("&s=")){
            try {
                var url = binding.youtubeEndUrl.text.toString()
                url = url.substring(url.indexOf("&s=") + "&s=".length)
                if (url.contains("&")) {
                    url = url.substring(0, url.indexOf("&"))
                }
                val secondsToEnd = parseInt(url)
                videoSeconds = secondsToEnd
            } catch (_: Exception) {
            }
        }
        val currentTimeAsOrder: Long = System.currentTimeMillis()
        when (exerciseType) {
            TYPE_STRENGTH -> {
                if (binding.seriesEdit.text.toString().isEmpty() ||
                    binding.repetitionEdit.text.toString().isEmpty() ||
                    binding.poidsEdit.text.toString().isEmpty()) {
                    KToast.warningToast(requireActivity(), resources.getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
                    return@OnClickListener
                }
                var tmpPoids = binding.poidsEdit.text.toString().replace(",".toRegex(), ".").toFloat()  /* Weight conversion */
                var unitPoids = UnitConverter.UNIT_KG // Kg
                val mContext = requireContext()
                if (binding.unitSpinner.selectedItem.toString() == mContext.getString(R.string.LbsUnitLabel)) {
                    tmpPoids = UnitConverter.LbstoKg(tmpPoids) // Always convert to KG
                    unitPoids = UnitConverter.UNIT_LBS // LBS
                }
                daoExerciseInProgram.addRecord(
                    currentTimeAsOrder,
                    programId, restTime,
                    binding.exerciseEdit.text.toString(),
                    TYPE_STRENGTH, binding.seriesEdit.text.toString().toInt(), binding.repetitionEdit.text.toString().toInt(),
                    tmpPoids,  // Always save in KG
                    profile!!, unitPoids,  // Store Unit for future display
                    "",  //Notes,
                    "", 0f, 0, 0, 0,
                    videoUrl, videoSeconds
                )
                if (mDbMachine.getMachine(binding.exerciseEdit.text.toString()) == null)
                    mDbMachine.addMachine(binding.exerciseEdit.text.toString(), "", TYPE_STRENGTH, "", false, null)
                exercisesList = daoExerciseInProgram.getAllExerciseInProgram(programId)
                val exercise = exercisesList[exercisesList.size - 1]
                (binding.exercisesRecycler.adapter!! as ExerciseInProgramAdapter).add(exercise)
            }
            TYPE_STATIC -> {
                if (binding.seriesEdit.text.toString().isEmpty() ||
                    binding.secondsEdit.text.toString().isEmpty() ||
                    binding.poidsEdit.text.toString().isEmpty()) {
                    KToast.warningToast(requireActivity(), resources.getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
                    return@OnClickListener
                }
                /* Weight conversion */
                var tmpPoids = binding.poidsEdit.text.toString().replace(",".toRegex(), ".").toFloat()
                var unitPoids = UnitConverter.UNIT_KG // Kg
                if (binding.unitSpinner.selectedItem.toString() == requireContext().getString(R.string.LbsUnitLabel)) {
                    tmpPoids = UnitConverter.LbstoKg(tmpPoids) // Always convert to KG
                    unitPoids = UnitConverter.UNIT_LBS // LBS
                }
                try {
                    restTime = binding.restTimeEdit.text.toString().toInt()
                } catch (_: NumberFormatException) {
                    restTime = 0
                    binding.restTimeEdit.setText("0")
                }
                daoExerciseInProgram.addRecord(
                    currentTimeAsOrder,
                    programId,
                    restTime,
                    binding.exerciseEdit.text.toString(), TYPE_STATIC, binding.seriesEdit.text.toString().toInt(),
                    1, tmpPoids, profile!!, unitPoids,  // Store Unit for future display
                    "", "", 0F, 0, binding.secondsEdit.text.toString().toInt(), 0,
                    videoUrl,videoSeconds
                )
                if (mDbMachine.getMachine(binding.exerciseEdit.text.toString()) == null)
                    mDbMachine.addMachine(binding.exerciseEdit.text.toString(), "", TYPE_STATIC, "", false, null)
                exercisesList = daoExerciseInProgram.getAllExerciseInProgram(programId)
                val exercise = exercisesList[exercisesList.size - 1]
                (binding.exercisesRecycler.adapter!! as ExerciseInProgramAdapter).add(exercise)
            }
            TYPE_CARDIO -> {
                if (binding.durationEdit.text.toString().isEmpty() &&  // Only one is mandatory
                    binding.distanceEdit.text.toString().isEmpty()) {
                    KToast.warningToast(requireActivity(),
                        resources.getText(R.string.missinginfo).toString() + " Distance missing",
                        Gravity.BOTTOM, KToast.LENGTH_SHORT)
                    return@OnClickListener
                }
                var duration = 0L
                try {
                    if (binding.durationEdit.text.toString().isNotEmpty()) {
                        duration = DateConverter.durationStringToLong(binding.durationEdit.text.toString())
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    duration = 0
                }
                var distance: Float
                distance = if (binding.distanceEdit.text.toString().isEmpty()) {
                    0f
                } else {
                    binding.distanceEdit.text.toString().replace(",".toRegex(), ".").toFloat()
                }
                var unitDistance = UnitConverter.UNIT_KM
                if (binding.unitDistanceSpinner.selectedItem.toString()
                    == requireContext().getString(R.string.MilesUnitLabel)) {
                    distance = UnitConverter.MilesToKm(distance) // Always convert to KG
                    unitDistance = UnitConverter.UNIT_MILES
                }
                daoExerciseInProgram.addRecord(
                    currentTimeAsOrder,
                    programId, restTime,
                    binding.exerciseEdit.text.toString(),
                    TYPE_CARDIO,
                    1,
                    1, 0f,
                    profile!!,
                    1,
                    "",
                    "",
                    distance,
                    duration,
                    0,
                    unitDistance,
                    videoUrl,
                    videoSeconds)
                if (mDbMachine.getMachine(binding.exerciseEdit.text.toString()) == null)
                    mDbMachine.addMachine(binding.exerciseEdit.text.toString(), "", TYPE_CARDIO, "", false, null)
                exercisesList = daoExerciseInProgram.getAllExerciseInProgram(programId)
                val exercise = exercisesList[exercisesList.size - 1]
                (binding.exercisesRecycler.adapter!! as ExerciseInProgramAdapter).add(exercise)
            }
        }
        requireActivity().findViewById<View>(R.id.drawer_layout)?.requestFocus()
        hideKeyboard()
        lTableColor = (lTableColor + 1) % 2 // Change the color each time you add data
        refreshData()
        /* Reinitialisation des machines */
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_dropdown_item_1line, daoExerciseInProgram.getAllExerciseInProgramAsList(programId))
        binding.exerciseEdit.setAdapter(adapter)
        binding.addButton.setText(R.string.AddLabel)
    }
    private val onClickMachineListWithIcons = View.OnClickListener { v ->
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
                val mTableAdapter = MachineCursorAdapter(requireActivity(), c, 0, mDbMachine)
                machineList.adapter = mTableAdapter
            } else {
                val mTableAdapter = machineList.adapter as MachineCursorAdapter
                oldCursor = mTableAdapter.swapCursor(c)
                oldCursor.close()
            }
            machineList.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
                val textView = view.findViewById<TextView>(R.id.LIST_MACHINE_ID)
                val machineID = textView.text.toString().toLong()
                val lMachineDb = DAOMachine(context)
                val lMachine = lMachineDb.getMachine(machineID)
                setCurrentExercise(lMachine!!.name)
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
    private val clickDateEdit = View.OnClickListener { v: View ->
        when (v.id) {
            R.id.durationEdit -> showTimePicker(binding.durationEdit)
        }
    }
    private val touchRazEdit = OnFocusChangeListener { v: View, hasFocus: Boolean ->
        if (hasFocus) {
            when (v.id) {
                R.id.seriesEdit -> binding.seriesEdit.setText("")
                R.id.repetitionEdit -> binding.repetitionEdit.setText("")
                R.id.secondsEdit -> binding.secondsEdit.setText("")
                R.id.poidsEdit -> binding.poidsEdit.setText("")
                R.id.durationEdit -> showTimePicker(binding.durationEdit)
                R.id.distanceEdit -> binding.distanceEdit.setText("")
                R.id.exerciseEdit -> {
                    binding.exerciseImage.setImageResource(R.drawable.ic_gym_bench_50dp)
                    binding.minMaxLayout.visibility = GONE
                    showExerciseTypeSelector(true)
                }
            }
            v.post {
                val imm = Objects.requireNonNull(requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)) as InputMethodManager
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            if (v.id == R.id.exerciseEdit) { // If a creation of a new machine is not ongoing.
                if (binding.exerciseTypeSelectorLayout.isGone) setCurrentExercise(binding.exerciseEdit.text.toString())
            }
        }
    }

    private fun showDeleteDialog(idToDelete: Long, position: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.DeleteRecordDialog))
            .setMessage(resources.getString(R.string.areyousure))
            .setNegativeButton(resources.getString(R.string.global_no)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.global_yes)) { dialog, which ->
                daoExerciseInProgram.deleteRecord(idToDelete)
                (binding.exercisesRecycler.adapter!! as ExerciseInProgramAdapter).removeAt(position)
                exercisesList =
                    daoExerciseInProgram.getAllExerciseInProgram(programId).toMutableList()
                KToast.infoToast(
                    requireActivity(),
                    resources.getText(R.string.removedid).toString(),
                    Gravity.BOTTOM,
                    KToast.LENGTH_LONG
                )
            }.show()
    }

    override fun onStart() {
        super.onStart()
        mainActivity = this.activity as MainActivity
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
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
        } catch (_: Exception) {
            0
        }
        val min: Int = try {
            tx.substring(3, 5).toInt()
        } catch (_: Exception) {
            0
        }
        val sec: Int = try {
            tx.substring(6).toInt()
        } catch (_: Exception) {
            0
        }
        if (timeTextView!!.id == R.id.durationEdit) {
            val mDurationFrag = TimePickerDialogFragment.newInstance(durationSet, hour, min, sec)
            val fm = requireActivity().supportFragmentManager
            mDurationFrag.show(fm.beginTransaction(), "dialog_time")
        }
    }

    val fragment: ExercisesInProgramFragment
        get() = this

    private val profile: Profile?
        get() = mainActivity.currentProfile

    val machine: String
        get() = binding.exerciseEdit.text.toString()

    private fun setCurrentExercise(machineStr: String) {
        if (machineStr.isEmpty()) {
            binding.exerciseImage.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            showExerciseTypeSelector(true)
            binding.minMaxLayout.visibility = GONE
            return
        }
        val lMachine = mDbMachine.getMachine(machineStr)
        if (lMachine == null) {
            binding.exerciseEdit.setText("")
            binding.exerciseImage.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            changeExerciseTypeUI(TYPE_STRENGTH, true)
            return
        }
        binding.exerciseEdit.setText(lMachine.name)
        // Update exercise Image
        binding.exerciseImage.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
        val imgUtil = ImageUtil()
        ImageUtil.setThumb(binding.exerciseImage, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one
        changeExerciseTypeUI(lMachine.type, false)
    }

    @SuppressLint("SetTextI18n")
    private fun refreshData() {
        if (programs!!.size != daoProgram.allProgramsNames.size) {//only for program list refresh after add
            programs = daoProgram.allProgramsNames //update programs
            programId = daoProgram.getRecord(programs!![0])!!.id
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, programs!!)
            binding.programSelect.adapter = adapter
            binding.programSelect.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View?, position: Int, id: Long) {
                    programId = daoProgram.getRecord(programs!![position])!!.id
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        daoExerciseInProgram.setProfile(profile)
        exercisesList = daoExerciseInProgram.getAllExerciseInProgram(programId).toMutableList()
        binding.exercisesRecycler.adapter = ExerciseInProgramAdapter(requireContext(), exercisesList, itemClickDeleteRecord, null)
    }

    private fun showExerciseTypeSelector(displaySelector: Boolean) {
        if (displaySelector) binding.exerciseTypeSelectorLayout.visibility = View.VISIBLE else binding.exerciseTypeSelectorLayout.visibility = GONE
    }

    private fun changeExerciseTypeUI(pType: Int, displaySelector: Boolean) {
        showExerciseTypeSelector(displaySelector)
        when (pType) {
            TYPE_CARDIO -> {
                binding.cardioSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.record_background_odd))
                binding.bodybuildingSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                binding.staticExerciseSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                binding.serieCardView.visibility = GONE
                binding.repetitionCardView.visibility = GONE
                binding.weightCardView.visibility = GONE
                binding.secondsCardView.visibility = GONE
                binding.restTimeLayout.visibility = GONE
                binding.distanceCardView.visibility = View.VISIBLE
                binding.durationCardView.visibility = View.VISIBLE
                selectedType = TYPE_CARDIO
            }
            TYPE_STATIC -> {
                binding.cardioSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                binding.bodybuildingSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                binding.staticExerciseSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.record_background_odd))
                binding.serieCardView.visibility = View.VISIBLE
                binding.seriesEdit.setText("1")
                binding.repetitionCardView.visibility = GONE
                binding.secondsCardView.visibility = View.VISIBLE
                binding.weightCardView.visibility = View.VISIBLE
                binding.restTimeLayout.visibility = View.VISIBLE
                binding.distanceCardView.visibility = GONE
                binding.durationCardView.visibility = GONE
                selectedType = TYPE_STATIC
            }
            TYPE_STRENGTH -> {
                binding.cardioSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                binding.bodybuildingSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.record_background_odd))
                binding.staticExerciseSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                binding.serieCardView.visibility = View.VISIBLE
                binding.seriesEdit.setText("1")
                binding.repetitionCardView.visibility = View.VISIBLE
                binding.secondsCardView.visibility = GONE
                binding.weightCardView.visibility = View.VISIBLE
                binding.restTimeLayout.visibility = View.VISIBLE
                binding.distanceCardView.visibility = GONE
                binding.durationCardView.visibility = GONE
                selectedType = TYPE_STRENGTH
            }
            else -> {
                binding.cardioSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                binding.bodybuildingSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.record_background_odd))
                binding.staticExerciseSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                binding.serieCardView.visibility = View.VISIBLE
                binding.repetitionCardView.visibility = View.VISIBLE
                binding.secondsCardView.visibility = GONE
                binding.weightCardView.visibility = View.VISIBLE
                binding.restTimeLayout.visibility = View.VISIBLE
                binding.distanceCardView.visibility = GONE
                binding.durationCardView.visibility = GONE
                selectedType = TYPE_STRENGTH
            }
        }
    }

    private fun saveSharedParams() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.edit {
            this.putString("restTime", binding.restTimeEdit.text.toString())
            this.putBoolean("restCheck", binding.restTimeCheck.isChecked)
            this.putBoolean("showDetails", binding.detailsLayout.isShown)
        }
    }

    private fun saveSharedVideo() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.edit {
            this.putString("youtubeStartUrl", binding.youtubeStartUrl.text.toString())
            if (binding.youtubeEndUrl.text.toString().contains("&s=")) {
                var url = binding.youtubeEndUrl.text.toString()
                url = url.substring(url.indexOf("&s=") + "&s=".length)
                if (url.contains("&")) {
                    url = url.substring(0, url.indexOf("&"))
                }
                try {
                    val secondsToEnd = parseInt(url)
                    this.putInt("videoSeconds", secondsToEnd)
                } catch (_: Exception) {
                    this.putInt("videoSeconds", 50)
                }
            }
        }
    }

    private fun restoreSharedParams() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        binding.restTimeEdit.setText(sharedPref?.getString("restTime", ""))
        binding.restTimeCheck.isChecked = sharedPref!!.getBoolean("restCheck", true)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) refreshData()
    }

    private fun hideKeyboard() {
        val inputMethodManager = Objects.requireNonNull(requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE)) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
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
        fun newInstance(name: String?, id: Int): ExercisesInProgramFragment {
            val f = ExercisesInProgramFragment()
            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.arguments = args
            return f
        }
    }
}
