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
    private var programIdArg: Long = 0
    private var profilIdArg: Long = 0
    private var pagerAdapter: FragmentPagerItemAdapter? = null
    private lateinit var programSave: ImageButton
    private var program: Program? = null
    private var toBeSaved = false
    private val onClickToolbarItem = View.OnClickListener { v: View ->
        when (v.id) {
            R.id.saveButton -> {
                saveProgram()
                requireActivity().findViewById<View>(R.id.tab_machine_details).requestFocus()
            }
            R.id.deleteButton -> deleteProgram()
            else -> saveProgramDialog()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.program_pager, container, false)

        // Locate the viewpager in activity_main.xml
        val mViewPager: ViewPager = view.findViewById(R.id.program_pager)
        if (mViewPager.adapter == null) {
            val args = this.arguments
            programIdArg = args!!.getLong("programID")
            profilIdArg = args.getLong("programProfile")
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
        val programDelete = view.findViewById<ImageButton>(R.id.deleteButton)
        programSave = view.findViewById(R.id.saveButton)
        program = mDbProgram.getRecord(programIdArg)
        programSave.visibility = View.GONE // Hide Save button by default
        programDelete.setOnClickListener(onClickToolbarItem)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        actionToolbarProgram.visibility=View.VISIBLE
    }
    fun requestForSave() {
        toBeSaved = true // setting state
        programSave.visibility = View.VISIBLE
    }

    private fun saveProgramDialog() {
        if (getExerciseFragment()!!.toBeSaved || toBeSaved) {
            val backDialogBuilder = AlertDialog.Builder(activity)
            backDialogBuilder.setTitle(resources.getText(R.string.global_confirm))
            backDialogBuilder.setMessage(resources.getText(R.string.backDialog_confirm_text))

            backDialogBuilder.setPositiveButton(resources.getString(R.string.global_yes)) { _: DialogInterface?, _: Int ->
                if (saveProgram()) {
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

    private fun saveProgram(): Boolean {
        var result = false
        val initialProgram: Program = program!!
        val newProgram = getExerciseFragment()!!.program
        val programName = newProgram?.programName
        val mDbProgram = DAOProgram(context)

        if (programName == "") {
            KToast.warningToast(activity, resources.getText(R.string.name_is_required).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
        } else if (initialProgram.programName != programName) {
            val programWithSameName: Program? = mDbProgram.getRecord(programName)
            if (newProgram != null) {
                if (programWithSameName != null && newProgram.id != programWithSameName.id && newProgram.type != programWithSameName.type) {
                    val dialogBuilder = AlertDialog.Builder(this.activity)
                    dialogBuilder.setTitle(requireActivity().resources.getText(R.string.global_warning))
                    dialogBuilder.setMessage(R.string.renameProgram_error_text2)
                    dialogBuilder.setPositiveButton(resources.getText(R.string.global_yes)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                } else if (programWithSameName != null && newProgram.id != programWithSameName.id && newProgram.type == programWithSameName.type) {
                    val dialogBuilder = AlertDialog.Builder(this.activity)
                    dialogBuilder.setTitle(resources.getText(R.string.global_warning))
                    dialogBuilder.setMessage(resources.getText(R.string.renameProgram_warning_text))
                    dialogBuilder.setNegativeButton(resources.getText(R.string.global_no)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                    val dialog = dialogBuilder.create()
                    dialog.show()
                } else {
                    mDbProgram.updateRecord(newProgram)
                    saveButton.visibility = View.GONE
                    toBeSaved = false
                    getExerciseFragment()!!.programSaved()
                    result = true
                }
            }
        } else {
            if (newProgram != null) {
                mDbProgram.updateRecord(newProgram)
            }
            saveButton.visibility = View.GONE
            toBeSaved = false
            getExerciseFragment()!!.programSaved()
            result = true
        }
        return result
    }

    private fun deleteProgram() {
        val deleteDialogBuilder = AlertDialog.Builder(this.activity)
        deleteDialogBuilder.setTitle(resources.getText(R.string.global_confirm))
        deleteDialogBuilder.setMessage(resources.getText(R.string.deleteProgram_confirm_text))

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
        val lProfile = mDbProfil.getProfil(profilIdArg)
        val listRecords = mDbRecord.getAllRecordByMachinesArray(lProfile, program!!.programName)
        for (record in listRecords) {
            mDbRecord.deleteRecord(record.id)
        }
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
        fun newInstance(id: Long, profile: Long): ProgramDetailsPager {
            val f = ProgramDetailsPager()
            val args = Bundle()
            args.putLong("programID", id)
            args.putLong("programProfile", profile)
            f.arguments = args
            return f
        }
    }
}
