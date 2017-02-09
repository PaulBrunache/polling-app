package faskteam.faskandroid.contollers.main_controllers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import faskteam.faskandroid.entities.ChoiceType;
import faskteam.faskandroid.R;
import faskteam.faskandroid.entities.Session;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.SlidingTabLayout;
import faskteam.faskandroid.utilities.progress_spinner.ProgressSpinnerController;


public class PollCreateActivity extends ActionBarActivity {

    //All Material Items
    private ImageView mPollCreateAccept;
    private ImageView mPollCreateCancel;
    private Toolbar mToolbar;
    private SlidingTabLayout mTabs;
    private ViewPager mViewPager;
    private PollCreateAdapter mAdapter;
    private PollCreateGroupFragment mGroupFragment;
    private PollCreatePollFragment mPollFragment;
    private PollCreateLocationFragment mLocationFragment;

    private ProgressSpinnerController spinnerController;

    //Information to build requests
    private int groupSelectedPos;
    private String question;
    private boolean openToOthers = false;
    private ChoiceType choiceType = ChoiceType.RADIO;
    private Calendar closeTime;
    private String closeTimeStr;
    private int uID = -1;
    private String sessionID = Session.getInstance().getSessionID();
    private int groupId;
    private boolean publicPoll;
    private double latitude;
    private double longitude;
    private List<String> mGroups = new ArrayList<>();
    private List<Integer> mGroupIds = new ArrayList<>();
    private List<String> mChoices = new ArrayList<>();
    private List<String> mImageUrls = new ArrayList<>();
    private int numUploaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_create);

        uID = getIntent().getIntExtra("uID", -1);
        Log.i("USERID", ""+uID);

        //Reference to Toolbar
        mToolbar = (Toolbar) findViewById(R.id.poll_create_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);
        //getSupportActionBar().setTitle("Poll Creation");
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        spinnerController = new ProgressSpinnerController(this, (ViewGroup) findViewById(R.id.poll_create_container));

        //Reference to Tabs with ViewPager and PagerAdapter
        mViewPager = (ViewPager) findViewById(R.id.poll_create_viewpager);
        mAdapter = new PollCreateAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabs = (SlidingTabLayout) findViewById(R.id.poll_create_tabs);
        mTabs.setSelectedIndicatorColors(R.color.color_accent);
        mTabs.setDistributeEvenly(true);
        mTabs.setViewPager(mViewPager);
        mTabs.setOnPageChangeListener(mTabPageChangeListener());

        //Reference to Cancel and Accept Buttons on Toolbar (Not really a part of Toolbar tho)
        //Should find another way to add this
        mPollCreateAccept = (ImageView) findViewById(R.id.poll_create_accept);
        mPollCreateAccept.setOnClickListener(pollCreateAccept());

        mPollCreateCancel = (ImageView) findViewById(R.id.poll_create_cancel);
        mPollCreateCancel.setOnClickListener(pollCreateCancel());

        try {
            getGroupsForUser(this.uID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class PollCreateAdapter extends FragmentPagerAdapter {
        String[] tabs;

        public PollCreateAdapter(FragmentManager fm) {
            super(fm);
            tabs = getResources().getStringArray(R.array.poll_create_tabs);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return mPollFragment = PollCreatePollFragment.getInstance();
                case 1:
                    return mGroupFragment = PollCreateGroupFragment.getInstance();
                case 2:
                    return mLocationFragment = PollCreateLocationFragment.getInstance();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        @Override
        public int getCount() {
            //Change to three when Location Fragment added
            return 3;
        }
    }

    public ViewPager.OnPageChangeListener mTabPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                if(lastPage > position && position == 1){
//                lastPage = position;
                if (position == 0 && mPollFragment.isChoiceEmpty()) {
                    Log.e("CHOICE", mChoices.toString());
                    for(int i = 1; i < mChoices.size(); i++) {
                        mPollFragment.reAddChoices(mChoices.get(i));
                    }
                }
                if (position == 1) {
//                    if (mChoices.size() != 0) {
//                        mChoices.clear();
//                    }
                    mChoices = mPollFragment.getChoices();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
    }

    private View.OnClickListener pollCreateAccept() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create Poll object then send Poll object to Web services.
                //Information from Poll Tab
                question = mPollFragment.getQuestion();
                //mChoices = mPollFragment.getChoices();
                choiceType = mPollFragment.getChoiceType();

                //Information from Group Tab
                groupSelectedPos = mGroupFragment.getGroupSelectedPos();
                openToOthers = mGroupFragment.isOpenToOthers();
                closeTime = mGroupFragment.getmCloseTime();

                Date closeDate = closeTime.getTime();
                SimpleDateFormat strFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                closeTimeStr = strFormat.format(closeDate);

                groupId = mGroupIds.get(groupSelectedPos);
                publicPoll = mGroupFragment.isPublicPrivate();

                //Information from Location Tab
                longitude = mLocationFragment.getLongitude();
                latitude = mLocationFragment.getLatitude();

                try {
                    if(choiceType == ChoiceType.MULTIMEDIA) {
                        List<String> imagePaths = mPollFragment.getImagePaths();
                        for(int i = 0; i < imagePaths.size(); i++) {
                            Toast.makeText(getApplicationContext(), "Uploading image "+(i+1)+" of "+imagePaths.size(),
                                    Toast.LENGTH_SHORT).show();
                            uploadPhotos(imagePaths.get(i), imagePaths.size());
                        }
                    }
                    else {
                        createPollWS();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Return intent with bundled extras
            }
        };
    }

    //Creates Dialog that ensures cancel/back navigation
    private void cancelDialogBuilder() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PollCreateActivity.this);
        alertDialogBuilder.setTitle("Leave before finishing?");
        alertDialogBuilder.setMessage("All data will not be saved and you'll have to start again!");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent();
                setResult(Activity.RESULT_CANCELED, i);
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Calls cancelDialogBuilder method when back button pressed
            cancelDialogBuilder();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private View.OnClickListener pollCreateCancel() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Calls cancelDialogBuilder method when cancel button pressed
                cancelDialogBuilder();
            }
        };
    }

    private void uploadPhotos(String imagePath, int totalFiles){
        spinnerController.startSpinner();

        final int total = totalFiles;
        File imageFile = new File(imagePath);

        final String PICTURE_URL = "http://www.peacockpi.us:81/cgi-bin/FAsk/fask_rest/upload_image.py";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("File", imageFile);
            params.put("UserID", uID);
            params.put("Session", sessionID);

            client.post(PICTURE_URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.i("PicUploadResp", response.toString());
                    try {
                        String url = response.getString("Data");
                        mImageUrls.add(url);
                        numUploaded++;
                        if(numUploaded == total){
                            createPollWS();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    spinnerController.stopSpinner();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.i("PicUploadErr", errorResponse.toString());
                    Toast.makeText(getApplicationContext(), "Error uploading your images. Try again.", Toast.LENGTH_LONG)
                    .show();
                    spinnerController.stopSpinner();
                }
            });
        } catch(FileNotFoundException e){
            Toast.makeText(this, "File Not Found", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void createPollWS() {
        spinnerController.startSpinner();

        JSONObject createPollRequest = new JSONObject();
        JSONObject pollData = new JSONObject();
        JSONArray choiceList = new JSONArray();
        //List<JSONObject> choiceList = new ArrayList<>();


        Log.i("Choice", mChoices.toString());
        try {
            pollData.put("Type", choiceType);
            pollData.put("Question", question);
            pollData.put("Open", 1);
            pollData.put("UserID", uID);
            pollData.put("CloseDate", closeTimeStr);
            pollData.put("IsPublic", publicPoll ? 1 : 0);
            pollData.put("Longitude", longitude);
            pollData.put("Latitude", latitude);
            pollData.put("OpenChoice", openToOthers ? 1 : 0);

            Log.i("ImageUrls", mImageUrls.toString());
            if(choiceType == ChoiceType.MULTIMEDIA){
                for(int i = 0; i < mImageUrls.size(); i++) {
                    JSONObject choiceUrlJSON = new JSONObject();
                    choiceUrlJSON.put("Text", mImageUrls.get(i));
                    Log.i("Url", choiceUrlJSON.toString());
                    choiceList.put(choiceUrlJSON);
                }
            }
            else {
                Log.i("ChoiceSize", "" + mChoices.size());
                for (int i = 0; i < mChoices.size(); i++) {
                    JSONObject choiceJSON = new JSONObject();
                    choiceJSON.put("Text", mChoices.get(i));
                    choiceList.put(choiceJSON);
                }
            }
            //ChoiceList is a string should be list
            createPollRequest.put("poll", pollData);
            createPollRequest.put("choices", choiceList);
//            createPollRequest.put("group", mGroupIds.get(groupSelectedPos));
            createPollRequest.put("group", groupId == -1 ? -1 : groupId);
            //Leaving this at -1 until I figure how this should done
            createPollRequest.put("friends", groupId == -1 ? 1 : -1);

            Log.i("PollRequest", createPollRequest.toString());

            HttpConnect.requestJsonPost("/poll_create_activity", createPollRequest.toString(), new HttpConnect.HttpResult<JSONObject>() {
                @Override
                public void onCallbackSuccess(JSONObject response) {
                    spinnerController.stopSpinner();
                    Intent i = new Intent();
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }

                @Override
                public void onCallbackError(String errorMsg, boolean fromServer) {
                    if (fromServer) {
                        DisplayAlert.displayAlertError(PollCreateActivity.this, errorMsg, null);
                    } else {
                        DisplayAlert.displayAlertError(PollCreateActivity.this, getString(R.string.general_error_msg), null);
                    }
                    Log.i("PollAccept", "There was an error when clicking Poll accept button");
                    spinnerController.stopSpinner();
                    Intent i = new Intent();
                    setResult(Activity.RESULT_CANCELED, i);
                    finish();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getGroupsForUser(int userID) {
        HttpConnect.requestJsonGet(String.format("/user/%d/groups", userID), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    Log.i("RESP", "" + data);
                    mGroups.add(0, "Friends");
                    mGroupIds.add(0, -1);
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject group = data.getJSONObject(i);
                        mGroups.add(i+1, group.getString("GroupName"));
                        mGroupIds.add(i+1, group.getInt("GroupID"));
                    }
                    mGroupFragment.setGroups(mGroups);
                    Log.i("DREW", mGroups.toString());
                    Log.i("DREW", mGroupIds.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                if (fromServer) {
                    DisplayAlert.displayAlertError(PollCreateActivity.this, errorMsg, null);
                } else {
                    DisplayAlert.displayAlertError(PollCreateActivity.this, getString(R.string.general_error_msg), null);
                }
            }
        });
    }

    //Fragment that represents Poll Info Tab
    public static class PollCreatePollFragment extends Fragment {
        private Button mPollCreateAddChoice;
        private Button mPollCreateAddPhoto;
        private LinearLayout mPollChoiceLayout;
        private EditText mPollCreateQuestion;
        private RadioGroup mChoiceTypeRadioGroup;
        private List<String> imagePaths = new ArrayList<>();
        private String question;
        private ChoiceType choiceType = ChoiceType.RADIO;
        private List<String> choices = new ArrayList<>();

        public static PollCreatePollFragment getInstance() {
            return new PollCreatePollFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.fragment_poll_create_poll, container, false);

            //Reference to Poll Question
            mPollCreateQuestion = (EditText) layout.findViewById(R.id.poll_question);

            //LinearLayout that holds the edittexts for Choices
            mPollChoiceLayout = (LinearLayout) layout.findViewById(R.id.poll_choice_view);

            //Reference to Choice Type Radio Group
            mChoiceTypeRadioGroup = (RadioGroup) layout.findViewById(R.id.choice_type_radio_group);
            mChoiceTypeRadioGroup.setOnCheckedChangeListener(radioGroupListener());

            //Button that adds additional Choices
            mPollCreateAddChoice = (Button) layout.findViewById(R.id.add_more_choices);
            mPollCreateAddChoice.setOnClickListener(addMoreChoices());

            //Button that adds Photos to vote on
            mPollCreateAddPhoto = (Button) layout.findViewById(R.id.add_picture);
            mPollCreateAddPhoto.setOnClickListener(addPhoto());

            return layout;
        }

        //Radio Group that checks for choice type
        private RadioGroup.OnCheckedChangeListener radioGroupListener() {
            return new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.choice_type_radio:
                            choiceType = ChoiceType.RADIO;
                            break;
                        case R.id.choice_type_check:
                            choiceType = ChoiceType.CHECKLIST;
                            break;
                        case R.id.choice_type_rate:
                            choiceType = ChoiceType.RATE;
                            break;
                        case R.id.choice_type_multimedia:
                            choiceType = ChoiceType.MULTIMEDIA;
                            break;
                    }
                }
            };
        }

        private View.OnClickListener addPhoto(){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadMultimediaChoices();
                    mChoiceTypeRadioGroup.clearCheck();
                    mChoiceTypeRadioGroup.check(R.id.choice_type_multimedia);
                }
            };
        }

        private void uploadMultimediaChoices(){
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(photoPickerIntent, "Complete action using"), 1);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == 1 && resultCode == RESULT_OK){
                Uri selectedPhotoUri = data.getData();
                String imagePath = FileUtils.getPath(getContext(), selectedPhotoUri);

                if (imagePath != null && FileUtils.isLocal(imagePath)) {
                    imagePaths.add(imagePath);
                    mPollChoiceLayout.addView(addImagePath(imagePath));
                }
            }
        }

        //Populates the Choice array
        private void checkForChoice() {
            //Setting Poll Choices
            int numChoices = mPollChoiceLayout.getChildCount();
            for (int i = 0; i < numChoices; i++) {
                View view = mPollChoiceLayout.getChildAt(i);
                if (view instanceof EditText) {
                    if (((EditText) view).getText().length() != 0) {
                        choices.add(((EditText) view).getText().toString());
                    }
                }
            }
        }

        //Button that creates more choices
        private View.OnClickListener addMoreChoices() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPollChoiceLayout.addView(createNewEditView());
                }
            };
        }

        private TextView addImagePath(String imagePath){
            int numItems = mPollChoiceLayout.getChildCount();
            for(int i = 0; i < numItems; i++){
                View view = mPollChoiceLayout.getChildAt(i);
                if (view instanceof EditText){
                    mPollChoiceLayout.removeView(view);
                }
            }

            int margins = (int) getResources().getDimension(R.dimen.poll_create_edit_text_margin);
            final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lparams.setMargins(margins, margins / 2, margins, margins / 2);
            final TextView textView = new TextView(getActivity());
            textView.setLayoutParams(lparams);
            textView.setText(imagePath);
            return textView;
        }

        //Creating editTExt to fit the LinearLayout
        private EditText createNewEditView() {
            int margins = (int) getResources().getDimension(R.dimen.poll_create_edit_text_margin);
            final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lparams.setMargins(margins, margins / 2, margins, margins / 2);
            final EditText editText = new EditText(getActivity());
            editText.setLayoutParams(lparams);
            editText.setHint("Enter Choice Here.");
            return editText;
        }

        public void reAddChoices(String choice) {
            int margins = (int) getResources().getDimension(R.dimen.poll_create_edit_text_margin);
            final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lparams.setMargins(margins, margins / 2, margins, margins / 2);
            final EditText editText = new EditText(getActivity());
            editText.setLayoutParams(lparams);
            editText.setHint("Enter Choice Here.");
            editText.setText(choice);
            mPollChoiceLayout.addView(editText);
        }

        public boolean isChoiceEmpty() {
            boolean isEmpty = false;
            int numChoices = mPollChoiceLayout.getChildCount();
            if (numChoices == 1) {
                isEmpty = true;
            }
            return isEmpty;
        }

        public String getQuestion() {
            question = mPollCreateQuestion.getText().toString();
            return question;
        }

        public ChoiceType getChoiceType() {
            return choiceType;
        }

        public List<String> getChoices() {
            checkForChoice();
            return choices;
        }

        public List<String> getImagePaths() { return imagePaths; }
    }

    //The Fragment that represents the Group Info Tab
    public static class PollCreateGroupFragment extends Fragment {
        private Button mDatePickerBtn;
        private ListView mGroupChooseListView;
        private CheckBox mPublicPrivate;
        private CheckBox mOpenToOthers;
        private Calendar mTimeNow;
        private Calendar mCloseTime;
        private String closeTimeStr;
        private List<String> mGroups = new ArrayList<>();
        private String groupSelected = "";
        private boolean openToOthers = false;
        private boolean publicPrivate = false;
        private int groupSelectedPos = -1;
        private ArrayAdapter<String> arrayAdapter;

        public static PollCreateGroupFragment getInstance()         {
            return new PollCreateGroupFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //The Group Info Tab Fragment
            View layout = inflater.inflate(R.layout.fragment_poll_create_group, container, false);

            mTimeNow = Calendar.getInstance();

            mCloseTime = Calendar.getInstance();
            mCloseTime.set(Calendar.HOUR_OF_DAY, mTimeNow.get(Calendar.HOUR_OF_DAY)+1);

            //The Group List View
            mGroupChooseListView = (ListView) layout.findViewById(R.id.group_choose_list_view);
            mGroupChooseListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mGroupChooseListView.setOnItemClickListener(listViewListener());

            //The Checkbox for public or private access
            mPublicPrivate = (CheckBox) layout.findViewById(R.id.public_private);
            mPublicPrivate.setOnClickListener(publicPrivateListener());

            //The Checkbox to allow others to add choices
            mOpenToOthers = (CheckBox) layout.findViewById(R.id.open_to_others);
            mOpenToOthers.setOnClickListener(openToOthersListener());

            //The Button the brings up the Date Picker Dialog
            mDatePickerBtn = (Button) layout.findViewById(R.id.poll_create_date_picker);
            mDatePickerBtn.setOnClickListener(datePickerListener());

            return layout;
        }

        //Group Selection ListView
        private AdapterView.OnItemClickListener listViewListener() {
            return new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    groupSelected = (String) parent.getItemAtPosition(position);
                    mGroupChooseListView.setItemChecked(position, true);
                    groupSelectedPos = position;
                }
            };
        }

        //Checkbox to allow public versus private Polling
        private View.OnClickListener publicPrivateListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPublicPrivate.isChecked()) {
                        publicPrivate = true;
                    } else {
                        publicPrivate = false;
                    }
                }
            };
        }

        //Checkbox to allow others to add Choices
        private View.OnClickListener openToOthersListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOpenToOthers.isChecked()) {
                        openToOthers = true;
                    } else {
                        openToOthers = false;
                    }
                }
            };
        }

        //Date Picker Dialog Button in Group Fragment
        private View.OnClickListener datePickerListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDateTimePicker(v);
                }
            };
        }

        private void showDateTimePicker(View v) {
            LayoutInflater inflater = LayoutInflater.from(v.getContext());
            View dateTimePicker = inflater.inflate(R.layout.date_time_picker_dialog, null);

            final DatePicker dp = (DatePicker) dateTimePicker.findViewById(R.id.date_picker);
            final TimePicker tp = (TimePicker) dateTimePicker.findViewById(R.id.time_picker);

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setView(dateTimePicker);
            builder.setTitle("Select End Date and Time");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mCloseTime.set(Calendar.YEAR, dp.getYear());
                    mCloseTime.set(Calendar.MONTH, dp.getMonth());
                    mCloseTime.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
                    mCloseTime.set(Calendar.HOUR_OF_DAY, tp.getCurrentHour());
                    mCloseTime.set(Calendar.MINUTE, tp.getCurrentMinute());
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

        public void setGroups(List<String> groups) {
            mGroups = groups;
            arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice, mGroups);
            mGroupChooseListView.setAdapter(arrayAdapter);
            mGroupChooseListView.setItemChecked(0, true);
            groupSelectedPos = 0;
        }

        public Calendar getmCloseTime() { return mCloseTime; }

        public boolean isOpenToOthers() {
            return openToOthers;
        }

        public boolean isPublicPrivate() { return publicPrivate; }

        public int getGroupSelectedPos() { return groupSelectedPos; }
    }

    public static class PollCreateLocationFragment extends Fragment
            implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener{

        private FragmentActivity mContext;
        private SearchView mSearchLocation;
        private GoogleApiClient mGoogleApiClient;
        private Geocoder mGeocoder;
        private MapView mMapView;
        private GoogleMap googleMap;
        private Location mLocation;
        private LatLng mLatLng;
        private List<Address> mAddress = null;
        private double longitude;
        private double latitude;

        public static PollCreateLocationFragment getInstance() {
            return new PollCreateLocationFragment();
        }

        @Override
        public void onAttach(Context context) {
            mContext = (FragmentActivity) context;
            super.onAttach(context);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.fragment_poll_create_location, container, false);

            mSearchLocation = (SearchView) layout.findViewById(R.id.search_location);
            mSearchLocation.setOnQueryTextListener(searchLocationListener());

            if(checkGoogleServices()){
                buildGoogleApiClient();
            }

            mGeocoder = new Geocoder(mContext, Locale.getDefault());

            mMapView = (MapView) layout.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);

            googleMap = mMapView.getMap();
            googleMap.setMyLocationEnabled(true);

            try {
                MapsInitializer.initialize(this.getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return layout;
        }

        @Override
        public void onStart() {
            super.onStart();
            if(mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }

        @Override
        public void onResume() {
            checkGoogleServices();
            mMapView.onResume();
            super.onResume();
        }

        @Override
        public void onDestroy() {
            mMapView.onDestroy();
            super.onDestroy();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {

        }

        @Override
        public void onMapReady(GoogleMap googleMap) {

        }

        protected synchronized void buildGoogleApiClient(){
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }

        public void onConnectionFailed(ConnectionResult result) {
            Log.i("Location", "Connection failed: Error Code = " + result.getErrorCode());
        }

        public void onConnected(Bundle bundle){
            displayCurrentLocation();
        }

        public void onConnectionSuspended(int val){
            mGoogleApiClient.connect();
        }


        @Override
        public void onStop() {
            super.onStop();
            if(mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
            }
        }

        private synchronized void displayCurrentLocation(){
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if(mLocation != null){
                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();
                Log.i("CurrentLoc", "Current Location is: " + latitude + " " + longitude);
                mLatLng = new LatLng(latitude, longitude);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
            }

        }

        private boolean checkGoogleServices(){
            int returnVal = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
            if(returnVal != ConnectionResult.SUCCESS){
                if(GooglePlayServicesUtil.isUserRecoverableError(returnVal)){
                    GooglePlayServicesUtil.getErrorDialog(returnVal, mContext, 1000).show();
                }
                else {
                    Toast.makeText(getContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
                }
                return false;
            }
            return true;
        }

        public SearchView.OnQueryTextListener searchLocationListener(){
            return new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if(mGoogleApiClient != null){
                        try {
                            mAddress = mGeocoder.getFromLocationName(query, 5);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if(mAddress != null){
                                if(mAddress.size() > 0){
                                    mLatLng = new LatLng(mAddress.get(0).getLatitude(), mAddress.get(0).getLongitude());
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15));
                                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);

                                    longitude = mLatLng.longitude;
                                    latitude = mLatLng.latitude;
                                    System.out.print("MAPSEARCH" + "New long lat is: " + longitude + ", " + latitude);
                                }
                            }
                        }
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            };
        }

        public double getLongitude(){ return longitude; }

        public double getLatitude(){ return latitude; }
    }
}
