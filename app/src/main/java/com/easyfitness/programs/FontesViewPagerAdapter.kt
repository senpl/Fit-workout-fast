package com.easyfitness.programs

import ProgramSelectFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2 // Use ViewPager2
import com.easyfitness.R
//import com.easyfitness.programs.ProgramSelectFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
// Import your other fragments for the tabs like FonteHistoryFragment, FonteGraphFragment if they exist

// You'll need an adapter
class FontesViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Define your tabs. You can make this more dynamic.
    private val fragmentCreators: Map<Int, () -> Fragment> = mapOf(
        0 to { ProgramSelectFragment() }, // First tab
        1 to { ExercisesInProgramFragment() },  // Second tab (example)
        2 to { ProgramsFragment() }    // Third tab (example)
        // Add more tabs as needed
    )

    override fun getItemCount(): Int = fragmentCreators.size

    override fun createFragment(position: Int): Fragment {
        return fragmentCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}


class FontesPagerFragment : Fragment() { // Your original class, converted to Kotlin for example
    // Consider using ViewBinding for FontesPagerFragment's layout
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout // Your SmartTabLayout or a standard TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Ensure this layout contains a ViewPager2 and a TabLayout
        val view = inflater.inflate(R.layout.programrunner_pager, container, false) // RENAME this layout if it's not generic

        viewPager = view.findViewById(R.id.programrunner_pager) // RENAME this ID to view_pager_fontes or similar
        tabLayout = view.findViewById(R.id.noviewpagertab) // Your TabLayout ID

        // IMPORTANT: Use this for FragmentStateAdapter inside a Fragment
        val adapter = FontesViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Link TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.ProgramRunnerLabel) // Title for ProgramSelectFragment
                1 -> getString(R.string.HistoryLabel)    // Title for History
                2 -> getString(R.string.GraphLabel)      // Title for Graph
                else -> null
            }
        }.attach()

        // The old OnPageChangeListener can be adapted if needed, but TabLayoutMediator handles many things.
        // If you need to refresh data when a tab becomes visible, fragments have onResume or you can use
        // viewPager.registerOnPageChangeCallback for more specific logic.

        return view
    }

    // ... other methods from your original FontesPagerFragment if needed ...
}
