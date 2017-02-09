package faskteam.faskandroid.contollers.main_controllers;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import faskteam.faskandroid.R;
import faskteam.faskandroid.entities.Choice;
import faskteam.faskandroid.entities.ChoiceType;
import faskteam.faskandroid.entities.Friend;
import faskteam.faskandroid.entities.Poll;
import faskteam.faskandroid.entities.Session;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.CommonFunctions;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.MenuSearchListener;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;


/**
 * Class that retrieves information and data for session user's polls
 */
public class MyPollsFragment extends MyFragment implements View.OnClickListener,
        RecyclerViewAdapter.OnItemClickListener, RecyclerViewAdapter.OnMenuItemSelectedListener,
        MenuSearchListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String CLASS_TAG = "MyPollsFragment";

    private RecyclerView pollList;
    private MyPollListAdapter adapter;
    private ImageView createPollBtn;
    private SwipeRefreshLayout refreshLayout;

    private List<Poll> openPolls = new ArrayList<>();
    private List<Poll> closedPolls = new ArrayList<>();

    private boolean isCheckingOpen = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //initialize components
        //Inflate layout
        View view = inflater.inflate(R.layout.fragment_my_polls, container, false);
        pollList = (RecyclerView) view.findViewById(R.id.poll_list);
        createPollBtn = (ImageView) view.findViewById(R.id.create_poll_btn);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.my_polls_refresh_layout);

        //start the spinner here to block ui while data request is loading
        adapter = new MyPollListAdapter(getActivity());
        adapter.setOnItemClickListener(this);
        adapter.setOnMenuSelectedListner(this);
        pollList.setAdapter(adapter);
        pollList.setLayoutManager(new LinearLayoutManager(getActivity()));
        pollList.setHasFixedSize(true);
        refreshLayout.setOnRefreshListener(this);
        //set create button click listener
        createPollBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //update nav drawer selected menu item and update app bar title
        activity.setMenuSelected(R.id.nav_my_polls);
        activity.setAppBarTitle("My Polls");

        fetchData();
    }


    private void fetchData() {
        startSpinner();
        User sessionUser = Session.getInstance().getUser();

        HttpConnect.requestJsonGet(String.format("/user/%d/my_polls", sessionUser.getuID()), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);

                    //we should be receiving an array of json dictionaries containing poll info/data
                    //so we'll iterate through each JSONObject
                    for (int i = 0; i < data.length(); i++) {
                        //grab the json object for the current index
                        JSONObject pollJson = data.getJSONObject(i);

                        //pull data from json
                        int pollID = pollJson.getInt("PollID");
                        String question = pollJson.getString("Question");
                        int creatorID = pollJson.getInt("UserID");
                        String creatorUsername = pollJson.optString("UserName", "FAsk User");
                        Date closeDate = CommonFunctions.stringToDate(pollJson.optString("CloseDate", ""));
                        ChoiceType choiceType = ChoiceType.checkType(pollJson.getString("Type"));
                        boolean isOpen = pollJson.optInt("Open", 0) == 1;
                        boolean openChoices = (pollJson.optInt("OpenChoice", 0) == 1);
                        boolean isPublic = (pollJson.optInt("IsPublic", 0) == 1);

                        String sLat = pollJson.optString("Latitude", "0");
                        String sLon = pollJson.optString("Longitude", "0");
                        double lat = (sLat != null && !sLat.equals("null")) ? Double.parseDouble(sLat) : 0.0;
                        double lon = (sLon != null && !sLon.equals("null")) ? Double.parseDouble(sLon) : 0.0;
                        //create new location object for poll
                        Location location = new Location("");
                        location.setLatitude(lat);
                        location.setLongitude(lon);

                        // create poll object and add it to adapter
                        Poll poll = new Poll(pollID, question, new ArrayList<Choice>(), choiceType, false, creatorID, isOpen);
                        poll.setOpenChoice(openChoices);
                        poll.setIsPublic(isPublic);
                        poll.setCreatorUsername(creatorUsername);
                        poll.setPollLocation(location);
                        poll.setCloseDate(closeDate);

                        if (poll.isOpen()) {
                            openPolls.add(poll);
                        } else {
                            closedPolls.add(poll);
                        }
                    }

                    if (isCheckingOpen) {
                        adapter.replaceDataList(openPolls);
                    } else {
                        adapter.replaceDataList(closedPolls);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    DisplayAlert.displayAlertError(activity, getResources().getString(R.string.general_error_msg), "");
                }
                stopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                if (fromServer) {
                    DisplayAlert.displayAlertError(activity, errorMsg, null);
                } else {
                    DisplayAlert.displayAlertError(activity, getString(R.string.general_error_msg), null);
                }
                stopSpinner();
            }
        });
    }

    private void showClosedPolls() {
        adapter.replaceDataList(closedPolls);
    }

    private void showOpenPolls() {
        adapter.replaceDataList(openPolls);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_poll_fragments, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_view_closed:
                isCheckingOpen = false;
                showClosedPolls();
                break;
            case R.id.action_view_open:
                isCheckingOpen = true;
                showOpenPolls();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == createPollBtn.getId()) {
            Intent intent = new Intent(activity, PollCreateActivity.class);
            intent.putExtra("uID", Session.getInstance().getUser().getuID());
            startActivityForResult(intent, MainActivity.REQUEST_CREATE_POLL_ACTIVITY);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(activity, AnswerPollActivity.class);

        Poll poll = adapter.getItem(position);
        Bundle pollData = new Bundle();

        pollData.putString("Question", poll.getQuestion());
        pollData.putInt("PollID", poll.getId());
        pollData.putString("Type", poll.getChoiceType().toString());
        pollData.putBoolean("openChoices", poll.isOpenChoice());
        pollData.putString("creatorUsername", poll.getCreatorUsername());
        pollData.putInt("UserID", poll.getCreatorId());
        pollData.putSerializable("closeDate", poll.getCloseDate());
        pollData.putBoolean("isOpen", poll.isOpen());
        pollData.putParcelable("location", poll.getPollLocation());

        intent.putExtras(pollData);

        startActivityForResult(intent, MainActivity.REQUEST_ANSWER_POLL_ACTIVITY);

    }

    private void clearLists() {
        openPolls.clear();
        closedPolls.clear();
    }


    @Override
    public void onSearchSubmit(String search) {
        Log.i(CLASS_TAG, "SearchSubmit: " + search);
        startSpinner();

        //clear poll lists
        clearLists();

        //html encode string
        search = CommonFunctions.URLEncode(search);
        Log.i(CLASS_TAG, "html escaped search string: " + search);

        User sessionUser = Session.getInstance().getUser();

        HttpConnect.requestJsonGet(String.format("/search/user/%d/name/%s/my_polls", sessionUser.getuID(), search),
                new HttpConnect.HttpResult<JSONObject>() {
                    @Override
                    public void onCallbackSuccess(JSONObject response) {
                        try {
                            JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);

                            //we should be receiving an array of json dictionaries containing poll info/data
                            //so we'll iterate through each JSONObject
                            for (int i = 0; i < data.length(); i++) {
                                //grab the json object for the current index
                                JSONObject pollJson = data.getJSONObject(i);

                                //pull data from json
                                int pollID = pollJson.getInt("PollID");
                                String question = pollJson.getString("Question");
                                int creatorID = pollJson.getInt("UserID");
                                String creatorUsername = pollJson.optString("UserName", "FAsk User");
                                Date closeDate = CommonFunctions.stringToDate(pollJson.optString("CloseDate", ""));
                                ChoiceType choiceType = ChoiceType.checkType(pollJson.getString("Type"));
                                boolean isOpen = pollJson.optInt("Open", 0) == 1;
                                boolean openChoices = (pollJson.optInt("OpenChoice", 0) == 1);
                                boolean isPublic = (pollJson.optInt("IsPublic", 0) == 1);

                                String sLat = pollJson.optString("Latitude", "0");
                                String sLon = pollJson.optString("Longitude", "0");
                                double lat = (sLat != null && !sLat.equals("null")) ? Double.parseDouble(sLat) : 0.0;
                                double lon = (sLon != null && !sLon.equals("null")) ? Double.parseDouble(sLon) : 0.0;
                                //create new location object for poll
                                Location location = new Location("");
                                location.setLatitude(lat);
                                location.setLongitude(lon);

                                // create poll object and add it to adapter
                                Poll poll = new Poll(pollID, question, new ArrayList<Choice>(), choiceType, false, creatorID, isOpen);
                                poll.setOpenChoice(openChoices);
                                poll.setIsPublic(isPublic);
                                poll.setCreatorUsername(creatorUsername);
                                poll.setPollLocation(location);
                                poll.setCloseDate(closeDate);

                                if (poll.isOpen()) {
                                    openPolls.add(poll);
                                } else {
                                    closedPolls.add(poll);
                                }
                            }

                            if(isCheckingOpen) {
                                adapter.replaceDataList(openPolls);
                            } else {
                                adapter.replaceDataList(closedPolls);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            DisplayAlert.displayAlertError(activity, getResources().getString(R.string.general_error_msg), "");
                        }
                        stopSpinner();
                    }

                    @Override
                    public void onCallbackError(String errorMsg, boolean fromServer) {
                        if (fromServer) {
                            DisplayAlert.displayAlertError(activity, errorMsg, null);
                        } else {
                            DisplayAlert.displayAlertError(activity, getString(R.string.general_error_msg), null);
                        }
                        stopSpinner();
                    }
                });
    }

    @Override
    public void onRefresh() {
        Log.i(CLASS_TAG, "onRefresh");
        clearLists();
        fetchData();
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onMenuItemSelected(View view, final int position, int id) {
        final Poll poll = adapter.getItem(position);

        String msg = String.format("Are you sure you want to delete the poll:\n\"%s\" and its corresponding votes?", poll.getQuestion());

        DisplayAlert.displayCustomMessage(activity, msg, null, new DisplayAlert.AlertDetails("Yes", "No"), new DisplayAlert.AlertCallback() {
            @Override
            public void onPositive(DialogInterface dialog) {
                deletePoll(poll.getId(), position);
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

    private void deletePoll(int pollID, final int position) {
        startSpinner();
        HttpConnect.requestJsonDelete(String.format("/poll/%d", pollID), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                //remove item from list view
                adapter.removeItem(position);
                Toast.makeText(activity, "Poll successfully deleted.", Toast.LENGTH_LONG).show();
                stopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();
                DisplayAlert.displayAlertError(activity, errorMsg, null);
            }
        });

    }

}

class MyPollListAdapter extends RecyclerViewAdapter<Poll, MyPollListAdapter.MyViewHolder> {

    public MyPollListAdapter(Context context) {
        super(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.my_poll_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Poll poll = mDataList.get(position);
        holder.pollQuestion.setText(poll.getQuestion());
        holder.pollTimeRemaining.setText(poll.getTimeRemaining());
    }

    public class MyViewHolder extends RecyclerViewAdapter.ViewHolder {

        View menuButton;
        TextView pollTimeRemaining;
        TextView pollQuestion;
        TextView pollBody;
        TextView pollTag1;
        TextView pollTag2;
        TextView pollTag3;


        public MyViewHolder(View itemView) {
            super(itemView);

            pollTimeRemaining = (TextView) itemView.findViewById(R.id.poll_time_remaining);
            pollQuestion = (TextView) itemView.findViewById(R.id.poll_question);
            menuButton = itemView.findViewById(R.id.my_polls_row_menu);

            menuButton.setOnClickListener(this);

//            pollBody = (TextView) itemView.findViewById(R.id.poll_body);
//            pollTag1 = (TextView) itemView.findViewById(R.id.poll_tag_1);
//            pollTag2 = (TextView) itemView.findViewById(R.id.poll_tag_2);
//            pollTag3 = (TextView) itemView.findViewById(R.id.poll_tag_3);

        }

        private void showItemMenu(View view) {
            PopupMenu menu = new PopupMenu(mContext, view);
            menu.inflate(R.menu.menu_my_polls_list_item);
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    mMenuListner.onMenuItemSelected(MyViewHolder.this.itemView, getAdapterPosition(), item.getItemId());
                    return true;
                }
            });
            menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {

                }
            });
            menu.show();
        }

        @Override
        public void onClick(View v) {
            int vID = v.getId();
            if (vID == menuButton.getId()) {
                showItemMenu(v);
            } else {
                super.onClick(v);
            }
        }

    }
}
