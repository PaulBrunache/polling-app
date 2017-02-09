package faskteam.faskandroid.entities;

import android.support.annotation.Nullable;

import java.util.Date;
import java.util.HashMap;

public class User {

    private int uID;
    private String username;

    private Date lastLogin;
    private String phoneNumber = "";
    private String email = "";
    private String avatarURI = "";
    private boolean isFaceBookUser = false;

    private PointsObject pointsObject = new PointsObject(0,0);

    //using a group here as the friends list since it seems that
    //both a group and a friends list would function the same way..
    //so essentially, a friends list is a group who's members hashMap
    //contains all the users friends...
    //we can just change "friendsList" to a regular hashmap if need be later
    private Group friendsList = new Group("friends_list");
    private HashMap<String, Group> groups = new HashMap<>();

    public User() {}

    public User(int uID) {
        this(uID, null);
    }
    public User(int uID, @Nullable String username) {
        this(uID, username, null, null);
    }
    public User(int uID, @Nullable String username, @Nullable String email) {
        this(uID, username, email, null);
    }
    public User(int uID, String username, @Nullable Date lastLogin) {
        this(uID, username, null, lastLogin);
        Date ls = getLastLogin();
    }

    public User(int uID, String username, String email, @Nullable Date lastLogin) {
        this.uID = uID;
        this.username = username;
        this.lastLogin = lastLogin;
        this.email = email;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public int getuID() {
        return uID;
    }

    public void setuID(int id) {
        this.uID = id;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarURI() {
        return avatarURI;
    }

    public void setAvatarURI(String avatarURI) {
        this.avatarURI = avatarURI;
    }

    public void addFriend(Friend f) {
        friendsList.addMember(f.getNickname(), f);
    }

    public boolean isFaceBookUser() {
        return isFaceBookUser;
    }

    public void setIsFaceBookUser(boolean isFaceBookUser) {
        this.isFaceBookUser = isFaceBookUser;
    }

    public void setPointsObject(PointsObject pointsObject) {
        this.pointsObject = pointsObject;
    }

    public PointsObject getPointsObject() {
        return pointsObject;
    }

    public static class PointsObject {
        private int answerPoints = 0;
        private int askPoints = 0;

        public PointsObject(int answerPoints, int askPoints) {
            this.answerPoints = answerPoints;
            this.askPoints = askPoints;
        }


        public int getAnswerPoints() {
            return answerPoints;
        }

        public int getAskPoints() {
            return askPoints;
        }
    }
}
