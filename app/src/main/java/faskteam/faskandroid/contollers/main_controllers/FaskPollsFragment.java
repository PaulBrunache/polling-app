package faskteam.faskandroid.contollers.main_controllers;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.CommonFunctions;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.MenuSearchListener;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;


public class FaskPollsFragment extends MyFragment implements RecyclerViewAdapter.OnItemClickListener,
        MenuSearchListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String CLASS_TAG = FaskPollsFragment.class.getSimpleName();

    private RecyclerView pollList;
    private GenericPollListAdapter adapter;
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

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fask_polls, container, false);
        pollList = (RecyclerView) view.findViewById(R.id.poll_list);
        adapter = new GenericPollListAdapter(getActivity());
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fask_polls_swipe_layout);

        adapter.setOnItemClickListener(this);
        refreshLayout.setOnRefreshListener(this);

        pollList.setAdapter(adapter);
        pollList.setLayoutManager(new LinearLayoutManager(getActivity()));
        pollList.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //update nav drawer selected menu item and update app bar title
        activity.setMenuSelected(R.id.nav_fask_polls);
        activity.setAppBarTitle(getResources().getString(R.string.nav_fask_polls));
        fetchData();
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


    private void fetchData() {
        startSpinner();
        User sessionUser = Session.getInstance().getUser();

        HttpConnect.requestJsonGet(String.format("/user/%d/asked_polls", sessionUser.getuID()), new HttpConnect.HttpResult<JSONObject>() {
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
    public void onItemClick(View view, int position) {
        Intent intent;
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

        intent = new Intent(activity, AnswerPollActivity.class);


        intent.putExtras(pollData);
        startActivityForResult(intent, MainActivity.REQUEST_ANSWER_POLL_ACTIVITY);

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

        //use search item and search server for items containing search string
        User sessionUser = Session.getInstance().getUser();

        HttpConnect.requestJsonGet(String.format("/search/%s/user/%d/asked_polls", search, sessionUser.getuID()),
                new HttpConnect.HttpResult<JSONObject>() {
                    @Override
                    public void onCallbackSuccess(JSONObject response) {
                        try {
                            JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);

                            //we should be receiving an array of json dictionaries containing poll info/data
                            //so we'll iterate through each JSONObject
                            for (int i = 0; i < data.length(); i++) {
                                //grab the json object for the current index
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
                        DisplayAlert.displayAlertError(activity, errorMsg, null);

                        stopSpinner();
                    }
                });
    }

    private void clearLists() {
        openPolls.clear();
        closedPolls.clear();
    }

    @Override
    public void onRefresh() {
        Log.i(CLASS_TAG, "onRefresh");
        clearLists();
        fetchData();
        refreshLayout.setRefreshing(false);
    }
}