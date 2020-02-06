package pl.snikk.multicooltikids;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.Date;

public class AddKidDialogFragment extends DialogFragment {

    // A unique int value to identify the response from gallery when selecting an avatar
    private static final int ACTIVITY_SELECT_IMAGE = 27364;

    // The parent view of this dialog
    View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Get the parent view of this dialog
        view = inflater.inflate(R.layout.dialog_kid, null);

        // Get the datepicker
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.datePicker);
        datePicker.setCalendarViewShown(false);

        // Set max date as current date and min date as current date - 18 years
        datePicker.setMinDate(new Date().getTime()-568024668000l);
        datePicker.setMaxDate(new Date().getTime());

        // Build the dialog
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = ((EditText)view.findViewById(R.id.editText_kid_name)).getText().toString();
                        long sex = ((Spinner)view.findViewById(R.id.spinner_kid_sex)).getSelectedItemId();
                        String description = ((EditText)view.findViewById(R.id.editText_kid_description)).getText().toString();
                        String date = datePicker.getYear()+"-"+datePicker.getMonth()+"-"+datePicker.getDayOfMonth();

                        if (name.length() > 0) {
                            // Get the avatar and convert it to base64 string
                            ImageView avatar = (ImageView) view.findViewById(R.id.imageView_kid_avatar);
                            String encodedImage = "";
                            try {
                                Drawable d = avatar.getDrawable();
                                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                                ByteArrayOutputStream baOs = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baOs);
                                byte[] imageBytes = baOs.toByteArray();
                                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                            } catch (NullPointerException e) {

                            }

                            SharedPreferences data = getActivity().getSharedPreferences("data", getActivity().MODE_PRIVATE);
                            String email = data.getString("email", "");
                            String ssid = data.getString("ssid", "");

                            ConnectionHandler ch = new ConnectionHandler(getActivity());
                            ch.addKid(email, ssid, name, date, (int)sex, description, encodedImage);
                        } else
                            Toast.makeText(getActivity(), R.string.name_empty, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddKidDialogFragment.this.getDialog().cancel();
                    }
                });

        // Get the avatar image/button and add an onclick listener
        ImageView avatar = (ImageView) view.findViewById(R.id.imageView_kid_avatar);
        avatar.setOnClickListener(new AvatarClickListener());

        // Get the sex spinner and apply sex choices from the string array resource
        Spinner sexSpinner = (Spinner) view.findViewById(R.id.spinner_kid_sex);
        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sex_array, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(sexAdapter);

        return builder.create();
    }

    private class AvatarClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // Launch a gallery app which lets the user select an avatar
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
        }
    }

    // Handle image selection data returned by the gallery app
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check if we're getting correct response
        if( requestCode == ACTIVITY_SELECT_IMAGE) {
            // Do we even have correct URI?
            try {
                // Get the URI of selected image
                Uri selectedImage = data.getData();
                // Create a bitmap with the URI, scale down to 200px
                Bitmap img = decodeUri(selectedImage);

                // Get the avatar imageView and apply the bitmap
                ImageView avatar = (ImageView) view.findViewById(R.id.imageView_kid_avatar);
                avatar.setImageBitmap(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Create a bitmap out of the supplied URI, scaling it down to 200px
    // From: http://stackoverflow.com/a/5086706
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 200;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o2);

    }
}
