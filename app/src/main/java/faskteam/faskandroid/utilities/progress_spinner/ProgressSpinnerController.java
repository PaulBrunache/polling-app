package faskteam.faskandroid.utilities.progress_spinner;

import android.content.Context;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import faskteam.faskandroid.R;
import faskteam.faskandroid.utilities.CommonFunctions;


public class ProgressSpinnerController {

    private ViewGroup parent;
    private RelativeLayout spinnerContainer;
    private ProgressSpinnerView spinner;

    private Context context;

    private static final float DEFAULT_SPINNER_DIAMETER = 60.0f;

    public ProgressSpinnerController(Context context, ViewGroup parent){
        this(context, parent, DEFAULT_SPINNER_DIAMETER);
    }

    public ProgressSpinnerController (Context context, ViewGroup parent, float diameter){
        this.parent = parent;
        this.context = context;
        init(diameter);
    }

    //set layout structure of progress spinner and its container viewgroup.
    private void init(float diameter) {

        int scaledDiameter = CommonFunctions.scaleToDisplay(context, diameter);

        spinnerContainer = new RelativeLayout(context);
        ViewGroup.LayoutParams vg_params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        spinnerContainer.setLayoutParams(vg_params);
        spinnerContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.ui_bg_full_shadow));
        spinnerContainer.setClickable(true);

        spinner = new ProgressSpinnerView(context);
        RelativeLayout.LayoutParams rl_params = new RelativeLayout.LayoutParams(scaledDiameter, scaledDiameter);
        rl_params.addRule(RelativeLayout.CENTER_IN_PARENT);
        spinner.setLayoutParams(rl_params);

        spinnerContainer.addView(spinner);
        parent.addView(spinnerContainer);
        stopSpinner();
    }

    public void startSpinner() {
        spinnerContainer.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
    }

    public void stopSpinner() {
        spinnerContainer.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
    }
}
