package com.example.boivin.appointmentscheduler.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.Objects;

public class TimePeriod implements Comparable<TimePeriod> {
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime start;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime end;

    public TimePeriod() {

    }

    public TimePeriod(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    @Override
    public int compareTo(TimePeriod o) {
        return this.getStart().compareTo(o.getStart());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimePeriod peroid = (TimePeriod) o;
        return this.start.equals(peroid.getStart()) &&

                this.end.equals(peroid.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "TimePeriod{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
