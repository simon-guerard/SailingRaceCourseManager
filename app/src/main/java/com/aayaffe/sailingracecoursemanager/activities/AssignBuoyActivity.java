package com.aayaffe.sailingracecoursemanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.aayaffe.sailingracecoursemanager.calclayer.DBObject;
import com.aayaffe.sailingracecoursemanager.R;
import com.aayaffe.sailingracecoursemanager.db.FireStoreEvents;
import com.aayaffe.sailingracecoursemanager.db.FirebaseDB;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;
import java.util.List;

public class AssignBuoyActivity extends AppCompatActivity {

    private static final String TAG = "AssignBuoyActivity";
    private FirebaseDB commManager;
    private FireStoreEvents eventsDb = new FireStoreEvents();
    private FirebaseListAdapter<DBObject> mAdapter;
    private DBObject currentBoat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_buoy);
        commManager = FirebaseDB.getInstance(this);
        Intent i = getIntent();
        //currentBoat = commManager.getBoat(i.getStringExtra("boatUid"));
        eventsDb.getBoat(commManager.getCurrentEvent().getUuid(),i.getStringExtra("boatUid"), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    currentBoat = document.toObject(DBObject.class);
                }
                else {
                    Log.e(TAG, "Cannot find Boat");
                }
            }
        });

        setupToolbar();
        ListView boatsView = (ListView) findViewById(R.id.BuoysList);
        FirebaseListOptions<DBObject> options = new FirebaseListOptions.Builder<DBObject>()
                .setLayout(R.layout.two_line_with_action_icon_list_item)
                .setQuery(commManager.getEventBuoysReference(), DBObject.class)
                .build();
        mAdapter = new FirebaseListAdapter<DBObject>(options) {
            @Override
            protected void populateView(View view, final DBObject b, int position) {
                ((TextView)view.findViewById(android.R.id.text1)).setText(b.getName());
                ((TextView)view.findViewById(android.R.id.text2)).setText(getAssignedBoatName(b));
                final ImageButton remove =(ImageButton)view.findViewById(R.id.remove_assignment_button);
                List<DBObject> assigned = commManager.getAssignedBoats(b);
                if (assigned!=null && !assigned.isEmpty())
                {
                    remove.setVisibility(View.VISIBLE);
                    remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            removeAssignment(b);
                            remove.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        };
        mAdapter.startListening();
        boatsView.setAdapter(mAdapter);
        boatsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String selectedBuoyName = ((TextView)view.findViewById(android.R.id.text1)).getText().toString();
                DBObject dbo = (DBObject) parent.getItemAtPosition(position);
                if (currentBoat!=null) {
                    commManager.assignBuoy(currentBoat, dbo.getUuidString());
                }
                onBackPressed();
            }
        });

    }

    private void removeAssignment(DBObject b) {
        commManager.removeAssignments(b);
    }

    private String getAssignedBoatName(DBObject b) {
        List<DBObject> boats = commManager.getAssignedBoats(b);
        if (boats==null)
            return "";
        if (boats.isEmpty())
            return "";
        if (boats.get(0)!=null)
            return boats.get(0).getName();
        return null;
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar==null) {
            Log.e(TAG,"Unable to find toolbar view.");
            return;
        }
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle(R.string.choose_buoy);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.stopListening();
    }
}

