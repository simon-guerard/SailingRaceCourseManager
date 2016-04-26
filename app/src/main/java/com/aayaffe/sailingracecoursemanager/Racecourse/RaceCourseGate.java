package com.aayaffe.sailingracecoursemanager.Racecourse;

import com.aayaffe.sailingracecoursemanager.communication.ObjectTypes;
import com.aayaffe.sailingracecoursemanager.geographical.AviLocation;

/**
 * Created by aayaffe on 23/04/2016.
 */
public class RaceCourseGate extends RaceCourseObjectLong {
    public RaceCourseGate(AviLocation stbd, AviLocation port) {
        super(stbd, port);
        type = ObjectTypes.Gate;
    }
}