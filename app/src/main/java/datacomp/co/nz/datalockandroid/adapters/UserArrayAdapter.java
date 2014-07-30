package datacomp.co.nz.datalockandroid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
            v = inf.inflate(R.layout.user_list_item, parent, false);
        }

        //get the current answer
        User user = users.get(pos);

        ((TextView) v.findViewById(R.id.name)).setText(user.getName());
        ((TextView) v.findViewById(R.id.email)).setText(user.getEmail());
        ((TextView) v.findViewById(R.id.phone)).setText(user.getPhone());

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
