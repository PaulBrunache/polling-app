package faskteam.faskandroid.entities;

/**
 * will use at some point when we implement session IDs in the future
 */
public class Session {

//    TODO check into auth header for session ID

    private static Session session;
    private final String SESSION_ID;

    private final User S_USER;
//
//    private static Session session;
//
    private Session(String sessionID, User sUser) {
        this.SESSION_ID = sessionID;
        this.S_USER = sUser;
    }

    public static Session getInstance() {
        return session;
    }

    public static boolean hasInstance() {
        return session != null;
    }

    public static boolean createInstance(String sessionID, User sUser) {
        if (session == null) {
            session = new Session(sessionID, sUser);
            return true;
        }
        return false;
    }

    public static void endSession() {
        session = null;
    }

    public String getSessionID() {
        return SESSION_ID;
    }

    public User getUser() {
        return S_USER;
    }

}
