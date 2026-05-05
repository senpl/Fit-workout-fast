package com.easyfitness.machines

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.DAOProfil
import com.easyfitness.DAO.DAORecord
import com.easyfitness.DAO.Machine
import com.easyfitness.R
import com.easyfitness.fonte.FonteHistoryFragment
import com.fitworkoutfast.MainActivity
import com.github.ivbaranov.mfb.MaterialFavoriteButton
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import com.onurkaganaldemir.ktoastlib.KToast


class ExerciseDetailsPager : Fragment() {
    var top_toolbar: Toolbar? = null
    var machineIdArg: Long = 0
    var machineProfilIdArg: Long = 0
    var pagerAdapter: FragmentPagerItemAdapter? = null
    var mViewPager: ViewPager? = null
    var viewPagerTab: SmartTabLayout? = null
    var deleteButton: ImageButton? = null
    var saveButton: ImageButton? = null
    lateinit var favoriteButton: MaterialFavoriteButton
    var machine: Machine? = null
    var isFavorite: Boolean = false
    var toBeSaved: Boolean = false
    var mDbMachine: DAOMachine? = null
    var mDbRecord: DAORecord? = null
    private val name: String? = null
    private val id = 0
    private val onClickToolbarItem = View.OnClickListener { v: View? ->
        // Handle presses on the action bar items
        val id = v!!.getId()
        if (id == R.id.saveButton) {
            saveMachine()
            requireActivity()
            requireActivity().findViewById<View?>(R.id.tab_machine_details).requestFocus()
        } else if (id == R.id.deleteButton) {
            deleteMachine()
        } else {
            saveMachineDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.exercise_pager, container, false)

        // Locate the viewpager in activity_main.xml
        mViewPager = view.findViewById<ViewPager?>(R.id.pager)

        if (mViewPager!!.getAdapter() == null) {
            val args = this.getArguments()
            machineIdArg = args!!.getLong("machineID")
            machineProfilIdArg = args.getLong("machineProfile")

            pagerAdapter = FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                    .add(getString(R.string.MachineLabel), MachineDetailsFragment::class.java, args)
                    .add(getString(R.string.HistoryLabel), FonteHistoryFragment::class.java, args)
                    .create()
            )

            mViewPager!!.setAdapter(pagerAdapter)

            viewPagerTab = view.findViewById<SmartTabLayout?>(R.id.viewpagertab)
            viewPagerTab!!.setViewPager(mViewPager)

            viewPagerTab!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    val frag1 = pagerAdapter!!.getPage(position)
                    if (frag1 != null) frag1.onHiddenChanged(false) // Refresh data
                }

                override fun onPageScrollStateChanged(state: Int) {
                }
            })
        }

        mDbRecord = DAORecord(context)
        mDbMachine = DAOMachine(context)
        machine = mDbMachine!!.getMachine(machineIdArg)

        (getActivity() as MainActivity).activityToolbar.visibility = View.GONE
        top_toolbar = view.findViewById<Toolbar?>(R.id.actionToolbarMachine)
        top_toolbar!!.setNavigationIcon(R.drawable.ic_back)
        top_toolbar!!.setNavigationOnClickListener(onClickToolbarItem)

        deleteButton = view.findViewById<ImageButton?>(R.id.deleteButton)
        deleteButton!!.setOnClickListener(onClickToolbarItem)
        saveButton = view.findViewById<ImageButton?>(R.id.saveButton)
        saveButton!!.setOnClickListener(onClickToolbarItem)
        saveButton!!.setVisibility(View.GONE) // Hide Save button by default
        favoriteButton = view.findViewById<MaterialFavoriteButton?>(R.id.favButton)
        favoriteButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            val mFav = v as MaterialFavoriteButton
            val t = mFav.isFavorite()
            mFav.setFavoriteAnimated(!t)
            isFavorite = !t
            requestForSave()
        })
        favoriteButton!!.setFavorite(machine!!.favorite == true)

        return view
    }

    override fun onStart() {
        super.onStart()
    }

    fun requestForSave() {
        toBeSaved = true // setting state
        saveButton!!.setVisibility(View.VISIBLE)
    }

    private fun saveMachineDialog() {
        if (this.exerciseFragment!!.toBeSaved() || toBeSaved) {
            // Afficher une boite de dialogue pour confirmer
            val backDialogBuilder = AlertDialog.Builder(getActivity())

            backDialogBuilder.setTitle(
                requireActivity().getResources().getText(R.string.global_confirm)
            )
            backDialogBuilder.setMessage(
                requireActivity().getResources().getText(R.string.backDialog_confirm_text)
            )

            // Si oui, supprimer la base de donnee et refaire un Start.
            backDialogBuilder.setPositiveButton(
                getResources().getString(R.string.global_yes),
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    if (saveMachine()) {
                        requireActivity().onBackPressed()
                    }
                })

            backDialogBuilder.setNegativeButton(
                getResources().getString(R.string.global_no),
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> requireActivity().onBackPressed() })

            val backDialog = backDialogBuilder.create()
            backDialog.show()
        } else {
            requireActivity().onBackPressed()
        }
    }

    private val mainActivity: MainActivity?
        get() = getActivity() as MainActivity?

    private fun saveMachine(): Boolean {
        var result = false
        val initialMachine = machine
        val newMachine = this.exerciseFragment!!.machine
        val lMachineName = newMachine.name // Potentiel nouveau nom dans le EditText

        // Si le nom est different du nom actuel
        if (lMachineName == "") {
            KToast.warningToast(
                getActivity(),
                getResources().getText(R.string.name_is_required).toString(),
                Gravity.BOTTOM,
                KToast.LENGTH_SHORT
            )
        } else if (initialMachine!!.name != lMachineName) {
            val machineWithSameName = mDbMachine!!.getMachine(lMachineName)
            // Si une machine existe avec le meme nom => Merge
            if (machineWithSameName != null && newMachine.id != machineWithSameName.id && newMachine.type != machineWithSameName.type) {
                val dialogBuilder = AlertDialog.Builder(this.getActivity())

                dialogBuilder.setTitle(
                    requireActivity().getResources().getText(R.string.global_warning)
                )
                dialogBuilder.setMessage(R.string.renameMachine_error_text2)
                dialogBuilder.setPositiveButton(
                    getResources().getText(R.string.global_yes),
                    DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> dialog!!.dismiss() })

                val dialog = dialogBuilder.create()
                dialog.show()
            } else if (machineWithSameName != null && newMachine.id != machineWithSameName.id && newMachine.type == machineWithSameName.type) {
                val dialogBuilder = AlertDialog.Builder(this.getActivity())

                dialogBuilder.setTitle(
                    requireActivity().getResources().getText(R.string.global_warning)
                )
                dialogBuilder.setMessage(
                    requireActivity().getResources().getText(R.string.renameMachine_warning_text)
                )
                // Si oui, supprimer la base de donnee et refaire un Start.
                dialogBuilder.setPositiveButton(
                    getResources().getText(R.string.global_yes),
                    DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                        // Rename all the records with that machine and rename them
                        val lDbRecord = DAORecord(requireView().getContext())
                        val mDbProfil = DAOProfil(requireView().getContext())
                        val lProfile = mDbProfil.getProfil(machineProfilIdArg)

                        val listRecords = lDbRecord.getAllRecordByMachinesArray(
                            lProfile,
                            initialMachine.name
                        ) // Recupere tous les records de la machine courante
                        for (record in listRecords) {
                            record.setExercise(newMachine.name) // Change avec le nouveau nom. Normalement pas utile.
                            record.setExerciseKey(machineWithSameName.id) // Met l'ID de la nouvelle machine
                            lDbRecord.updateRecord(record) // Met a jour
                        }

                        mDbMachine!!.delete(initialMachine) // Supprime l'ancienne machine

                        toBeSaved = false
                        saveButton!!.setVisibility(View.GONE)
                        requireActivity().onBackPressed()
                    })

                dialogBuilder.setNegativeButton(
                    getResources().getText(R.string.global_no),
                    DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                        // Do nothing but close the dialog
                        dialog!!.dismiss()
                    })

                val dialog = dialogBuilder.create()
                dialog.show()
            } else {
                newMachine.favorite=favoriteButton.isFavorite
                this.mDbMachine!!.updateMachine(newMachine)

                // Rename all the records with that machine and rename them
                val lDbRecord = DAORecord(getContext())
                val mDbProfil = DAOProfil(getContext())
                val lProfile = mDbProfil.getProfil(machineProfilIdArg)
                val listRecords = lDbRecord.getAllRecordByMachinesArray(
                    lProfile,
                    initialMachine.name
                ) // Recupere tous les records de la machine courante
                for (record in listRecords) {
                    record.setExercise(lMachineName) // Change avec le nouveau nom (DEPRECATED)
                    lDbRecord.updateRecord(record) // met a jour
                }

                saveButton!!.setVisibility(View.GONE)
                toBeSaved = false
                this.exerciseFragment!!.machineSaved()
                result = true
            }
        } else {
            // Si le nom n'a pas ete modifie.
            newMachine.favorite=(favoriteButton!!.isFavorite())
            mDbMachine!!.updateMachine(newMachine)

            saveButton!!.setVisibility(View.GONE)
            toBeSaved = false
            this.exerciseFragment!!.machineSaved()
            result = true
        }
        return result
    }

    private fun deleteMachine() {
        // afficher un message d'alerte
        val deleteDialogBuilder = AlertDialog.Builder(this.getActivity())

        deleteDialogBuilder.setTitle(
            requireActivity().getResources().getText(R.string.global_confirm)
        )
        deleteDialogBuilder.setMessage(
            requireActivity().getResources().getText(R.string.deleteMachine_confirm_text)
        )

        // Si oui, supprimer la base de donnee et refaire un Start.
        deleteDialogBuilder.setPositiveButton(
            this.getResources().getString(R.string.global_yes),
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                // Suppress the machine
                mDbMachine!!.delete(machine)
                // Suppress the associated Fontes records
                deleteRecordsAssociatedToMachine()
                requireActivity().onBackPressed()
            })

        deleteDialogBuilder.setNegativeButton(
            this.getResources().getString(R.string.global_no),
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                // Do nothing
                dialog!!.dismiss()
            })

        val deleteDialog = deleteDialogBuilder.create()
        deleteDialog.show()
    }

    private fun deleteRecordsAssociatedToMachine() {
        val mDbRecord = DAORecord(getContext())
        val mDbProfil = DAOProfil(getContext())

        val lProfile = mDbProfil.getProfil(this.machineProfilIdArg)

        val listRecords = mDbRecord.getAllRecordByMachinesArray(lProfile, machine!!.name)
        for (record in listRecords) {
            mDbRecord.deleteRecord(record.getId())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()

        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.machine_details_menu, menu)

        val item = menu.findItem(R.id.saveButton)
        item.setVisible(toBeSaved)

        super.onCreateOptionsMenu(menu, inflater)
    }

    val exerciseFragment: MachineDetailsFragment?
        get() {
            val mpExerciseFrag: MachineDetailsFragment?
            mpExerciseFrag = pagerAdapter!!.getPage(0) as MachineDetailsFragment?
            return mpExerciseFrag
        }

    val historicFragment: FonteHistoryFragment?
        get() {
            val mpHistoryFrag: FonteHistoryFragment?
            mpHistoryFrag = pagerAdapter!!.getPage(1) as FonteHistoryFragment?
            return mpHistoryFrag
        }

    val viewPager: ViewPager?
        get() = requireView().findViewById<View?>(R.id.pager) as ViewPager?

    val viewPagerAdapter: FragmentPagerItemAdapter?
        get() = ((requireView().findViewById<View?>(R.id.pager)) as ViewPager).getAdapter() as FragmentPagerItemAdapter?

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    override fun onSaveInstanceState(outState: Bundle) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            // rafraichit le fragment courant

            if (this.viewPagerAdapter != null) {
                // Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit.
                // Il faut donc que cela soit pris en compte dans le refresh des fragments.
                var frag1: Fragment?
                for (i in 0..2) {
                    frag1 = this.viewPagerAdapter!!.getPage(i)
                    if (frag1 != null) frag1.onHiddenChanged(false) // Refresh data
                }
            }
        }
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(machineId: Long, machineProfile: Long): ExerciseDetailsPager {
            val f = ExerciseDetailsPager()

            // Supply index input as an argument.
            val args = Bundle()
            args.putLong("machineID", machineId)
            args.putLong("machineProfile", machineProfile)
            f.setArguments(args)

            return f
        }
    }
}
