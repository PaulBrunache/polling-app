package faskteam.faskandroid.entities;

import java.util.Date;

public class Friend {

    private User friend;
    private String nickname;
    private Date dateAdded;
    private int friendshipID;

    public Friend(User friend, String nickname, Date dateAdded) {
        this.friend = friend;
        this.nickname = nickname;
        this.dateAdded = dateAdded;
    }

    //if user adds a friend without a nickname we want to use the
    //username of the friend
    public Friend(User friend) {
        this.friend = friend;
        this.nickname = friend.getUsername();
        dateAdded = new Date();
    }


    public User getUser() {
        return friend;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setFriendshipID(int friendshipID) {
        this.friendshipID = friendshipID;
    }

    public int getFriendshipID() {
        return friendshipID;
    }
}
