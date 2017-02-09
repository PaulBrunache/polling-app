package faskteam.faskandroid.entities;

import java.util.ArrayList;
import java.util.List;


public class Choice {
    /**
     * Hey.. i'm making a revision here.
     * I added a field for the id of the choice.
     */
    private String choice;
    private final int id;
    private int numberSelected;
    private List<User> whoAnswered;
    private boolean isFinalChoice;

    /**
     * Constructor for choice objects created from polls already existing in database.
     * @param choice
     * @param ID
     */
    public Choice(String choice, int ID){
        this.choice = choice;
        this.id = ID;
        this.numberSelected = 0;
        this.whoAnswered = new ArrayList<User>();
        this.isFinalChoice = false;
    }

    /**
     * Constructor for choice objects created as user is creating a new poll.
     * Field id will not be needed here since id will be assigned by the database.
     * @param choice
     */
    public Choice(String choice){
        this.choice = choice;
        this.id = -1;
        this.numberSelected = 0;
        this.whoAnswered = new ArrayList<User>();
        this.isFinalChoice = false;
    }


    //Again, I just generated all the getter/setters. We should
    //discuss how much we may need to limit these. Just a thought.
    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public List<User> getWhoAnswered() {
        return whoAnswered;
    }

    public void addWhoAnswered(User user) {
        this.whoAnswered.add(user);
    }

    public int getNumberSelected() { return numberSelected; }

    public void setNumberSelected(int numberSelected) {
        this.numberSelected = numberSelected;
    }

    public boolean isFinalChoice() {
        return isFinalChoice;
    }

    public void setFinalChoice(boolean isFinalChoice) {
        this.isFinalChoice = isFinalChoice;
    }

    public int getId() {
        return id;
    }
}
