package com.example.imagesharerfinal.Login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.imagesharerfinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;


public class RegistrationFragment extends Fragment {

    private FirebaseAuth mAuth;

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
        // Inflate the layout for this fragment
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
                //check if email and username is in use
                //check if email and passwords match
                //if all checks out then use firebase to store email and passwords
                //store the user's email and username in the database
                checkEmail();
                checkPassword();
                checkUsername();
            }
        });



        return view;
    }

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

        return true;
    }

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