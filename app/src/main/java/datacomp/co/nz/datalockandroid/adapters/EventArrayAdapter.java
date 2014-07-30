package datacomp.co.nz.datalockandroid.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import datacomp.co.nz.datalockandroid.R;
import datacomp.co.nz.datalockandroid.model.Event;
import datacomp.co.nz.datalockandroid.model.User;

/**
 * Created by jonker on 27/07/14.
 */
public class EventArrayAdapter extends ArrayAdapter<View> {
    public static final String TAG = "EventArrayAdapter";
    private List<Event> events;

    public EventArrayAdapter(Context context, int resource) {
        super(context, resource);
    }


    public EventArrayAdapter(Context context, int resource, List events) {
        super(context, resource, events);
        this.events = events;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = convertView;
        if (v == null) {
            v = inf.inflate(R.layout.event_list_item, parent, false);
        }

        //get the current answer
        Event event = events.get(pos);

        Log.d(TAG, "populate item");

        ((TextView) v.findViewById(R.id.action)).setText(event.getAction());
        ((TextView) v.findViewById(R.id.user_id)).setText(String.valueOf(event.getUserId()));
        ((TextView) v.findViewById(R.id.date_time)).setText(event.getCreatedAt());

        return v;
    }

    @Override
    public boolean isEmpty(){
        return false;
    }


    public List<Event> getEvents() {
        return events;
    }

    public void setUsers(List<Event> events) {
        this.events = events;
    }
}
