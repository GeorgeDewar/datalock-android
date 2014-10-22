package datacomp.co.nz.datalockandroid;



import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import datacomp.co.nz.datalockandroid.adapters.UserArrayAdapter;
import datacomp.co.nz.datalockandroid.model.User;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class UsersFragment extends Fragment {
    public static final String TAG = "UsersFragment";
    MainActivity mainActivity;

    ListView userList;

    public static UsersFragment newInstance() {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);
        userList = (ListView) rootView.findViewById(R.id.user_list);
        return rootView;
}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        new GetUsersTask().execute();
    }

    @Override
    public void onResume(){
        super.onResume();
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        ((MenuItem) menu.findItem(R.id.action_add_user)).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("onOptionsItemSelected", "yes");
        switch (item.getItemId()) {
            case R.id.action_add_user:
                //add new user
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private class GetUsersTask extends AsyncTask<Void, Void, List<User>> {
        List<User> users = new ArrayList<User>();

        @Override
        protected List<User> doInBackground(Void... params) {
            try {
                URL url = new URL(mainActivity.getPreferences(Context.MODE_PRIVATE).getString(mainActivity.SERVER_ARG, "") + "/api/users") ;

                Log.d(TAG, "unlock users url: " + url);

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

                for(int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonUser = jsonArray.get(i).getAsJsonObject().getAsJsonObject("api");

                    long id = jsonUser.get("id").getAsLong();
                    String email = jsonUser.get("email").getAsString();
                    String name = jsonUser.get("name").getAsString();
                    boolean admin = jsonUser.get("admin").getAsBoolean();
                    int pin = jsonUser.get("pin").getAsInt();
                    String phone = jsonUser.get("ph_number").getAsString();
                    String created = jsonUser.get("created_at").getAsString();
                    String updated = jsonUser.get("updated_at").getAsString();

                    User user = new User(id, email, name, admin, pin, phone, created, updated);
                    users.add(user);
                }
                return users;

            } catch (SocketTimeoutException e) {
                Log.e(TAG, "timeout exception", e);
            }catch(Exception e){
                Log.e(TAG, "Failed to connect", e);
            }
            return null;
        }

        protected void onPostExecute(final List<User> result){
            Log.d(TAG, "post execute");
            if (result != null){
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userList.setAdapter(new UserArrayAdapter(getActivity(), R.layout.user_list_item, result));
                    }
                });
            }
        }

        protected void onCancelled(){
        }
    }



}
