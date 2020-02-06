package pl.snikk.multicooltikids;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class LoginDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_login, null);
        builder.setView(view)
                .setTitle(R.string.log_in)
                // Add action buttons
                .setPositiveButton(R.string.log_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = ((EditText) view.findViewById(R.id.editText_login_username)).getText().toString();
                        String pass = ((EditText) view.findViewById(R.id.editText_login_password)).getText().toString();

                        ConnectionHandler ch = new ConnectionHandler(getActivity());
                        ch.logIn(name, pass);
                    }
                })
                .setNeutralButton(R.string.register, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RegisterDialogFragment registerDialog = new RegisterDialogFragment();
                        registerDialog.show(getFragmentManager(), "RegisterDialogFragment");
                    }
                });
        return builder.create();
    }
}
