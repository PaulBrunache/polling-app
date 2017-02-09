package faskteam.faskandroid.entities;

import java.util.HashMap;


public class FriendsList extends Group {

    public FriendsList(String name, HashMap<String, Friend> members) {
        super(name, members);
    }

    public FriendsList(String name) {
        super(name);
    }
}
