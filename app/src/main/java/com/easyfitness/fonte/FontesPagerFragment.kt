package com.easyfitness.fonte

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.R
import com.easyfitness.programs.NonSwipeableViewPager
import com.easyfitness.programs.ProgramCursorAdapter
import com.easyfitness.programs.ProgramRunner
import com.easyfitness.programs.ProgramViewModel
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import kotlinx.coroutines.launch

class FontesPagerFragment : Fragment() {
    private var pagerAdapter: FragmentPagerItemAdapter? = null
    private val viewModel: ProgramViewModel by activityViewModels()
    private lateinit var daoProgram: DAOProgram

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.programrunner_pager, container, false)

        daoProgram = DAOProgram(requireContext())
        val startLayout = view.findViewById<LinearLayout>(R.id.startProgramLayout)
        val workoutLayout = view.findViewById<LinearLayout>(R.id.workoutRunnerLayout)
        val programListView = view.findViewById<ListView>(R.id.programListView)

        // Locate the viewpager
        val mViewPager = view.findViewById<NonSwipeableViewPager>(R.id.programrunner_pager)

        if (mViewPager.getAdapter() == null) {
            val args: Bundle? = this.requireArguments()
            args!!.putLong("machineID", -1)
            args.putLong("machineProfile", -1)

            pagerAdapter =
                FragmentPagerItemAdapter(
                    getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                        .add(R.string.ProgramRunnerLabel, ProgramRunner::class.java)
                        .create()
                )

            mViewPager.setAdapter(pagerAdapter)

            val viewPagerTab = view.findViewById<SmartTabLayout>(R.id.noviewpagertab)
            viewPagerTab.setViewPager(mViewPager)

            viewPagerTab.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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

        // Set up the ListView
        val cursor = daoProgram.allPrograms
        val adapter = ProgramCursorAdapter(requireContext(), cursor, 0, daoProgram)
        programListView.adapter = adapter

        val sharedPrefs = requireContext().getSharedPreferences("program_runner_prefs", Context.MODE_PRIVATE)

        programListView.onItemClickListener = AdapterView.OnItemClickListener { _, itemView, _, _ ->
            val nameTextView = itemView.findViewById<TextView>(R.id.LIST_Program_name)
            val programName = nameTextView.text.toString()

            // Save selection
            sharedPrefs.edit().putString("last_program_name", programName).apply()

            viewModel.selectProgram(programName)
            viewModel.startWorkout()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isWorkoutActive.collect { isActive ->
                    if (isActive) {
                        startLayout.visibility = View.GONE
                        workoutLayout.visibility = View.VISIBLE
                    } else {
                        startLayout.visibility = View.VISIBLE
                        workoutLayout.visibility = View.GONE
                        // Refresh cursor if returning to list
                        val newCursor = daoProgram.allPrograms
                        val oldAdapter = programListView.adapter as ProgramCursorAdapter
                        val oldCursor = oldAdapter.swapCursor(newCursor)
                        oldCursor?.close()
                    }
                }
            }
        }

        // Check for saved program on initial load
        if (!viewModel.isWorkoutActive.value) {
            val lastProgram = sharedPrefs.getString("last_program_name", null)
            if (lastProgram != null) {
                viewModel.selectProgram(lastProgram)
                viewModel.startWorkout()
            }
        }

        // Inflate the layout for this fragment
        return view
    }

    private val viewPagerAdapter: FragmentPagerItemAdapter?
        get() = ((requireView().findViewById<View?>(R.id.programrunner_pager)) as ViewPager).getAdapter() as FragmentPagerItemAdapter?

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            if (this.viewPagerAdapter != null) {
                var frag1: Fragment?
                for (i in 0..<this.viewPagerAdapter!!.getCount()) {
                    frag1 = this.viewPagerAdapter!!.getPage(i)
                    if (frag1 != null) frag1.onHiddenChanged(false) // Refresh data
                }
            }
        }
    }

    companion object {
        fun newInstance(name: String?, id: Int): FontesPagerFragment {
            val f = FontesPagerFragment()
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.setArguments(args)
            return f
        }
    }
}
