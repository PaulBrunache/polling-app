package faskteam.faskandroid.contollers.main_controllers;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import faskteam.faskandroid.R;
import faskteam.faskandroid.utilities.FragmentCollectionPagerAdapter;


public class AllPollsFragment extends Fragment {

    public static final String CLASS_TAG = "AllPollsFragment";

    private MainActivity activity;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private View view;
    private FragmentManager manager;
    private FragmentCollectionPagerAdapter pagerAdapter;

    public static final String STATE_TAB_POS = "position";

    private Fragment[] fragments = {
            new TabNearbyPollsFragment(),
            new TabGlobalPollsFragment()
    };
    private String[] tabTitles = {"Nearby", "Global"};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        //Inflate layout
        view = inflater.inflate(R.layout.fragment_generic_view_pager, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        manager = getChildFragmentManager();
        pagerAdapter = new FragmentCollectionPagerAdapter(manager, fragments, tabTitles, activity);

        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);

        //update nav drawer selected menu item and update app bar title
        activity.setMenuSelected(R.id.nav_nearby_polls);
        activity.setAppBarTitle(getResources().getString(R.string.nav_global_polls));

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setFillViewport(true);

        activity.setCurrentFragment(fragments[0]);



        viewPager.addOnPageChangeListener(getOnPageChangeListener());

        return view;
    }

    public TabLayout.TabLayoutOnPageChangeListener getOnPageChangeListener() {
        return new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    Fragment f = pagerAdapter.getRegisteredFragment(viewPager.getCurrentItem());
                    activity.setCurrentFragment(f);
                }
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            //restore pager position
            viewPager.setCurrentItem(savedInstanceState.getInt(STATE_TAB_POS));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //store currently selected tab upon screen rotation or other config changes
        outState.putInt(STATE_TAB_POS, tabLayout.getSelectedTabPosition());
    }
}
