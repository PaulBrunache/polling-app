package faskteam.faskandroid.entities;

public class FacebookUser extends User {
    public static final String CLASS_TAG = FacebookUser.class.getSimpleName();

    private String firstName;
    private String lastName;
    private String fbId;

    public FacebookUser() {
        setIsFaceBookUser(true);
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }
}
