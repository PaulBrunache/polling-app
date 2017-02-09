package faskteam.faskandroid.contollers.main_controllers.social;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import faskteam.faskandroid.R;
import faskteam.faskandroid.contollers.main_controllers.MainActivity;
import faskteam.faskandroid.contollers.main_controllers.TabGlobalPollsFragment;
import faskteam.faskandroid.contollers.main_controllers.TabNearbyPollsFragment;
import faskteam.faskandroid.utilities.FragmentCollectionPagerAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class SocialFragment extends Fragment {

    private MainActivity activity;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private View view;
    private FragmentManager manager;
    private FragmentCollectionPagerAdapter pagerAdapter;

    public static final String CLASS_TAG = "AllPollsFragment";
    public static final String STATE_TAB_POS = "position";

    private Fragment[] fragments = {
            new TabFriendsFragment(),
            new TabFriendsSearchFragment(),
            new TabFriendsPendingRequestsFragment(),
            new TabFriendsFromContactsFragment(),
            new GroupsFragment()
    };
    private String[] tabTitles = {"Friends", "Search", "Requests", "Suggestions", "Groups"};
//    private String[] tabTitles = {"Friends", "Requests", "From Contacts", "Groups"};


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
        activity.setMenuSelected(R.id.nav_friends_and_groups);
        activity.setAppBarTitle(getResources().getString(R.string.nav_friends));

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setFillViewport(true);

//        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
//        actionBar.setNavigationMode();

        return view;
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
