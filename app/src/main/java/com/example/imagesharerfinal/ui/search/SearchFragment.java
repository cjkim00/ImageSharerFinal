package com.example.imagesharerfinal.ui.search;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.imagesharerfinal.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class SearchFragment extends Fragment {

    protected Profile[] mProfiles;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected SearchRecyclerViewAdapter mAdapter;

    TextView searchText;


    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDataSet();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchText = view.findViewById(R.id.plainText_search_bar_fragment_search);

        Button searchButton = view.findViewById(R.id.button_search_button_fragment_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("BUTTON", "Button pressed");
                String text = searchText.getText().toString();

                //TODO: use the text string to get back information from the database
                try {
                    searchDatabase(text);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //TODO: parse the information from the database and use it to create the recyclerView
            }
        });

        //initialize the recyclerview
        mRecyclerView = view.findViewById(R.id.recyclerView_search_list_fragment_search);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(0);


        mAdapter = new SearchRecyclerViewAdapter(mProfiles);
        mRecyclerView.setAdapter(mAdapter);


        return view;
    }

    public void initDataSet() {
        //TODO:initialize data set with data from the database
        mProfiles = new Profile[50];
        for(int i = 0; i < 50; i++) {
            Profile tempProfile = new Profile("Username" + i, "", "", -1, -1);
            mProfiles[i] = tempProfile;
        }
    }

    public void searchDatabase(String text) throws InterruptedException {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                .appendPath("find_member")
                .build();

        Thread thread = new Thread(() -> {
            URL url = null;
            try {
                url = new URL(uri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
            } catch (IOException e) {
                e.printStackTrace();
            }

            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            JSONObject msg = new JSONObject();
            try {
                //TODO: change name to the text in the textbox
                msg.put("User", "name");
            } catch (JSONException e) {
                Log.e("JSON ERROR", e.getMessage());
            }

            DataOutputStream os = null;
            try {
                os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(msg.toString());
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                int status = conn.getResponseCode();

                if (status == 200) {
                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();
                    //mUsernameExists = getResults(stringBuilder.toString());
                    getResults(stringBuilder.toString(), "username");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        });
        thread.start();
        thread.join();
    }

    /**
     * This method is a helper function for checkUsernameInDatabase() that parses the data returned
     * by the web service.
     *
     * @param results The data returned by the web service before being parsed.
     * @return Returns the parsed value returned by the web service.
     */
    public Profile getResults(String results, String text) {
        try {
            JSONObject root = new JSONObject(results);
            if (root.has("success") && root.getBoolean("success")) {
                JSONArray data = root.getJSONArray("data");
                //TODO: use a loop to get the data of each json object
                JSONObject usernameExists = data.getJSONObject(0);
                Log.i("BUTTON", "" + usernameExists.getString("username") + " " + data.length());
            }
        } catch (JSONException e) {

        }
        //return false;
        return new Profile();
    }
}