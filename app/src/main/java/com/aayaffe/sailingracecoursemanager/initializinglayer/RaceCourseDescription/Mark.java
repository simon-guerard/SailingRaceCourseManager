package com.aayaffe.sailingracecoursemanager.initializinglayer.RaceCourseDescription;

import java.io.Serializable;
import java.util.UUID;

/**
 * Avi Marine Innovations - www.avimarine.in
 *
 * Created by Amit Y. on 29/12/2016.
 */

public class Mark implements Serializable{
    public String name; //Display name
    public int id; //ID used to identify in race course
    public MarkLocation ml;
    public boolean isGatable;
    public GateConfiguration go;
    public boolean DummyMark; //True if mark is to be used only for length calculations
    private UUID uuid;

    public Mark(String name, int id, MarkLocation ml, GateConfiguration go) {
        this(name, id, ml,go,false);
    }
    public Mark(String name, int id, MarkLocation ml, GateConfiguration go, boolean dummyMark) {
        this(name, id, ml);
        isGatable = true;
        this.go = go;
        DummyMark = dummyMark;
    }


    public Mark(String name, int id, MarkLocation ml) {
        this(name,id,ml,false);
    }
    public Mark(String name, int id, MarkLocation ml, boolean dummyMark) {
        this.name = name;
        this.id = id;
        this.ml = ml;
        this.isGatable = false;
        uuid = UUID.randomUUID();
        DummyMark = dummyMark;
    }

    public void setUuidString(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }
    public String getUuidString() {
        return this.uuid.toString();
    }
}


