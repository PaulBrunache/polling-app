package faskteam.faskandroid.contollers.main_controllers.social;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import faskteam.faskandroid.R;
import faskteam.faskandroid.contollers.main_controllers.MyFragment;
import faskteam.faskandroid.entities.Session;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;


public class TabFriendsSearchFragment extends MyFragment implements View.OnClickListener,
        RecyclerViewAdapter.OnItemClickListener {
    public static final String CLASS_TAG = TabFriendsSearchFragment.class.getSimpleName();

    private RecyclerView list;
    private View btnSearch;
    private View btnClearSearch;
    private EditText editTextSearch;

    private UserListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        //Inflate layout
        view = inflater.inflate(R.layout.fragment_search_friends, container, false);

        list = (RecyclerView) view.findViewById(R.id.search_friends_list);
        btnSearch = view.findViewById(R.id.search_friends_btn_search);
        btnClearSearch = view.findViewById(R.id.search_friends_btn_clear);
        editTextSearch = (EditText) view.findViewById(R.id.search_friends_edit_text);

        adapter = new UserListAdapter(getActivity());
        adapter.setOnItemClickListener(this);

        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setHasFixedSize(true);

        btnSearch.setOnClickListener(this);
        btnClearSearch.setOnClickListener(this);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editTextSearch.getText().toString().length() > 0) {
                    btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    btnClearSearch.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    performSearch();
                }
                return false;
            }
        });
        return view;
    }

    private void performSearch() {
        startSpinner();

        //get user search string, es
        String searchString = Html.escapeHtml(editTextSearch.getText().toString());
        Log.i(CLASS_TAG, "searching for: " + searchString);

        //clear listview items
        adapter.clearDataList();

        HttpConnect.requestJsonGet(String.format("/search/username/email/%s", searchString), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    //for each user info JSON in the array, create a User and Friend object
                    //update session user's friends list, and update adapterview's list
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject userJson = data.getJSONObject(i);
                        User user = new User();

                        int userId = userJson.getInt("UserID");
                        String username = userJson.getString("UserName");
//                        int answerPoints = userJson.getInt("AnswerPoints");
//                        int askPoints = userJson.getInt("AskPoints");
//                        User.PointsObject pointsObject = new User.PointsObject(answerPoints, askPoints);
                        String email = userJson.getString("Email");

                        user.setEmail(email);
//                        user.setPointsObject(pointsObject);
                        user.setUsername(username);
                        user.setuID(userId);

                        adapter.addItem(user);
                    }
                    System.out.println(response.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                stopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();
                DisplayAlert.displayAlertError(activity, "Sorry. An error occurred when retrieving Fask users. Please try again.", null);
            }
        });
    }

    private void clearSearchBox() {
        editTextSearch.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_friends_btn_search:
                performSearch();
                break;
            case R.id.search_friends_btn_clear:
                clearSearchBox();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        final User contact = adapter.getItem(position);
        String msg = "Are you sure you want to add " + contact.getUsername() + " as a FAsk friend?";

        DisplayAlert.displayCustomMessage(activity, msg, null
                , new DisplayAlert.AlertDetails("Yes", "No"), new DisplayAlert.AlertCallback() {
            @Override
            public void onPositive(DialogInterface dialog) {
                sendFriendRequest(contact);
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


    private void sendFriendRequest(User contact) {
        startSpinner();

        JSONObject requestData = new JSONObject();
        try {
            requestData.put("UserID", Session.getInstance().getUser().getuID());
            requestData.put("FriendID", contact.getuID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpConnect.requestJsonPost("/friendship", requestData.toString(), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                stopSpinner();

                try {
                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
                    JSONObject dataJson = data.getJSONObject(0);
                    int result = dataJson.getInt("FriendshipID");
                    System.out.println(result);
                    if (result == -1) {
                        DisplayAlert.displayAlertError(activity, getString(R.string.general_error_msg), null);
                    } else {
                        DisplayAlert.displaySimpleMessage(activity, "Friend request was successfully sent", null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                stopSpinner();

                if (fromServer) {
                    DisplayAlert.displayAlertError(activity, errorMsg, null);
                } else {
                    DisplayAlert.displayAlertError(activity, getString(R.string.general_error_msg), null);
                }
            }
        });

    }
}

class UserListAdapter extends RecyclerViewAdapter<User, UserListAdapter.MyViewHolder> {

    public UserListAdapter(Context context) {
        super(context);
    }

    @Override
    public UserListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.friend_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserListAdapter.MyViewHolder holder, int position) {
        User u = mDataList.get(position);
        holder.username.setText(u.getUsername());
    }


    public class MyViewHolder extends RecyclerViewAdapter.ViewHolder {
        CircleImageView avatar;
        TextView username;
        View removeBtn;

        public MyViewHolder(View itemView) {
            super(itemView);
            avatar = (CircleImageView) itemView.findViewById(R.id.user_avatar);
            username = (TextView) itemView.findViewById(R.id.username);
            removeBtn = itemView.findViewById(R.id.remove_btn);
        }

    }
}
