package com.aayaffe.sailingracecoursemanager.communication;

import com.aayaffe.sailingracecoursemanager.calclayer.DBObject;
import com.aayaffe.sailingracecoursemanager.Events.Event;
import com.aayaffe.sailingracecoursemanager.geographical.AviLocation;
import com.aayaffe.sailingracecoursemanager.initializinglayer.Boat;
import com.aayaffe.sailingracecoursemanager.Users.User;
import com.aayaffe.sailingracecoursemanager.initializinglayer.RaceCourseDescriptor;

import java.util.List;
import java.util.UUID;

/**
 * Created by aayaffe on 22/09/2015.
 */
public interface ICommManager {
    int login();

    void setCommManagerEventListener(CommManagerEventListener listener);

    int writeBoatObject(DBObject o);
    int writeBuoyObject(DBObject o);

    int updateBoatLocation(Event e, DBObject boat, AviLocation loc);

    List<DBObject> getAllBoats();  //ships
    List<DBObject> getAllBuoys();  //Just buoys, without ships

    int sendAction(RaceManagerAction a, DBObject o);

    long getNewBuoyId();

    void removeBuoyObject(String title);

    User findUser(String uid);

    void addUser(User u);

    void logout();

    Event getEvent(String eventName);

    long getSupportedVersion();

    DBObject getObjectByUUID(UUID u);

    void writeEvent(Event neu);

    String getCurrentEventName();

    void setCurrentEventName(String currentEventName);

    List<DBObject> getAssignedBuoys(DBObject b);

    List<DBObject> getAssignedBoats(DBObject b);

    DBObject getBoat(String currentBoatName);

    void assignBuoy(DBObject boat, String selectedBuoyName);

    void removeAssignment(DBObject buoy, DBObject boat);

    void removeAssignments(DBObject b);

    List<Boat> getBoatTypes();

    void removeBoat(UUID u);

    void deleteEvent(Event event);

    void addRaceCourseDescriptor(RaceCourseDescriptor ct);

    List<RaceCourseDescriptor> getRaceCourseDescriptors();

    boolean isAdmin(User u);
}
