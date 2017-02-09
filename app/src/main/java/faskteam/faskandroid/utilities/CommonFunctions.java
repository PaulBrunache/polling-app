package faskteam.faskandroid.utilities;

import android.content.Context;
import android.util.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.Source;


public final class CommonFunctions {

    private static final String CLASS_TAG = "CommonFunctions";

    private CommonFunctions(){}

    public static Date stringToDate(String s) {
        Date d = null;

        //2015-10-22T00:00:00
        s = s.replace("T", " ");
        DateFormat df = new SimpleDateFormat("yyyy-MM-d H:m:s", Locale.ENGLISH);
        try {
            d = df.parse(s);
            Calendar cal = Calendar.getInstance();

            //todo remove
            cal.setTime(d);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static String dateToString(Date d) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-d H:m:s", Locale.ENGLISH);
        Log.i(CLASS_TAG, "dateToString");
        return df.format(d);
    }

    public static String removeWhiteSpace(String s) {
        return s.replaceAll("\\s+", "");
    }

    public static boolean checkWhiteSpace(String s) {
        return s.matches("\\s");
    }

    public static int scaleToDisplay (Context context, float desiredDP) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (scale * desiredDP + 0.5f);
    }

    public static String URLEncode(String s) {
        try {
            s = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

}
