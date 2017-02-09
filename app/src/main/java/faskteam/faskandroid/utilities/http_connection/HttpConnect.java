package faskteam.faskandroid.utilities.http_connection;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import faskteam.faskandroid.R;
import faskteam.faskandroid.contollers.main_controllers.MainActivity;
import faskteam.faskandroid.contollers.main_controllers.MyApplication;
import faskteam.faskandroid.utilities.DisplayAlert;



/**
 * Creates a singleton class using the android/google volley library. A RequestQueue object
 * is created to queue up the various requests passed to this class.
 * <br>
 * Constructor is private so it cannot be called outside of this class. The reason for this
 * is because we only want one object of this classes to be used throughout the entire
 * application lifecycle. For that to happen, the object has to be instantiated with the
 * Application Context.
 * <p/>
 * <p><a href="https://developer.android.com/training/volley/index.html">Volley Tutorial</a></p>
 * <p><a href="http://afzaln.com/volley/">Volley Documentation</a></p>
 */
public class HttpConnect {

    private static HttpConnect mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    public static final int MY_SOCKET_TIMEOUT_MS = 120000;

    private static final String CLASS_TAG = "HttpConnect";
    public static final String URL = "http://www.peacockpi.us:81/cgi-bin/FAsk/fask_rest/fask.py";
    public static final String KEY_RESP_ERROR = "Error";
    public static final String KEY_RESP_ERROR_MSG = "ErrorMessage";
    public static final String KEY_RESP_DATA = "Data";

    private HttpConnect() {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        mRequestQueue = Volley.newRequestQueue(MyApplication.getAppContext(), new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                System.setProperty("http.keepAlive", "false");
                return super.createConnection(url);
            }
        });

        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap>
                    cache = new LruCache<String, Bitmap>(20);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });


    }

    public static synchronized HttpConnect getInstance() {
        if (mInstance == null) {
            mInstance = new HttpConnect();
            //TODO Remove.. check to make sure only one object is made throughout the application
            //lifecycle
            Log.i(CLASS_TAG, "creating new instance");
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public ImageLoader getmImageLoader() {
        return mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


    /**
     * Sends a GET request to the passed in url parameter. Gets data from
     * url server and sends a call back response to calling class in the form of T, where T
     * is the class parameter from {@link HttpConnect.HttpResult&lt;T&gt;
     *
     *
     * @param url           Url String
     * @param callback      HttpResult&lt;{@link JSONObject}&gt; interface method that is passed back the return data and
     *                      a boolean upon request completion. Boolean will be true if connection
     *                      and data transfer is successful, or false otherwise.
     */
    public static void requestJsonGet(String url, HttpConnect.HttpResult<JSONObject> callback) {
        requestJson(url, Request.Method.GET, null, null, callback);
    }

    public static void requestJsonGet(String url, HttpConnect.HttpResult<JSONObject> callback, Map<String, String> headers) {
        requestJson(url, Request.Method.GET, null, headers, callback);
    }

    public static void requestJsonPost(String url, String data, HttpConnect.HttpResult<JSONObject> callback) {
        requestJson(url, Request.Method.POST, data, null, callback);
    }

    public static void requestJsonPut(String url, String data, HttpConnect.HttpResult<JSONObject> callback) {
        requestJson(url, Request.Method.PUT, data, null, callback);
    }

    public static void requestJsonDelete(String url, HttpConnect.HttpResult<JSONObject> callback) {
        requestJson(url, Request.Method.DELETE, null, null, callback);
    }

    /**
     * Sends a request to the desired url using the passed in request method. Gets data from
     * url in the form of a {@link JSONObject}.
     * If json data needs to be sent in the request body, convert to string before passing into
     * data parameter.
     *
     * This method expects that the server will send a response that includes an error check value.
     * With this value, each calling class no longer has to check for errors on their own. We can
     * check here and send a response to the {@link HttpConnect.HttpResult} callback object.
     * <p/>
     * For GET requests use {@link #requestJsonGet(String, HttpResult)} method.
     * <br/>
     * For POST requests use {@link #requestJsonPost(String, String, HttpResult)} method.
     * <br/>
     * For PUT requests use {@link #requestJsonPut(String, String, HttpResult)} method.
     * <br/>
     * For DELETE requests use {@link #requestJsonDelete(String, HttpResult)} method.
     * <p/>
     *
     * @param url           Url String
     * @param requestMethod reference Request.Method interface in Request class
     * @param data          Data String
     * @param callback      {@link HttpResult}&lt;{@link JSONObject}&gt; interface method that is passed back the return data and
     *                      a boolean upon request completion. Boolean will be true if connection
     *                      and data transfer is successful, or false otherwise.
     */
    public static void requestJson(String url, int requestMethod, String data, Map<String, String> headers, final HttpConnect.HttpResult<JSONObject> callback) {

        Log.i(CLASS_TAG, "sending to: " + URL + url);

        MyJsonObjectRequest request = new MyJsonObjectRequest(requestMethod, URL + url, data, headers, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(CLASS_TAG, "response: " + response.toString());
                /* the server should be giving us a dictionary that has an error
                * check value... we will send an error call back to the calling
                * class if we received an error... otherwise send the data*/
                try {
                    int errorVal = response.getInt(KEY_RESP_ERROR);

                    switch (errorVal) {
                        case 0:
                            //no error
                            callback.onCallbackSuccess(response);
                            break;
                        case 2:
                            //session not found
                            Activity activity = MainActivity.getInstance();
                            if (activity != null) {
                                showSessionErrorDialog(activity);
                            }
                            break;
                        case 3:
                            //error is suitable for users to read
                            callback.onCallbackError(response.getString(KEY_RESP_ERROR_MSG), true);
                            break;
                        default:
                            callback.onCallbackError(MyApplication.getAppContext().getString(R.string.general_error_msg), false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(CLASS_TAG, "" + error.toString());
                callback.onCallbackError(MyApplication.getAppContext().getString(R.string.general_error_msg), false);
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //add request to queue
        getInstance().getRequestQueue().add(request);
    }

    /**
     *
     * @param activity
     */
    private static void showSessionErrorDialog(final Activity activity) {
        String msg = "Sorry. Your session has expired. Please sign in to FAsk to continue.";
        DisplayAlert.AlertDetails buttonNames = new DisplayAlert.AlertDetails("Ok", "");
        DisplayAlert.displayCustomMessage(activity, msg, "Oops", buttonNames, new DisplayAlert.AlertCallback() {
            @Override
            public void onPositive(DialogInterface dialog) {

            }

            @Override
            public void onNegative(DialogInterface dialog) {

            }

            @Override
            public void onAny(DialogInterface dialog) {
                activity.finish();
                dialog.dismiss();
            }
        });
    }

    /**
     * Callback object where results of http connection are sent.
     * <p/>
     * If the Http connection is successful and there is no error from the server,
     * {@link #onCallbackSuccess(Object)} will be called and have the data portion of the response
     * object returned.
     * <p/>
     * If there is an error, {@link #onCallbackError(String, boolean)}
     * will be called, where the boolean indicates if the error message stored in the
     * String is from the server (True) or not (False)
     * <p/>
     *
     * Expected
     *
     *
     * @param <T> extends Object.. The response object usually of type {@link JSONObject}
     */
    public interface HttpResult<T> {
        void onCallbackSuccess(T response);
        void onCallbackError(String errorMsg, boolean fromServer);
    }

}
