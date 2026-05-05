package com.easyfitness.fonte

import android.database.Cursor
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import com.easyfitness.BtnClickListener
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.DAORecord
import com.easyfitness.DAO.DAOUtils
import com.easyfitness.DAO.Machine
import com.easyfitness.DAO.Profile
import com.easyfitness.R
import com.fitworkoutfast.MainActivity
import com.onurkaganaldemir.ktoastlib.KToast
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class FonteHistoryFragment : Fragment() {
    var dateList: Spinner? = null
    var exerciseList: Spinner? = null

    var paramButton: Button? = null
    var filterList: ListView? = null

    var mainActivity: MainActivity? = null

    lateinit var mExerciseArray: MutableList<String?>
    lateinit var mDateArray: MutableList<String?>

    lateinit var mAdapterMachine: ArrayAdapter<String?>
    var mAdapterDate: ArrayAdapter<String?>? = null

    var machineIdArg: Long = -1
    var machineProfilIdArg: Long = -1

    var selectedMachine: Machine? = null
    private var mDb: DAORecord? = null
    private val itemClickDeleteRecord =
        BtnClickListener { idToDelete: Long -> this.showDeleteDialog(idToDelete) }
    private val itemlongclickDeleteRecord =
        OnItemLongClickListener { listView: AdapterView<*>?, view: View?, position: Int, id: Long ->
            mDb!!.deleteRecord(id)
            FillRecordTable(
                exerciseList!!.getSelectedItem().toString(), dateList!!
                    .getSelectedItem().toString()
            )

            KToast.infoToast(
                getActivity(),
                getResources().getText(R.string.removedid).toString(),
                Gravity.BOTTOM,
                KToast.LENGTH_SHORT
            )
            true
        }
    private val onItemSelectedList: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long
            ) {
                if (parent.getId() == R.id.filterMachine) {
                    // Save current date
                    val currentDateSelection = ""

                    //  Update currentSelectedMachine
                    val lDbMachine = DAOMachine(getContext())
                    val machine: Machine? = null
                    if (exerciseList!!.getSelectedItem().toString() != requireView().resources
                            .getText(R.string.all).toString()
                    ) {
                        selectedMachine =
                            lDbMachine.getMachine(exerciseList!!.getSelectedItem().toString())
                    } else {
                        selectedMachine = null
                    }
                    // Update associated Dates
                    refreshDates(selectedMachine)
                    if (dateList!!.getCount() > 1) {
                        dateList!!.setSelection(1) // Select latest date
                    } else {
                        dateList!!.setSelection(0) // Or select "All"
                    }
                }
                if (dateList!!.getCount() >= 1 && exerciseList!!.getCount() >= 1) {
                    FillRecordTable(
                        exerciseList!!.getSelectedItem().toString(), dateList!!
                            .getSelectedItem().toString()
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.tab_history, container, false)

        val args = this.getArguments()
        machineIdArg = args!!.getLong("machineID")
        machineProfilIdArg = args.getLong("machineProfile")

        dateList = view.findViewById<Spinner?>(R.id.filterDate)
        exerciseList = view.findViewById<Spinner?>(R.id.filterMachine)
        filterList = view.findViewById<ListView?>(R.id.listFilterRecord)

        // Initialisation de l'historique
        mDb = DAORecord(view.getContext())

        mExerciseArray = ArrayList<String?>()
        mExerciseArray!!.add(requireContext().getResources().getText(R.string.all).toString())
        mAdapterMachine = ArrayAdapter<String?>(
            requireContext(), android.R.layout.simple_spinner_item,  //simple_spinner_dropdown_item
            mExerciseArray
        )
        mAdapterMachine!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exerciseList!!.setAdapter(mAdapterMachine)
        mDb!!.closeCursor()

        if (machineIdArg != -1L) {
            // Hide the spinner
            view.findViewById<View?>(R.id.tableRowFilterMachine).setVisibility(View.GONE)
            val lDbMachine = DAOMachine(getContext())
            selectedMachine = lDbMachine.getMachine(machineIdArg)
            mExerciseArray!!.add(selectedMachine!!.name)
            mAdapterMachine!!.notifyDataSetChanged()
            exerciseList!!.setSelection(mAdapterMachine!!.getPosition(selectedMachine!!.name))
        } else {
            exerciseList!!.setOnItemSelectedListener(onItemSelectedList)
        }

        mDateArray = ArrayList<String?>()
        mDateArray!!.add(requireContext().getResources().getText(R.string.all).toString())
        mAdapterDate = ArrayAdapter<String?>(
            requireContext(), android.R.layout.simple_spinner_item,
            mDateArray
        )
        mAdapterDate!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dateList!!.setAdapter(mAdapterDate)

        // Initialisation des evenements
        filterList!!.setOnItemLongClickListener(itemlongclickDeleteRecord)
        dateList!!.setOnItemSelectedListener(onItemSelectedList)

        return view
    }

    override fun onStart() {
        super.onStart()
        this.mainActivity = this.getActivity() as MainActivity?
        refreshData()
    }

    val name: String?
        get() = requireArguments().getString("name")

    val fragmentId: Int
        get() = requireArguments().getInt("id", 0)

    /*  */
    private fun FillRecordTable(pMachine: String?, pDate: String) {
        var pDate = pDate
        var oldCursor: Cursor? = null

        // Retransform date filter value in SQLLite date format
        if (pDate != requireContext().getResources().getText(R.string.all).toString()) {
            var date: Date?
            try {
                val dateFormat3 =
                    android.text.format.DateFormat.getDateFormat(requireContext().getApplicationContext())
                dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"))
                date = dateFormat3.parse(pDate)
            } catch (e: ParseException) {
                e.printStackTrace()
                date = Date()
            }

            val dateFormat = SimpleDateFormat(DAOUtils.DATE_FORMAT)
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
            pDate = dateFormat.format(date)
        }

        // Get Values
        val c = mDb!!.getFilteredRecords(this.profil, pMachine, pDate)

        if (c == null || c.getCount() == 0) {
            filterList!!.setAdapter(null)
        } else {
            if (filterList!!.getAdapter() == null) {
                val mTableAdapter = RecordCursorAdapter(
                    requireContext(), c, 0, itemClickDeleteRecord, null
                )
                filterList!!.setAdapter(mTableAdapter)
            } else {
                oldCursor = (filterList!!.getAdapter() as RecordCursorAdapter).swapCursor(c)
                if (oldCursor != null) oldCursor.close()
            }
        }
    }

    private fun refreshData() {
        val fragmentView = getView()
        if (fragmentView != null) {
            if (this.profil != null) {
                // If the fragment is used to display record of a specific machine
                if (machineIdArg == -1L)  // Refresh the list
                {
                    // Initialisation des machines
                    mExerciseArray!!.clear()
                    mExerciseArray!!.add(
                        requireContext().getResources().getText(R.string.all).toString()
                    )
                    mExerciseArray!!.addAll(mDb!!.getAllMachinesStrList(this.profil))
                    mAdapterMachine!!.notifyDataSetChanged()
                    mDb!!.closeCursor()

                    exerciseList!!.setSelection(0) // Default value is "all" when there is a list

                    /*
                    if (mAdapterMachine.getPosition(this.getFontesMachine()) != -1) {
                        exerciseList.setSelection(mAdapterMachine.getPosition(this.getFontesMachine()));
                    } else { // if not found, set selection to 0
                        exerciseList.setSelection(0);
                    }
*/
                }

                refreshDates(selectedMachine)
            }
        }
    }

    /**
     * @param m if m is null then, get the dates for all machines
     */
    private fun refreshDates(m: Machine?) {
        val fragmentView = getView()
        if (fragmentView != null) {
            if (this.profil != null) {
                mDateArray!!.clear()
                mDateArray!!.add(requireView().getResources().getText(R.string.all).toString())
                mDateArray!!.addAll(mDb!!.getAllDatesList(this.profil, m))
                if (mDateArray!!.size > 1) {
                    dateList!!.setSelection(1)
                }
                mAdapterDate!!.notifyDataSetChanged()
                mDb!!.closeCursor()
            }
        }
    }

    private val profil: Profile?
        get() = mainActivity!!.currentProfile

    private val fontesMachine: String
        get() = this.mainActivity!!.currentMachine

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            refreshData()
        }
    }

    private fun showDeleteDialog(idToDelete: Long) {
        SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(getResources().getText(R.string.areyousure).toString())
            .setCancelText(getResources().getText(R.string.global_no).toString())
            .setConfirmText(getResources().getText(R.string.global_yes).toString())
            .showCancelButton(true)
            .setConfirmClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                mDb!!.deleteRecord(idToDelete)
                FillRecordTable(
                    exerciseList!!.getSelectedItem().toString(), dateList!!
                        .getSelectedItem().toString()
                )
                KToast.infoToast(
                    getActivity(),
                    getResources().getText(R.string.removedid).toString(),
                    Gravity.BOTTOM,
                    KToast.LENGTH_LONG
                )
                sDialog!!.dismissWithAnimation()
            })
            .show()
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(machineId: Long, machineProfile: Long): FonteHistoryFragment {
            val f = FonteHistoryFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putLong("machineID", machineId)
            args.putLong("machineProfile", machineProfile)
            f.setArguments(args)

            return f
        }
    }
}
