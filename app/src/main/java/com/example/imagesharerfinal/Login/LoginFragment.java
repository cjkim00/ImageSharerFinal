package com.example.imagesharerfinal.Login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;


public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private String mEmail;
    private String mUsername;
    private String mDescription;
    private String mImageLocation;
    private int mFollowing;
    private int mFollowers;

    EditText editTextEmail;
    EditText editTextPassword;



    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);


        Log.i("TOAST", "test");
        Button loginButton = (Button) view.findViewById(R.id.button_login_login);
        Button registerButton = (Button) view.findViewById(R.id.button_login_register);
        editTextEmail = view.findViewById(R.id.edit_text_login_email);
        editTextPassword = view.findViewById(R.id.edit_text_login_password);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isEmailValid = checkEmail();
                boolean isPasswordValid = checkPassword();
                if (isEmailValid && isPasswordValid) {
                    loginUser();
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new RegistrationFragment());
            }
        });


        return view;
    }

    /**
     * This method logs the user in using Google Firebase authentication.
     */
    public void loginUser() {
        mAuth.signInWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            //start image sharing activity


                            try {
                                getUserInfoFromDatabase();

                                Intent intent = new Intent(getActivity(), ImageSharerActivity.class);
                                intent.putExtra("Email", mEmail);
                                intent.putExtra("Username", mUsername);
                                intent.putExtra("Description", mDescription);
                                intent.putExtra("ProfileImageLocation", mImageLocation);
                                intent.putExtra("Following", mFollowing);
                                intent.putExtra("Followers", mFollowers);

                                Log.i("DatabaseCheck", "TEST2: " + mEmail + " " + mUsername + " " + mDescription + " " + mImageLocation + " "  + mFollowing + " " + mFollowers);

                                startActivity(intent);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * This method checks to see if the email entered is valid.
     * @return Returns true if the inputted email is valid or false if the email is not valid.
     */
    public boolean checkEmail() {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (email.length() == 0) {
            editTextEmail.setError("Please enter an email address");
            return false;
        }

        if (!isValid(email)) {
            editTextEmail.setError("Enter a valid email address");
            return false;
        }

        return true;
    }

    /**
     * This method checks to see if the inputted password is valid.
     * @return Returns true if the password is valid or false if the password is not valid.
     */
    public boolean checkPassword() {
        String password = editTextPassword.getText().toString();
        if (password.length() == 0) {
            editTextPassword.setError("Please enter your password");
            return false;
        }

        return true;
    }

    public void getUserInfoFromDatabase() throws InterruptedException {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                .appendPath("get_user_info")
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
                msg.put("User", editTextEmail.getText().toString());
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
                    Log.i("DatabaseCheck", "TEST");
                    getResults(stringBuilder.toString());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        });
        thread.start();
        thread.join();
    }

    public void getResults(String results) {
        try {
            JSONObject root = new JSONObject(results);
            if (root.has("success") && root.getBoolean("success")) {
                JSONArray data = root.getJSONArray("data");
                JSONObject userData = data.getJSONObject(0);
                mEmail = userData.getString("email");
                mUsername = userData.getString("username");
                mDescription = userData.getString("profiledescription");
                mImageLocation = userData.getString("profileimagelocation");
                mFollowing = userData.getInt("followingtotal");
                mFollowers = userData.getInt("followerstotal");
                Log.i("DatabaseCheck", "TEST: " + mEmail + " " + mUsername + " " + mDescription + " " + mImageLocation + " "  + mFollowing + " " + mFollowers);
                //return usernameExists.getBoolean("exists");
            }
        } catch (JSONException e) {

        }
        //return false;
    }

    /**
     * THis method checks to see if the inputted email is a valid email address.
     *
     * @param email The inputted email to be checked.
     * @return Returns true if the inputted email is valid or false if the inputted email is not
     * valid.
     */
    public static boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }


    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager)
                .beginTransaction();
        fragmentTransaction.replace(((ViewGroup) (requireView().getParent()))
                .getId(), fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }
}