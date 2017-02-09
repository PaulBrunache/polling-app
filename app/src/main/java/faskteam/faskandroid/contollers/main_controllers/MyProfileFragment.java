package faskteam.faskandroid.contollers.main_controllers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import faskteam.faskandroid.R;
import faskteam.faskandroid.entities.Session;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;

/**
 * Created by Andrew on 11/29/2015.
 */
public class MyProfileFragment extends MyFragment implements View.OnClickListener{

    private User user = Session.getInstance().getUser();
    private ImageView userProfileImage;
    private TextView usernameView;
    private TextView totalPoints;
    private TextView askedPoints;
    private TextView answeredPoints;
    private TextView fbConnectTextView;
    private ListView achievementList;
    private View facebookConnect;
    private CallbackManager callbackManager;
    private LoginManager fbLoginManager;
    private LinearLayout profileIcons;
    private ImageView icon1;
    private ImageView icon2;
    private ImageView icon3;
    private ImageView icon4;

    //TODO: Get facebook id then call service with fbid + userid

    private int userIcon;
    private int totalP;
    private int askedP;
    private int answeredP;
    private List<String> achievementTitles = new ArrayList<String>();

    private static final String[] fbReadPermissions = {
            "public_profile",
            "user_friends",
            "email"
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_profile_view, container, false);

        callbackManager = CallbackManager.Factory.create();
        fbLoginManager = LoginManager.getInstance();

        userProfileImage = (CircleImageView) layout.findViewById(R.id.profile_image);
        userProfileImage.setOnClickListener(profileImageClicked());

        usernameView = (TextView) layout.findViewById(R.id.profile_username);
        usernameView.setText(user.getUsername());

        totalPoints = (TextView) layout.findViewById(R.id.profile_points);
        askedPoints = (TextView) layout.findViewById(R.id.profile_asked_points);
        answeredPoints = (TextView) layout.findViewById(R.id.profile_answered_points);

        achievementList = (ListView) layout.findViewById(R.id.profile_acheivements);
        achievementList.setChoiceMode(ListView.CHOICE_MODE_NONE);

        profileIcons = (LinearLayout) layout.findViewById(R.id.profile_icons);
        icon1 = (ImageView) layout.findViewById(R.id.icon_option_1);
        icon1.setOnClickListener(this);
        icon2 = (ImageView) layout.findViewById(R.id.icon_option2);
        icon2.setOnClickListener(this);
        icon3 = (ImageView) layout.findViewById(R.id.icon_option3);
        icon3.setOnClickListener(this);
        icon4 = (ImageView) layout.findViewById(R.id.icon_option4);
        icon4.setOnClickListener(this);

        facebookConnect = layout.findViewById(R.id.profile_connect_facebook);
        facebookConnect.setOnClickListener(facebookButtonSelected());
        fbConnectTextView = (TextView) layout.findViewById(R.id.profile_fb_connect_text);
        //only show facebook connect option if user is connected to facebook
        if (user.isFaceBookUser()) {
            fbConnectTextView.setText(":) Profile is connected with Facebook");
        }

        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            getUserInfo();
        } catch (Exception e) {
            e.printStackTrace();
            DisplayAlert.displayAlertError(activity, "Sorry. An error occurred when retrieving profile information", null);
        }
    }

    private void getUserInfo(){
        startSpinner();
//        String url = "/user/" + user.getuID();
//        Log.i("UserIDProf", "" + user.getuID());

        HttpConnect.requestJsonGet(String.format("/user/%d", user.getuID()), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    Log.i("UserProfile", response.toString());
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    for(int i = 0; i < data.length(); i++){
                        JSONObject uData = data.getJSONObject(i);
                        askedP = uData.getInt("AskPoints");
                        askedPoints.setText(getString(R.string.profile_asked_points, askedP));
                        answeredP = uData.getInt("AnswerPoints");
                        answeredPoints.setText(getString(R.string.profile_answered_points, answeredP));
                        totalP = askedP + answeredP;
                        totalPoints.setText(getString(R.string.profile_total_points, totalP));
                        userIcon = uData.optInt("UserIcon", -1);
                        getUserIcon(userIcon);
                        getUserAchievements();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();
                DisplayAlert.displayAlertError(getContext(), errorMsg, null);
            }
        });
    }

    private void getUserAchievements(){
        //String url = "/achievements/ask_points/" + askedP + "/answer_points/" + answeredP;
        achievementTitles.clear();
        HttpConnect.requestJsonGet(String.format("/achievements/ask_points/%d/answer_points/%d", askedP, answeredP), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    Log.i("Acheivements", data.toString());
                    for (int i = 0; i < data.length(); i++) {
                        achievementTitles.add(data.getString(i));
                    }
                    if (achievementTitles.size() > 0) {
                        usernameView.setText(getNameTitles(achievementTitles.size() - 1));
                        setAchievementList(achievementTitles);
                    }
                    //stopSpinner();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                stopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();
                DisplayAlert.displayAlertError(getContext(), errorMsg, null);
            }
        });
    }

    private void setAchievementList(List<String> achievements) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, achievements);
        achievementList.setAdapter(adapter);
    }

    private String getNameTitles(int item){
        return user.getUsername() + ", the " + achievementTitles.get(item);
    }

    private View.OnClickListener facebookButtonSelected(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Profile", "facebookButtonSelected");
                connectWithFacebook();
            }
        };
    }

    private View.OnClickListener profileImageClicked(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(profileIcons.getVisibility() == View.GONE) {
                    profileIcons.setVisibility(View.VISIBLE);
                }
                else if(profileIcons.getVisibility() == View.VISIBLE) {
                    profileIcons.setVisibility(View.GONE);
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.icon_option_1:
                updateProfileIcon(1);
                break;
            case R.id.icon_option2:
                updateProfileIcon(2);
                break;
            case R.id.icon_option3:
                updateProfileIcon(3);
                break;
            case R.id.icon_option4:
                updateProfileIcon(4);
                break;
        }
    }

    private void updateProfileIcon(int iconId) {
        startSpinner();
        JSONObject iconRequest = new JSONObject();
        try {
            iconRequest.put("UserID", activity.getSessionUser().getuID());
            iconRequest.put("UserIcon", iconId);
            HttpConnect.requestJsonPut("/user", iconRequest.toString(), new HttpConnect.HttpResult<JSONObject>() {
                @Override
                public void onCallbackSuccess(JSONObject response) {
                    stopSpinner();
                    Toast.makeText(activity, "Your profile icon has been updated.", Toast.LENGTH_LONG).show();
                    profileIcons.setVisibility(View.GONE);
                    getUserInfo();
                }

                @Override
                public void onCallbackError(String errorMsg, boolean fromServer) {
                    stopSpinner();
                    DisplayAlert.displayAlertError(activity, errorMsg, null);
                }
            });
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectWithFacebook(){
        fbLoginManager.logInWithReadPermissions(this, Arrays.asList(fbReadPermissions));
        fbLoginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("ProfileLogin", "Success");

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.i("ProfileLogin", object.toString());
                                JSONObject requestID = new JSONObject();
                                try {
                                    String facebookID = object.getString("id");
                                    requestID.put("FacebookID", facebookID);
                                    requestID.put("UserID", user.getuID());
                                    putFacebookIDRequest(requestID);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.i("Profile", "Facebook Regis Cancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.i("Profile", "Facebook Regis Error");
                //TODO remove
                System.out.println("Error :" + error.getMessage());
                error.printStackTrace();
                if (error instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        fbLoginManager.logOut();
                        fbLoginManager.logInWithReadPermissions(MyProfileFragment.this, Arrays.asList(fbReadPermissions));
                    }
                }
            }
        });


    }

    private void putFacebookIDRequest(JSONObject object){
        startSpinner();
        HttpConnect.requestJsonPut("/add_facebook", object.toString(), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                stopSpinner();
                fbConnectTextView.setText(":) Profile is connected with Facebook");
                DisplayAlert.displaySimpleMessage(getContext(), "Thanks you! Use Facebook Login Button to sign into FAsk.", "Success");
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();
                DisplayAlert.displayAlertError(getContext(), errorMsg, null);
            }
        });
    }

    private void getUserIcon(int iconNum){

        switch(iconNum){
            case 1:
                userProfileImage.setImageResource(R.drawable.bumblebee_100);
                break;
            case 2:
                userProfileImage.setImageResource(R.drawable.cat_100);
                break;
            case 3:
                userProfileImage.setImageResource(R.drawable.panda_100);
                break;
            case 4:
                userProfileImage.setImageResource(R.drawable.year_of_monkey_100);
                break;
            default:
                userProfileImage.setImageResource(R.drawable.user_icon);
                break;
        }
    }
}
