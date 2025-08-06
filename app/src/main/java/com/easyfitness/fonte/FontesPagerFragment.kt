package com.easyfitness.fonte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.easyfitness.R
import com.easyfitness.programs.NonSwipeableViewPager
import com.easyfitness.programs.ProgramRunner
import com.easyfitness.programs.ProgramRunnerComposeViewModel
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

class FontesPagerFragment : Fragment() {
    private var pagerAdapter: FragmentPagerItemAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.programrunner_pager, container, false)

        // Locate the viewpager in activity_main.xml
        val mViewPager = view.findViewById<NonSwipeableViewPager>(R.id.programrunner_pager)

        if (mViewPager.getAdapter() == null) {
            val args = this.getArguments()
            args!!.putLong("machineID", -1)
            args.putLong("machineProfile", -1)


//            pagerAdapter =
//                FragmentPagerItemAdapter( //                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
//                    //                .add(R.string.ProgramRunnerLabel, ProgramRunnerScreenKt.class)
//                    getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
//                        .add(
//                            R.string.ProgramRunnerLabel,
//                            ProgramRunnerComposeViewModel::class.java
//                        )
////                        FragmentPagerItemAdapter( //                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
////                    //                .add(R.string.ProgramRunnerLabel, ProgramRunnerScreenKt.class)
////                    getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
////                        .add(
////                            R.string.ProgramRunnerLabel,
////                            ProgramRunner::class.java
////                        )
//                        //                .add(R.string.ExerciceLabel, FontesFragment.class)
//                        //                .add(R.string.GraphLabel, FonteGraphFragment.class, args)
//                        //                .add(R.string.HistoryLabel, FonteHistoryFragment.class, args)
//                        .create()
//                )

            mViewPager.setAdapter(pagerAdapter)

            val viewPagerTab = view.findViewById<SmartTabLayout>(R.id.noviewpagertab)
            viewPagerTab.setViewPager(mViewPager)

            viewPagerTab.setOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    //if (position != 0) {
                    //pagerAdapter.getItem(position).onHiddenChanged(false);
                    //Fragment frag1 = (Fragment) pagerAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());

                    val frag1 = pagerAdapter!!.getPage(position)
                    if (frag1 != null) frag1.onHiddenChanged(false) // Refresh data


                    //}
                }

                override fun onPageScrollStateChanged(state: Int) {
                }
            })

            // Locate the viewpager in activity_main.xml
            //ViewPager viewPager = view.findViewById(R.id.pager);

            // Set the ViewPagerAdapter into ViewPager
            //viewPager.setAdapter(new FontesViewPagerAdapter(getChildFragmentManager(), getActivity().getApplicationContext()));

            // Bind the tabs to the ViewPager
            //PagerSlidingTabStrip tabs = view.findViewById(R.id.tabs);
            //tabs.setViewPager(viewPager);
        }

        // Inflate the layout for this fragment
        return view
    }

    private val viewPagerAdapter: FragmentPagerItemAdapter?
        //    public void onPageSelected(int position) {
        get() = ((requireView().findViewById<View?>(R.id.programrunner_pager)) as ViewPager).getAdapter() as FragmentPagerItemAdapter?

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            // rafraichit le fragment courant

            if (this.viewPagerAdapter != null) {
                // Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit.
                // Il faut donc que cela soit pris en compte dans le refresh des fragments.
                var frag1: Fragment?
                for (i in 0..<this.viewPagerAdapter!!.getCount()) {
                    frag1 = this.viewPagerAdapter!!.getPage(i)
                    if (frag1 != null) frag1.onHiddenChanged(false) // Refresh data
                }
            }
        }
    } //    public FontesFragment getFontesFragment() {
    //        if (mpFontesFrag == null)
    //            mpFontesFrag = (FontesFragment) getChildFragmentManager().findFragmentByTag(MainActivity.FONTES);
    //        if (mpFontesFrag == null) mpFontesFrag = FontesFragment.newInstance(MainActivity.FONTES, 1);
    //
    //        //mpFontesFrag.onHiddenChanged(false);
    //        return mpFontesFrag;
    //    }
    //
    //    public FonteGraphFragment getGraphFragment() {
    //        if (mpGraphFrag == null)
    //            mpGraphFrag = (FonteGraphFragment) getChildFragmentManager().findFragmentByTag(MainActivity.GRAPHIC);
    //        if (mpGraphFrag == null)
    //            mpGraphFrag = FonteGraphFragment.newInstance(MainActivity.GRAPHIC, 2);
    //
    //        //mpGraphFrag.onHiddenChanged(false);
    //        return mpGraphFrag;
    //    }
    //
    //    public FonteHistoryFragment getHistoricFragment() {
    //        if (mpHistoryFrag == null)
    //            mpHistoryFrag = (FonteHistoryFragment) getChildFragmentManager().findFragmentByTag(MainActivity.HISTORY);
    //        if (mpHistoryFrag == null)
    //            mpHistoryFrag = FonteHistoryFragment.newInstance(-1, -1);
    //
    //        //mpHistoryFrag.onHiddenChanged(false);
    //        return mpHistoryFrag;
    //    }

    companion object {
        //    private String name;
        //    private int id;
        //    private FontesFragment mpFontesFrag = null;
        //    private FonteHistoryFragment mpHistoryFrag = null;
        //    private FonteGraphFragment mpGraphFrag = null;
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): FontesPagerFragment {
            val f = FontesPagerFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.setArguments(args)

            return f
        }
    }
}
