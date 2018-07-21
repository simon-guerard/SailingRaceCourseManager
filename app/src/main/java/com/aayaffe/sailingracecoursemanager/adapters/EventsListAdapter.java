package com.aayaffe.sailingracecoursemanager.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aayaffe.sailingracecoursemanager.calclayer.DBObject;
import com.aayaffe.sailingracecoursemanager.calclayer.RaceCourse;
import com.aayaffe.sailingracecoursemanager.db.FireStoreEvents;
import com.aayaffe.sailingracecoursemanager.db.FireStoreUsers;
import com.aayaffe.sailingracecoursemanager.events.Event;
import com.aayaffe.sailingracecoursemanager.R;
import com.aayaffe.sailingracecoursemanager.Users.User;
import com.aayaffe.sailingracecoursemanager.Users.Users;
import com.aayaffe.sailingracecoursemanager.activities.ChooseEventActivity;
import com.aayaffe.sailingracecoursemanager.db.IDBManager;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Avi Marine Innovations - www.avimarine.in
 *
 * Created by Amit Y. on 14/01/2017.
 */

public class EventsListAdapter extends FirebaseListAdapter<Event> {
    private final IDBManager commManager;
    private final Users users;
    private final Activity mActivity;
    private final FireStoreUsers usersDb;
    private static final String TAG = "EventsListAdapter";

    /**
     * @param activity    The activity containing the ListView

     * @param users
     */
    public EventsListAdapter(FirebaseListOptions options, Activity activity, IDBManager commManager, FireStoreUsers usersDb, Users users) {
        super(options);
        this.mActivity = activity;
        this.commManager = commManager;
        this.users = users;
        this.usersDb = usersDb;

    }

    @Override
    protected void populateView(View view, final Event event, int position) {


        usersDb.getUser(event.getManagerUuid(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    User manager = document.toObject(User.class);
                    populateView2(view,event,position, manager);
                }
                else {
                    Log.e(TAG, "Cannot find user manager");
                }
            }
        });
//        User manager = commManager.findUser(event.getManagerUuid());

    }

    private void populateView2(View view, final Event event, int position, User manager){
        ((TextView)view.findViewById(android.R.id.text1)).setText(event.getName());
        String dates = getDateRangeString(event);
        final ImageButton delete = view.findViewById(R.id.delete_event_button);
        final ImageButton viewOnly = view.findViewById(R.id.view_only_event_button);
        final ImageView rcFlag = view.findViewById(R.id.rc_icon);

        if ((manager!=null && manager.equals(users.getCurrentUser()))||users.isAdmin(users.getCurrentUser())){
            delete.setVisibility(View.VISIBLE);
            delete.setEnabled(true);
            delete.setOnClickListener(view1 -> ((ChooseEventActivity)mActivity).deleteEvent(event));
        }

        else {
            delete.setVisibility(View.GONE);
            delete.setEnabled(false);
        }
        if (manager!=null && manager.equals(users.getCurrentUser())){
            rcFlag.setVisibility(View.VISIBLE);
        }
        else{
            rcFlag.setVisibility(View.GONE);
        }
        if (users.isAdmin(users.getCurrentUser())){
            viewOnly.setVisibility(View.VISIBLE);
            viewOnly.setEnabled(true);
            viewOnly.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ChooseEventActivity)mActivity).viewOnly(event);
                }
            });
        }
        else {
            viewOnly.setVisibility(View.GONE);
            viewOnly.setEnabled(false);
        }
        if (manager==null) {
            ((TextView) view.findViewById(android.R.id.text2)).setText(mActivity.getString(R.string.race_officer) + ": " +mActivity.getString(R.string.unknown));
        }
        else {
            ((TextView) view.findViewById(android.R.id.text2)).setText(mActivity.getString(R.string.race_officer) + ": " + manager.DisplayName);
        }
        ((TextView) view.findViewById(R.id.text3)).setText(dates);
    }

    @NonNull
    public static String getDateRangeString(Event event) {
        return getDateRangeString(event.yearStart,event.yearEnd,event.monthStart,event.monthEnd,event.dayStart,event.dayEnd);
    }

    public static String getDateRangeString(int yearStart, int yearEnd, int monthStart, int monthEnd, int dayStart, int dayEnd) {
        if ( dayEnd == 0 || yearStart == 0 || dayStart == 0 || yearEnd == 0)
            return "";
        if (dayStart == dayEnd && monthStart == monthEnd && yearStart == yearEnd)
            return String.valueOf(dayStart) + '/' + (monthStart+1) + '/' + yearStart;
        return String.valueOf(dayStart) + '/' + (monthStart+1) + '/' + yearStart + " - " + dayEnd + '/' + (monthEnd+1) + '/' + yearEnd;
    }
}
