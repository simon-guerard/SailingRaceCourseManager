package com.aayaffe.sailingracecoursemanager.Users;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.aayaffe.sailingracecoursemanager.db.FireStoreUsers;
import com.aayaffe.sailingracecoursemanager.db.IDBManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

/**
 * Avi Marine Innovations - www.avimarine.in
 *
 * Created by Amit Y. on 16/02/2016.
 */
public class Users {
    private static Users instance;
    private static final String TAG = "Users";
    private static User currentUser;
    private static IDBManager commManager;
    private static SharedPreferences sharedPreferences = null;
    private static FireStoreUsers usersDb;


    private Users(IDBManager cm,SharedPreferences sp){
        sharedPreferences = sp;
        if (commManager==null)
            commManager = cm;
        usersDb = new FireStoreUsers();
        setCurrentUser(null);
    }

    public static void Init(IDBManager cm, SharedPreferences sp){
        if (instance == null){
            instance = new Users(cm,sp);
        }
    }
     public static Users getInstance(){
         return instance;
     }

    /**
     *
     * @return The currently logged in user, null if no user is logged in.
     */
    public User getCurrentUser() {
        if (currentUser==null&&commManager!=null){
            usersDb.getUser(commManager.getLoggedInUid(), new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        currentUser = task.getResult().toObject(User.class);
                    }
                }
            });
//            currentUser = commManager.findUser(commManager.getLoggedInUid());
        }
        return currentUser;

    }

    public static void setCurrentUser(User currentUser) {
        Users.currentUser = currentUser;
        if (currentUser!=null) {
            Users.currentUser.setLastConnection(new Date());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("UID", currentUser.Uid);
            editor.apply();
        }
//        if (commManager!=null)
//            commManager.addUser(Users.currentUser);
        if (usersDb!=null)
            usersDb.addUser(Users.currentUser);
    }
    public static void setCurrentUser(String Uid, String displayName) {
        Log.d(TAG,"Uid = "+Uid+" displayName = " + displayName);
        usersDb.getUser(Uid, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    User u = task.getResult().toObject(User.class);
                    if (u!=null) {
                        setCurrentUser(u);
                    }
                    else{ //New user in the system
                        u = new User();
                        u.Uid = Uid;
                        u.DisplayName = displayName;
                        u.setJoined(new Date());
                        setCurrentUser(u);
                    }
                }
            }
        });
//        User u = commManager.findUser(Uid);
//        if (u!=null) {
//            setCurrentUser(u);
//        }
//        else{ //New user in the system
//            u = new User();
//            u.Uid = Uid;
//            u.DisplayName = displayName;
//            u.setJoined(new Date());
//            setCurrentUser(u);
//        }
    }

    /**
     * Logs out of the db and application
     */
    public static void logout() {
        currentUser = null;
        commManager.logout();
    }


    public boolean isAdmin(User u){
        return commManager.isAdmin(u);
    }
}
