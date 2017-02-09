package faskteam.faskandroid.entities;


public enum ChoiceType {

    RADIO, CHECKLIST, RATE, MULTIMEDIA;

    private static final String RADIO_STRING = "Radio";
    private static final String CHECKLIST_STRING = "Checklist";
    private static final String RATE_STRING = "Rate";
    private static final String MULTIMEDIA_STRING = "Multimedia";

    public static ChoiceType checkType(String type) {
        switch (type) {
            case RADIO_STRING:
                return RADIO;
            case CHECKLIST_STRING:
                return CHECKLIST;
            case RATE_STRING:
                return RATE;
            case MULTIMEDIA_STRING:
                return MULTIMEDIA;
            default:
                return RADIO;
        }
    }

    public String toString() {
        switch (this) {
            case RADIO:
                return RADIO_STRING;
            case CHECKLIST:
                return CHECKLIST_STRING;
            case RATE:
                return RATE_STRING;
            case MULTIMEDIA:
                return MULTIMEDIA_STRING;
            default:
                return RADIO_STRING;
        }
    }
}
