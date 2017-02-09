package faskteam.faskandroid.contollers.main_controllers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import faskteam.faskandroid.R;
import faskteam.faskandroid.entities.Poll;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;


public class TabGlobalPollsFragment extends MyFragment implements View.OnClickListener, RecyclerViewAdapter.OnItemClickListener {

    private RecyclerView pollList;
    private GenericPollListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;
        //Inflate layout
        view = inflater.inflate(R.layout.fragment_generic_poll, container, false);
        pollList = (RecyclerView) view.findViewById(R.id.poll_list);
        adapter = new GenericPollListAdapter(getActivity());

        adapter.setOnItemClickListener(this);
        pollList.setAdapter(adapter);
        pollList.setLayoutManager(new LinearLayoutManager(getActivity()));
        pollList.setHasFixedSize(true);

        //update nav drawer selected menu item and update app bar title
        activity.setMenuSelected(R.id.nav_nearby_polls);
        activity.setAppBarTitle(getResources().getString(R.string.nav_global_polls));

//        fetchData();

        return view;
    }

    private void fetchData() {
        /*HttpConnect.requestJsonGET(String.format("/user/%d/asked_polls", activity.getSessionUser().getuID()), new HttpResult<JSONArray>() {
            @Override
            public void onCallback(JSONArray response, boolean success) {
                if (!success) {
                    DisplayAlert.displayAlertError(activity, getResources().getString(R.string.general_error_msg), "");
                } else {
                    try {
                        System.out.println(response.toString(4));

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject pollJson = response.getJSONObject(i);
                            int pollID = pollJson.getInt("PollID");
                            String question = pollJson.getString("Question");
                            int creatorID = pollJson.getInt("UserID");
                            ChoiceType choiceType = ChoiceType.checkType(pollJson.getString("Type"));
//                            boolean openChoices = (pollJson.getInt("Open") == 1);

//                            Poll poll = new Poll(pollID, question, new ArrayList<Choice>(), choiceType, false, creatorID);
//                            adapter.addItem(poll);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        //TODO send user error message
                        DisplayAlert.displayAlertError(activity, getResources().getString(R.string.general_error_msg), "");
                    }
                }
            }
        });*/
    }

    @Override
    public void onClick(View v) {
        Log.i("SAM", "" + v.getId());
    }

    @Override
    public void onItemClick(View view, int position) {
//        AnswerPollFragment fragment = new AnswerPollFragment();
//        Poll poll = adapter.getItem(position);
//        Bundle pollData = new Bundle();
//
//        pollData.putString("Question", poll.getQuestion());
//        pollData.putInt("PollID", poll.getId());
//        pollData.putString("Type", poll.getChoiceType().toString());
//        pollData.putBoolean("openChoices", poll.isOpenChoice());
//        pollData.putInt("UserID", poll.getCreatorId());
//        fragment.setArguments(pollData);
//
//        activity.pushFragment(fragment, null);
        Intent intent;
        Poll poll = adapter.getItem(position);
        Bundle pollData = new Bundle();

        pollData.putString("Question", poll.getQuestion());
        pollData.putInt("PollID", poll.getId());
        pollData.putString("Type", poll.getChoiceType().toString());
        pollData.putBoolean("openChoices", poll.isOpenChoice());
        pollData.putInt("UserID", poll.getCreatorId());
        pollData.putBoolean("isOpen", poll.isOpen());

        intent = new Intent(activity, AnswerPollActivity.class);


        intent.putExtras(pollData);
        startActivityForResult(intent, MainActivity.REQUEST_ANSWER_POLL_ACTIVITY);
    }


}
