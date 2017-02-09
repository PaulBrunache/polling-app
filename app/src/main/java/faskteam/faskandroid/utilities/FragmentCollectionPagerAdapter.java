package faskteam.faskandroid.utilities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import faskteam.faskandroid.contollers.main_controllers.MainActivity;

public class FragmentCollectionPagerAdapter extends FragmentPagerAdapter {

    public static final String CLASS_TAG = "FragmentCollectionPagerAdapter";

    private SparseArray<Fragment> registeredFragments = new SparseArray<>();

    private Fragment[] fragments;
    private String[] tabTitles;
    private MainActivity activity;

    public FragmentCollectionPagerAdapter(FragmentManager fm, Fragment[] fragments, String[] tabTitles, MainActivity activity) {
        super(fm);
        this.fragments = fragments;
        this.tabTitles = tabTitles;
        this.activity = activity;
    }


    @Override
    public Fragment getItem(int position) {
        //TODO add arguments to fragment
        return fragments[position];
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}