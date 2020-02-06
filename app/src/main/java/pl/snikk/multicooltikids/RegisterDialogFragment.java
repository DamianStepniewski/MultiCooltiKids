package pl.snikk.multicooltikids;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_register, null);
        builder.setView(view)
                .setTitle(R.string.register)
                // Add action buttons
                .setPositiveButton(R.string.accept, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RegisterDialogFragment.this.getDialog().cancel();
                    }
                });

        final AlertDialog mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            // Get the accept button and override its listener so the dialog isn't dismissed every time the button is pressed
            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    // Perform all of the registering prechecks here
                    @Override
                    public void onClick(View v) {
                        // Get all the textfields' values
                        String name = ((EditText) view.findViewById(R.id.editText_register_name)).getText().toString();
                        String email = ((EditText) view.findViewById(R.id.editText_register_email)).getText().toString();
                        String password = ((EditText) view.findViewById(R.id.editText_register_password)).getText().toString();
                        String password2 = ((EditText) view.findViewById(R.id.editText_register_password2)).getText().toString();
                        String phone = ((EditText) view.findViewById(R.id.editText_register_telephone)).getText().toString();

                        // Check if name is not empty
                        if (name.length() > 0)
                            // Check if we have a valid e-mail
                            if (email.matches(Utils.EMAIL_REGEX))
                                // Check if passwords are long enough
                                if (password.length() > 5 && password2.length() > 5)
                                // Check if passwords match
                                    if (password.equals(password2)) {
                                        // All prechecks passed, send the data
                                        ConnectionHandler ch = new ConnectionHandler(getActivity());
                                        ch.register(name, email, password, phone);
                                        mAlertDialog.dismiss();
                                    } else {
                                        // Passwords don't match
                                        Toast.makeText(getActivity(), R.string.passwords_dont_match, Toast.LENGTH_SHORT).show();
                                } else {
                                    // Passwords aren't at least 6 characters long
                                    Toast.makeText(getActivity(), R.string.password_length_invalid, Toast.LENGTH_LONG).show();
                                }
                            else {
                                // Email address is invalid
                                Toast.makeText(getActivity(), R.string.email_invalid, Toast.LENGTH_SHORT).show();
                            }
                        else {
                            // Name field is empty
                            Toast.makeText(getActivity(), R.string.name_empty, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        return mAlertDialog;
    }
}
