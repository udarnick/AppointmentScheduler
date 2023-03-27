package com.example.boivin.appointmentscheduler.service;

import com.example.boivin.appointmentscheduler.entity.WorkingPlan;
import com.example.boivin.appointmentscheduler.model.TimePeriod;

public interface WorkingPlanService {
    void updateWorkingPlan(WorkingPlan workingPlan);

    void addBreakToWorkingPlan(TimePeriod breakToAdd, int planId, String dayOfWeek);

    void deleteBreakFromWorkingPlan(TimePeriod breakToDelete, int planId, String dayOfWeek);

    WorkingPlan getWorkingPlanByProviderId(int providerId);
}
