package datacomp.co.nz.datalockandroid;



import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import datacomp.co.nz.datalockandroid.adapters.EventArrayAdapter;
import datacomp.co.nz.datalockandroid.adapters.UserArrayAdapter;
import datacomp.co.nz.datalockandroid.model.Event;
import datacomp.co.nz.datalockandroid.model.User;


public class EventsFragment extends Fragment {
    public static final String TAG = "EventsFragment";
    MainActivity mainActivity;

    ListView eventsList;


    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);
        eventsList = (ListView) rootView.findViewById(R.id.events_list);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        new GetEventsTask().execute();
    }

    @Override
    public void onResume(){
        super.onResume();
    }


    private class GetEventsTask extends AsyncTask<Void, Void, List<Event>> {
        List<Event> events = new ArrayList<Event>();

        @Override
        protected List<Event> doInBackground(Void... params) {
            try {
                URL url = new URL(mainActivity.getPreferences(Context.MODE_PRIVATE).getString(mainActivity.SERVER_ARG, "") + "/api/recent_events") ;

                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("GET");

                con.setRequestProperty("Accept", "*/*");
                con.setRequestProperty("Host", mainActivity.getPreferences(Context.MODE_PRIVATE).getString(mainActivity.SERVER_ARG, ""));
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; GT-I9505 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.141 Mobile Safari/537.36");

                con.setUseCaches(false);
                con.setDoInput(true);
                con.setDoOutput(false);

                int status = con.getResponseCode();
                String message = con.getResponseMessage();
                Log.d(TAG, String.valueOf(status) + " - " + message);

                BufferedReader r = new BufferedReader(new InputStreamReader( con.getInputStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }

                Log.d(TAG, "input stream: \n" + total.toString());


                JsonParser jsonParser = new JsonParser();
                JsonObject jo = (JsonObject)jsonParser.parse(total.toString());
                JsonArray jsonArray = jo.getAsJsonArray("api");
//
                for(int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonUser = jsonArray.get(i).getAsJsonObject().getAsJsonObject("api");

                    long id = jsonUser.get("id").getAsLong();
                    String action = jsonUser.get("action").getAsString();
                    long userId;
                    if (jsonUser.get("user_id").isJsonNull()) {
                        userId = -1;
                    } else {
                        userId = jsonUser.get("user_id").getAsLong();
                    }
                    Log.d(TAG, "userId: " + userId);
                    String createdAt = jsonUser.get("created_at").getAsString();
                    String updatedAt = jsonUser.get("updated_at").getAsString();

                    Event event = new Event(id, action, userId, createdAt, updatedAt);
                    events.add(event);
                }
                return events;

            } catch (SocketTimeoutException e) {
                Log.e(TAG, "timeout exception", e);
            }catch(Exception e){
                Log.e(TAG, "Failed to connect", e);
            }
            return null;
        }

        protected void onPostExecute(final List<Event> result){
            Log.d(TAG, "post execute");
            if (result != null){
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) Log.d(TAG, "1");
                        if (eventsList == null) Log.d(TAG, "2");
                        if (result == null) Log.d(TAG, "3");
                        eventsList.setAdapter(new EventArrayAdapter(getActivity(), R.layout.event_list_item, result));
                    }
                });
            }
        }

        protected void onCancelled(){
        }
    }

}
