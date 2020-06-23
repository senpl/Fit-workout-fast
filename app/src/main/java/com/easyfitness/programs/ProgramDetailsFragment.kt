package com.easyfitness.programs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.DAORecord
import com.easyfitness.DAO.Program
import com.easyfitness.R

class ProgramDetailsFragment : Fragment() {
    private lateinit var programName: EditText

    private var nameArg: String = ""
    private var idArg: Long = 0
    private var profilIdArg: Long = 0

    private var pager: ProgramDetailsPager? = null
    private var daoProgram: DAOProgram? = null
    private var mDbRecord: DAORecord? = null
    private var program1: Program? = null
    private var fragmentView: View? = null

    var toBeSaved = false
    private var watcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int,
                                   before: Int, count: Int) {
            requestForSave()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                       after: Int) {
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.program_details, container, false)
        fragmentView = view

        // Initialisation de l'historique
        daoProgram = DAOProgram(requireContext())
        mDbRecord = DAORecord(context)
        programName = view.findViewById(R.id.programName)
        val args = this.arguments
        idArg = args!!.getLong("programID")
        profilIdArg = args.getLong("programProfile")
        program1 = daoProgram!!.getRecord(idArg)
        if(program1!=null)
        nameArg = program1!!.programName!!

        if (nameArg == "") {
            requestForSave()
        }
        programName.setText(nameArg)
        programName.addTextChangedListener(watcher)
        return view
    }

    val `this`: ProgramDetailsFragment
        get() = this

    private fun requestForSave() {
        toBeSaved = true // setting state
        if (pager != null) pager!!.requestForSave()
    }

    fun programSaved() {
        toBeSaved = false
    }

    val program: Program?
        get() {
            val m = program1
            if (m != null) {
                m.programName = programName.text.toString()
            }
            //        m.setProfil(selectedType);
            return m
        }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(id: Long, profile: Long): ProgramDetailsFragment {
            val f = ProgramDetailsFragment()
            val args = Bundle()
            args.putLong("programID", id)
            args.putLong("programProfile", profile)
            f.arguments = args
            return f
        }
    }
}
