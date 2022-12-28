package com.example.boivin.appointmentscheduler.service.impl;

import com.example.boivin.appointmentscheduler.dao.WorkingPlanRepository;
import com.example.boivin.appointmentscheduler.entity.WorkingPlan;
import com.example.boivin.appointmentscheduler.model.TimePeriod;
import com.example.boivin.appointmentscheduler.security.CustomUserDetails;
import com.example.boivin.appointmentscheduler.service.WorkingPlanService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkingPlanServiceImpl implements WorkingPlanService {

    private final WorkingPlanRepository workingPlanRepository;

    public WorkingPlanServiceImpl(WorkingPlanRepository workingPlanRepository) {
        this.workingPlanRepository = workingPlanRepository;
    }

    @Override
    @PreAuthorize("#updateData.provider.id == principal.id")
    public void updateWorkingPlan(WorkingPlan updateData) {
        WorkingPlan workingPlan = workingPlanRepository.getOne(updateData.getId());
        workingPlan.getMonday().setWorkingHours(updateData.getMonday().getWorkingHours());
        workingPlan.getTuesday().setWorkingHours(updateData.getTuesday().getWorkingHours());
        workingPlan.getWednesday().setWorkingHours(updateData.getWednesday().getWorkingHours());
        workingPlan.getThursday().setWorkingHours(updateData.getThursday().getWorkingHours());
        workingPlan.getFriday().setWorkingHours(updateData.getFriday().getWorkingHours());
        workingPlan.getSaturday().setWorkingHours(updateData.getSaturday().getWorkingHours());
        workingPlan.getSunday().setWorkingHours(updateData.getSunday().getWorkingHours());
        workingPlanRepository.save(workingPlan);
    }

    @Override
    public void addBrakeToWorkingPlan(TimePeriod brakeToAdd, int planId, String dayOfWeek) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        WorkingPlan workingPlan = workingPlanRepository.getOne(planId);
//        if (workingPlan.getProvider().getId().equals(currentUser.getId())) {
//            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
//        }
        workingPlan.getDay(dayOfWeek).getBrakes().add(brakeToAdd);
        workingPlanRepository.save(workingPlan);
    }

    @Override
    public void deleteBrakeFromWorkingPlan(TimePeriod brakeToDelete, int planId, String dayOfWeek) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        WorkingPlan workingPlan = workingPlanRepository.getOne(planId);
//        if (workingPlan.getProvider().getId().equals(currentUser.getId())) {
//            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
//        }
        workingPlan.getDay(dayOfWeek).getBrakes().remove(brakeToDelete);
        workingPlanRepository.save(workingPlan);
    }


    @Override
    @PreAuthorize("#providerId == principal.id")
    public WorkingPlan getWorkingPlanByProviderId(int providerId) {
        return workingPlanRepository.getWorkingPlanByProviderId(providerId);
    }


}
