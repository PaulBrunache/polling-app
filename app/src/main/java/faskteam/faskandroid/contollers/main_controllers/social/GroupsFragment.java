package faskteam.faskandroid.contollers.main_controllers.social;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import faskteam.faskandroid.contollers.main_controllers.MyFragment;
import faskteam.faskandroid.entities.Friend;
import faskteam.faskandroid.entities.Group;
import faskteam.faskandroid.R;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends MyFragment implements RecyclerViewAdapter.OnItemClickListener, View.OnClickListener{
    private final String CLASS_TAG = "GroupsFragment";
    private GroupListAdapter adapter;
    private RecyclerView groupsList;
    private View addButton;

    //TODO: Add support for modifying existing groups

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        adapter = new GroupListAdapter(getActivity());
        adapter.setOnItemClickListener(this);
        groupsList = (RecyclerView) view.findViewById(R.id.list_group);
        groupsList.setAdapter(adapter);
        groupsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupsList.setHasFixedSize(true);
        addButton = view.findViewById(R.id.fab_add_btn_group);
        addButton.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setAppBarTitle("Groups");
        getGroupsForUser();
    }

    private void getGroupsForUser() {
        startSpinner();
        HttpConnect.requestJsonGet(String.format("/user/%d/groups", activity.getSessionUser().getuID()), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);

                    Log.i(CLASS_TAG, data.toString());
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject groupJson = data.getJSONObject(i);
                        String groupName = groupJson.getString("GroupName");

                        //gIds.add(groupJson.getInt("GroupID"));
                        populateGroup(groupJson.getInt("GroupID"), groupName);
                        //Group newGroup = new Group(groupName);
                        //mGroups.add(newGroup);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                stopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();
                DisplayAlert.displayAlertError(activity, errorMsg, null);
            }
        });
    }

    private void populateGroup(final int gID, String gName) {
        final String groupName = gName;
        HttpConnect.requestJsonGet(String.format("/group/%d/users", gID), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    HashMap<String, Friend> groupMembers = new HashMap<String, Friend>();
                    Log.i(CLASS_TAG, data.toString());
                    for (int i = 0; i < data.length(); i++) {

                        JSONObject friendJSON = data.getJSONObject(i);
                        User u;

                        int uID = friendJSON.getInt("UserID");
                        String uName = friendJSON.getString("UserName");
                        String email;
                        if (friendJSON.getString("Email") != null) {
                            email = friendJSON.getString("Email");
                            u = new User(uID, uName, email);
                        } else {
                            u = new User(uID, uName);
                        }
                        Friend f = new Friend(u);
                        groupMembers.put(f.getNickname(), f);

                    }
                    Log.i(CLASS_TAG, "Group Name" + groupName + " Number of Members " + groupMembers.toString());
                    Group group = new Group(groupName, groupMembers);
                    group.setGroupID(gID);
                    //mGroups.add(group);
                    adapter.addItem(group);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                DisplayAlert.displayAlertError(activity, errorMsg, null);
            }
        });
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == addButton.getId()) {
            AddGroupsFragment addGroupsFragment = new AddGroupsFragment();
            activity.pushFragment(addGroupsFragment, null);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i(CLASS_TAG, "Click at " + position);
        GroupDetailsFragment gdf = new GroupDetailsFragment();
        gdf.setgID(adapter.getItem(position).getGroupID());
        activity.pushFragment(gdf, "");
        Log.i(CLASS_TAG, "gID " + adapter.getItem(position).getGroupID());
    }
}

class GroupListAdapter extends RecyclerViewAdapter<Group, GroupListAdapter.MyViewHolder> {

    public GroupListAdapter(Context context) {
        super(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.group_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupListAdapter.MyViewHolder holder, int position) {
        Log.i("GroupsFragment", mDataList.toString());
        Group g = mDataList.get(position);
        holder.groupName.setText(g.getName());
        holder.members.setText(getMemNumStr(position));
    }

    private String getMemNumStr(int pos){
        int size = mDataList.get(pos).getGroupSize();
        String retVal;
        if(size > 1){
            retVal = size + " members";
        }
        else {
            retVal = size + " member";
        }
        return retVal;
    }

    public class MyViewHolder extends RecyclerViewAdapter.ViewHolder {
        TextView groupName;
        TextView members;
        View rootView;


        public MyViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            itemView.setOnClickListener(this);
            groupName = (TextView) itemView.findViewById(R.id.groupname);
            members = (TextView) itemView.findViewById(R.id.num_group_member);
        }
    }
}
