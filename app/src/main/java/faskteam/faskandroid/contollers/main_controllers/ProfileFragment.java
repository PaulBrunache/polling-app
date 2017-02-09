package faskteam.faskandroid.contollers.main_controllers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import faskteam.faskandroid.R;


public class ProfileFragment extends Fragment {

    private MainActivity mainActivity;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        //update nav drawer selected menu item and update app bar title
//        mainActivity.setMenuSelected(R.id.nav_main);
        mainActivity.setAppBarTitle("Profile");
        // Inflate the layout for this fragment

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();

    }
}
