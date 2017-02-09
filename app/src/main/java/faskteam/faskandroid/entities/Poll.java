package faskteam.faskandroid.entities;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Poll {

    private static final String CLASS_TAG = Poll.class.getSimpleName();

    private int creatorId;
    private String creatorUsername;

    private final int id;
    private String question;
    private List<Choice> choices;
    private boolean favorite;
    private ChoiceType choiceType;
    private boolean openChoice;
    private boolean userClosed;
    private boolean autoClosed;
    private boolean isOpen;
    private int numberOfChoices;
    private boolean isPollLocatable;
    //Just using Android location for now should consider Google's location service
    private Location pollLocation;
    private Date pollDateAdded;
    private Date pollDuration;
    private Date closeDate;
    private boolean isPublic = false;

    public Poll(){
        this.id = -1;
        this.question = null;
        this.choices = new ArrayList<Choice>();
        this.favorite = false;
        this.choiceType = ChoiceType.RADIO;
        this.openChoice = false;
        this.userClosed = false;
        this.autoClosed = false;
        this.isOpen = false;
        this.isPollLocatable = false;
        this.pollLocation = null;
        this.pollDateAdded = new Date();
        this.pollDuration = new Date(this.pollDateAdded.getMinutes()+60);
    }

    public Poll(int id, String question, ArrayList<Choice> choices, ChoiceType choiceType, boolean openChoice, int creatorId, boolean isOpen) {
        this(id, question, choices, choiceType, openChoice, creatorId, null, isOpen);
    }

    public Poll(int id, String question, ArrayList<Choice> choices, ChoiceType choiceType, boolean openChoice, int creatorId, String creatorUsername, boolean isOpen) {
        this.creatorId = creatorId;
        this.creatorUsername = creatorUsername;
        this.id = id;
        this.question = question;
        this.choices = choices;
        this.choiceType = choiceType;
        this.openChoice = openChoice;
        this.favorite = false;
        this.userClosed = false;
        this.autoClosed = false;
        this.isOpen = isOpen;
        this.isPollLocatable = false;
        this.pollLocation = null;
        this.pollDateAdded = new Date();
        this.pollDuration = new Date(this.pollDateAdded.getMinutes()+60);
    }

    //I just generated all the getter/setters obviously we won't use them all
    //or even need to use them all. We should probably discuss further what needs to
    //be kept and not.
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(String choice) {

        this.choices.add(new Choice(choice));
        this.numberOfChoices++;
    }

    public void setChoices(Choice choice) {
        this.choices.add(choice);
        this.numberOfChoices++;
    }

    public void removeChoices(Choice choice) {
        this.choices.remove(choice);
    }

    public int numberOfChoices() { return numberOfChoices; }

    public String getChoiceTextByIndex(int index) {
        Choice choice = this.choices.get(index);
        return choice.getChoice();
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public ChoiceType getChoiceType() {
        return choiceType;
    }

    public void setChoiceType(ChoiceType choiceType) {
        this.choiceType = choiceType;
    }

    public boolean isOpenChoice() {
        return openChoice;
    }

    public void setOpenChoice(boolean openChoice) {
        this.openChoice = openChoice;
    }

    public boolean isUserClosed() {
        return userClosed;
    }

    public void setUserClosed(boolean userClosed) {
        this.userClosed = userClosed;
    }

    public boolean isAutoClosed() {
        return autoClosed;
    }

    public void setAutoClosed(boolean autoClosed) {
        this.autoClosed = autoClosed;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void selectBestChoice(Choice choice) {
        choice.setFinalChoice(true);
    }

    public Date getPollDateAdded() { return this.pollDateAdded; }

    public void setPollDateAdded(Date time) { this.pollDateAdded = time; }

    public Date getPollDuration() { return this.pollDuration; }

    public void setPollDuration(int minutes) {
        this.pollDuration = new Date(this.pollDateAdded.getMinutes()+minutes);
    }

    public boolean isPollLocatable() { return this.isPollLocatable; }

    public void setPollLocatable(boolean locatable) { this.isPollLocatable = locatable; }

    public Location getPollLocation() { return this.pollLocation; }

    public void setPollLocation(String provider) {
        this.pollLocation = new Location(provider);
    }

    public void setPollLocation(Location location) {
        this.pollLocation = location;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public int getId() {
        return id;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String username) {
        this.creatorUsername = username;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public String getTimeRemaining() {

        if(!isOpen()) {
            Log.i(CLASS_TAG, "!isOpen");
            return "Closed";
        }
        //update poll remaining time
        Date now = Calendar.getInstance().getTime();
        try {
            long timeRemaining = closeDate.getTime() - now.getTime();
            Log.i(CLASS_TAG, "time remaining: " + timeRemaining);

            if (timeRemaining > 0) {
                long time;
                String unit;
                if(timeRemaining <= 3600000) {
                    //60 min or less
                    time = TimeUnit.MILLISECONDS.toMinutes(timeRemaining);
                    unit = "min";
                } else if (timeRemaining <= 216000000) {
                    //23 hrs or less
                    time = TimeUnit.MILLISECONDS.toHours(timeRemaining);
                    unit = "hrs";
                } else {
                    //days
                    time = TimeUnit.MILLISECONDS.toDays(timeRemaining);
                    unit = "days";
                }

                return String.format("%s %s", Long.toString(time), unit);

            } else {
                return "Closed";
            }
        } catch (Exception e) {
            Log.e(CLASS_TAG, "" + e.getMessage());
            return "Closed";
        }

    }
}
