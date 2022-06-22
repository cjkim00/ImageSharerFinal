package com.example.imagesharerfinal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import com.example.imagesharerfinal.ui.profile.ProfileFragment;
import com.example.imagesharerfinal.ui.search.SearchFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.imagesharerfinal.databinding.ActivityImageSharerBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

public class ImageSharerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityImageSharerBinding binding;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    ImageView profilePicture;

    DrawerLayout drawer;

    private String mEmail;
    private String mUsername;
    private String mDescription;
    private String mImageLocation;
    private int mFollowing;
    private int mFollowers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityImageSharerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarImageSharer.toolbar);
        binding.appBarImageSharer.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPhotoPicker();
            }
        });

        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;



        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_profile, R.id.nav_search)
                .setOpenableLayout(drawer)
                .build();
        /*
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_image_sharer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration); //controls the menu itself
        NavigationUI.setupWithNavController(navigationView, navController);//controls the swaping of fragment
        */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        Intent intent = getIntent();

        mEmail = intent.getStringExtra("Email");
        mUsername = intent.getStringExtra("Username");
        mDescription = intent.getStringExtra("Description");
        mImageLocation = intent.getStringExtra("ProfileImageLocation");

        mFollowing = intent.getIntExtra("Following", -1);
        mFollowers = intent.getIntExtra("Followers", -1);


        Log.i("DatabaseCheck", "TEST3: " + mEmail + " " + mUsername + " " + mDescription + " " + mImageLocation + " "  + mFollowing + " " + mFollowers);


        //change fields in nav view
        View headerView = navigationView.getHeaderView(0);

        TextView emailView = headerView.findViewById(R.id.textView_email_nav_header);
        emailView.setText(mEmail);

        TextView usernameView = headerView.findViewById(R.id.textView_username_nav_header);
        usernameView.setText(mUsername);

        //set profile picture
        profilePicture = headerView.findViewById(R.id.imageView_profile_image_nav_header);

        StorageReference profilePictureRef = storageReference.child(mImageLocation);
        final long ONE_MEGABYTE = 1024 * 1024;
        profilePictureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profilePicture.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //TODO: set profile picture as default
            }
        });








    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_sharer, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_image_sharer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void openPhotoPicker() {
        // Launches photo picker in single-select mode.
// This means that the user can select one photo or video.
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launchActivity.launch(intent);

    }

    ActivityResultLauncher<Intent> launchActivity
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null
                            && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        //Bitmap selectedImageBitmap;
                        try {
                            Bitmap selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    selectedImageUri);


                            Random rand = new Random();
                            String randomNumber = "";
                            for(int i = 0; i < 15; i++) {
                                randomNumber = randomNumber + String.valueOf(rand.nextInt(9));
                            }

                            String uploadTime = Calendar.getInstance().getTime().toString();

                            String newFileName = uploadTime + randomNumber;

                            Uri file = Uri.fromFile(new File(selectedImageUri.toString()));
                            //StorageReference imageReference = storageReference.child(selectedImageUri.toString());

                            StorageReference imageReference = storageReference.child(newFileName);


                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data2 = baos.toByteArray();
                            UploadTask uploadTask = imageReference.putBytes(data2);
                            uploadTask = imageReference.putFile(file);

                            // Register observers to listen for when the download is done or if it fails
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                    // ...

                                }
                            });
                            try {
                                sendPostInfoToDatabase(newFileName);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i("IMAGEURL", selectedImageUri.toString());





                    }
                }
            });

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.i("SELECTED", "SELECTED: " + item.toString());
        if(item.toString().equals("Profile")) {
            //BitmapDrawable bitmapDrawable = (BitmapDrawable) profilePicture.getDrawable();
            //Bitmap bitmap = bitmapDrawable.getBitmap();

            //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

            //byte[] imageByteArray = byteArrayOutputStream.toByteArray();

            Bundle bundle = new Bundle();
            //bundle.putByteArray("profile", imageByteArray);
            bundle.putString("username", mUsername);
            bundle.putString("description", mDescription);
            bundle.putInt("followers", mFollowers);
            bundle.putInt("Following", mFollowing);
            //Log.i("profileImage", "LENGTH: " + imageByteArray.length);
            //TODO: add image location to bundle (might have to send image location from the main activity)

            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setArguments(bundle);

            replaceFragment(profileFragment);
            drawer.closeDrawers();

        }

        if(item.toString().equals("Search")) {
            replaceFragment(new SearchFragment());
        }


        return true;
    }

    public void sendPostInfoToDatabase(String postLocation) throws InterruptedException {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath("cjkim00-image-sharing-app.herokuapp.com")
                .appendPath("InsertPost")
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
                msg.put("PostLocation", postLocation);
                msg.put("Email", "Test Email");
                msg.put("Description", "Test Description");
                msg.put("Likes", "0");
                msg.put("Views", "0");
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

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager)
                .beginTransaction();
        fragmentTransaction.replace(R.id.layout_activity_layout_content_image_sharer, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

}