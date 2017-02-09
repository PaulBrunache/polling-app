package faskteam.faskandroid.contollers.main_controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import faskteam.faskandroid.R;
import faskteam.faskandroid.entities.Choice;
import faskteam.faskandroid.entities.ChoiceType;
import faskteam.faskandroid.entities.Poll;
import faskteam.faskandroid.entities.Session;
import faskteam.faskandroid.utilities.CommonFunctions;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.progress_spinner.ProgressSpinnerController;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;
import faskteam.faskandroid.utilities.recyclerview.SimpleItemTouchHelperCallback;

public class AnswerPollActivity extends AppCompatActivity implements View.OnClickListener, ImageClickListener {

    private static final String CLASS_TAG = AnswerPollActivity.class.getSimpleName();

    private ProgressSpinnerController spinnerController;

    private HorizontalBarChart chart;
    private View answerButton;
    private View closeButton;
    private TextView pollCreator;
    private TextView pollQuestion;
    private TextView pollTimeRemaining;
    private TextView pollBody;
    private TextView pollVotes;

    private FragmentManager manager;

    private Poll poll = null;

    private int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_poll);
        Toolbar toolbar = (Toolbar) findViewById(R.id.answer_poll_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        manager = getSupportFragmentManager();

        spinnerController = new ProgressSpinnerController(this, (ViewGroup) findViewById(R.id.activity_answer_poll_container));
        chart = (HorizontalBarChart) findViewById(R.id.chart);
        answerButton = findViewById(R.id.poll_button_answer);
        closeButton = findViewById(R.id.poll_button_close);
        pollCreator = (TextView) findViewById(R.id.poll_creator);
        pollQuestion = (TextView) findViewById(R.id.poll_question);
        pollTimeRemaining = (TextView) findViewById(R.id.poll_time_remaining);
        pollBody = (TextView) findViewById(R.id.poll_body);
        pollVotes = (TextView) findViewById(R.id.poll_votes);

        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        answerButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);

        recreatePoll();
        fetchPollChoices();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (manager.getBackStackEntryCount() > 0) {
                    manager.popBackStack();
                } else {
                    finish();
                }
//                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void recreatePoll() {
        //we need to get the data for this poll from the previous fragment
        //this data was passed in as arguments
        Bundle pollData = getIntent().getExtras();

        try {

            String pollQuestion = pollData.getString("Question");
            int pollId = pollData.getInt("PollID");
            String type = pollData.getString("Type");
            boolean openChoices = pollData.getBoolean("openChoices");
            int creatorID = pollData.getInt("UserID");
            boolean isOpen = pollData.getBoolean("isOpen");
            String creatorUsername = pollData.getString("creatorUsername", "");
//            String closeDate = pollData.getString("closeDate", "");
            Date closeDate = (Date) pollData.getSerializable("closeDate");
            Location location = pollData.getParcelable("location");

            poll = new Poll(pollId, pollQuestion, new ArrayList<Choice>(), ChoiceType.checkType(type), openChoices, creatorID, isOpen);
            poll.setCreatorUsername(creatorUsername);
//            poll.setCloseDate(CommonFunctions.stringToDate(closeDate));
            poll.setCloseDate(closeDate);
            poll.setPollLocation(location);

            setPollInformation();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(CLASS_TAG, "error:" + e.getMessage());
        }

    }

    private void setPollInformation() {
        pollCreator.setText(poll.getCreatorUsername());
        pollQuestion.setText(poll.getQuestion());
        pollTimeRemaining.setText(poll.getTimeRemaining());

        //check if the user is looking at their own poll
        if (poll.getCreatorId() == Session.getInstance().getUser().getuID()) {
            //if so, show the close poll button
            closeButton.setVisibility(View.VISIBLE);
            answerButton.setVisibility(View.GONE);
        }
    }

    private void fetchPollChoices() {
        spinnerController.startSpinner();

        chart.setData(new BarData());

        HttpConnect.requestJsonGet(String.format("/choice/%d/choices_and_votes", poll.getId()), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);

                    //we should be receiving an array of json dictionaries containing poll info/data
                    //so we'll iterate through each JSONObject
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject choiceJson = data.getJSONObject(i);
                        Choice choice = new Choice(choiceJson.getString("Text"), choiceJson.getInt("ChoiceID"));
                        JSONArray votes = choiceJson.getJSONArray("Votes");

                        //each vote has a weight.. for non rate poll types, this weight is equal to 1, which is
                        //the same as incrementing the vote count... For rate vote types, this weight can be
                        //N, where N is of type integer.
                        int weightTotal = 0;
                        for(int j = 0; j < votes.length(); j++) {
                            JSONObject voteJson = votes.getJSONObject(j);
                            int weight = voteJson.optInt("Weight", 1);
                            weightTotal += weight;
                        }

                        //update the number users who selected this choice
                        choice.setNumberSelected(weightTotal);

                        //fill chart data
                        String choiceTitle;
                        if (poll.getChoiceType().equals(ChoiceType.MULTIMEDIA)) {
                            choiceTitle = "Image " + (i + 1);
                        } else {
                            choiceTitle = choice.getChoice();
                        }

                        List<BarEntry> entries = new ArrayList<>();
                        entries.add(new BarEntry(choice.getNumberSelected(), i));

                        BarDataSet dataSet = new BarDataSet(entries, choiceTitle);

                        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);

                        chart.getBarData().addDataSet(dataSet);
                        chart.getBarData().addXValue(choiceTitle);

                        //tell chart that we have changed the data set
                        chart.notifyDataSetChanged();
                        invalidateChart();

                        updateVoteCount(votes.length());

                        poll.setChoices(choice);
                    }
                    Log.i(CLASS_TAG, "poll choice count: " + poll.getChoices().size());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                spinnerController.stopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                DisplayAlert.displayAlertError(AnswerPollActivity.this, getString(R.string.general_error_msg), null);
                spinnerController.stopSpinner();
            }
        });

    }

    private synchronized void invalidateChart() {
        chart.invalidate();

    }

    private void openRadioAnswerSheet()
    {
        final int[] selectedChoiceIndex = new int[1];

        String[] choicesArray = new String[poll.getChoices().size()];
        for (int i = 0; i < choicesArray.length; i++) {
            choicesArray[i] = poll.getChoices().get(i).getChoice();
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Poll Answer");
        builder.setSingleChoiceItems(choicesArray, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedChoiceIndex[0] = which;
                    }
                })
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Choice selectedChoice = poll.getChoices().get(selectedChoiceIndex[0]);
                        saveSelectedChoiceVote(selectedChoice);
                    }
                }).setNegativeButton("Cancel", null);

        if (poll.isOpen()) {
            builder.setNeutralButton("Add Answer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openAddChoiceDialog(dialog);

                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openAddChoiceDialog(final DialogInterface previousDialog) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText dialogLayout = new EditText(this);

        builder.setTitle("Add Poll Answer");
        builder.setMessage("Enter a new answer choice into text field. Click submit to submit answer, or click cancel to return to answer choices.");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String newChoice = dialogLayout.getText().toString();
                saveNewChoice(newChoice);

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((AlertDialog) previousDialog).show();
            }
        });

        //first create a framelayout
        FrameLayout frameLayout = new FrameLayout(this);
        builder.setView(frameLayout);

        AlertDialog dialog = builder.create();
        dialogLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        frameLayout.addView(dialogLayout);

        dialog.show();

    }

    private void openChecklistAnswerSheet() {

        String[] choicesArray = new String[poll.getChoices().size()];
        final boolean[] checkedItems = new boolean[choicesArray.length];

        for (int i = 0; i < choicesArray.length; i++) {
            choicesArray[i] = poll.getChoices().get(i).getChoice();
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Poll Answer");
        builder.setMultiChoiceItems(choicesArray, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveMultiChoiceVotes(checkedItems);
                    }
                }).setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void openRateAnswerSheet() {
        final RateListAdapter adapter = new RateListAdapter(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Poll Answer");
        builder.setMessage("Order rank from highest(top) to lowest(bottom).");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //rank goes from best to worse in the order of higher number to lowest number
                List<Choice> rankedChoices = adapter.getDataList();

                saveRateChoiceVotes(rankedChoices);

            }
        }).setNegativeButton("Cancel", null);

        //to add the recycler view we need to get the body of the dialog view and add dialog to it
        //first create a framelayout
        FrameLayout frameLayout = new FrameLayout(this);
        builder.setView(frameLayout);

        AlertDialog dialog = builder.create();
        LayoutInflater inflater = dialog.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.custom_poll_answer_sheet, frameLayout);

        //we need a list view in the dialog
        RecyclerView rateList = (RecyclerView) dialogLayout.findViewById(R.id.custom_poll_choice_list);
        rateList.setLayoutManager(new LinearLayoutManager(this));
        rateList.setHasFixedSize(true);

        //create adapter for rate list
        adapter.replaceDataList(poll.getChoices());
        //set rate list adapter to our adapter
        rateList.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rateList);
        dialog.show();

    }

    private void openMultimediaAnswerSheet() {
        final MultimediaListAdapter adapter = new MultimediaListAdapter(this);
        final View modalLayout = findViewById(R.id.activity_answer_poll_modal);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Poll Answer");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Choice selectedChoice = adapter.getItem(adapter.getCheckedIndex());
                saveSelectedChoiceVote(selectedChoice);
            }
        }).setNegativeButton("Cancel", null);

        //to add the recycler view we need to get the body of the dialog view and add dialog to it
        //first create a framelayout
        FrameLayout frameLayout = new FrameLayout(this);
        builder.setView(frameLayout);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = dialog.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.custom_poll_answer_sheet, frameLayout);

        //we need a list view in the dialog
        RecyclerView imageList = (RecyclerView) dialogLayout.findViewById(R.id.custom_poll_choice_list);
        imageList.setLayoutManager(new LinearLayoutManager(this));
        imageList.setHasFixedSize(true);

        //create adapter for rate list
        adapter.replaceDataList(poll.getChoices());
        adapter.setImageClickListener(new ImageClickListener() {
            @Override
            public void onImageClicked(Bitmap image) {
                Log.i(CLASS_TAG, "onImageClicked");
                dialog.dismiss();

                //get modal frame
                modalLayout.setVisibility(View.VISIBLE);

                ImageViewerFragment fragment = new ImageViewerFragment();
                Bundle b = new Bundle();
                b.putParcelable("image", image);
                fragment.setArguments(b);

                manager.beginTransaction().add(R.id.activity_answer_poll_modal, fragment).addToBackStack(null).commit();
                manager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if(manager.getBackStackEntryCount() == 0) {
                            dialog.show();
                            modalLayout.setVisibility(View.GONE);
                            manager.removeOnBackStackChangedListener(this);
                        }
                        Log.i(CLASS_TAG, "backstack count: " + manager.getBackStackEntryCount());
                    }
                });
            }
        });
        //set rate list adapter to our adapter
        imageList.setAdapter(adapter);

        dialog.show();
    }

    private void saveNewChoice(String newChoice) {
        spinnerController.startSpinner();
        /*
            url: /add_choice
            http request body: {PollID: poll_id, Text: text, UserID: user_id}
        */

        JSONObject requestData = new JSONObject();

        int uID = Session.getInstance().getUser().getuID();

        try {
            requestData.put("UserID", uID);
            requestData.put("PollID", poll.getId());
            requestData.put("Text", newChoice);
        } catch (Exception e) {

        }

        HttpConnect.requestJsonPost("/add_choice", requestData.toString(), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                Toast.makeText(AnswerPollActivity.this, "New answer choice submitted successfully.", Toast.LENGTH_LONG).show();
                spinnerController.stopSpinner();
                finish();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                DisplayAlert.displayAlertError(AnswerPollActivity.this, errorMsg, null);
                spinnerController.stopSpinner();
            }
        });

    }

    private void saveSelectedChoiceVote(Choice selectedChoice) {
        spinnerController.startSpinner();

        //we need to send the choice id and the user id to votes table
        int uID = Session.getInstance().getUser().getuID();
        JSONObject voteJson = new JSONObject();
        try {
            voteJson.put("UserID", uID);
            voteJson.put("ChoiceID", selectedChoice.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpConnect.requestJsonPost("/vote", voteJson.toString(), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                Toast.makeText(AnswerPollActivity.this, "Vote Submitted successfully", Toast.LENGTH_LONG).show();
                spinnerController.stopSpinner();
                finish();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                DisplayAlert.displayAlertError(AnswerPollActivity.this, errorMsg, null);
                spinnerController.stopSpinner();
            }


        });
    }

    private void saveRateChoiceVotes(List<Choice> rankedChoices) {
        spinnerController.startSpinner();

        int uID = Session.getInstance().getUser().getuID();
        JSONObject votesRequestData = new JSONObject();

        try {
            votesRequestData.put("UserID", uID);

            //create json array for each choice user selected
            JSONArray voteItems = new JSONArray();
            JSONObject voteItem;

            int size = rankedChoices.size();
            for(int i = 0; i < size; i++) {
                //create new json item for vote choice
                voteItem = new JSONObject();
                Choice choice = rankedChoices.get(i);

                voteItem.put("ChoiceID", choice.getId());
                //weight here shouldn't matter
                voteItem.put("Weight", size-i);

                //add vote item to json array
                voteItems.put(voteItem);
            }

            //add array to request data
            votesRequestData.put("Votes", voteItems);
        } catch (Exception e) {
            Log.e(CLASS_TAG, "failed to send ranked vote to server: " + e.getMessage());
        }

        HttpConnect.requestJsonPost("/weighted_votes", votesRequestData.toString(), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                Toast.makeText(AnswerPollActivity.this, "Vote Submitted successfully", Toast.LENGTH_LONG).show();
                spinnerController.stopSpinner();
                finish();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                DisplayAlert.displayAlertError(AnswerPollActivity.this, errorMsg, null);
                spinnerController.stopSpinner();
            }


        });
    }

    //for check box polls
    private void saveMultiChoiceVotes(boolean[] votes) {
        spinnerController.startSpinner();

        int uID = Session.getInstance().getUser().getuID();
        JSONObject votesRequestData = new JSONObject();

        try {
            votesRequestData.put("UserID", uID);

            //create json array for each choice user selected
            JSONArray voteItems = new JSONArray();
            JSONObject voteItem;
            for(int i = 0; i < votes.length; i++) {
                if(votes[i]) {
                    //create new json item for vote choice
                    voteItem = new JSONObject();
                    Choice choice = poll.getChoices().get(i);

                    voteItem.put("ChoiceID", choice.getId());
                    //weight here shouldn't matter
                    voteItem.put("Weight", 1);

                    //add vote item to json array
                    voteItems.put(voteItem);
                }
            }

            //add array to request data
            votesRequestData.put("Votes", voteItems);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpConnect.requestJsonPost("/weighted_votes", votesRequestData.toString(), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                Toast.makeText(AnswerPollActivity.this, "Vote Submitted successfully", Toast.LENGTH_LONG).show();
                spinnerController.stopSpinner();
                finish();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                DisplayAlert.displayAlertError(AnswerPollActivity.this, errorMsg, null);
                spinnerController.stopSpinner();
            }


        });
    }

    //save choice to db
//    private void saveSelectedChoice(int selectedChoiceIndex) {
//        spinnerController.startSpinner();
//        //we need to send the choice id and the user id to votes table
//        Choice choice = poll.getChoices().get(selectedChoiceIndex);
//        int uID = Session.getInstance().getUser().getuID();
//        JSONObject voteJson = new JSONObject();
//        try {
//            voteJson.put("UserID", uID);
//            voteJson.put("ChoiceID", choice.getId());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        HttpConnect.requestJsonPost("/vote", voteJson.toString(), new HttpConnect.HttpResult<JSONObject>() {
//            @Override
//            public void onCallbackSuccess(JSONObject response) {
//                Toast.makeText(AnswerPollActivity.this, "Vote Submitted successfully", Toast.LENGTH_LONG).show();
//                spinnerController.stopSpinner();
//            }
//
//            @Override
//            public void onCallbackError(String errorMsg, boolean fromServer) {
//                DisplayAlert.displayAlertError(AnswerPollActivity.this, errorMsg, null);
//                spinnerController.stopSpinner();
//            }
//
//
//        });
//    }

    private void closeRadioAnswerSheet()
    {
        final ClosePollListAdapter adapter = new ClosePollListAdapter(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Poll Answer");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Choice selectedChoice = adapter.getItem(adapter.getCheckedIndex());
                saveSelectedCloseChoice(selectedChoice);
            }
        }).setNegativeButton("Cancel", null);

        //to add the recycler view we need to get the body of the dialog view and add dialog to it
        //first create a framelayout
        FrameLayout frameLayout = new FrameLayout(this);
        builder.setView(frameLayout);

        AlertDialog dialog = builder.create();
        LayoutInflater inflater = dialog.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.custom_poll_answer_sheet, frameLayout);

        //we need a list view in the dialog
        RecyclerView imageList = (RecyclerView) dialogLayout.findViewById(R.id.custom_poll_choice_list);
        imageList.setLayoutManager(new LinearLayoutManager(this));
        imageList.setHasFixedSize(true);

        //create adapter for rate list
        adapter.replaceDataList(poll.getChoices());
        //set rate list adapter to our adapter
        imageList.setAdapter(adapter);

        dialog.show();
    }

    private void closeMultimediaAnswerSheet() {
        final MultimediaListAdapter adapter = new MultimediaListAdapter(this);
        final View modalLayout = findViewById(R.id.activity_answer_poll_modal);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Poll Answer");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Choice selectedChoice = adapter.getItem(adapter.getCheckedIndex());
                saveSelectedCloseChoice(selectedChoice);
            }
        }).setNegativeButton("Cancel", null);

        //to add the recycler view we need to get the body of the dialog view and add dialog to it
        //first create a framelayout
        FrameLayout frameLayout = new FrameLayout(this);
        builder.setView(frameLayout);

        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = dialog.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.custom_poll_answer_sheet, frameLayout);

        //we need a list view in the dialog
        RecyclerView imageList = (RecyclerView) dialogLayout.findViewById(R.id.custom_poll_choice_list);
        imageList.setLayoutManager(new LinearLayoutManager(this));
        imageList.setHasFixedSize(true);

        //create adapter for rate list
        adapter.replaceDataList(poll.getChoices());
        adapter.setImageClickListener(new ImageClickListener() {
            @Override
            public void onImageClicked(Bitmap image) {
                Log.i(CLASS_TAG, "onImageClicked");
                dialog.dismiss();

                //get modal frame
                modalLayout.setVisibility(View.VISIBLE);

                ImageViewerFragment fragment = new ImageViewerFragment();
                Bundle b = new Bundle();
                b.putParcelable("image", image);
                fragment.setArguments(b);

                manager.beginTransaction().add(R.id.activity_answer_poll_modal, fragment).addToBackStack(null).commit();
                manager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (manager.getBackStackEntryCount() == 0) {
                            dialog.show();
                            modalLayout.setVisibility(View.GONE);
                            manager.removeOnBackStackChangedListener(this);
                        }
                        Log.i(CLASS_TAG, "backstack count: " + manager.getBackStackEntryCount());
                    }
                });
            }
        });

        //set rate list adapter to our adapter
        imageList.setAdapter(adapter);

        dialog.show();
    }

    private void saveSelectedCloseChoice(Choice selectedChoice) {
        spinnerController.startSpinner();

        //we need to send the choice id and the poll id to votes table
        JSONObject voteJson = new JSONObject();
        try {
            voteJson.put("PollID", poll.getId());
            voteJson.put("ChoiceID", selectedChoice.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpConnect.requestJsonPut("/close_poll", voteJson.toString(), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                Toast.makeText(AnswerPollActivity.this, "Poll Closed successfully", Toast.LENGTH_LONG).show();
                spinnerController.stopSpinner();
                finish();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                DisplayAlert.displayAlertError(AnswerPollActivity.this, errorMsg, null);
                spinnerController.stopSpinner();
            }
        });
    }


    private void updateVoteCount(int numVotes) {
        total += numVotes;
        pollVotes.setText("" + total);
    }

    @Override
    public void onClick(View v) {
        int vID = v.getId();

        if (vID == answerButton.getId()) {
            determineAnswerSheet();
        } else if (vID == closeButton.getId()) {
            determineCloseSheet();
        }
    }

    private void determineAnswerSheet() {
        switch (poll.getChoiceType()) {
            case RADIO:
                openRadioAnswerSheet();
                break;
            case CHECKLIST:
                openChecklistAnswerSheet();
                break;
            case RATE:
                openRateAnswerSheet();
                break;
            case MULTIMEDIA:
                openMultimediaAnswerSheet();
                break;
        }
    }

    private void determineCloseSheet() {
        switch (poll.getChoiceType()) {
            case RADIO:
            case CHECKLIST:
            case RATE:
                closeRadioAnswerSheet();
                break;
            case MULTIMEDIA:
                closeMultimediaAnswerSheet();
                break;
        }
    }

    @Override
    public void onImageClicked(Bitmap image) {
        Log.i(CLASS_TAG, "onImageClicked");

        //get modal frame
        ViewGroup modalLayout = (ViewGroup) findViewById(R.id.activity_answer_poll_modal);
        ImageViewerFragment fragment = new ImageViewerFragment();
        Bundle b = new Bundle();
        b.putParcelable("image", image);
        fragment.setArguments(b);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().add(R.id.activity_answer_poll_modal, fragment).commit();
    }


    public class RateListAdapter extends RecyclerViewAdapter<Choice, RateListAdapter.MyViewHolder> {

        public RateListAdapter(Context context) {
            super(context);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.rate_poll_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Choice choice = mDataList.get(position);
            holder.choiceString.setText(choice.getChoice());
        }

        public class MyViewHolder extends RecyclerViewAdapter.ViewHolder {
            TextView choiceString;

            public MyViewHolder(View itemView) {
                super(itemView);
                choiceString = (TextView) itemView.findViewById(R.id.rate_answer_choice);
            }
        }
    }

    public class MultimediaListAdapter extends RecyclerViewAdapter<Choice, MultimediaListAdapter.MyViewHolder> {

        private MyViewHolder currentChecked = null;
        private int checkedIndex = -1;
        private ImageClickListener imageClickListener;

        public MultimediaListAdapter(Context context) {
            super(context);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.multimedia_poll_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            Choice choice = mDataList.get(position);
            String imageUrl = choice.getChoice();

            ImageLoader imageLoader = HttpConnect.getInstance().getmImageLoader();
            imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    holder.imageView.setImageBitmap(response.getBitmap());
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(CLASS_TAG, "Error in image request: " + error);
                }
            });
        }

        public int getCheckedIndex() {
            return checkedIndex;
        }


        public void setImageClickListener(ImageClickListener imageClickListener) {
            this.imageClickListener = imageClickListener;
        }


        public class MyViewHolder extends RecyclerViewAdapter.ViewHolder {
            RadioButton radioButton;
            ImageView imageView;

            public MyViewHolder(View itemView) {
                super(itemView);
                radioButton = (RadioButton) itemView.findViewById(R.id.multimedia_radio_button);
                imageView = (ImageView) itemView.findViewById(R.id.multimedia_imageview);

                itemView.setTag(this);
                radioButton.setOnClickListener(this);
                imageView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int vID = v.getId();
                if (vID == imageView.getId()) {
                    imageClickListener.onImageClicked(((BitmapDrawable)imageView.getDrawable()).getBitmap());
                } else {
                    if(currentChecked != null && !currentChecked.equals(this)) {
                        currentChecked.radioButton.setChecked(false);
                    }

                    radioButton.setChecked(true);
                    currentChecked = MyViewHolder.this;
                    checkedIndex = getAdapterPosition();
                }

            }
        }
    }

    public class ClosePollListAdapter extends RecyclerViewAdapter<Choice, ClosePollListAdapter.MyViewHolder> {

        private MyViewHolder currentChecked = null;
        private int checkedIndex = -1;

        public ClosePollListAdapter(Context context) {
            super(context);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.close_poll_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {

            Choice choice = mDataList.get(position);

            holder.choiceText.setText(choice.getChoice());
            holder.voteCount.setText(String.format("%d", choice.getNumberSelected()));

        }

        public int getCheckedIndex() {
            return checkedIndex;
        }


        public class MyViewHolder extends RecyclerViewAdapter.ViewHolder {
            RadioButton radioButton;
            TextView choiceText;
            TextView voteCount;

            public MyViewHolder(View itemView) {
                super(itemView);
                radioButton = (RadioButton) itemView.findViewById(R.id.close_poll_radio_button);
                choiceText = (TextView) itemView.findViewById(R.id.close_poll_choice_text);
                voteCount = (TextView) itemView.findViewById(R.id.close_poll_vote_count);

                if (radioButton == null) {
                    Log.e("Close poll", "radio is null");
                }
                radioButton.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(currentChecked != null && !currentChecked.equals(this)) {
                    currentChecked.radioButton.setChecked(false);
                }

                radioButton.setChecked(true);
                currentChecked = MyViewHolder.this;
                checkedIndex = getAdapterPosition();
            }
        }
    }


}

interface ImageClickListener {
    void onImageClicked(Bitmap image);
}