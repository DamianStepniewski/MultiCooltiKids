package pl.snikk.multicooltikids;


import android.app.Activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
    public static final String URL = "http://multicoolti.neth10.edl.pl/";

    public static final String REQUEST_SERVER = "jsonRequest";
    public static final String REQUEST_VENUES = "venuesRequest";
    public static final String REQUEST_MEETINGS = "meetingsRequest";
    public static final String REQUEST_FRIENDS = "friendsRequest";
    public static final String REQUEST_MESSAGE = "messageRequest";

    public static final String SERVER_URL = "http://multicoolti.neth10.edl.pl/index.php";

    // A static reference to currently used (on top) activity, used by the UpdateCheckerService
    private static Activity currentActivity = null;

    // Rebuild the input string from scratch to get rid of all of the trash and BOM
    public static String rebuildString(String input) {
        char[] utf8 = null;
        StringBuilder properString = new StringBuilder("");

        utf8 = input.toCharArray();

        for (int i = 0; i < utf8.length; i++) {
            if ((int) utf8[i] < 65000) {
                properString.append(utf8[i]);
            }
        }
        return properString.toString();
    }

    public static int dateToAge(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date birthday = dateFormat.parse(dateString);
            Date now = new Date();

            return getDiffYears(birthday, now);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    // Set the currently used activity
    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    // Returns the currently used activity
    public static Activity getCurrentActivity() {
        return currentActivity;
    }
}
