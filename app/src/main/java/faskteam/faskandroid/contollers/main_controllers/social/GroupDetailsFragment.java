package faskteam.faskandroid.contollers.main_controllers.social;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import faskteam.faskandroid.R;
import faskteam.faskandroid.contollers.main_controllers.MainActivity;
import faskteam.faskandroid.entities.Friend;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;

public class GroupDetailsFragment extends Fragment implements RecyclerViewAdapter.OnItemClickListener {

    private final String CLASS_TAG = "GroupDetailsFragment";

    private MainActivity mainActivity;
    private GroupDetailsAdapter adapter;
    private RecyclerView groupsList;
    private SearchView searchView;
    private int gID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_detail, container, false);
        mainActivity = (MainActivity) getActivity();
        mainActivity.setAppBarTitle("Group Detail");

        adapter = new GroupDetailsAdapter(mainActivity);
        adapter.setOnItemClickListener(this);

        groupsList = (RecyclerView) view.findViewById(R.id.group_members);
        groupsList.setAdapter(adapter);
        groupsList.setLayoutManager(new LinearLayoutManager(mainActivity));
        groupsList.setHasFixedSize(true);

        searchView = (SearchView) view.findViewById(R.id.group_member_add);
        searchView.setOnQueryTextListener(addMemberQuery());

        getUsersForGroup();

        return view;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        Log.i(CLASS_TAG, "onOptinonsItemSelected");
//        switch(item.getItemId()){
//            case android.R.id.home:
//                fragmentManager.popBackStack();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

//    private FragmentManager.OnBackStackChangedListener backButtonPressed(){
//        return new FragmentManager.OnBackStackChangedListener() {
//            @Override
//            public void onBackStackChanged() {
//                mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//                mainActivity.getSupportActionBar().setHomeButtonEnabled(false);
//            }
//        };
//    }

    private SearchView.OnQueryTextListener addMemberQuery(){
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                addUserToGroup(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
    }

    private void addUserToGroup(String query) {
        mainActivity.startSpinner();
        HttpConnect.requestJsonGet("/user/" + query + "/user_name", new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
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
                            u.setPhoneNumber(friendJSON.getString("Phone"));
                        } else {
                            u = new User(uID, uName);
                            u.setPhoneNumber(friendJSON.getString("Phone"));
                        }

                        Friend f = new Friend(u);
                        adapter.addItem(f);
                        putUserToGroup(f);
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

    private void putUserToGroup(Friend friend) {
        JSONObject userRequest = new JSONObject();
        JSONArray userToAdd = new JSONArray();
        JSONArray emptyArray = new JSONArray();
        try {
            mainActivity.startSpinner();
            userRequest.put("GroupID", gID);
            Log.i(CLASS_TAG, "" + friend.getUser().getuID());
            userToAdd.put(friend.getUser().getuID());
            userRequest.put("Add", userToAdd);
            userRequest.put("Remove", emptyArray);

            HttpConnect.requestJsonPut("/group_members", userRequest.toString(), new HttpConnect.HttpResult<JSONObject>() {
                @Override
                public void onCallbackSuccess(JSONObject response) {
                    Log.i(CLASS_TAG, response.toString());
                    Toast.makeText(mainActivity, "Your friend has been added to this group!", Toast.LENGTH_LONG).show();
                    mainActivity.stopSpinner();
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
        //mainActivity.stopSpinner();
    }

    private void getUsersForGroup(){
        mainActivity.startSpinner();
        HttpConnect.requestJsonGet(String.format("/group/%d/users", gID), new HttpConnect.HttpResult<JSONObject>() {
            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    Log.i(CLASS_TAG, data.toString());
                    for(int i = 0; i < data.length(); i++){
                        JSONObject friendJSON = data.getJSONObject(i);
                        User u;

                        int uID = friendJSON.getInt("UserID");
                        Log.i(CLASS_TAG, ""+uID);
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



    @Override
    public void onItemClick(View view, int position) {

    }

    public void setgID(int gID) {
        this.gID = gID;
    }

    class GroupDetailsAdapter extends RecyclerViewAdapter<Friend, GroupDetailsAdapter.MyViewHolder>{

        public GroupDetailsAdapter(Context context) {
            super(context);
        }

        @Override
        public GroupDetailsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.friend_row_group, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(GroupDetailsAdapter.MyViewHolder holder, int position) {
            Friend f = mDataList.get(position);
            Log.i("GroupDetail", mDataList.toString());
            holder.friendName.setText(f.getUser().getUsername());
            holder.friendIcon.setImageResource(R.drawable.user_icon);
        }

        public class MyViewHolder extends RecyclerViewAdapter.ViewHolder implements View.OnClickListener{
            TextView friendName;
            ImageView removeBtn;
            CircleImageView friendIcon;

            public MyViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                friendName = (TextView) itemView.findViewById(R.id.username);
                removeBtn = (ImageView) itemView.findViewById(R.id.remove_btn);
                removeBtn.setVisibility(View.VISIBLE);
                removeBtn.setClickable(true);
                removeBtn.setOnClickListener(this);
                friendIcon = (CircleImageView) itemView.findViewById(R.id.user_avatar);
            }

            @Override
            public void onClick(View v) {
                Log.i("GroupDetailsFragment", "removebtnonclick");
                if(v.getId() == R.id.remove_btn){
                    Log.i("GroupDetailsFragment", "removebtnclicked");
                    Friend f = mDataList.get(getAdapterPosition());
                    removeUserToGroup(f, getAdapterPosition());
                }
            }

            private void removeUserToGroup(Friend friend, int pos) {
                mainActivity.startSpinner();
                final int position = pos;
                JSONObject userRequest = new JSONObject();
                JSONArray userToRemove = new JSONArray();
                JSONArray emptyArray = new JSONArray();
                try {
                    mainActivity.startSpinner();
                    userRequest.put("GroupID", gID);
                    Log.i(CLASS_TAG, "" + friend.getUser().getuID());
                    userToRemove.put(friend.getUser().getuID());
                    userRequest.put("Remove", userToRemove);
                    userRequest.put("Add", emptyArray);

                    HttpConnect.requestJsonPut("/group_members", userRequest.toString(), new HttpConnect.HttpResult<JSONObject>() {
                        @Override
                        public void onCallbackSuccess(JSONObject response) {
                            Log.i("GroupDetailsFragment", response.toString());
                            mainActivity.stopSpinner();
                            Toast.makeText(mainActivity, "Your friend has been removed from this group!", Toast.LENGTH_LONG).show();
                            adapter.removeItem(position);
                            adapter.notifyDataSetChanged();
                            mainActivity.stopSpinner();
                            if(adapter.getItemCount() == 0){
                                deleteGroup();
                            }
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

            private void deleteGroup() {
                HttpConnect.requestJsonDelete(String.format("/group/%d", gID), new HttpConnect.HttpResult<JSONObject>() {
                    @Override
                    public void onCallbackSuccess(JSONObject response) {
                        Toast.makeText(mainActivity, "Group has been deleted.", Toast.LENGTH_LONG).show();
                        mainActivity.getSupportFragmentManager().popBackStack();
                    }

                    @Override
                    public void onCallbackError(String errorMsg, boolean fromServer) {
                        DisplayAlert.displayAlertError(mainActivity, errorMsg, null);
                    }
                });
            }
        }
    }
}
