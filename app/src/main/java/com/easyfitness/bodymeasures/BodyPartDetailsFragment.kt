package com.easyfitness.bodymeasures

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.database.Cursor
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import com.easyfitness.BtnClickListener
import com.easyfitness.DAO.Profile
import com.easyfitness.DAO.bodymeasures.BodyMeasure
import com.easyfitness.DAO.bodymeasures.BodyPart
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure
import com.easyfitness.DAO.bodymeasures.DAOBodyPart
import com.easyfitness.R
import com.easyfitness.graph.DateGraph
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.EditableInputView.EditableInputView
import com.easyfitness.utils.EditableInputView.EditableInputView.OnTextChangedListener
import com.easyfitness.utils.ExpandedListView
import com.easyfitness.utils.Keyboard
import com.fitworkoutfast.MainActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.onurkaganaldemir.ktoastlib.KToast
import java.util.Calendar

class BodyPartDetailsFragment : Fragment(), OnDateSetListener {
    private var addButton: TextView? = null
    private lateinit var nameEdit: EditableInputView
    private var measureList: ExpandedListView? = null
    private var bodyToolbar: Toolbar? = null
    private var mChart: LineChart? = null
    private var mDateGraph: DateGraph? = null
    private var mBodyMeasureDb: DAOBodyMeasure? = null
    private var mDbBodyPart: DAOBodyPart? = null
    private var mInitialBodyPart: BodyPart? = null
    private val mCurrentPhotoPath: String? = null

    private val itemClickDeleteRecord =
        BtnClickListener { idToDelete: Long -> this.showDeleteDialog(idToDelete) }
    private val onClickAddMeasure: View.OnClickListener = View.OnClickListener {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        editDate = TextView(context)
        val date = DateConverter.newDate
        editDate!!.setLayoutParams(params)
        editDate!!.setText(DateConverter.dateToLocalDateStr(date, requireContext()))
        editDate!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        editDate!!.setGravity(Gravity.CENTER)
        editDate!!.setOnClickListener(View.OnClickListener { view: View? ->
            val calendar = Calendar.getInstance()
            calendar.setTime(DateConverter.newDate)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                this@BodyPartDetailsFragment, year, month, day
            )
            datePickerDialog.show()
        })

        editText = EditText(context)
        editText!!.setText("")
        editText!!.setHint("Enter value here")
        editText!!.setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        editText!!.setGravity(Gravity.CENTER)
        editText!!.setLayoutParams(params)
        editText!!.requestFocus()
        editText!!.selectAll()

        val linearLayout = LinearLayout(requireContext().applicationContext)

        linearLayout.setLayoutParams(params)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.addView(editDate)
        linearLayout.addView(editText)

        val dialog = SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(getString(R.string.new_measure))
            .showCancelButton(true)
            .setCancelClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                editText!!.clearFocus()
                Keyboard.hide(requireContext(), editText)
                sDialog!!.dismissWithAnimation()
            })
            .setCancelText(requireContext().getString(R.string.global_cancel))
            .setConfirmText(requireContext().getString(R.string.AddLabel))
            .setConfirmClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                Keyboard.hide(sDialog!!.getContext(), editText)
                var value = 0f
                try {
                    value = editText!!.getText().toString().toFloat()
                    val lDate = DateConverter.localDateStrToDate(
                        editDate!!.getText().toString(),
                        requireContext()
                    )
                    mBodyMeasureDb!!.addBodyMeasure(
                        lDate!!,
                        mInitialBodyPart!!.id,
                        value,
                        1
                    )
                    refreshData()
                } catch (e: Exception) {
                    KToast.errorToast(
                        getActivity(),
                        "Format Error",
                        Gravity.BOTTOM,
                        KToast.LENGTH_SHORT
                    )
                }
                sDialog.dismissWithAnimation()
            })
        dialog.setCustomView(linearLayout)
        dialog.setOnShowListener(OnShowListener { sDialog: DialogInterface? ->
            Keyboard.show(
                requireContext(),
                editText
            )
        })
        dialog.show()
    }

    private val bodyPartDetailsFragment: BodyPartDetailsFragment
        get() = this

    private val itemlongclickDeleteRecord =
        OnItemLongClickListener { listView: AdapterView<*>?, view: View?, position: Int, id: Long ->

            // Get the cursor, positioned to the corresponding row in the result set
            //Cursor cursor = (Cursor) listView.getItemAtPosition(position);
            val selectedID = id

            val profilListArray = arrayOfNulls<String>(1) // un seul choix
            profilListArray[0] = requireActivity().getResources().getString(R.string.DeleteLabel)

            val itemActionBuilder = AlertDialog.Builder(getActivity())
            itemActionBuilder.setTitle("").setItems(
                profilListArray,
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    when (which) {
                        0 -> {
                            mBodyMeasureDb!!.deleteMeasure(selectedID)
                            refreshData()
                            KToast.infoToast(
                                getActivity(),
                                requireActivity().getResources().getText(R.string.removedid)
                                    .toString() + " " + selectedID,
                                Gravity.BOTTOM,
                                KToast.LENGTH_SHORT
                            )
                        }

                        else -> {}
                    }
                })
            itemActionBuilder.show()
            true
        }

    private var deleteButton: ImageButton? = null
    private val onTextChangeListener =
        OnTextChangedListener { view: EditableInputView? -> this.requestForSave(requireView()) }
    private lateinit var editDate: TextView
    private lateinit var editText: EditText
    private var bodyPartImageView: ImageView? = null

    private val onClickToolbarItem = View.OnClickListener { v: View? ->
        // Handle presses on the action bar items
        if (v!!.getId() == R.id.deleteButton) {
            delete()
        }
    }

    private fun delete() {
        // afficher un message d'alerte
        val deleteDialogBuilder = AlertDialog.Builder(this.getActivity())

        deleteDialogBuilder.setTitle(
            requireActivity().getResources().getText(R.string.global_confirm)
        )
        deleteDialogBuilder.setMessage(
            requireActivity().getResources().getText(R.string.delete_bodypart_confirm)
        )

        // Si oui, supprimer la base de donnee et refaire un Start.
        deleteDialogBuilder.setPositiveButton(
            this.getResources().getString(R.string.global_yes),
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                // Suppress the machine
                mDbBodyPart!!.delete(mInitialBodyPart!!.id)
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
        val mDbBodyMeasure = DAOBodyMeasure(requireContext())

        val lProfile = this.profile

        val listBodyMeasure =
            mDbBodyMeasure.getBodyPartMeasuresList(mInitialBodyPart!!.id, lProfile!!)
        for (record in listBodyMeasure) {
            mDbBodyMeasure.deleteMeasure(record!!.id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.bodytracking_details_fragment, container, false)

        mDbBodyPart = DAOBodyPart(requireContext())

        addButton = view.findViewById<TextView?>(R.id.buttonAdd)
        nameEdit = view.findViewById(R.id.BODYPART_NAME)
        measureList = view.findViewById<ExpandedListView?>(R.id.listWeightProfil)
        bodyToolbar = view.findViewById<Toolbar?>(R.id.bodyTrackingDetailsToolbar)
        bodyPartImageView = view.findViewById<ImageView>(R.id.BODYPART_LOGO)
        val nameCardView = view.findViewById<CardView?>(R.id.nameCardView)

        /* Initialisation BodyPart */
        val bodyPartID = requireArguments().getLong("bodyPartID", 0)
        mInitialBodyPart = mDbBodyPart!!.getBodyPart(bodyPartID)

        // Hide Values Input if needed.
        /*if (!getArguments().getBoolean("showInput", true)) {
            addButton.setVisibility(View.GONE);
        } else {
            addButton.setVisibility(View.VISIBLE);
        }*/
        if (mInitialBodyPart!!.bodyPartResKey != -1) {
            bodyPartImageView!!.setVisibility(View.VISIBLE)
            bodyPartImageView!!.setImageDrawable(mInitialBodyPart!!.getPicture(requireContext()))
        } else {
            bodyPartImageView!!.setImageDrawable(null) // Remove the image, Custom is not managed yet
            bodyPartImageView!!.setVisibility(View.GONE)
        }

        /* Initialisation des boutons */
        if (mInitialBodyPart!!.type == BodyPartExtensions.TYPE_WEIGHT) {
            nameEdit!!.ActivateDialog(false)
        }
        nameEdit!!.setOnTextChangeListener(onTextChangeListener)
        addButton!!.setOnClickListener(onClickAddMeasure)
        measureList!!.setOnItemLongClickListener(itemlongclickDeleteRecord)

        /* Initialisation des evenements */

        // Add the other graph
        mChart = view.findViewById<LineChart?>(R.id.weightChart)
        mChart!!.setDescription(null)
        mDateGraph = DateGraph(getContext(), mChart, "")
        mBodyMeasureDb = DAOBodyMeasure(view.getContext())

        (getActivity() as MainActivity).activityToolbar.setVisibility(View.GONE)

        nameEdit!!.text=(mInitialBodyPart!!.getName(requireContext()))
        bodyToolbar!!.setNavigationIcon(R.drawable.ic_back)
        bodyToolbar!!.setNavigationOnClickListener(View.OnClickListener { v: View? -> requireActivity().onBackPressed() })

        deleteButton = view.findViewById<ImageButton>(R.id.deleteButton)
        deleteButton!!.setOnClickListener(onClickToolbarItem)
        if (mInitialBodyPart!!.type == BodyPartExtensions.TYPE_WEIGHT) {
            deleteButton!!.setVisibility(View.GONE) // Weight bodypart should not be deleted.
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        refreshData()
    }

    private fun DrawGraph(valueList: MutableList<BodyMeasure>) {
        // Recupere les enregistrements

        if (valueList.size < 1) {
            mChart!!.clear()
            return
        }

        val yVals = ArrayList<Entry?>()

        var minBodyMeasure = -1f

        for (i in valueList.indices.reversed()) {
            val value = Entry(
                DateConverter.nbDays(valueList.get(i).date!!.getTime().toDouble()).toFloat(),
                valueList.get(i).bodyMeasure
            )
            yVals.add(value)
            if (minBodyMeasure == -1f) minBodyMeasure = valueList.get(i).bodyMeasure
            else if (valueList.get(i).bodyMeasure < minBodyMeasure) minBodyMeasure =
                valueList.get(i).bodyMeasure
        }

        mDateGraph!!.draw(yVals)
    }

    /*  */
    private fun FillRecordTable(valueList: MutableList<BodyMeasure>) {
        var oldCursor: Cursor? = null

        if (valueList.isEmpty()) {
            //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
            measureList!!.setAdapter(null)
        } else {
            // ...
            if (measureList!!.getAdapter() == null) {
                val mTableAdapter = BodyMeasureCursorAdapter(
                    requireActivity(),
                    mBodyMeasureDb!!.cursor,
                    0,
                    itemClickDeleteRecord
                )
                measureList!!.setAdapter(mTableAdapter)
            } else {
                oldCursor = (measureList!!.getAdapter() as BodyMeasureCursorAdapter).swapCursor(
                    mBodyMeasureDb!!.cursor
                )
                if (oldCursor != null) oldCursor.close()
            }
        }
    }

    val name: String?
        get() = requireArguments().getString("name")

    private fun refreshData() {
        val fragmentView = getView()
        if (fragmentView != null) {
            if (this.profile != null) {
                val valueList = mBodyMeasureDb!!.getBodyPartMeasuresList(
                    mInitialBodyPart!!.id,
                    this.profile!!
                )
//                DrawGraph(valueList)
//                // update table
//                FillRecordTable(valueList)
            }
        }
    }

    private fun showDeleteDialog(idToDelete: Long) {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        mBodyMeasureDb!!.deleteMeasure(idToDelete)
                        refreshData()
                        Toast.makeText(
                            getActivity(),
                            getResources().getText(R.string.removedid)
                                .toString() + " " + idToDelete,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {}
                }
            }

        val builder = AlertDialog.Builder(getContext())
        builder.setMessage(getResources().getText(R.string.DeleteRecordDialog))
            .setPositiveButton(getResources().getText(R.string.global_yes), dialogClickListener)
            .setNegativeButton(getResources().getText(R.string.global_no), dialogClickListener)
            .show()
    }

    private val profile: Profile?
        get() = (getActivity() as MainActivity).currentProfile

    val fragment: Fragment
        get() = this

    private fun requestForSave(view: View) {
        var toUpdate = false

        // Save all the fields in the Profile
        val id = view.getId()
        if (id == R.id.BODYPART_NAME) {
            mInitialBodyPart!!.customName = (nameEdit!!.text)
            toUpdate = true
        } else if (id == R.id.BODYPART_LOGO) {
            // TODO if it has been deleted, remove the CustomPicture
            mInitialBodyPart!!.customPicture=(mCurrentPhotoPath)
            toUpdate = true
        }

        if (toUpdate) {
            mDbBodyPart!!.update(mInitialBodyPart!!)
            KToast.infoToast(
                requireActivity(),
                mInitialBodyPart!!.customName + " updated",
                Gravity.BOTTOM,
                KToast.LENGTH_SHORT
            )
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val date = DateConverter.dateToDate(year, month, dayOfMonth)
        if (editDate != null) editDate!!.setText(
            DateConverter.dateToLocalDateStr(
                date,
                requireContext()
            )
        )
    } /*
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }
*/

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        @JvmStatic
        fun newInstance(bodyPartID: Long, showInput: Boolean): BodyPartDetailsFragment {
            val f = BodyPartDetailsFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putLong("bodyPartID", bodyPartID)
            args.putBoolean("showInput", showInput)
            f.setArguments(args)

            return f
        }
    }
}
