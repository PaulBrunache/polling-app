package faskteam.faskandroid.contollers.login_controllers;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.android.volley.Request;
//import com.wnafee.vector.compat.ResourcesCompat;

import org.json.JSONException;
import org.json.JSONObject;

import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.R;
import faskteam.faskandroid.utilities.CommonFunctions;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.http_connection.HttpResult;

public class RegistrationFragment extends DialogFragment implements View.OnClickListener {

    private View signUpCancel;
    private View signUpSubmit;
    private LoginActivity activity;
    private EditText fieldUsername;
    private EditText fieldPassword;
    private EditText fieldPasswordVerify;
    private EditText fieldEmail;
    private EditText fieldPhone;

    private Resources resources;

    private String username;
    private String password;
    private String email;
    private String phone;

    public RegistrationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_registration, container, false);

        activity = (LoginActivity) getActivity();
        resources = getResources();

        signUpCancel = view.findViewById(R.id.sign_up_cancel);
        signUpSubmit = view.findViewById(R.id.sign_up_submit);
        fieldUsername = (EditText) view.findViewById(R.id.regis_username);
        fieldPassword = (EditText) view.findViewById(R.id.regis_password);
        fieldPasswordVerify = (EditText) view.findViewById(R.id.regis_password_verify);
        fieldEmail = (EditText) view.findViewById(R.id.regis_email);
        fieldPhone = (EditText) view.findViewById(R.id.regis_phone);

        signUpCancel.setOnClickListener(this);
        signUpSubmit.setOnClickListener(this);

        if (getDialog() != null) {
            //remove obnoxiously large title view from dialog..
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onClick(View v) {
        int vID = v.getId();
        /* check the id of the calling view...
         * no matter which button is clicked, we'll be removing the registration fragment from the
         * fragment manager stack...*/
        if (vID == signUpSubmit.getId()) {
            Log.i("SAM - registrationFrag", "submit button");
            activity.startSpinner();

            if (validateForm()) {
                performRegistration();
            } else {
                activity.stopSpinner();
            }
            //validate form
            //connect to server and send info
        } else {
            /* only two views had their on click listeners set...so we can
            * almost safely assume that those are the only two views that
            * will call this method.*/
            activity.getSupportFragmentManager().popBackStack();
        }


    }

    /*
        validate user submitted form... if there are any errors alert user
     */
    private boolean validateForm() {

        username = fieldUsername.getText().toString();
        password = fieldPassword.getText().toString();
        String passwordVerify = fieldPasswordVerify.getText().toString();
        email = fieldEmail.getText().toString();
        phone = fieldPhone.getText().toString();

        //check if all fields are filled
        if (username.length() == 0 || password.length() == 0 || passwordVerify.length() == 0 || email.length() == 0) {
            DisplayAlert.displayAlertError(activity, resources.getString(R.string.require_all_fields), null);
            return false;
        }
        if (CommonFunctions.checkWhiteSpace(username) || CommonFunctions.checkWhiteSpace(password) || CommonFunctions.checkWhiteSpace(email)) {
            DisplayAlert.displayAlertError(activity,resources.getString(R.string.regis_white_space), null);
            return false;
        }
        //check password length
        if (password.length() < LoginActivity.MIN_PASSWORD_LENGTH) {
            DisplayAlert.displayAlertError(activity,resources.getString(R.string.regis_password_min_len), null);
            return false;
        }

        //check password format
        if (!correctFormat(password)) {
            DisplayAlert.displayAlertError(activity,resources.getString(R.string.regis_password_format), null);
            return false;
        }

        //check that passwordVerify matches the password
        if (!passwordVerify.equals(password)) {
            DisplayAlert.displayAlertError(activity,resources.getString(R.string.regis_password_verify), null);
            return false;
        }

        return true;
    }

    //determines if the supplied password has the minimum requirements as far as the number of
    //different and special characters and etc.
    private boolean correctFormat(String password) {
        //TODO complete validate password format with special character check
        return password.matches(".*[0-9].*") && password.matches(".*[a-z].*") && password.matches(".*[A-Z].*");
    }


    private void performRegistration() {
        JSONObject credentials = new JSONObject();


        try {
            credentials.put("UserName", this.username);
            credentials.put("Pass", this.password);
            credentials.put("Email", this.email);
            credentials.put("Phone", this.phone);
            credentials.put("FacebookCreds", 0);

            HttpConnect.requestJsonPost("/user", credentials.toString(), new HttpConnect.HttpResult<JSONObject>() {

                @Override
                public void onCallbackSuccess(JSONObject response) {
                    activity.stopSpinner();
                    DisplayAlert.displaySimpleMessage(activity, "Thank you for registering! Log in to continue.", null);
                    //return back to login screen
                    activity.getSupportFragmentManager().popBackStack();
                }

                @Override
                public void onCallbackError(String errorMsg, boolean fromServer) {
                    activity.stopSpinner();
                    if (fromServer) {
                        DisplayAlert.displayAlertError(activity, errorMsg, null);
                    } else {
                        DisplayAlert.displayAlertError(activity, "Sorry. An error occurred. Please try again", null);
                    }
                }
            });


            //TODO remove... keeping for easy user deletion during testing
//            HttpConnect.requestJson("/user/26"
//                    , Request.Method.DELETE, null, new HttpResult() {
//                @Override
//                public void onCallback(JSONObject response, boolean success) {
//                    if (!success) {
//                        DisplayAlert.displayAlertError(activity, "Sorry. An error occurred. Please try again", null);
//                    } else {
//                        DisplayAlert.displayAlertError(activity, "Thank you for Deleting! Log in to continue.", null);
//                        //return back to login screen
//                        activity.getSupportFragmentManager().popBackStack();
//                    }
//                }
//            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
