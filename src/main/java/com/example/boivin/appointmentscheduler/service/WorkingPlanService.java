package com.example.boivin.appointmentscheduler.service;

import com.example.boivin.appointmentscheduler.entity.WorkingPlan;
import com.example.boivin.appointmentscheduler.model.TimePeriod;

public interface WorkingPlanService {
    void updateWorkingPlan(WorkingPlan workingPlan);

    void addBrakeToWorkingPlan(TimePeriod brakeToAdd, int planId, String dayOfWeek);

    void deleteBrakeFromWorkingPlan(TimePeriod brakeToDelete, int planId, String dayOfWeek);

    WorkingPlan getWorkingPlanByProviderId(int providerId);
}
