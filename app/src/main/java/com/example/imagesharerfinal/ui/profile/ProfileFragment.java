package com.example.imagesharerfinal.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.imagesharerfinal.ImageSharerActivity;
import com.example.imagesharerfinal.R;

import org.w3c.dom.Text;

import java.util.Objects;


public class ProfileFragment extends Fragment {

    private String mEmail;
    private String mUsername;
    private String mDescription;
    private String mImageLocation;
    private int mFollowing;
    private int mFollowers;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();



        mEmail = intent.getStringExtra("Email");
        mUsername = intent.getStringExtra("Username");
        mDescription = intent.getStringExtra("Description");
        mImageLocation = intent.getStringExtra("ProfileImageLocation");

        mFollowing = intent.getIntExtra("Following", -1);
        mFollowers = intent.getIntExtra("Followers", -1);




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView profilePicture = view.findViewById(R.id.imageView_profile_image_profile_fragment);
        //ImageView currentProfileImage = view.findViewById(R.id.imageView_profile_image_nav_header);

        TextView username = view.findViewById(R.id.textView_username_profile_fragment);
        TextView description = view.findViewById(R.id.textView_description_fragment_profile);
        TextView followers = view.findViewById(R.id.textView_followers_count_profile_fragment);
        TextView following = view.findViewById(R.id.textView_following_count_profile_fragment);

        Bundle bundle = getArguments();

        //byte[] imageByteArray = bundle.getByteArray("profile");

        String usernameStr = bundle.getString("username");
        String descriptionStr = bundle.getString("description");

        int followersInt = bundle.getInt("followers");
        int followingInt = bundle.getInt("following");

        username.setText(usernameStr);
        description.setText(descriptionStr);
        followers.setText("Followers: " + String.valueOf(followersInt));
        following.setText("Following: " + String.valueOf(followingInt));

        //TODO: get image from firebase storage and display it

        //Bitmap bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
        //profilePicture.setImageBitmap(bitmap);

        Button editButton = view.findViewById(R.id.button_edit_profile_fragment_profile);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        //TODO: add edit profile functionality

        return view;
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