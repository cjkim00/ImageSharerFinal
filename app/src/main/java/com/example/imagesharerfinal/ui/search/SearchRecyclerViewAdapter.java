package com.example.imagesharerfinal.ui.search;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagesharerfinal.R;

public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder> {

    private Profile[] mProfiles;
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mProfilePicture;
        private final TextView mUsername;
        private final TextView mFollowerCount;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: when a viewholder item is clicked bring up a new fragment that shows the
                    //user's profile.
                    Log.i("CLICKTEST", "ITEM WAS CLICKED.");
                }
            });

            mProfilePicture = view.findViewById(R.id.imageView_profile_picture_profile_item);

            mUsername = view.findViewById(R.id.textView_username_profile_item);
            mFollowerCount = view.findViewById(R.id.textView_follower_count_profile_item);
        }

        public ImageView getmProfilePicture() {
            return mProfilePicture;
        }

        public TextView getmUsername() {
            return mUsername;
        }

        public TextView getmFollowerCount() {
            return mFollowerCount;
        }

    }

    public SearchRecyclerViewAdapter(Profile[] dataSet) {
        mProfiles = dataSet;
    }

    @NonNull
    @Override
    public SearchRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRecyclerViewAdapter.ViewHolder holder, int position) {
        //TODO:get profile picture from firebase and set it to the imageview

        holder.getmUsername().setText(mProfiles[position].getUsername());
        holder.getmFollowerCount().setText(String.valueOf(mProfiles[position].getFollowers()));
    }

    @Override
    public int getItemCount() {
        return mProfiles.length;
    }


}
