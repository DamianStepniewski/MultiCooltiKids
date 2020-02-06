package pl.snikk.multicooltikids;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class NextVenueDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_next_venue, null);

        ((TextView)view.findViewById(R.id.venues_name)).setText(getArguments().getString("name"));
        ((TextView)view.findViewById(R.id.venues_time)).setText(getArguments().getString("time"));
        ((TextView)view.findViewById(R.id.venues_address)).setText(getArguments().getString("address"));
        ((TextView)view.findViewById(R.id.venues_desc)).setText(Html.fromHtml(getArguments().getString("description")));

        builder.setView(view).setTitle(R.string.next_venue);
        return builder.create();
    }
}
