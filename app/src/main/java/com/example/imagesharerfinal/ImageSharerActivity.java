package com.example.imagesharerfinal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import java.util.Random;

public class ImageSharerActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityImageSharerBinding binding;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

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
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_image_sharer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

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




        launchSomeActivity.launch(intent);

    }

    ActivityResultLauncher<Intent> launchSomeActivity
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

}