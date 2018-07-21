package com.aayaffe.sailingracecoursemanager.Users;

import java.util.Date;

/**
 * Avi Marine Innovations - www.avimarine.in
 *
 * Created by Amit Y. on 17/02/2016.
 */
@com.google.firebase.firestore.IgnoreExtraProperties
@com.google.firebase.database.IgnoreExtraProperties
public class User{
    public String Uid;
    public String DisplayName;
    //public String Email;
    public Long joined;
    public Long lastConnection;
    @com.google.firebase.firestore.Exclude
    @com.google.firebase.database.Exclude
    public Date getJoined() {
        return new Date(joined);
    }
    @com.google.firebase.firestore.Exclude
    @com.google.firebase.database.Exclude
    public void setJoined(Date joined) {
        this.joined = joined.getTime();
    }
    @com.google.firebase.firestore.Exclude
    @com.google.firebase.database.Exclude
    public Date getLastConnection() {
        return new Date(lastConnection);
    }
    @com.google.firebase.firestore.Exclude
    @com.google.firebase.database.Exclude
    public void setLastConnection(Date lastConnection) {
        this.lastConnection = lastConnection.getTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Uid != null ? Uid.equals(user.Uid) : user.Uid == null;

    }

    @Override
    public int hashCode() {
        return Uid != null ? Uid.hashCode() : 0;
    }
}
