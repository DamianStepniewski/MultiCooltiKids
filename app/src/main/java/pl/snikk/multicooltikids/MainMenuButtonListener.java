package pl.snikk.multicooltikids;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainMenuButtonListener implements View.OnClickListener{

    private Context ctx;

    public MainMenuButtonListener(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        SharedPreferences data = ctx.getSharedPreferences("data", ctx.MODE_PRIVATE);

        if (!data.getBoolean("isLoggedIn", false))
            Toast.makeText(ctx, R.string.not_logged_in, Toast.LENGTH_SHORT).show();
        else {
            if (id == R.id.button_messages)
                onMessagesClick();
            else if (id == R.id.button_friends)
                onFriendsClick();
            else if (id == R.id.button_newFriend)
                onNewFriendClick();
            else if (id == R.id.button_venues)
                onVenuesClick();
            else if (id == R.id.button_next)
                onNextPlaceClick();
            else if (id == R.id.button_meetings)
                onMyMeetingsClick();
        }
    }

    private void onMessagesClick() {
        Intent intent = new Intent(ctx, MessageActivity.class);
        ctx.startActivity(intent);
    }

    private void onFriendsClick() {
        Intent intent = new Intent(ctx, FriendsActivity.class);
        ctx.startActivity(intent);
    }

    private void onNewFriendClick() {
        SharedPreferences data = ctx.getSharedPreferences("data", ctx.MODE_PRIVATE);
        if (data.getBoolean("status", false)) {
            ConnectionHandler ch = new ConnectionHandler(ctx);
            ch.findFriend(data.getString("email", ""), data.getString("ssid", ""));
        } else
            Toast.makeText(ctx, R.string.friend_status_error, Toast.LENGTH_LONG).show();
    }

    private void onVenuesClick() {
        Intent intent = new Intent(ctx, VenuesActivity.class);
        ctx.startActivity(intent);
    }

    private void onNextPlaceClick() {
        SharedPreferences data = ctx.getSharedPreferences("data", ctx.MODE_PRIVATE);
        ConnectionHandler ch = new ConnectionHandler(ctx);
        ch.getNearestVenue(data.getString("email", ""), data.getString("ssid", ""));
    }

    private void onMyMeetingsClick() {
        Intent intent = new Intent(ctx, MeetingsActivity.class);
        ctx.startActivity(intent);
    }
}

