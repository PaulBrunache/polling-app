package faskteam.faskandroid.entities;

import java.util.HashMap;

public class Group {

    private String name;
    private HashMap<String, Friend> members;
    private int groupID;

    public Group(String name, HashMap<String, Friend> members) {
        this.name = name;
        this.members = members;
    }
    public Group(String name) {
        this.name = name;
        this.members = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    /***
     * Store an entire group members map to the members field
     * @param members HashMap
     */
    public void setMembers(HashMap<String, Friend> members) {
        this.members = members;
    }

    /***
     * Retrieve one single member from Group object
     * @param nickname String
     * @return Friend object who's nickname matches parameter
     * or null if object does not exist
     */
    public Friend getMember(String nickname) {
        return members.get(nickname);
    }

    public int getGroupSize() { return members.size(); }

    /***
     * Adds member to group members hashmap using the nickname parameter
     * as the key.
     * @param nickname String
     * @param member Friend
     */
    public void addMember(String nickname, Friend member) {
        members.put(nickname, member);
    }

    /***
     * Checks if the given friend nickname exists in the members map
     * @param nickname String
     * @return True if the map contains the key or False otherwise
     */
    public boolean checkMember(String nickname) {
        return members.containsKey(nickname);
    }

    /***
     * Removes the desired friend object from the group using the
     * nickname as the key
     * @param nickname String
     * @return The removed Friend object or null if the key does not exist
     * in the map
     */
    public Friend removeMember (String nickname) {
        return members.remove(nickname);
    }

    public void emptyGroup () {
        members = new HashMap<>();
    }

    public void setGroupID(int gID) { this.groupID = gID; }

    public int getGroupID() { return groupID; }
}
