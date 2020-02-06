package pl.snikk.multicooltikids;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.collection.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FindFriendDialogFragment extends DialogFragment {

    private String email = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Get the parent view of this dialog
        View view = inflater.inflate(R.layout.dialog_find_friend, null);

        ((TextView)view.findViewById(R.id.textView_found_friend_name)).setText(getArguments().getString("name"));
        ((TextView)view.findViewById(R.id.textView_found_friend_phone)).setText(getArguments().getString("phone"));

        email = getArguments().getString("email");

        try {
            ArrayList<JSONObject> kidsArray = (ArrayList<JSONObject>) getArguments().getSerializable("kids");

            LinearLayout container = (LinearLayout) view.findViewById(R.id.layout_findFriend_container);

            // Create a kid layout for every kid in this user's profile
            for (JSONObject kid : kidsArray) {
                try {
                    View kidView = inflater.inflate(R.layout.layout_kid_found, null);
                    ((TextView) kidView.findViewById(R.id.textView_kid_name)).setText(kid.getString("name"));

                    int age = Utils.dateToAge(kid.getString("birthday"));
                    String ageString = String.valueOf(age) + " " + (age == 1 ? getString(R.string.year_old) : getString(R.string.years_old));
                    ((TextView) kidView.findViewById(R.id.textView_kid_age)).setText(ageString);

                    String[] sex = getResources().getStringArray(R.array.sex_array);
                    ((TextView) kidView.findViewById(R.id.textView_kid_sex)).setText(sex[kid.getInt("sex")]);

                    ((TextView) kidView.findViewById(R.id.textView_kid_description)).setText(kid.getString("description"));

                    // Dynamically load the avatar if it's set
                    if (kid.getString("avatar").length() > 0) {
                        NetworkImageView avatar = (NetworkImageView) kidView.findViewById(R.id.imageView_kid);

                        RequestQueue mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

                        ImageLoader mImageLoader = new ImageLoader(mRequestQueue,
                                new ImageLoader.ImageCache() {
                                    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);

                                    @Override
                                    public Bitmap getBitmap(String url) {
                                        return cache.get(url);
                                    }

                                    @Override
                                    public void putBitmap(String url, Bitmap bitmap) {
                                        cache.put(url, bitmap);
                                    }
                                });
                        avatar.setBackground(null);
                        avatar.setImageUrl(Utils.URL + "avatars/" + kid.getString("avatar"), mImageLoader);
                    }

                    container.addView(kidView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (NullPointerException e) {
            Log.d("MCKids", "No kids found");
        }

        // Build the dialog
        builder.setView(view)
                .setTitle(R.string.found_friend)

                // Add action buttons
                .setPositiveButton(R.string.invite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getActivity(), ChooseVenueActivity.class);
                        intent.putExtra("email", email);
                        getActivity().startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.resign, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getActivity(), R.string.find_friend_resign, Toast.LENGTH_LONG).show();
                    }
                });

        return builder.create();
    }
}
