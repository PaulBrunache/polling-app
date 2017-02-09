package faskteam.faskandroid.contollers.main_controllers.social;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import faskteam.faskandroid.R;
import faskteam.faskandroid.contollers.main_controllers.MyFragment;
import faskteam.faskandroid.entities.FacebookUser;
import faskteam.faskandroid.entities.Session;
import faskteam.faskandroid.entities.User;
import faskteam.faskandroid.utilities.DisplayAlert;
import faskteam.faskandroid.utilities.SimpleContact;
import faskteam.faskandroid.utilities.http_connection.HttpConnect;
import faskteam.faskandroid.utilities.recyclerview.RecyclerViewAdapter;


public class TabFriendsFromContactsFragment extends MyFragment implements ContactsAdapterViewClickListener {
    public static final String CLASS_TAG = TabFriendsFromContactsFragment.class.getSimpleName();

    private AddFriendListAdapter fromPhoneAdapter;
    private AddFriendListAdapter fromFacebookAdapter;
    private RecyclerView fromPhoneList;
    private RecyclerView fromFacebookList;
    private int fromPhoneListID;
    private int fromFacebookListID;
    private List<SimpleContact> phoneContacts = new ArrayList<>();
    private List<FacebookUser> facebookContacts = new ArrayList<>();

    private LooperThread looperThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        looperThread = new LooperThread();
        looperThread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_from_contacts, container, false);

        fromPhoneList = (RecyclerView) view.findViewById(R.id.phone_contacts_list);
        fromFacebookList = (RecyclerView) view.findViewById(R.id.facebook_contacts_list);
        fromFacebookListID = fromFacebookList.getId();
        fromPhoneListID = fromPhoneList.getId();
        fromPhoneAdapter = new AddFriendListAdapter(getActivity(), fromPhoneListID);
        fromFacebookAdapter = new AddFriendListAdapter(getActivity(), fromFacebookListID);

        fromPhoneAdapter.setOnItemClickListener(this);
        fromFacebookAdapter.setOnItemClickListener(this);

        fromPhoneList.setLayoutManager(new LinearLayoutManager(getActivity()));
        fromPhoneList.setHasFixedSize(true);
        fromPhoneList.setAdapter(fromPhoneAdapter);
        fromFacebookList.setLayoutManager(new LinearLayoutManager(getActivity()));
        fromFacebookList.setHasFixedSize(true);
        fromFacebookList.setAdapter(fromFacebookAdapter);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Helper.postRunnable(new Runnable() {
            @Override
            public void run() {
                getFacebookContacts();
            }
        });

        Helper.postRunnable(new Runnable() {
            @Override
            public void run() {
                getDeviceContacts();
            }
        });

        Helper.postRunnable(new Runnable() {
            @Override
            public void run() {
                matchContactToFaskUser();
            }
        });
    }

    private void getFacebookContacts() {
        Log.i(TabFriendsFromContactsFragment.class.getSimpleName(), "getFacebookContacts");

        //clear facebook contacts list
        facebookContacts.clear();

        GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {

                    @Override
                    public void onCompleted(JSONArray objects, GraphResponse response) {
                        Log.i(CLASS_TAG, "onComplete");

                        if (objects != null) {
                            Log.i(CLASS_TAG, objects.toString());

                            //iterate through friends json array
                            int size = objects.length();
                            for (int i = 0; i < size; i++) {
                                try {
                                    JSONObject fbFriendDict = objects.getJSONObject(i);
                                    String firstName = fbFriendDict.getString("first_name");
                                    String lastName = fbFriendDict.getString("last_name");
                                    String id = fbFriendDict.getString("id");

                                    FacebookUser fbUser = new FacebookUser();
                                    fbUser.setFbId(id);
                                    fbUser.setFirstName(firstName);
                                    fbUser.setLastName(lastName);

                                    //add contact to facebook phoneContacts list
                                    facebookContacts.add(fbUser);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name");
        request.setParameters(parameters);
        request.executeAndWait();
    }

    private void getDeviceContacts() {
        int read_contacts = 0;
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                    read_contacts);

        } else {
            phoneContacts = ReadPhoneContacts(activity);
        }
    }


    //check and match device phone numbers to numbers in database
    private void matchContactToFaskUser() {
        postStartSpinner();

        JSONArray contactJsonArray = new JSONArray();
        JSONArray facebookContactJSONArray = new JSONArray();

        //add all phoneContacts to json array
        for (final SimpleContact contact : phoneContacts) {
            contactJsonArray.put(contact.getNumber());
        }
        for (FacebookUser fbContact : facebookContacts) {
            facebookContactJSONArray.put(fbContact.getFbId());
        }

        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("Contacts", contactJsonArray);
            requestJson.put("FacebookIDs", facebookContactJSONArray);
            Log.i(CLASS_TAG, requestJson.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //send to server
        HttpConnect.requestJsonPost("/contacts", requestJson.toString(), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject(HttpConnect.KEY_RESP_DATA);
                    //we want to add each user to the phoneContacts list
                    JSONArray phoneContacts = data.getJSONArray("Phone");
                    JSONArray facebookContacts = data.getJSONArray("Facebook");

                    appendContacts(phoneContacts, fromPhoneAdapter);
                    appendContacts(facebookContacts, fromFacebookAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                postStopSpinner();
            }

            @Override
            public void onCallbackError(String errorMsg, boolean fromServer) {
                postStopSpinner();
            }
        });


    }

    private void appendContacts(JSONArray arr, AddFriendListAdapter adapter) throws JSONException {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject userInfo = arr.getJSONObject(i);

            User user = new User(userInfo.getInt("UserID"), userInfo.getString("UserName"), new Date());
            user.setPhoneNumber(userInfo.getString("Phone"));
            int facebookCred = userInfo.getInt("FacebookCreds");

            adapter.addItem(user);
        }
    }

    //This Context parameter is nothing but your Activity class's Context
    public List<SimpleContact> ReadPhoneContacts(Context cntx) {
        List<SimpleContact> contacts = new ArrayList<>();

        Cursor cursor = cntx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Integer contactsCount = 0;
        try {
            contactsCount = cursor.getCount(); // get how many phoneContacts you have in your phoneContacts list
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (contactsCount > 0) {

            while (cursor.moveToNext()) {

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //the below cursor will give you details for multiple phoneContacts
                    Cursor pCursor = cntx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list
                    while (pCursor.moveToNext()) {

                        int phoneType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        //String isStarred 		= pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));
                        String phoneNo = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //you will get all phone numbers according to it's type as below switch case.
                        //Logs.e will print the phone number along with the name in DDMS. you can use these details where ever you want.
                        switch (phoneType) {
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                Log.e(contactName + ": TYPE_MOBILE", " " + phoneNo);
                                contacts.add(new SimpleContact(contactName, phoneNo));
                                break;
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
//                                Log.e(contactName + ": TYPE_HOME", " " + phoneNo);
//                                break;
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
//                                Log.e(contactName + ": TYPE_WORK", " " + phoneNo);
//                                break;
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
//                                Log.e(contactName + ": TYPE_WORK_MOBILE", " " + phoneNo);
//                                break;
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
//                                Log.e(contactName + ": TYPE_OTHER", " " + phoneNo);
//                                break;
                            default:
                                break;
                        }
                    }
                    pCursor.close();
                }
            }
            cursor.close();
        }

        return contacts;
    }

    @Override
    public void onItemClick(View view, int position) {
//
//        final User contact = fromPhoneAdapter.getItem(position);
//        String msg = "Are you sure you want to add " + contact.getUsername() + " as a FAsk friend?";
//
//        DisplayAlert.displayCustomMessage(activity, msg, null
//                , new DisplayAlert.AlertDetails("Yes", "No"), new DisplayAlert.AlertCallback() {
//            @Override
//            public void onPositive(DialogInterface dialog) {
//                sendFriendRequest(contact);
//            }
//
//            @Override
//            public void onNegative(DialogInterface dialog) {
//
//            }
//
//            @Override
//            public void onAny(DialogInterface dialog) {
//                dialog.dismiss();
//            }
//        });
    }

    @Override
    public void onItemClick(View view, int position, int adapterID) {
        final User contact;

        if (adapterID == fromPhoneListID) {
            contact = fromPhoneAdapter.getItem(position);
        } else {
            contact = fromFacebookAdapter.getItem(position);
        }

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
        postStartSpinner();

        Log.e("SAM", Session.getInstance().getSessionID());
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("UserID", activity.getSessionUser().getuID());
            requestData.put("FriendID", contact.getuID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpConnect.requestJsonPost("/friendship", requestData.toString(), new HttpConnect.HttpResult<JSONObject>() {

            @Override
            public void onCallbackSuccess(JSONObject response) {
                postStopSpinner();

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
                postStopSpinner();

                if (fromServer) {
                    DisplayAlert.displayAlertError(activity, errorMsg, null);
                } else {
                    DisplayAlert.displayAlertError(activity, getString(R.string.general_error_msg), null);
                }
            }
        });

    }

    public void postStartSpinner() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startSpinner();
            }
        });
    }

    public void postStopSpinner() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopSpinner();
            }
        });
    }

}

class AddFriendListAdapter extends RecyclerViewAdapter<User, AddFriendListAdapter.MyViewHolder> {

    private int adapterID;

    public AddFriendListAdapter(Context context, int id) {
        super(context);
        this.adapterID = id;
    }

    @Override
    public AddFriendListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.contact_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User contact = mDataList.get(position);
        holder.name.setText(contact.getUsername());

    }

    public class MyViewHolder extends RecyclerViewAdapter.ViewHolder {
        CircleImageView avatar;
        TextView name;

        public MyViewHolder(View itemView) {
            super(itemView);
            avatar = (CircleImageView) itemView.findViewById(R.id.user_avatar);
            name = (TextView) itemView.findViewById(R.id.contact_name);
        }

        @Override
        public void onClick(View v) {
            ((ContactsAdapterViewClickListener)mListener).onItemClick(v, getAdapterPosition(), adapterID);
        }
    }




}

interface ContactsAdapterViewClickListener extends RecyclerViewAdapter.OnItemClickListener {

    void onItemClick(View view, int position, int adapterID);
}

class LooperThread extends Thread {
    Handler handler;

    public LooperThread() {
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new Handler();
        Looper.loop();
    }

//    public Handler getHandler() {
//        return handler;
//    }
}

class Helper {
    private static final HandlerThread HANDLER_THREAD;
    private static final Handler HANDLER;
    static {
        HANDLER_THREAD = new HandlerThread("handler_thread");
        HANDLER_THREAD.start();

        HANDLER = new Handler(HANDLER_THREAD.getLooper());
    }

    public static void postRunnable(Runnable r) {
        HANDLER.post(r);
    }
}
