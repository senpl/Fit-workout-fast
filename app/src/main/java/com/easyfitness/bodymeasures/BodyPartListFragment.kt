package com.easyfitness.bodymeasures

import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import com.easyfitness.DAO.Profile
import com.easyfitness.DAO.bodymeasures.BodyMeasure
import com.easyfitness.DAO.bodymeasures.BodyPart
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure
import com.easyfitness.DAO.bodymeasures.DAOBodyPart
import com.easyfitness.R
import com.easyfitness.bodymeasures.BodyPartDetailsFragment.Companion.newInstance
import com.easyfitness.utils.Keyboard
import com.fitworkoutfast.MainActivity

class BodyPartListFragment : Fragment() {
    lateinit var dataModels: ArrayList<BodyPart?>
    lateinit var measureList: ListView

    private val clickAddButton = View.OnClickListener { v: View? ->
        val editText = EditText(getContext())
        editText.setText("")
        editText.setGravity(Gravity.CENTER)
        editText.requestFocus()

        val linearLayout = LinearLayout(requireContext().getApplicationContext())
        linearLayout.setOrientation(LinearLayout.VERTICAL)
        linearLayout.addView(editText)

        val dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(requireContext().getString(R.string.enter_bodypart_name))
            .setCancelText(requireContext().getString(R.string.global_cancel))
            .setHideKeyBoardOnDismiss(true)
            .setCancelClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                editText.clearFocus()
                Keyboard.hide(getContext(), editText)
                sDialog!!.dismissWithAnimation()
            })
            .setConfirmClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                editText.clearFocus()
                Keyboard.hide(getContext(), editText)
                val daoBodyPart = DAOBodyPart(getContext())
                val temp_key = daoBodyPart.add(
                    -1,
                    editText.getText().toString(),
                    "",
                    daoBodyPart.getCount(),
                    BodyPartExtensions.TYPE_MUSCLE
                )

                sDialog!!.dismiss()
                val bodyPartDetailsFragment = newInstance(temp_key, true)
                val transaction = requireActivity().getSupportFragmentManager().beginTransaction()
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(
                    R.id.fragment_container,
                    bodyPartDetailsFragment,
                    MainActivity.BODYTRACKINGDETAILS
                )
                transaction.addToBackStack(null)
                // Commit the transaction
                transaction.commit()
            })
        //Keyboard.hide(context, editText);});
        dialog.setOnShowListener(OnShowListener { sDialog: DialogInterface? ->
            editText.requestFocus()
            Keyboard.show(getContext(), editText)
        })

        dialog.setCustomView(linearLayout)
        dialog.show()
    }

    private val onClickListItem =
        OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            val textView = requireView().findViewById<TextView>(R.id.LIST_BODYPART_ID)
            val bodyPartID = textView.getText().toString().toLong()

            val fragment = newInstance(bodyPartID, true)
            val transaction = requireActivity().getSupportFragmentManager().beginTransaction()
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, fragment, MainActivity.BODYTRACKINGDETAILS)
            transaction.addToBackStack(null)

            // Commit the transaction
            transaction.commit()
        }
    private var mdbBodyPart: DAOBodyPart? = null
    private var mdbMeasure: DAOBodyMeasure? = null
    private var mListAdapter: BodyPartListAdapter? = null
    private var addButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            mdbMeasure = DAOBodyMeasure(this.getContext())
            mdbBodyPart = DAOBodyPart(this.getContext())
            dataModels = ArrayList<BodyPart?>()
            mListAdapter = BodyPartListAdapter(dataModels, requireContext())
//            mListAdapter!!.setProfile(this.profile)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.tab_bodytracking, container, false)


        if (savedInstanceState == null) {
            addButton = view.findViewById<Button>(R.id.addBodyPart)
            addButton!!.setOnClickListener(clickAddButton)

            measureList = view.findViewById<ListView?>(R.id.listBodyMeasures)
            // Initialisation des evenements
            measureList!!.setOnItemClickListener(onClickListItem)
            measureList!!.setAdapter(mListAdapter)
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        mdbBodyPart!!.deleteAllEmptyBodyPart()
        refreshData()
    }

    private fun refreshData() {
        if (dataModels == null) {
            dataModels = ArrayList<BodyPart?>()
        }

        dataModels!!.clear()

        val lBodyPartList = mdbBodyPart!!.getMusclesList()
        for (bp in lBodyPartList) {
            var bm: BodyMeasure? = null
            if (this.profile != null) bm =
                mdbMeasure!!.getLastBodyMeasures(bp.getId(), this.profile)

            bp.setLastMeasure(bm)

            dataModels!!.add(bp)
        }

        if (mListAdapter == null) {
            mListAdapter = BodyPartListAdapter(dataModels, requireContext())
//            mListAdapter!!.setProfile(this.profile)
            measureList!!.setAdapter(mListAdapter)
        } else {
            mListAdapter!!.notifyDataSetChanged()
        }
    }

    private val profile: Profile?
        get() = (getActivity() as MainActivity).currentProfile

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): BodyPartListFragment {
            val f = BodyPartListFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.setArguments(args)

            return f
        }
    }
}
