package pl.snikk.multicooltikids;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordChangeDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(view)
                .setTitle("Change password")

                // Add action buttons
                .setPositiveButton(R.string.accept, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PasswordChangeDialogFragment.this.getDialog().cancel();
                    }
                });

        final AlertDialog mAlertDialog = builder.create();

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            // Get the accept button and override its listener so the dialog isn't dismissed every time the button is pressed
            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // Get all the textfields' values
                        String password = ((EditText) view.findViewById(R.id.editText_dialog_newPassword)).getText().toString();
                        String password2 = ((EditText) view.findViewById(R.id.editText_dialog_newPassword2)).getText().toString();

                        // Check if passwords are long enough
                        if (password.length() > 5 && password2.length() > 5) {
                            // Check if passwords match
                            if (password.equals(password2)) {

                                // Send the request
                                SharedPreferences data = getActivity().getSharedPreferences("data", getActivity().MODE_PRIVATE);
                                ConnectionHandler ch = new ConnectionHandler(getActivity());
                                ch.changePassword(password, data.getString("email", ""), data.getString("ssid", ""));
                                mAlertDialog.dismiss();
                            } else
                                // Passwords don't match
                                Toast.makeText(getActivity(), R.string.passwords_dont_match, Toast.LENGTH_SHORT).show();
                        } else
                            // Passwords are too short
                            Toast.makeText(getActivity(), R.string.password_length_invalid, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return mAlertDialog;
    }
}
