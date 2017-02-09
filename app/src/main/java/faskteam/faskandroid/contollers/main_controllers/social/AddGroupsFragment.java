package faskteam.faskandroid.contollers.main_controllers.social;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import faskteam.faskandroid.contollers.main_controllers.MainActivity;
import faskteam.faskandroid.entities.Friend;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.R;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;


public class AddGroupsFragment extends Fragment implements RecyclerViewAdapter.OnItemClickListener, View.OnClickListener {

    private AddGroupListAdapter adapter;
    private MainActivity mainActivity;
    private RecyclerView addGroupList;
    private EditText addGroupName;
    private FragmentManager fragmentManager;
    private List<Friend> friendsToAdd = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_group, container, false);
        mainActivity = (MainActivity) getActivity();
        mainActivity.setAppBarTitle("Create Group");

        fragmentManager = mainActivity.getSupportFragmentManager();

        adapter = new AddGroupListAdapter(mainActivity);
        adapter.setOnItemClickListener(this);

        addGroupList = (RecyclerView) view.findViewById(R.id.list_add_group);
        addGroupList.setAdapter(adapter);
        addGroupList.setLayoutManager(new LinearLayoutManager(mainActivity));
        addGroupList.setHasFixedSize(true);

        addGroupName = (EditText) view.findViewById(R.id.group_name);

        getFriends();

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_groups, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.group_accept){
            createNewGroup();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(mainActivity, "View friend's profile here.", Toast.LENGTH_LONG).show();
    }

    private void getFriends() {
        mainActivity.startSpinner();
        HttpConnect.requestJsonGet(String.format("/user/%d/friends", mainActivity.getSessionUser().getuID()), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    Log.i("AddGroups", data.toString());
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject friendJSON = data.getJSONObject(i);
                        User u;

                        int uID = friendJSON.getInt("UserID");
                        String uName = friendJSON.getString("UserName");
                        String email;
                        if (friendJSON.getString("Email") != null) {
                            email = friendJSON.getString("Email");
                            u = new User(uID, uName, email);
                            u.setPhoneNumber(friendJSON.getString("Phone"));
                        } else {
                            u = new User(uID, uName);
                            u.setPhoneNumber(friendJSON.getString("Phone"));
                        }

                        Friend f = new Friend(u);
                        adapter.addItem(f);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mainActivity.stopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                mainActivity.stopSpinner();
                DisplayAlert.displayAlertError(mainActivity, errorMsg, null);
            }
        });
    }

    private void createNewGroup() {
        mainActivity.startSpinner();

        friendsToAdd = AddGroupListAdapter.getFriendsToAdd();
        Log.i("FriendGroup", friendsToAdd.toString());
        JSONObject groupRequest = new JSONObject();
        JSONArray groupMemList = new JSONArray();
        try {
            groupRequest.put("GroupName", addGroupName.getText().toString());
            groupRequest.put("UserID", mainActivity.getSessionUser().getuID());

            for(int i = 0; i < friendsToAdd.size(); i++){
//                JSONObject groupMember = new JSONObject();
//                groupMember.put("FriendID", friendsToAdd.get(i).getUser().getuID());
                groupMemList.put(friendsToAdd.get(i).getUser().getuID());
                //groupMemList.put(groupMember);
            }
            groupRequest.put("Members", groupMemList);
            Log.i("GroupPost", groupRequest.toString());
            HttpConnect.requestJsonPost("/group/members", groupRequest.toString(), new HttpConnect.HttpResult<JSONObject>() {
                @Override
                public void onCallbackSuccess(JSONObject response) {
                    friendsToAdd.clear();
                    adapter.clearFriendsToAdd();
                    //Move back on fragment stack
                    mainActivity.stopSpinner();
                    fragmentManager.popBackStack();
                }

                @Override
                public void onCallbackError(String errorMsg, boolean fromServer) {
                    mainActivity.stopSpinner();
                    DisplayAlert.displayAlertError(mainActivity, errorMsg, null);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(mainActivity, "View friend's profile here.", Toast.LENGTH_LONG).show();
    }
}
class AddGroupListAdapter extends RecyclerViewAdapter<Friend, AddGroupListAdapter.MyViewHolder> {

    private static List<Friend> friendsToAdd = new ArrayList<>();

    public AddGroupListAdapter(Context context) {
        super(context);
    }

    public static List<Friend> getFriendsToAdd() {
        return friendsToAdd;
    }

    public void clearFriendsToAdd() { friendsToAdd.clear(); }

    @Override
    public AddGroupListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.friend_row_group, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onBindViewHolder(AddGroupListAdapter.MyViewHolder holder, int position) {
        Friend f = mDataList.get(position);
        Log.i("AddGroup", mDataList.toString());
        holder.friendName.setText(f.getUser().getUsername());
        holder.friendIcon.setImageResource(R.drawable.user_icon);
    }

    public class MyViewHolder extends RecyclerViewAdapter.ViewHolder implements View.OnClickListener{
        TextView friendName;
        ImageView removeBtn;
        ImageView addBtn;
        CircleImageView friendIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            friendName = (TextView) itemView.findViewById(R.id.username);
            removeBtn = (ImageView) itemView.findViewById(R.id.remove_btn);
            addBtn = (ImageView) itemView.findViewById(R.id.add_btn);
            friendIcon = (CircleImageView) itemView.findViewById(R.id.user_avatar);
        }

        @Override
        public void onClick(View v) {
            Log.i("FriendClick", mDataList.get(getAdapterPosition()).getNickname());
            Log.i("FriendClick", "onClick" + getAdapterPosition());
            if(v.findViewById(R.id.add_btn).getVisibility() == View.GONE) {
                friendsToAdd.add(mDataList.get(getAdapterPosition()));
                v.findViewById(R.id.add_btn).setVisibility(View.VISIBLE);
            }
            else if (v.findViewById(R.id.add_btn).getVisibility() == View.VISIBLE){
                friendsToAdd.remove(mDataList.get(getAdapterPosition()));
                v.findViewById(R.id.add_btn).setVisibility(View.GONE);
            }
            Log.i("FriendClick", friendsToAdd.toString());
        }
    }
}
