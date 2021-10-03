package com.easyfitness.programs

import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.FilterQueryProvider
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.Profile
import com.easyfitness.R
import com.easyfitness.databinding.TabProgramsBinding
import com.fitworkoutfast.MainActivity

class ProgramsFragment : Fragment(R.layout.tab_programs) {
    private var mTableAdapter: ProgramCursorAdapter? = null
    private var _binding: TabProgramsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val onTextChangeListener: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.isEmpty()) {
                mTableAdapter!!.notifyDataSetChanged()
                if(binding.programsList.adapter!=null) {
                    mTableAdapter = binding.programsList.adapter as ProgramCursorAdapter
                    refreshData()
                }
            } else {
                if (mTableAdapter != null) {
                    mTableAdapter!!.filter.filter(charSequence)
                    mTableAdapter!!.notifyDataSetChanged()
                }
            }
        }

        override fun afterTextChanged(editable: Editable) {}
    }

    private val clickAddButton = View.OnClickListener {
        val programName = binding.newProgramName.text.toString()
        if (programName.isEmpty()) {
            Toast.makeText(context, "Enter not empty program name", Toast.LENGTH_LONG).show()
        } else {
            val lDAOProgram = DAOProgram(context)
            val profileId: Long? = (requireActivity() as MainActivity).currentProfile?.id
            lDAOProgram.addRecord(programName, profileId!!)
            binding.newProgramName.setText("")
            mTableAdapter!!.notifyDataSetChanged()
            refreshData()
            Toast.makeText(context, "Added to program list", Toast.LENGTH_LONG).show()
        }
    }
    private val onClickListItem = OnItemClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
        // Get Machine Name selected
        val textViewID = view.findViewById<TextView>(R.id.LIST_Program_ID)
        val programID = java.lang.Long.valueOf(textViewID.text.toString())
        val programDetailsPager = ProgramDetailsPager.newInstance(programID, (activity as MainActivity?)!!.currentProfile!!.id)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, programDetailsPager, "ProgramDetails")
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabProgramsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addProgramButton.setOnClickListener(clickAddButton)
        binding.searchField.addTextChangedListener(onTextChangeListener)
        binding.programsList.onItemClickListener = onClickListItem
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        //for add Programs menu
        val addId = 555
        if (item.itemId == addId) {
            clickAddButton.onClick(view)
        }
        return super.onOptionsItemSelected(item)
    }

    val name: String?
        get() = requireArguments().getString("name")

    val `this`: ProgramsFragment
        get() = this

    private fun refreshData() {
        val c: Cursor?
        val oldCursor: Cursor?
        val fragmentView = view
        if (fragmentView != null) {
            if (profil != null) {
                val daoProgram = DAOProgram(context)
                c = daoProgram.allPrograms
                if (c == null || c.count <= 0) {
                    binding.programsList.adapter = null
                } else {
                    if (binding.programsList.adapter == null) {
                        mTableAdapter = ProgramCursorAdapter(requireContext(), c, 0, daoProgram)
                        binding.programsList.adapter = mTableAdapter
                    } else {
                        mTableAdapter = binding.programsList.adapter as ProgramCursorAdapter
                        oldCursor = mTableAdapter!!.swapCursor(c)
                        oldCursor?.close()
                    }
                    mTableAdapter!!.filterQueryProvider = FilterQueryProvider { constraint: CharSequence -> daoProgram.getFilteredPrograms(constraint) }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) refreshData()
    }

    private val profil: Profile?
        get() = (requireActivity() as MainActivity).currentProfile

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(name: String?, id: Long?): ProgramsFragment {
            val f = ProgramsFragment()
            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putLong("profilId", id!!)
            f.arguments = args
            return f
        }
    }
}
