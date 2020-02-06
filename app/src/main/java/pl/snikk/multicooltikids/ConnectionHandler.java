package pl.snikk.multicooltikids;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConnectionHandler {

    private Context ctx;
    private RequestQueue queue;

    public ConnectionHandler(Context ctx) {
        this.ctx = ctx;
        this.queue = Volley.newRequestQueue(ctx.getApplicationContext());

    }

    // Register a new user
    public void register(String name, String email, String password, String phone) {
        Log.d("d", name);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("do", "register");
        params.put("name", name);
        params.put("email", email);
        params.put("pass", password);
        params.put("phone", phone);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void logIn(String email, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "login");
        params.put("email", email);
        params.put("pass", password);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void checkForUpdates(String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "update");
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void changePassword(String pass, String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "changePass");
        params.put("pass", pass);
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void changeEmail(String newEmail, String oldEmail, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "changeEmail");
        params.put("newEmail", newEmail);
        params.put("email", oldEmail);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void changeName(String name, String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "changeName");
        params.put("name", name);
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void changePhone(String phone, String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "changePhone");
        params.put("phone", phone);
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void addKid(String email, String ssid, String name, String birthday, int sex, String description, String image) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "addKid");
        params.put("email", email);
        params.put("ssid", ssid);
        params.put("name", name);
        params.put("birthday", birthday);
        params.put("sex", String.valueOf(sex));
        params.put("description", description);
        params.put("image", image);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void getKids(String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "getKids");
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void removeKid(String email, String ssid, String kid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "removeKid");
        params.put("email", email);
        params.put("ssid", ssid);
        params.put("kid", kid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void setPosition(String email, String ssid, String lat, String lng) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "setPosition");
        params.put("email", email);
        params.put("ssid", ssid);
        params.put("lat", lat);
        params.put("long", lng);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void getVenues() {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "getVenues");
        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_VENUES);
    }

    public void findFriend(String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "findFriend");
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void getNearestVenue(String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "findVenue");
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void addMeeting(String fromEmail, String toEmail, long timestamp, String name, String address, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "addMeeting");
        params.put("fromEmail", fromEmail);
        params.put("toEmail", toEmail);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("name", name);
        params.put("address", address);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void acceptMeeting(int id, String ssid, String email) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "acceptMeeting");
        params.put("email", email);
        params.put("ssid", ssid);
        params.put("id", String.valueOf(id));

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void declineMeeting(int id, String ssid, String email) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "declineMeeting");
        params.put("email", email);
        params.put("ssid", ssid);
        params.put("id", String.valueOf(id));

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void getMeetings(String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "getMeetings");
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_MEETINGS);
    }

    public void getFriends(String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "getFriends");
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_FRIENDS);
    }

    public void getMessages(String email, String ssid, int uid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "getMessages");
        params.put("email", email);
        params.put("ssid", ssid);
        params.put("uid", String.valueOf(uid));

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_MESSAGE);
    }

    public void checkNewMessages(String email, String ssid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "checkNewMessages");
        params.put("email", email);
        params.put("ssid", ssid);

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_SERVER);
    }

    public void sendMessage(String email, String ssid, String message, int uid) {
        HashMap<String, String> params = new HashMap<>();
        params.put("do", "sendMessage");
        params.put("message", message);
        params.put("email", email);
        params.put("ssid", ssid);
        params.put("to_uid", String.valueOf(uid));

        sendRequest(params, Utils.SERVER_URL, Utils.REQUEST_MESSAGE);
    }

    private void sendRequest(HashMap<String, String> params, String url, final String broadcastName) {

        // Create the request object
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, params, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                // Send the response back to the activity
                Intent intent = new Intent(broadcastName);
                intent.putExtra("json", response);
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError response) {
                Toast.makeText(ctx, response.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Queue our request
        queue.add(jsObjRequest);
    }

    // Custom Volley request class allowing us to send parameters via POST and receive as simple String
    private class CustomRequest extends Request<String>{

        private Response.Listener<String> listener;
        private Map<String, String> params;

        public CustomRequest(String url, Map<String, String> params,
                             Response.Listener<String> reponseListener, Response.ErrorListener errorListener) {
            super(Method.GET, url, errorListener);
            this.listener = reponseListener;
            this.params = params;
        }

        public CustomRequest(int method, String url, Map<String, String> params,
                             Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.listener = responseListener;
            this.params = params;
        }

        @Override
        protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
            return params;
        };

        @Override
        protected void deliverResponse(String response) {
            listener.onResponse(response);
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(jsonString,
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            }
        }
    }

    public class Kid {
        public String name;
        public Date birthday;
        public int sex;
        public String description;
        public Bitmap avatar;
    }
}
