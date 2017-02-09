package faskteam.faskandroid.contollers.main_controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import com.wnafee.vector.compat.ResourcesCompat;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import faskteam.faskandroid.contollers.main_controllers.social.SocialFragment;
import faskteam.faskandroid.entities.Session;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.R;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.MenuSearchListener;
import faskteam.faskandroid.utilities.gps_location.LocationCallback;
import faskteam.faskandroid.utilities.gps_location.MyLocationListener;
import faskteam.faskandroid.utilities.MySharedPreferences;
import faskteam.faskandroid.utilities.google_api.cloud_messaging.RegistrationIntentService;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.progress_spinner.ProgressSpinnerController;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CLASS_TAG = MainActivity.class.getSimpleName();

    private static MainActivity activity;
    private User sessionUser;
    private SharedPreferences sharedPreferences;
    GoogleApiClient googleApiClient = null;


    //main application app bar
    private Toolbar appBar;
    private TextView appBarTitle;
    //main application navigation drawer
    private NavigationView navDrawer;
    //main layout for activity
    private DrawerLayout drawerLayout;
    private SearchBox searchBox;

    private ActionBarDrawerToggle navDrawerToggle;
    private FragmentManager manager;
    private Fragment currentFragment;
    private ProgressSpinnerController globalSpinnerController;
    private BroadcastReceiver mRegistrationBroadcastReceiver;


    private int currentSelectedMenuItem = Integer.MIN_VALUE;

    //activity request codes
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 2000;
    public static final int REQUEST_CREATE_POLL_ACTIVITY = 2001;
    public static final int REQUEST_ANSWER_POLL_ACTIVITY = 2002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        manager = getSupportFragmentManager();
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        appBar = (Toolbar) findViewById(R.id.app_bar);
        appBarTitle = (TextView) findViewById(R.id.actionbar_title_text);
        navDrawer = (NavigationView) findViewById(R.id.nav_drawer);
        globalSpinnerController = new ProgressSpinnerController(this, (ViewGroup) findViewById(R.id.main_activity_container));
        searchBox = (SearchBox) findViewById(R.id.search_box);

        appBar.setTitle("");
        setSupportActionBar(appBar);

        searchBox.enableVoiceRecognition(this);

        //check first item in nav menu
        navDrawer.getMenu().getItem(1).setChecked(true);
        navDrawer.setNavigationItemSelectedListener(this);
        //create and set the toolbar drawer toggle button event listener
        navDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, appBar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);

        drawerLayout.setDrawerListener(navDrawerToggle);
        navDrawerToggle.syncState();

        initNavViewHeader();

        initGcmBroadcastReceiver();
        getGcmToken();

        sessionUser = Session.getInstance().getUser();

        //start first fragment view... soooo
        changeFragment(new MyPollsFragment(), null);
    }

    private void initNavViewHeader(){
        View navHeader = navDrawer.inflateHeaderView(R.layout.navigation_view_header);
        User sUser = Session.getInstance().getUser();
        ((TextView) navHeader.findViewById(R.id.nav_drawer_header_username)).setText(sUser.getUsername());
        ((TextView) navHeader.findViewById(R.id.nav_drawer_header_email)).setText(sUser.getEmail());
    }


    /**
     * Set navigation view event listener action
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        Fragment frag = null;
        String tag = "";
        int mId = menuItem.getItemId();
        if (mId != currentSelectedMenuItem || mId == R.id.nav_logout) {
            switch(mId) {
                case (R.id.nav_my_profile):
                    frag = new MyProfileFragment();
                    break;
                case (R.id.nav_my_polls):
                    frag = new MyPollsFragment();
                    tag = "my_polls";
                    break;
                case (R.id.nav_fask_polls):
                    frag = new FaskPollsFragment();
                    tag = "fask_polls";
                    break;
                case (R.id.nav_nearby_polls):
                    frag = new TabNearbyPollsFragment();
                    tag = "nearby";
                    break;
                case (R.id.nav_friends_and_groups):
                    frag = new SocialFragment();
                    break;
//                case (R.id.nav_rank):
//                    break;
//                case (R.id.nav_settings):
//                    break;
                case (R.id.nav_logout):
                    beginLogout();
                    break;
                default:
                    break;
            }
            if(frag != null) {
                changeFragment(frag, tag);
            }
            currentSelectedMenuItem = mId;
        }

        drawerLayout.closeDrawer(navDrawer);
        return true;
    }

    public void setMenuSelected(int id) {
        navDrawer.getMenu().findItem(id).setChecked(true);
    }


    public User getSessionUser() {
        return sessionUser;
    }

    /**
     * Removes the current fragment(s) in the activity and replaces it with the passed in Fragment
     * parameter. The removed fragment is added to the back stack of the fragment manager.
     * Tag String is used to name the fragment in the transaction hierarchy.
     *
     * @param f Fragment
     * @param tag String
     */
    protected void changeFragment(Fragment f, String tag) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_content_frag_container, f, tag);
        transaction.commit();
        currentFragment = f;
    }

    public void pushFragment(Fragment f, String tag) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_content_frag_container, f, tag);
        transaction.addToBackStack(null);
        transaction.commit();
        currentFragment = f;
    }

    public void setCurrentFragment(Fragment f) {
        this.currentFragment = f;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* called when returning from an activity created and opened from this activity */

        if (requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches.get(0));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_search) {
            openSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(manager.getBackStackEntryCount() == 0){
            beginLogout();
        } else {
            super.onBackPressed();
        }
    }

    public void openSearch() {
        searchBox.revealFromMenuItem(R.id.action_search, this);
        searchBox.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {
                // Use this to tint the screen
            }

            @Override
            public void onSearchCleared() {

            }

            @Override
            public void onSearchClosed() {
                // Use this to un-tint the screen
                closeSearch();
            }

            @Override
            public void onSearchTermChanged(String s) {
                // React to the search term changing
                // Called after it has updated results
                Log.i(CLASS_TAG, "SearchChanged: " + s);
            }

            @Override
            public void onSearch(String s) {
                Log.i(CLASS_TAG, "Searched: " + s);

                //search menu will only be visible on fragments that
                //implement MenuSearchListener and set the menu options
                //to show search icon

                //cast current fragment as listener
                MenuSearchListener listener = (MenuSearchListener) currentFragment;
                //pass search item to listener
                listener.onSearchSubmit(s);

            }

            @Override
            public void onResultClick(SearchResult searchResult) {
                //React to result being clicked
                Log.i(CLASS_TAG, "ResultClick: " + searchResult.title);

            }
        });

    }

    protected void closeSearch() {
        searchBox.hideCircularly(this);
//        if (searchBox.getSearchText().isEmpty()) {
//            toolbar.setTitle("");
//        }
    }

    private void beginLogout() {

        DisplayAlert.displayCustomMessage(this, "Are you sure you want to log out of FAsk?", null,
                new DisplayAlert.AlertDetails("Yes", "No"), new DisplayAlert.AlertCallback() {
                    @Override
                    public void onPositive(DialogInterface dialog) {
                        performLogout();
                    }

                    @Override
                    public void onNegative(DialogInterface dialog) {

                    }

                    @Override
                    public void onAny(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });

    }

    private void performLogout(){
        String gcmToken = sharedPreferences.getString(MySharedPreferences.FASK_GCM_TOKEN, "");
        int uID = Session.getInstance().getUser().getuID();

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("UserID", uID);
            requestData.put("RegistrationKey", gcmToken);

            //delete GCM registration token from to server
            HttpConnect.requestJsonPost("/logoff", requestData.toString(), new HttpConnect.HttpResult<JSONObject>() {

                @Override
                public void onCallbackSuccess(JSONObject response) {
                    Log.e(CLASS_TAG, "GCM id deleted from server: " + response.toString());
                }

                @Override
                public void onCallbackError(String errorMsg, boolean fromServer) {
                    Log.e(CLASS_TAG, "Error deleting GCM id from server: " + errorMsg);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }

        Session.endSession();

        //close the activity and return to login screen
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(MySharedPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        navDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void setAppBarTitle(String title) {
        appBarTitle.setText(title);
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("play services", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void initGcmBroadcastReceiver() {

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.w("MainActivity", "received registration broadcast");

                String token = sharedPreferences.getString(MySharedPreferences.FASK_GCM_TOKEN, "");

                //token should have a non empty empty value.. but
                // just in case its still not there...redue service call
                // otherwise send token to server...
                if(token.isEmpty()) {
                    // Start IntentService to register this application with GCM.
                    Intent newTokenIntent = new Intent(MainActivity.this, RegistrationIntentService.class);
                    startService(newTokenIntent);
                } else {
                    sendRegistrationToServer(token);
                }
            }
        };

    }

    public void getGcmToken() {
        if (checkGooglePlayServices()) {
            Log.i("MainActivity", "checking play");
            //check if the device already sent a token to the app server
            String token = sharedPreferences.getString(MySharedPreferences.FASK_GCM_TOKEN, "");

            //if not, register a new token.. and send to server
            if(token.isEmpty()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            } else {
                sendRegistrationToServer(token);
            }
        }
    }

    /**
     * Associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     */
    private void sendRegistrationToServer(String token) {
        Log.i(CLASS_TAG, "sendRegistrationToServer");
        Log.i(CLASS_TAG, "GCM Token: " + token);

        JSONObject data = new JSONObject();

        try {
            data.put("UserID", Session.getInstance().getUser().getuID());
            data.put("RegistrationKey", token);
            // Add custom implementation, as needed.
            HttpConnect.requestJsonPost("/gcm", data.toString(), new HttpConnect.HttpResult<JSONObject>() {
                @Override
                public void onCallbackSuccess(JSONObject response) {
                    try {
                        int error = response.getInt("Error");
                        if (error == 0) {
                            Log.i(CLASS_TAG, "Sent GCM Registration token to server: " + response.getString("Data"));
                            // You should store a boolean that indicates whether the generated token has been
                            // sent to your server. If the boolean is false, send the token to your server,
                            // otherwise your server should have already received the token.
                            sharedPreferences.edit().putBoolean(MySharedPreferences.SENT_TOKEN_TO_SERVER, true).apply();
                        } else {
                            DisplayAlert.displayAlertError(MainActivity.getInstance(), response.getString("ErrorMessage"), null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCallbackError(String errorMsg, boolean fromServer) {
                    Log.e(CLASS_TAG, "Error sending GCM id to server: " + errorMsg);
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void getGPSLocation(final LocationCallback callback) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        Location location = null;
        for(int i = providers.size()-1; i >=0; i--) {
            location = locationManager.getLastKnownLocation(providers.get(i));
            if(location != null) {
                break;
            }
        }
        callback.onReceiveLocation(location);
//        Criteria criteria = new Criteria();

//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        locationManager.requestSingleUpdate(criteria, new MyLocationListener(callback), null);

//        if(googleApiClient == null) {
//            googleApiClient = new GoogleApiClient.Builder(this)
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                        @Override
//                        public void onConnected(Bundle bundle) {
//                            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//                            callback.onReceiveLocation(location);
//                        }
//
//                        @Override
//                        public void onConnectionSuspended(int i) {
//
//                        }
//                    }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                        @Override
//                        public void onConnectionFailed(ConnectionResult connectionResult) {
//
//                        }
//                    }).build();
//        }
//
//        if(!googleApiClient.isConnected()) {
//            googleApiClient.connect();
//        }

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    /*allowing for global spinner control from fragments*/
    public void stopSpinner() {
        globalSpinnerController.stopSpinner();
    }
    public void startSpinner() {
        globalSpinnerController.startSpinner();
    }

    public static MainActivity getInstance() {
        return activity;
    }

}


//