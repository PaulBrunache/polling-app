package faskteam.faskandroid.contollers.main_controllers.social;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import faskteam.faskandroid.R;
import faskteam.faskandroid.contollers.main_controllers.MyFragment;
import faskteam.faskandroid.entities.Friend;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;


public class TabFriendsFragment extends MyFragment implements RecyclerViewAdapter.OnItemClickListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String CLASS_TAG = "TabFriendsFragment";

    private RecyclerView friendsList;
    private SwipeRefreshLayout refreshLayout;
    private FriendsListAdapter adapter;

//    private View addButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate layout
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsList = (RecyclerView) view.findViewById(R.id.list);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_friends_swipe_layout);
        adapter = new FriendsListAdapter(getActivity());

        refreshLayout.setOnRefreshListener(this);
        adapter.setOnItemClickListener(this);
        friendsList.setAdapter(adapter);
        friendsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsList.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchFriends();
    }

    private void fetchFriends() {
        startSpinner();
        HttpConnect.requestJsonGet(String.format("/user/%d/friends", activity.getSessionUser().getuID()), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    //for each user info JSON in the array, create a User and Friend object
                    //update session user's friends list, and update adapterview's list
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject friendJson = data.getJSONObject(i);
                        User fUser = new User(friendJson.getInt("UserID"), friendJson.getString("UserName"));
                        fUser.setPhoneNumber(friendJson.optString("Phone",""));
                        int friendshipID = friendJson.optInt("FriendshipID", -1);
                        Friend friend = new Friend(fUser);
                        friend.setFriendshipID(friendshipID);

                        adapter.addItem(friend);
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

    private void deleteFriendship(int friendshipID, final int position) {
        startSpinner();
        HttpConnect.requestJsonDelete(String.format("/friendship/%d", friendshipID), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                //remove item from list view
                adapter.removeItem(position);
                Toast.makeText(activity, "Friendship successfully removed.", Toast.LENGTH_LONG).show();
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
    public void onClick(View v) {
        int id = v.getId();
    }

    @Override
    public void onItemClick(View view, int position) {
        Friend friend = adapter.getItem(position);
        int friendshipID = friend.getFriendshipID();
         deleteFriendship(friendshipID, position);
    }

    @Override
    public void onRefresh() {
        Log.i(CLASS_TAG, "onRefresh");
        adapter.clearDataList();
        fetchFriends();
        refreshLayout.setRefreshing(false);
    }
}

class FriendsListAdapter extends RecyclerViewAdapter<Friend, FriendsListAdapter.MyViewHolder> {

    public FriendsListAdapter(Context context) {
        super(context);
    }

    @Override
    public FriendsListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.friend_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendsListAdapter.MyViewHolder holder, int position) {
        Friend f = mDataList.get(position);
        User u = f.getUser();
        holder.username.setText(u.getUsername());
    }


    public class MyViewHolder extends RecyclerViewAdapter.ViewHolder {
        CircleImageView avatar;
        TextView username;
        View removeBtn;
        View menuButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            avatar = (CircleImageView) itemView.findViewById(R.id.user_avatar);
            username = (TextView) itemView.findViewById(R.id.username);
            removeBtn = itemView.findViewById(R.id.remove_btn);
            menuButton = itemView.findViewById(R.id.friend_row_menu);

            menuButton.setOnClickListener(this);
        }

        private void showItemMenu(View view) {
            PopupMenu menu = new PopupMenu(mContext, view);
            menu.inflate(R.menu.menu_friends_list_item);
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemID = item.getItemId();
                    if(itemID == R.id.friend_item_menu_remove) {
                        mListener.onItemClick(MyViewHolder.this.itemView, getAdapterPosition());
                    }
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
            }
        }
    }
}
