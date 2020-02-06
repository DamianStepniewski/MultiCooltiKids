package pl.snikk.multicooltikids;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class VenuesAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<Venue> venues;

    public VenuesAdapter(Context context, ArrayList<Venue> venues) {
        this.context = context;
        this.venues = venues;
    }

    @Override
    public int getGroupCount() {
        return venues.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return venues.get(groupPosition).events.size();
    }

    @Override
    public Venue getGroup(int groupPosition) {
        return venues.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return venues.get(groupPosition).events.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View groupView = inflater.inflate(R.layout.venue_type, parent, false);

        ImageView thumb = (ImageView) groupView.findViewById(R.id.imageView_venue_type);
        if (venues.get(groupPosition).type.equals(context.getString(R.string.events)))
            thumb.setBackgroundResource(R.drawable.ic_calendar_text_grey600_48dp);
        else if (venues.get(groupPosition).type.equals(context.getString(R.string.monuments)))
            thumb.setBackgroundResource(R.drawable.ic_bank_grey600_48dp);

        TextView tvName = (TextView) groupView.findViewById(R.id.venue_type_name);
        tvName.setText(venues.get(groupPosition).type);
        return groupView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.venue, parent, false);

        TextView tvName = (TextView) rowView.findViewById(R.id.venues_name);
        tvName.setText(Html.fromHtml(venues.get(groupPosition).events.get(childPosition).name));

        TextView tvAddress = (TextView) rowView.findViewById(R.id.venues_address);
        tvAddress.setText(Html.fromHtml(venues.get(groupPosition).events.get(childPosition).address));

        TextView tvTime = (TextView) rowView.findViewById(R.id.venues_time);
        tvTime.setText(Html.fromHtml(venues.get(groupPosition).events.get(childPosition).time));

        TextView tvDesc = (TextView) rowView.findViewById(R.id.venues_desc);
        tvDesc.setText(Html.fromHtml(venues.get(groupPosition).events.get(childPosition).description));

        return rowView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
