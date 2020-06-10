package com.easyfitness.programs

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.easyfitness.DAO.DAOProfil
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.DAORecord
import com.easyfitness.DAO.Program
import com.easyfitness.MainActivity
import com.easyfitness.R
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import com.onurkaganaldemir.ktoastlib.KToast
import kotlinx.android.synthetic.main.program_pager.*

class ProgramDetailsPager : Fragment() {
    private var machineIdArg: Long = 0
    private var machineProfilIdArg: Long = 0
    private var pagerAdapter: FragmentPagerItemAdapter? = null
    private lateinit var programSave: ImageButton
    private var program: Program? = null
    private var toBeSaved = false
    private val onClickToolbarItem = View.OnClickListener { v: View ->
        when (v.id) {
            R.id.saveButton -> {
                saveMachine()
                requireActivity().findViewById<View>(R.id.tab_machine_details).requestFocus()
            }
            R.id.deleteButton -> deleteMachine()
            else -> saveMachineDialog()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.program_pager, container, false)

        // Locate the viewpager in activity_main.xml
        val mViewPager: ViewPager = view.findViewById(R.id.program_pager)
        if (mViewPager.adapter == null) {
            val args = this.arguments
            machineIdArg = args!!.getLong("programID")
            machineProfilIdArg = args.getLong("programProfile")
            pagerAdapter = FragmentPagerItemAdapter(
                childFragmentManager, FragmentPagerItems.with(context)
//                .add(R.string.ExercisesInProgramLabel, ExercisesInProgramFragment::class.java)
                .add(R.string.ProgramsLabel, ProgramDetailsFragment::class.java, args)
                .create())
            mViewPager.adapter = pagerAdapter
            val viewPagerTab: SmartTabLayout = view.findViewById(R.id.viewpagertab)
            viewPagerTab.setViewPager(mViewPager)
            viewPagerTab.setOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    val frag1 = pagerAdapter!!.getPage(position)
                    frag1?.onHiddenChanged(false) // Refresh data
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
        val mDbProgram = DAOProgram(context)

        (activity as MainActivity?)!!.activityToolbar.visibility = View.GONE
        val topToolbar: Toolbar = view.findViewById(R.id.actionToolbarProgram)
        topToolbar.setNavigationIcon(R.drawable.ic_back)
        topToolbar.setNavigationOnClickListener(onClickToolbarItem)
        val machineDelete = view.findViewById<ImageButton>(R.id.deleteButton)
        programSave = view.findViewById(R.id.saveButton)
        program = mDbProgram.getRecord(machineIdArg)
        programSave.visibility = View.GONE // Hide Save button by default
//        machineDelete.setOnClickListener(onClickToolbarItem);
        // Inflate the layout for this fragment
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        actionToolbarProgram.visibility=View.VISIBLE
    }
    fun requestForSave() {
        toBeSaved = true // setting state
        programSave.visibility = View.VISIBLE
    }

    private fun saveMachineDialog() {
        if (getExerciseFragment()!!.toBeSaved || toBeSaved) {
            // Afficher une boite de dialogue pour confirmer
            val backDialogBuilder = AlertDialog.Builder(activity)
            backDialogBuilder.setTitle(resources.getText(R.string.global_confirm))
            backDialogBuilder.setMessage(resources.getText(R.string.backDialog_confirm_text))

            // Si oui, supprimer la base de donnee et refaire un Start.
            backDialogBuilder.setPositiveButton(resources.getString(R.string.global_yes)) { _: DialogInterface?, _: Int ->
                if (saveMachine()) {
                    requireActivity().onBackPressed()
                }
            }
            backDialogBuilder.setNegativeButton(resources.getString(R.string.global_no)) { _: DialogInterface?, _: Int -> requireActivity().onBackPressed() }
            val backDialog = backDialogBuilder.create()
            backDialog.show()
        } else {
            requireActivity().onBackPressed()
        }
    }

    private fun saveMachine(): Boolean {
        var result = false
        val initialMachine: Program = program!!
        val newMachine = getExerciseFragment()!!.machine
        val lMachineName = newMachine?.programName // Potentiel nouveau nom dans le EditText
        val mDbProgram = DAOProgram(context)

        // Si le nom est different du nom actuel
        if (lMachineName == "") {
            KToast.warningToast(activity, resources.getText(R.string.name_is_required).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
        } else if (initialMachine.programName != lMachineName) {
            val machineWithSameName: Program? = mDbProgram.getRecord(lMachineName)
            // Si une machine existe avec le meme nom => Merge
            if (newMachine != null) {
                if (machineWithSameName != null && newMachine.id != machineWithSameName.id && newMachine.type != machineWithSameName.type) {
                    val dialogBuilder = AlertDialog.Builder(this.activity)
                    dialogBuilder.setTitle(requireActivity().resources.getText(R.string.global_warning))
                    dialogBuilder.setMessage(R.string.renameMachine_error_text2)
                    dialogBuilder.setPositiveButton(resources.getText(R.string.global_yes)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                } else if (machineWithSameName != null && newMachine.id != machineWithSameName.id && newMachine.type == machineWithSameName.type) {
                    val dialogBuilder = AlertDialog.Builder(this.activity)
                    dialogBuilder.setTitle(resources.getText(R.string.global_warning))
                    dialogBuilder.setMessage(resources.getText(R.string.renameMachine_warning_text))
                    // Si oui, supprimer la base de donnee et refaire un Start.
                    dialogBuilder.setPositiveButton(resources.getText(R.string.global_yes)) { _: DialogInterface?, which: Int ->
                        // Rename all the records with that machine and rename them
                        val lDbRecord = DAORecord(context)
                        val mDbProfil = DAOProfil(context)
                        val lProfile = mDbProfil.getProfil(machineProfilIdArg)
                        val listRecords = lDbRecord.getAllRecordByMachinesArray(lProfile, initialMachine.programName) // Recupere tous les records de la machine courante
                        for (record in listRecords) {
                            record.exercise = newMachine.programName // Change avec le nouveau nom. Normalement pas utile.
                            record.exerciseKey = machineWithSameName.id // Met l'ID de la nouvelle machine
                            lDbRecord.updateRecord(record) // Met a jour
                        }
                        mDbProgram.delete(initialMachine) // Supprime l'ancienne machine
                        toBeSaved = false
                        saveButton.visibility = View.GONE
                        requireActivity().onBackPressed()
                    }
                    dialogBuilder.setNegativeButton(resources.getText(R.string.global_no)) { dialog: DialogInterface, _: Int ->
                        // Do nothing but close the dialog
                        dialog.dismiss()
                    }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                } else {
//                    newMachine.favorite = favoriteButton.isFavorite()
                    mDbProgram.updateRecord(newMachine)

                    // Rename all the records with that machine and rename them
                    val lDbRecord = DAORecord(context)
                    val mDbProfil = DAOProfil(context)
                    val lProfile = mDbProfil.getProfil(machineProfilIdArg)
                    val listRecords = lDbRecord.getAllRecordByMachinesArray(lProfile, initialMachine.programName) // Recupere tous les records de la machine courante
                    for (record in listRecords) {
                        record.exercise = lMachineName // Change avec le nouveau nom (DEPRECATED)
                        lDbRecord.updateRecord(record) // met a jour
                    }
                    saveButton.visibility = View.GONE
                    toBeSaved = false
                    getExerciseFragment()!!.programSaved()
                    result = true
                }
            }
        } else {
            // Si le nom n'a pas ete modifie.
//            newMachine.favorite = favoriteButton.isFavorite()
            if (newMachine != null) {
                mDbProgram.updateRecord(newMachine)
            }
            saveButton.visibility = View.GONE
            toBeSaved = false
            getExerciseFragment()!!.programSaved()
            result = true
        }
        return result
    }

    private fun deleteMachine() {
        // afficher un message d'alerte
        val deleteDialogBuilder = AlertDialog.Builder(this.activity)
        deleteDialogBuilder.setTitle(resources.getText(R.string.global_confirm))
        deleteDialogBuilder.setMessage(resources.getText(R.string.deleteMachine_confirm_text))

        // Si oui, supprimer la base de donnee et refaire un Start.
        deleteDialogBuilder.setPositiveButton(resources.getString(R.string.global_yes)) { _: DialogInterface?, _: Int ->
            deleteRecordsAssociatedToProgram()
            val mDbProgram = DAOProgram(context)
            mDbProgram.delete(program)
            requireActivity().onBackPressed()
        }
        deleteDialogBuilder.setNegativeButton(resources.getString(R.string.global_no)) { dialog: DialogInterface, _: Int ->
            // Do nothing
            dialog.dismiss()
        }
        val deleteDialog = deleteDialogBuilder.create()
        deleteDialog.show()
    }

    private fun deleteRecordsAssociatedToProgram() {
        val mDbRecord = DAORecord(context)
        val mDbProfil = DAOProfil(context)
        val lProfile = mDbProfil.getProfil(machineProfilIdArg)
        //TODO
//        val listRecords = mDbRecord.getAllRecordByMachinesArray(lProfile, program!!.programName)
//        for (record in listRecords) {
//            mDbRecord.deleteRecord(record.id)
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()

        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.machine_details_menu, menu)
        val item = menu.findItem(R.drawable.ic_save_black_24dp)
        item.isVisible = toBeSaved
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getExerciseFragment(): ProgramDetailsFragment? {
        return pagerAdapter!!.getPage(0) as ProgramDetailsFragment
    }

    private val viewPagerAdapter: FragmentPagerItemAdapter?
        get() = (requireView().findViewById<View>(R.id.program_pager) as ViewPager).adapter as FragmentPagerItemAdapter?

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            if (viewPagerAdapter != null) {
                var frag1: Fragment?
                for (i in 0 until viewPagerAdapter!!.count) {
                    frag1 = viewPagerAdapter!!.getPage(i)
                    frag1?.onHiddenChanged(false) // Refresh data
                }
            }
        }
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(machineId: Long, machineProfile: Long): ProgramDetailsPager {
            val f = ProgramDetailsPager()

            // Supply index input as an argument.
            val args = Bundle()
            args.putLong("programID", machineId)
            args.putLong("programProfile", machineProfile)
            f.arguments = args
            return f
        }
    }
}
