package com.aayaffe.sailingracecoursemanager.Calc_Layer;
import java.io.Serializable;
/**
 * Created by Jonathan on 27/10/2016.
 */
public enum BuoyType {
    RACE_MANAGER(0),
    WORKER_BOAT(1),
    BUOY(2),
    FLAG_BUOY(3),
    TOMATO_BUOY(4),
    TRIANGLE_BUOY(5),
    START_LINE(6),
    FINISH_LINE(7),
    START_FINISH_LINE(8),
    GATE(9),
    REFERENCE_POINT(10),
    OTHER(11);
    private int value;
    BuoyType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
