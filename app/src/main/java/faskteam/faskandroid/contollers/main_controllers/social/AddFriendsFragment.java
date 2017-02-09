package faskteam.faskandroid.contollers.main_controllers.social;


import android.support.v4.app.DialogFragment;


public class AddFriendsFragment extends DialogFragment  {

//    private AddFriendListAdapter adapter;
//    private MainActivity activity;
//    private RecyclerView suggestedList;
//    private List<SimpleContact> contacts = new ArrayList<>();
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_from_contacts, container, false);
//        activity = (MainActivity) getActivity();
//        suggestedList = (RecyclerView) view.findViewById(R.id.suggested_list);
//        adapter = new AddFriendListAdapter(activity);
//
//        adapter.setOnItemClickListener(this);
//        suggestedList.setLayoutManager(new LinearLayoutManager(activity));
//        suggestedList.setHasFixedSize(true);
//        suggestedList.setAdapter(adapter);
//
//
//        getDeviceContacts();
//        matchContactToFaskUser();
//
//        if (getDialog() != null) {
//            //remove obnoxiously large title view from dialog..
//            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
//        }
//        // Inflate the layout for this fragment
//        return view;
//    }
//
//    private void getDeviceContacts() {
//        int read_contacts = 0;
//        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.READ_CONTACTS)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
//                    read_contacts);
//
//        } else {
//            contacts = ReadPhoneContacts(activity);
//        }
//    }
//
//
//    //check and match device phone numbers to numbers in database
//    private void matchContactToFaskUser() {
//        JSONArray contactJsonArray = new JSONArray();
//
//        //add all contacts to jsonarray
//        for (final SimpleContact contact : contacts) {
//            contactJsonArray.put(contact.getNumber());
//        }
//
//        JSONObject requestJson = new JSONObject();
//        try {
//            requestJson.put("Contacts", contactJsonArray);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        //send to server
//        HttpConnect.requestJsonPost("/contacts", requestJson.toString(), new HttpConnect.HttpResult<JSONObject>() {
//
//            @Override
//            public void onCallbackSuccess(JSONObject response) {
//                try {
//                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
//                    System.out.println(response.toString(4));
//                    //we want to add each user to the contacts list
//                    for (int i = 0; i < data.length(); i++) {
//                        JSONObject userInfo = data.getJSONObject(i);
//
//                        User user = new User(userInfo.getInt("UserID"), userInfo.getString("UserName"), new Date());
//                        user.setPhoneNumber(userInfo.getString("Phone"));
//                        adapter.addContact(user);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onCallbackError(String errorMsg, boolean fromServer) {
//
//            }
//        });
//
//
//    }
//
//
//    //This Context parameter is nothing but your Activity class's Context
//    public List<SimpleContact> ReadPhoneContacts(Context cntx) {
//        List<SimpleContact> contacts = new ArrayList<>();
//
//        Cursor cursor = cntx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
//        Integer contactsCount = 0;
//        try {
//            contactsCount = cursor.getCount(); // get how many contacts you have in your contacts list
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (contactsCount > 0) {
//
//            while (cursor.moveToNext()) {
//
//                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//
//                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//                    //the below cursor will give you details for multiple contacts
//                    Cursor pCursor = cntx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
//                            new String[]{id}, null);
//
//                    // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list
//                    while (pCursor.moveToNext()) {
//
//                        int phoneType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
//                        //String isStarred 		= pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));
//                        String phoneNo = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        //you will get all phone numbers according to it's type as below switch case.
//                        //Logs.e will print the phone number along with the name in DDMS. you can use these details where ever you want.
//                        switch (phoneType) {
//                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
//                                Log.e(contactName + ": TYPE_MOBILE", " " + phoneNo);
//                                contacts.add(new SimpleContact(contactName, phoneNo));
//                                break;
////                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
////                                Log.e(contactName + ": TYPE_HOME", " " + phoneNo);
////                                break;
////                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
////                                Log.e(contactName + ": TYPE_WORK", " " + phoneNo);
////                                break;
////                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
////                                Log.e(contactName + ": TYPE_WORK_MOBILE", " " + phoneNo);
////                                break;
////                            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
////                                Log.e(contactName + ": TYPE_OTHER", " " + phoneNo);
////                                break;
//                            default:
//                                break;
//                        }
//                    }
//                    pCursor.close();
//                }
//            }
//            cursor.close();
//        }
//
//        return contacts;
//    }
//
//    @Override
//    public void onItemClick(View view, int position) {
//        final User contact = adapter.getContact(position);
//        String msg = "Are you sure you want to add " + contact.getUsername() + " as a FAsk friend?";
//
//        DisplayAlert.displayCustomMessage(activity, msg, null
//                , new DisplayAlert.AlertDetails("Yes", "No"), new DisplayAlert.AlertCallback() {
//            @Override
//            public void onOk() {
//                sendFriendRequest(contact);
//            }
//
//            @Override
//            public void onCancel() {
//
//            }
//        });
//    }
//
//
//    private void sendFriendRequest(User contact) {
//        Log.e("SAM", Session.getInstance().getSessionID());
//        JSONObject requestData = new JSONObject();
//        try {
//            requestData.put("UserID", activity.getSessionUser().getuID());
//            requestData.put("FriendID", contact.getuID());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        HttpConnect.requestJsonPost("/friendship", requestData.toString(), new HttpConnect.HttpResult<JSONObject>() {
//
//            @Override
//            public void onCallbackSuccess(JSONObject response) {
//                try {
//                    JSONArray data = response.getJSONArray(HttpConnect.KEY_RESP_DATA);
//                    int result = response.getInt("FriendshipID");
//                    System.out.println(result);
//                    if (result == -1) {
//                        DisplayAlert.displayAlertError(activity, getString(R.string.general_error_msg), null);
//                    } else {
//                        DisplayAlert.displaySimpleMessage(activity, "Friend request was successfully sent", null);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onCallbackError(String errorMsg, boolean fromServer) {
//                if (fromServer) {
//                    DisplayAlert.displayAlertError(activity, errorMsg, null);
//                } else {
//                    DisplayAlert.displayAlertError(activity, getString(R.string.general_error_msg), null);
//                }
//            }
//        });
//
//    }
}
