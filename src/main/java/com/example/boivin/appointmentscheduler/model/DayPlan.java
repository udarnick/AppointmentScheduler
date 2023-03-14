package com.example.boivin.appointmentscheduler.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DayPlan {

    private TimePeriod workingHours;

    private List<TimePeriod> brakes;

    ArrayList<TimePeriod> timePeriodsWithBrakesExcluded = new ArrayList<>();

    public DayPlan() {
        brakes = new ArrayList<>();
    }

    public DayPlan(TimePeriod workingHours) {
        this.workingHours = workingHours;
        this.brakes = new ArrayList<>();
    }

    public List<TimePeriod> getTimePeriodsWithBrakesExcluded() {

        if (!timePeriodsWithBrakesExcluded.contains(getWorkingHours())) {
            timePeriodsWithBrakesExcluded.add(getWorkingHours());
        }
        List<TimePeriod> brakes = getBrakes();

        if (!brakes.isEmpty()) {
            ArrayList<TimePeriod> toAdd = new ArrayList<>();
            for (TimePeriod brake1 : brakes) {
                if (brake1.getStart().isBefore(workingHours.getStart())) {
                    brake1.setStart(workingHours.getStart());
                }
                if (brake1.getEnd().isAfter(workingHours.getEnd())) {
                    brake1.setEnd(workingHours.getEnd());
                }
                for (TimePeriod period : timePeriodsWithBrakesExcluded) {
                    if (brake1.getStart().equals(period.getStart()) && brake1.getEnd().isAfter(period.getStart()) && brake1.getEnd().isBefore(period.getEnd())) {
                        period.setStart(brake1.getEnd());
                    }
                    if (brake1.getStart().isAfter(period.getStart()) && brake1.getStart().isBefore(period.getEnd()) && brake1.getEnd().equals(period.getEnd())) {
                        period.setEnd(brake1.getStart());
                    }
                    if (brake1.getStart().isAfter(period.getStart()) && brake1.getEnd().isBefore(period.getEnd())) {
                        toAdd.add(new TimePeriod(period.getStart(), brake1.getStart()));
                        period.setStart(brake1.getEnd());
                    }
                }
            }
            timePeriodsWithBrakesExcluded.addAll(toAdd);
            Collections.sort(timePeriodsWithBrakesExcluded);
        }


        return timePeriodsWithBrakesExcluded;
    }

    public TimePeriod getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(TimePeriod workingHours) {
        this.workingHours = workingHours;
    }

    public List<TimePeriod> getBrakes() {
        return brakes;
    }

    public void setBrakes(List<TimePeriod> brakes) {
        this.brakes = brakes;
    }

    public void removeBrake(TimePeriod brakeToRemove) {
        brakes.remove(brakeToRemove);
    }

    public void addBrake(TimePeriod brakeToAdd) {
        brakes.add(brakeToAdd);
    }

}
