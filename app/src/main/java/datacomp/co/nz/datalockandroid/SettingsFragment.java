package datacomp.co.nz.datalockandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by danielj on 27/08/2014.
 */
public class SettingsFragment extends Fragment {
    EditText serverAddress;
    MainActivity mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        serverAddress = (EditText) rootView.findViewById(R.id.server_address);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        serverAddress.setText(mainActivity.getPreferences(Context.MODE_PRIVATE).getString(mainActivity.SERVER_ARG, "none"));
    }

    @Override
    public void onPause(){
        super.onPause();
        mainActivity.getPreferences(Context.MODE_PRIVATE).edit().putString(mainActivity.SERVER_ARG, serverAddress.getText().toString()).commit();
    }
}