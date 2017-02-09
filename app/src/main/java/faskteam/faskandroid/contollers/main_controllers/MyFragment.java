package faskteam.faskandroid.contollers.main_controllers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import faskteam.faskandroid.utilities.progress_spinner.ProgressSpinnerController;

public class MyFragment extends Fragment {
    public static final String CLASS_TAG = MyFragment.class.getSimpleName();

    private ProgressSpinnerController mSpinner;
    protected MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (MainActivity) getActivity();
        mSpinner = new ProgressSpinnerController(activity, (ViewGroup) view);
    }

    public void startSpinner() {
        mSpinner.startSpinner();
    }

    public void stopSpinner() {
        mSpinner.stopSpinner();
    }
}
