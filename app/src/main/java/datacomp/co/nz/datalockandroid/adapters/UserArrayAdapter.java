package datacomp.co.nz.datalockandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import datacomp.co.nz.datalockandroid.R;
import datacomp.co.nz.datalockandroid.model.User;

/**
 * Created by jonker on 27/07/14.
 */
public class UserArrayAdapter extends ArrayAdapter<View> {
    private List<User> users;

    public UserArrayAdapter(Context context, int resource) {
        super(context, resource);
    }


    public UserArrayAdapter(Context context, int resource, List users) {
        super(context, resource, users);
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = convertView;
        if (v == null) {
            v = inf.inflate(R.layout.admin_list_item, parent, false);
        }

        LinearLayout container = (LinearLayout) v;

        //get the current answer
        Task job = jobs.get(pos);

        ((TextView) v.findViewById(R.id.description)).setText(job.getTimeDesc());
        ((TextView) v.findViewById(R.id.group)).setText(job.getAltRef2());

        return v;
    }

    @Override
    public boolean isEmpty(){
        return false;
    }


    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> jobs) {
        this.users = jobs;
    }


}
