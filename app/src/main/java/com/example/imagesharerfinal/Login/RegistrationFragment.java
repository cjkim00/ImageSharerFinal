/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.imagesharerfinal.Login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.imagesharerfinal.ImageSharerActivity;
import com.example.imagesharerfinal.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * The RegistrationFragment class handles the registration of the user.
 */

public class RegistrationFragment extends Fragment {

    private FirebaseAuth mAuth;

    private boolean mUsernameExists = false;

    EditText email;
    EditText reEnterEmail;
    EditText username;
    EditText password;
    EditText reEnterPassword;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        email = view.findViewById(R.id.edit_text_registration_email);
        reEnterEmail = view.findViewById(R.id.edit_text_registration_reenter_email);

        username = view.findViewById(R.id.edit_text_registration_username);

        password = view.findViewById(R.id.edit_text_registration_password);
        reEnterPassword = view.findViewById(R.id.edit_text_registration_reenter_password);

        Button register = view.findViewById(R.id.button_registration_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checkEmail = checkEmail();
                boolean checkPassword = checkPassword();
                boolean checkUsername = checkUsername();
                if(checkEmail && checkPassword && checkUsername) {
                    //register user with firebase
                    registerWithFirebase();
                    //send user info into databse
                    try {
                        sendUserInfoToDatabase();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //start image sharing activity
                    Intent intent = new Intent(getActivity(), ImageSharerActivity.class);
                    startActivity(intent);
                }

            }
        });

        return view;
    }

    /**
     * This method uses a thread to send the user's email and username
     * into a database after the registration.
     * @throws InterruptedException
     */
    public void sendUserInfoToDatabase() throws InterruptedException {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                .appendPath("Registration")
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
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            JSONObject msg = new JSONObject();
            try {
                msg.put("Email", email.getText().toString());
                msg.put("Username", username.getText().toString());
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
                }

            } catch (IOException e) {
                e.printStackTrace();
            }



        });
        thread.start();
        thread.join();
    }

    /**
     * This method takes the inputs in the email and password fields and uses them to
     * register with Google Firebase.
     */
    public void registerWithFirebase() {
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();

        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("CREATION", "createUserWithEmail:success");
                            Toast.makeText(requireActivity(), "Authentication succeeded.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("CREATION", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(requireActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * This method checks the email field to make sure it has a valid input.
     * @return returns true if the input is valid or false if the input is not valid.
     */
    public boolean checkEmail() {

        String emailStr = email.getText().toString();
        String emailStr2 = reEnterEmail.getText().toString();

        if(emailStr.length() == 0) {
            email.setError("Field cannot be empty");
            return false;
        }

        if(!isValid(emailStr)) {
            email.setError("Enter a valid email address");
            return false;
        }

        if(!emailStr.equals(emailStr2)) {
            reEnterEmail.setError("Emails must match");
            return false;
        }
        return true;
    }

    /**
     * This method checks the password field to make sure it is not empty and contains at least 8
     * characters.
     * @return returns true if the password is valid and false if the password is not valid.
     */
    public boolean checkPassword() {

        String passwordStr = password.getText().toString();
        String passwordStr2 = reEnterPassword.getText().toString();

        if(passwordStr.length() == 0) {
            password.setError("Must enter a password");
            return false;
        }
        if(passwordStr.length() < 8) {
            password.setError("Password must be at least 8 characters");
            return false;
        }
        if(!passwordStr.equals(passwordStr2)) {
            reEnterPassword.setError("Passwords must match");
            return false;
        }

        return true;
    }

    /**
     * This method checks if the username is valid.
     * @return returns true if the username is valid or false is the username is not valid.
     */
    public boolean checkUsername() {
        String userStr = username.getText().toString();

        if(userStr.length() == 0) {
            username.setError("Field cannot be empty");
            return false;
        }

        if(userStr.length() < 4) {
            username.setError("Username must be at least 4 characters");
            return false;
        }

        //Check if username exists in database
        try {
            checkUsernameInDatabase();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i("READER INFO", "TEST: " + mUsernameExists);
        if(mUsernameExists) {
            username.setError("Username already exists");
        }

        return true;
    }

    /**
     * This method sends a post request to the web service to check if the username within the
     * username field is already being used.
     * @throws InterruptedException
     */
    public void checkUsernameInDatabase() throws InterruptedException {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                .appendPath("check_if_username_exists")
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
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            JSONObject msg = new JSONObject();
            try {
                msg.put("User", username.getText().toString());
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
                    mUsernameExists = getResults(stringBuilder.toString());
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
     * @param results The data returned by the web service before being parsed.
     * @return Returns the parsed value returned by the web service.
     */
    public boolean getResults(String results) {
        try {
            JSONObject root = new JSONObject(results);
            if(root.has("success") && root.getBoolean("success")) {
                JSONArray data = root.getJSONArray("data");
                JSONObject usernameExists = data.getJSONObject(0);
                Log.i("READER INFO", "" + usernameExists.getBoolean("exists"));

                return usernameExists.getBoolean("exists");
            }
        } catch (JSONException e) {

        }
        return false;
    }


    /**
     * THis method checks to see if the inputted email is a valid email address.
     * @param email The inputted email to be checked.
     * @return Returns true if the inputted email is valid or false if the inputted email is not
     * valid.
     */
    public static boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }



}