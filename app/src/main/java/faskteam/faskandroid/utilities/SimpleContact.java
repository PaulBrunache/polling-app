package faskteam.faskandroid.utilities;


public class SimpleContact {
    private String name;
    private String number;

    public SimpleContact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormattedNumber() {
        return number;
    }

    public String getNumber() {
        return number.replaceAll("[^0-9]", "");
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
