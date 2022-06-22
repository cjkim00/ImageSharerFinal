package com.example.imagesharerfinal.ui.search;

public class Profile {

    private String mUsername;
    private String mDescription;
    private String mImageLocation;
    private int mFollowing;
    private int mFollowers;

    public Profile() {
        mUsername = "EMPTY";
        mDescription = "EMPTY";
        mImageLocation = "EMPTY";
        mFollowing = -1;
        mFollowers = -1;
    }

    public Profile(String username, String description, String imageLocation, int following, int followers) {
        mUsername = username;
        mDescription = description;
        mImageLocation = imageLocation;
        mFollowing = following;
        mFollowers = followers;
    }

    public String getUsername() {
        if(mUsername == null) {
            return "NULL";
        } else {
            return mUsername;
        }
    }

    public String getDescription() {
        if(mDescription == null) {
            return "NULL";
        } else {
            return mDescription;
        }
    }

    public String getImageLocation() {
        if(mImageLocation == null) {
            return "NULL";
        } else {
            return mImageLocation;
        }
    }

    public int getFollowing() {
         return mFollowing;
    }

    public int getFollowers() {
        return mFollowers;
    }

}
