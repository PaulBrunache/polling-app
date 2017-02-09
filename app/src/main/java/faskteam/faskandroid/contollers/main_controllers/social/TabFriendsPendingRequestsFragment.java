package faskteam.faskandroid.contollers.main_controllers.social;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import faskteam.faskandroid.R;
import faskteam.faskandroid.contollers.main_controllers.MyFragment;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;


public class TabFriendsPendingRequestsFragment extends MyFragment implements RecyclerViewAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {
    public static final String CLASS_TAG = TabFriendsPendingRequestsFragment.class.getSimpleName();

    private RecyclerView requestsList;
    private PendingRequestsListAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    private List<Integer> friendshipIDs = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;
        //Inflate layout
        view = inflater.inflate(R.layout.fragment_friends, container, false);
        requestsList = (RecyclerView) view.findViewById(R.id.list);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_friends_swipe_layout);
        adapter = new PendingRequestsListAdapter(getActivity());

        adapter.setOnItemClickListener(this);
        refreshLayout.setOnRefreshListener(this);
        requestsList.setAdapter(adapter);
        requestsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        requestsList.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchRequests();
    }

    private void fetchRequests() {
        startSpinner();
        HttpConnect.requestJsonGet(String.format("/friendship/%d/asked", activity.getSessionUser().getuID()), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {

                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject friendReqJson = data.getJSONObject(i);
                        int friendshipID = friendReqJson.getInt("FrienshipID");
                        User fUser = new User(friendReqJson.getInt("UserID"), friendReqJson.getString("UserName"));

                        adapter.addItem(fUser);
                        friendshipIDs.add(friendshipID);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                stopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();
                DisplayAlert.displayAlertError(activity, "Sorry. An error occurred when retrieving friends. Please try again.", null);
            }
        });

    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("Pending Requests", "item click" + position);
        int friendshipID = friendshipIDs.get(position);
        acceptDeclineFriendship(friendshipID, position);
    }

    private void acceptDeclineFriendship(int friendshipID, final int position) {
        JSONObject data = new JSONObject();
        try {
            data.put("FriendshipID", friendshipID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startSpinner();
        HttpConnect.requestJsonPut("/friendship/accept", data.toString(), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                Toast.makeText(activity, "Friendship Accepted", Toast.LENGTH_LONG).show();
                adapter.removeItem(position);
                stopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();
                DisplayAlert.displayAlertError(activity, errorMsg, null);

            }
        });
    }

    @Override
    public void onRefresh() {
        Log.i(CLASS_TAG, "onRefresh");
        adapter.clearDataList();
        fetchRequests();
        refreshLayout.setRefreshing(false);
    }

    private class PendingRequestsListAdapter extends RecyclerViewAdapter<User, PendingRequestsListAdapter.MyViewHolder> {

        public PendingRequestsListAdapter(Context context) {
            super(context);
        }

        @Override
        public PendingRequestsListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.friend_request_row, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PendingRequestsListAdapter.MyViewHolder holder, int position) {
            User u = mDataList.get(position);
            holder.username.setText(u.getUsername());

        }


        public class MyViewHolder extends RecyclerViewAdapter.ViewHolder {
            CircleImageView avatar;
            TextView username;
            View btnDecline;
            View btnAccept;

            public MyViewHolder(View itemView) {
                super(itemView);
                avatar = (CircleImageView) itemView.findViewById(R.id.user_avatar);
                username = (TextView) itemView.findViewById(R.id.username);
                btnDecline = itemView.findViewById(R.id.btn_decline);
                btnAccept = itemView.findViewById(R.id.btn_accept);

                btnAccept.setOnClickListener(this);
                btnDecline.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_decline:
                        removeItem(getAdapterPosition());
                        break;
                    case R.id.btn_accept:
                        super.onClick(v);
                        break;
                }
            }
        }
    }

}
