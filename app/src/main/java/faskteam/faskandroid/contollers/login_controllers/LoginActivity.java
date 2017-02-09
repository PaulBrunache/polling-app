package faskteam.faskandroid.contollers.login_controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import faskteam.faskandroid.R;
import faskteam.faskandroid.contollers.main_controllers.MainActivity;
import faskteam.faskandroid.entities.Session;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.progress_spinner.ProgressSpinnerController;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CLASS_TAG = "LoginActivity";

    // UI references.
    private EditText mFieldUsername;
    private EditText mFieldPassword;
    private View mButtonForgotPassword;
    private Button mButtonLogin;
    private Button mButtonSignUp;
//    private LoginButton mFacebookLogin;
    private View mFacebookSignUp;
    private View mFacebookLogin;
    private ProgressSpinnerController globalSpinnerController;

    private CallbackManager callbackManager;
    private LoginManager fbLoginManager;


    //permissions for accessing facebook users' profile data
    private static final String[] fbReadPermissions = {
            "public_profile",
            "user_friends",
            "email"
    };

    //minimum password length
    public static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();
        fbLoginManager = LoginManager.getInstance();

        mFieldUsername = (EditText) findViewById(R.id.login_field_username);
        mFieldPassword = (EditText) findViewById(R.id.login_field_password);
        mButtonForgotPassword = findViewById(R.id.login_activity_textview_forgot_password);
        mButtonLogin = (Button) findViewById(R.id.login_activity_button_login_sign_in);
        mButtonSignUp = (Button) findViewById(R.id.login_activity_button_sign_up);
//        mFacebookLogin = (LoginButton) findViewById(R.id.facebook_btn);
        mFacebookLogin = findViewById(R.id.login_activity_button_facebook_signin);
        mFacebookSignUp = findViewById(R.id.login_activity_button_facebook__signup);

        mButtonSignUp.setOnClickListener(this);
        mButtonLogin.setOnClickListener(this);
        mButtonForgotPassword.setOnClickListener(this);
        mFacebookLogin.setOnClickListener(this);
        mFacebookSignUp.setOnClickListener(this);

//        facebookLoginAction();
//        facebookSignupAction();

        globalSpinnerController = new ProgressSpinnerController(this, (ViewGroup) findViewById(R.id.login_activity_container));


    }

    private void facebookLoginAction(){
        fbLoginManager.logInWithReadPermissions(this, Arrays.asList(fbReadPermissions));
        fbLoginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(CLASS_TAG, "Facebook Login Success");
                startSpinner();

                /* {FacebookID: facebook_id} */
                JSONObject requestData = new JSONObject();
                try {
                    requestData.put("FacebookID", loginResult.getAccessToken().getUserId());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                HttpConnect.requestJsonPost("/fb_login", requestData.toString(), new HttpConnect.HttpResult<JSONObject>() {
                    @Override
                    public void onCallbackSuccess(JSONObject response) {
                        stopSpinner();
                        try {
                            JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                            JSONObject userData = data.getJSONObject(0);

                            String username = userData.getString("UserName");
                            int uID = userData.getInt("UserID");
                            String email = userData.getString("Email");
                            String fbID = userData.optString("FacebookID", "");
                            boolean isFacebookUser = (!fbID.isEmpty() || !fbID.toLowerCase().equals("null"));

                            String sessionID = userData.getString("Session");
                            User sessionUser = new User(uID, username, email);
                            sessionUser.setIsFaceBookUser(isFacebookUser);

                            // check if there is a session running already..
                            if(Session.hasInstance()) {
                                //there shouldn't be, however if there is... close it
                                Log.e(CLASS_TAG, "There is a session already in progress");
                                Session.endSession();
                            }

                            //create new session
                            Session.createInstance(sessionID, sessionUser);
                            //start main activity
                            startMainActivity();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCallbackError(String errorMsg, boolean fromServer) {
                        stopSpinner();
                        DisplayAlert.displayAlertError(LoginActivity.this, getString(R.string.general_error_msg), null);
                    }
                });


            }

            @Override
            public void onCancel() {
                Log.i(CLASS_TAG, "Facebook Login Canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i(CLASS_TAG, "Facebook Login Error");
                //TODO remove
                System.out.println("Error :" + error.getMessage());
                error.printStackTrace();
                if (error instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        fbLoginManager.logOut();
                        fbLoginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList(fbReadPermissions));
                    }
                }
            }
        });
    }

    private void registerWithFacebook(JSONObject requestData) {
        startSpinner();
        HttpConnect.requestJsonPost("/fb_user", requestData.toString(), new HttpConnect.HttpResult<JSONObject>(){
            @Override
            public void onCallbackSuccess(JSONObject response) {
                stopSpinner();
                DisplayAlert.displaySimpleMessage(LoginActivity.this, "Thanks you! Use Facebook Login Button to sign into FAsk.", "Success");
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();
                DisplayAlert.displayAlertError(LoginActivity.this, getString(R.string.general_error_msg), null);
            }
        });
    }

    private void facebookSignupAction() {
        /* though this is the sign up, we'll still have to have the user login with facebook
        * ...after the login process we'll create a new fask user then sign them in
        * automatically...*/

        fbLoginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(CLASS_TAG, "Facebook Registration Success");
                /*
                * We need to create a new registration dictionary with the following...
                * currently the fask registration for facebook users doesnt allow them
                * to create their own usernames...
                *
                * {
                *   FacebookID: facebook_id,
                *   FirstName: first_name,
                *   LastName: last_name,
                *   Phone: phone (optional),
                *   Email: email (optional)
                * }
                */

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v(CLASS_TAG, response.toString());
                                Log.v(CLASS_TAG, object.toString());
                                JSONObject requestDat = new JSONObject();
                                try {
                                    //TODO check for phone number

                                    // grab the needed user data then send to fask server for
                                    // registration
                                    String firstname = object.getString("first_name");
                                    String lastname = object.getString("last_name");
                                    String facebookID = object.getString("id");
                                    String email = object.optString("email", null);
                                    requestDat.put("FirstName", firstname);
                                    requestDat.put("LastName", lastname);
                                    requestDat.put("FacebookID", facebookID);
                                    if (email != null) {
                                        requestDat.put("Email", email);
                                    }

                                    //register facebook user
                                    registerWithFacebook(requestDat);
                                } catch (Exception e) {
                                    //TODO remove
                                    e.printStackTrace();
                                }

                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.i(CLASS_TAG, "Facebook Registration Canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i(CLASS_TAG, "Facebook Regis Error");
                //TODO remove
                System.out.println("Error :" + error.getMessage());
                error.printStackTrace();
                if (error instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        fbLoginManager.logOut();
                        fbLoginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList(fbReadPermissions));
                    }
                }
            }
        });

        fbLoginManager.logInWithReadPermissions(this, Arrays.asList(fbReadPermissions));

    }

    private void forgotPasswordAction() {
        //TODO
    }


    private void signUpButtonAction() {
        FragmentManager fm = getSupportFragmentManager();
        RegistrationFragment rf = new RegistrationFragment();
        fm.beginTransaction().replace(R.id.sign_up_container, rf, null).addToBackStack(null).commit();
    }

    private void attemptLogin() {
        final String username = mFieldUsername.getText().toString();
        final String password = mFieldPassword.getText().toString();

        if (username.length() == 0 || password.length() == 0) {
            DisplayAlert.displayAlertError(LoginActivity.this, "Invalid username and password combination", null);
            return;
        }

        JSONObject credentials = new JSONObject();

        try {
            startSpinner();
            credentials.put("UserName", username);
            credentials.put("Pass", password);

            HttpConnect.requestJsonPost("/login", credentials.toString()
                    , new HttpConnect.HttpResult<JSONObject>() {
                @Override
                public void onCallbackSuccess(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                        JSONObject userData = data.getJSONObject(0);

                        String username = userData.getString("UserName");
                        int uID = userData.getInt("UserID");
                        String email = userData.getString("Email");

                        String sessionID = userData.getString("Session");
                        User sessionUser = new User(uID, username, email);

                        // check if there is a session running already..
                        if(Session.hasInstance()) {
                            //there shouldn't be, however if there is... close it
                            Log.e(CLASS_TAG, "There is a session already in progress");
                            Session.endSession();
                        }

                        //create new session
                        Session.createInstance(sessionID, sessionUser);
                        //start main activity
                        startMainActivity();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    stopSpinner();
                }

                @Override
                public void onCallbackError(String errorMsg, boolean fromServer) {
                    if (fromServer){
                        DisplayAlert.displayAlertError(LoginActivity.this, errorMsg, null);
                    } else {
                        DisplayAlert.displayAlertError(LoginActivity.this, getString(R.string.general_error_msg), null);
                    }
                    stopSpinner();
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.login_activity_button_sign_up:
                signUpButtonAction();
                break;
            case R.id.login_activity_button_login_sign_in:
                attemptLogin();
                break;
            case R.id.login_activity_textview_forgot_password:
                forgotPasswordAction();
                break;
            case R.id.login_activity_button_facebook_signin:
                facebookLoginAction();
                break;
            case R.id.login_activity_button_facebook__signup:
                facebookSignupAction();
                break;
            default:
        }
    }

    /*allowing for global spinner control from fragments*/
    public void stopSpinner() {
        globalSpinnerController.stopSpinner();
    }
    public void startSpinner() {
        globalSpinnerController.startSpinner();
    }

}

