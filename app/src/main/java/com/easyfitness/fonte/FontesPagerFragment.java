package com.easyfitness.fonte;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.easyfitness.R;
import com.easyfitness.programs.NonSwipeableViewPager;
import com.easyfitness.programs.ProgramRunner;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;


public class FontesPagerFragment extends Fragment {
    private FragmentPagerItemAdapter pagerAdapter = null;
    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static FontesPagerFragment newInstance(String name, int id) {
        FontesPagerFragment f = new FontesPagerFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.programrunner_pager, container, false);

        // Locate the viewpager in activity_main.xml
        NonSwipeableViewPager mViewPager = view.findViewById(R.id.programrunner_pager);

        if (mViewPager.getAdapter() == null) {

            Bundle args = this.getArguments();
            assert args != null;
            args.putLong("machineID", -1);
            args.putLong("machineProfile", -1);

            pagerAdapter = new FragmentPagerItemAdapter(
//                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
//                .add(R.string.ProgramRunnerLabel, ProgramRunner.class)
                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                .add(R.string.ProgramRunnerLabel, ProgramRunner.class)
              .create());

            mViewPager.setAdapter(pagerAdapter);

            SmartTabLayout viewPagerTab = view.findViewById(R.id.noviewpagertab);
            viewPagerTab.setViewPager(mViewPager);

            viewPagerTab.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    //if (position != 0) {
                    //pagerAdapter.getItem(position).onHiddenChanged(false);
                    //Fragment frag1 = (Fragment) pagerAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());

                    Fragment frag1 = pagerAdapter.getPage(position);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data

                    //}
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }

    private FragmentPagerItemAdapter getViewPagerAdapter() {
        return (FragmentPagerItemAdapter) ((ViewPager) (requireView().findViewById(R.id.programrunner_pager))).getAdapter();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            // rafraichit le fragment courant

            if (getViewPagerAdapter() != null) {
                // Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit.
                // Il faut donc que cela soit pris en compte dans le refresh des fragments.
                Fragment frag1;
                for (int i = 0; i < getViewPagerAdapter().getCount(); i++) {
                    frag1 = getViewPagerAdapter().getPage(i);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }
            }
        }
    }
}
