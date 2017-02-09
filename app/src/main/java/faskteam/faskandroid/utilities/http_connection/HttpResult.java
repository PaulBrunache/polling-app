package faskteam.faskandroid.utilities.http_connection;



public interface HttpResult<T> {

    void onCallback(T response, boolean success);

}
