package faskteam.faskandroid.utilities.http_connection;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import faskteam.faskandroid.entities.Session;


public class MyJsonObjectRequest extends Request<JSONObject> {
    public static final String CLASS_TAG = "MyJsonObjectRequest";

    /** Default charset for JSON request. */
    protected static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private final Response.Listener<JSONObject> mListener;
    private final String mRequestBody;
    private Map<String, String> mHeaders = new HashMap<>();

    public MyJsonObjectRequest(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        this(method, url, null, listener, errorListener);
    }

    public MyJsonObjectRequest(int method, String url, String data, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        this(method, url, data, null,  listener, errorListener);
    }

    public MyJsonObjectRequest(int method, String url, String data, Map<String, String> headers, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        mRequestBody = data;

        if (headers != null) {
            mHeaders = headers;
        }

        //add sessionID to headers
        if (Session.hasInstance()) {
            mHeaders.put("Session", Session.getInstance().getSessionID());
        }
    }

//    public static Map<String, String> jsonToMap(JSONObject json) throws JSONException {
//        Map<String, String> retMap = new HashMap<>();
//
//        if(json != JSONObject.NULL) {
//            retMap = toMap(json);
//        }
//        return retMap;
//    }
//
//    public static Map<String, String> toMap(JSONObject object) throws JSONException {
//        Map<String, String> map = new HashMap<>();
//
//        Iterator<String> keysItr = object.keys();
//        while(keysItr.hasNext()) {
//            String key = keysItr.next();
//            Object value = object.get(key);
//
//            if(value instanceof JSONArray) {
//                value = toList((JSONArray) value);
//            }
//
//            else if(value instanceof JSONObject) {
//                value = toMap((JSONObject) value);
//            }
//            map.put(key, value.toString());
//        }
//        return map;
//    }
//
//    public static List<Object> toList(JSONArray array) throws JSONException {
//        List<Object> list = new ArrayList<>();
//        for(int i = 0; i < array.length(); i++) {
//            Object value = array.get(i);
//            if(value instanceof JSONArray) {
//                value = toList((JSONArray) value);
//            }
//
//            else if(value instanceof JSONObject) {
//                value = toMap((JSONObject) value);
//            }
//            list.add(value);
//        }
//        return list;
//    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            Log.i("Request2", "" + jsonString);
            Log.i("REQUEST", "" + response.headers);
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            Log.e("Request-UnSupEnc", "" + e.getMessage());
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            Log.e("Request-Json", "" + je.getMessage());
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return super.getParams();
    }

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
//        return super.getHeaders();
        return mHeaders;
    }
}
