package com.easyfitness.machines

import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.FilterQueryProvider
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.Profile
import com.easyfitness.R
import com.fitworkoutfast.MainActivity

class MachineFragment : Fragment() {
    var machineList: ListView? = null
    var addButton: Button? = null
    var searchField: AutoCompleteTextView? = null
    var mTableAdapter: MachineCursorAdapter? = null

    private var mDbMachine: DAOMachine? = null
    var onTextChangeListener: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.length == 0) {
//                mTableAdapter.notifyDataSetChanged();
//                mTableAdapter = ((MachineCursorAdapter) machineList.getAdapter());
                refreshData()
            } else {
                if (mTableAdapter != null) {
                    mTableAdapter!!.getFilter().filter(charSequence)
                    mTableAdapter!!.notifyDataSetChanged()
                }
            }
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    }
    private val onClickListItem =
        OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            // Get Machine Name selected
            val textViewID = requireView().findViewById<TextView>(R.id.LIST_MACHINE_ID)
            val machineId = textViewID.getText().toString().toLong()

            val machineDetailsFragment: ExerciseDetailsPager =
                ExerciseDetailsPager.Companion.newInstance(
                    machineId,
                    (getActivity() as MainActivity).currentProfile!!.id
                )
            val transaction = requireActivity().getSupportFragmentManager().beginTransaction()
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(
                R.id.fragment_container,
                machineDetailsFragment,
                MainActivity.MACHINESDETAILS
            )
            transaction.addToBackStack(null)
            // Commit the transaction
            transaction.commit()
        }
    private val clickAddButton = View.OnClickListener { v: View? ->

        // create a temporarily exercise with name="" and open it like any other existing exercises
        val new_id: Long = -1


        val dlg = SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(getString(R.string.what_type_of_exercise))
            .setContentText("")
            .setCancelText(getResources().getText(R.string.CardioLabel).toString())
            .setConfirmText(getResources().getText(R.string.strength_category).toString())
            .setNeutralText(getResources().getText(R.string.staticExercise).toString())
            .showCancelButton(true)
            .setConfirmClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                var temp_machine_key: Long = -1
                val pMachine = ""
                val lDAOMachine = DAOMachine(requireContext())
                temp_machine_key =
                    lDAOMachine.addMachine(pMachine, "", DAOMachine.TYPE_STRENGTH, "", false, "")
                sDialog!!.dismissWithAnimation()

                val machineDetailsFragment: ExerciseDetailsPager =
                    ExerciseDetailsPager.Companion.newInstance(
                        temp_machine_key,
                        (getActivity() as MainActivity).currentProfile!!.id
                    )
                val transaction = requireActivity().getSupportFragmentManager().beginTransaction()
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(
                    R.id.fragment_container,
                    machineDetailsFragment,
                    MainActivity.MACHINESDETAILS
                )
                transaction.addToBackStack(null)
                // Commit the transaction
                transaction.commit()
            })
            .setNeutralClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                var temp_machine_key: Long = -1
                val pMachine = ""
                val lDAOMachine = DAOMachine(requireContext())
                temp_machine_key =
                    lDAOMachine.addMachine(pMachine, "", DAOMachine.TYPE_STATIC, "", false, "")
                sDialog!!.dismissWithAnimation()

                val machineDetailsFragment: ExerciseDetailsPager =
                    ExerciseDetailsPager.Companion.newInstance(
                        temp_machine_key,
                        (getActivity() as MainActivity).currentProfile!!.id
                    )
                val transaction = requireActivity().getSupportFragmentManager().beginTransaction()
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(
                    R.id.fragment_container,
                    machineDetailsFragment,
                    MainActivity.MACHINESDETAILS
                )
                transaction.addToBackStack(null)
                // Commit the transaction
                transaction.commit()
            })
            .setCancelClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                var temp_machine_key: Long = -1
                val pMachine = ""
                val lDAOMachine = DAOMachine(requireContext())
                temp_machine_key =
                    lDAOMachine.addMachine(pMachine, "", DAOMachine.TYPE_CARDIO, "", false, "")
                sDialog!!.dismissWithAnimation()

                val machineDetailsFragment: ExerciseDetailsPager =
                    ExerciseDetailsPager.Companion.newInstance(
                        temp_machine_key,
                        (getActivity() as MainActivity).currentProfile!!.id
                    )
                val transaction = requireActivity().getSupportFragmentManager().beginTransaction()
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(
                    R.id.fragment_container,
                    machineDetailsFragment,
                    MainActivity.MACHINESDETAILS
                )
                transaction.addToBackStack(null)
                // Commit the transaction
                transaction.commit()
            })

        dlg.show()

        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM)
            .setBackgroundResource(R.color.record_background_odd)
        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0, 0, 0, 0)
        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM)
                .setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        }
        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL)
            .setBackgroundResource(R.color.record_background_odd)
        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setPadding(0, 0, 0, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getButton(SweetAlertDialog.BUTTON_CANCEL)
                .setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        }

        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL)
            .setBackgroundResource(R.color.record_background_odd)
        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setPadding(0, 0, 0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL)
                .setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // activates onCreateOptionsMenu in this fragment

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.tab_machine, container, false)

        addButton = view.findViewById<Button?>(R.id.addExercise)
        addButton!!.setOnClickListener(clickAddButton)

        searchField = view.findViewById<AutoCompleteTextView?>(R.id.searchField)
        searchField!!.addTextChangedListener(onTextChangeListener)

        machineList = view.findViewById<ListView?>(R.id.listMachine)
        machineList!!.setOnItemClickListener(onClickListItem)

        // Initialisation de l'historique
        mDbMachine = DAOMachine(requireContext())

        return view
    }

    override fun onStart() {
        super.onStart()

        mDbMachine!!.deleteAllEmptyExercises()
        refreshData()

        // for resetting the search field at the start:
        searchField!!.setText("")
    }

    val name: String?
        get() = requireArguments().getString("name")

    val `this`: MachineFragment
        get() = this

    private fun refreshData() {
        val c: Cursor?
        val oldCursor: Cursor?

        val fragmentView = getView()
        if (fragmentView != null) {
            if (this.profil != null) {
                // Version avec table Machine

                c = mDbMachine!!.allMachines
                if (c == null || c.getCount() == 0) {
                    //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
                    machineList!!.setAdapter(null)
                } else {
                    if (machineList!!.getAdapter() == null) {
                        mTableAdapter = MachineCursorAdapter(requireActivity(), c, 0, mDbMachine)
                        machineList!!.setAdapter(mTableAdapter)
                    } else {
                        mTableAdapter = (machineList!!.getAdapter() as MachineCursorAdapter?)
                        oldCursor = mTableAdapter!!.swapCursor(c)
                        if (oldCursor != null) oldCursor.close()
                    }

                    mTableAdapter!!.setFilterQueryProvider(FilterQueryProvider { constraint: CharSequence? ->
                        mDbMachine!!.getFilteredMachines(
                            constraint
                        )
                    })
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) refreshData()
    }

    private val profil: Profile?
        get() = (getActivity() as MainActivity).currentProfile

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): MachineFragment {
            val f = MachineFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.setArguments(args)

            return f
        }
    }
}
