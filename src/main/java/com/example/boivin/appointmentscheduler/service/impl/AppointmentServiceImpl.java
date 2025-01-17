package com.example.boivin.appointmentscheduler.service.impl;

import com.example.boivin.appointmentscheduler.dao.AppointmentRepository;
import com.example.boivin.appointmentscheduler.dao.ChatMessageRepository;
import com.example.boivin.appointmentscheduler.entity.*;
import com.example.boivin.appointmentscheduler.entity.user.User;
import com.example.boivin.appointmentscheduler.entity.user.provider.Provider;
import com.example.boivin.appointmentscheduler.exception.AppointmentNotFoundException;
import com.example.boivin.appointmentscheduler.model.DayPlan;
import com.example.boivin.appointmentscheduler.model.TimePeriod;
import com.example.boivin.appointmentscheduler.service.AppointmentService;
import com.example.boivin.appointmentscheduler.service.NotificationService;
import com.example.boivin.appointmentscheduler.service.UserService;
import com.example.boivin.appointmentscheduler.service.WorkService;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final int NUMBER_OF_ALLOWED_CANCELATIONS_PER_MONTH = 1;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final WorkService workService;
    private final ChatMessageRepository chatMessageRepository;
    private final NotificationService notificationService;
    private final JwtTokenServiceImpl jwtTokenService;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, UserService userService, WorkService workService, ChatMessageRepository chatMessageRepository, NotificationService notificationService, JwtTokenServiceImpl jwtTokenService) {
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.workService = workService;
        this.chatMessageRepository = chatMessageRepository;
        this.notificationService = notificationService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void updateAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);
    }

    @Override
    @PostAuthorize("returnObject.provider.id == principal.id or returnObject.customer.id == principal.id or hasRole('ADMIN') ")
    public Appointment getAppointmentByIdWithAuthorization(int id) {
        return getAppointmentById(id);
    }

    @Override
    public Appointment getAppointmentById(int id) {
        return appointmentRepository.findById(id)
                .orElseThrow(AppointmentNotFoundException::new);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public void deleteAppointmentById(int id) {
        appointmentRepository.deleteById(id);
    }

    @Override
    @PreAuthorize("#customerId == principal.id")
    public List<Appointment> getAppointmentByCustomerId(int customerId) {
        return appointmentRepository.findByCustomerId(customerId);
    }

    @Override
    @PreAuthorize("#providerId == principal.id")
    public List<Appointment> getAppointmentByProviderId(int providerId) {
        return appointmentRepository.findByProviderId(providerId);
    }

    @Override
    public List<Appointment> getAppointmentsByProviderAtDay(int providerId, LocalDate day) {
        return appointmentRepository.findByProviderIdWithStartInPeriod(providerId, day.atStartOfDay(), day.atStartOfDay().plusDays(1));
    }

    @Override
    public List<Appointment> getAppointmentsByCustomerAtDay(int providerId, LocalDate day) {
        return appointmentRepository.findByCustomerIdWithStartInPeriod(providerId, day.atStartOfDay(), day.atStartOfDay().plusDays(1));
    }

    @Override
    public List<TimePeriod> getAvailableHours(int providerId, int customerId, int workId, LocalDate date) {
        Provider p = userService.getProviderById(providerId);
        WorkingPlan workingPlan = p.getWorkingPlan();
        DayPlan selectedDay = workingPlan.getDay(date.getDayOfWeek().toString().toLowerCase());

        List<Appointment> providerAppointments = getAppointmentsByProviderAtDay(providerId, date);
        List<Appointment> customerAppointments = getAppointmentsByCustomerAtDay(customerId, date);

        List<TimePeriod> availablePeriods = selectedDay.getTimePeriodsWithBrakesExcluded();
        availablePeriods = excludeAppointmentsFromTimePeriods(availablePeriods, providerAppointments);

        availablePeriods = excludeAppointmentsFromTimePeriods(availablePeriods, customerAppointments);
        return calculateAvailableHours(availablePeriods, workService.getWorkById(workId));
    }

    @Override
    public void createNewAppointment(int workId, int providerId, int customerId, LocalDateTime start) {
        if (isAvailable(workId, providerId, customerId, start)) {
            Appointment appointment = new Appointment();
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointment.setCustomer(userService.getCustomerById(customerId));
            appointment.setProvider(userService.getProviderById(providerId));
            Work work = workService.getWorkById(workId);
            appointment.setWork(work);
            appointment.setStart(start);
            appointment.setEnd(start.plusMinutes(work.getDuration()));
            appointmentRepository.save(appointment);
            notificationService.newNewAppointmentScheduledNotification(appointment, true);
        } else {
            throw new RuntimeException();
        }

    }

    @Override
    public void addMessageToAppointmentChat(int appointmentId, int authorId, ChatMessage chatMessage) {
        Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);
        if (appointment.getProvider().getId() == authorId || appointment.getCustomer().getId() == authorId) {
            chatMessage.setAuthor(userService.getUserById(authorId));
            chatMessage.setAppointment(appointment);
            chatMessage.setCreatedAt(LocalDateTime.now());
            chatMessageRepository.save(chatMessage);
            notificationService.newChatMessageNotification(chatMessage, true);
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
    }

    @Override
    public List<TimePeriod> calculateAvailableHours(List<TimePeriod> availableTimePeriods, Work work) {
        ArrayList<TimePeriod> availableHours = new ArrayList();
        for (TimePeriod period : availableTimePeriods) {
            TimePeriod workPeriod = new TimePeriod(period.getStart(), period.getStart().plusMinutes(work.getDuration()));
            while (workPeriod.getEnd().isBefore(period.getEnd()) || workPeriod.getEnd().equals(period.getEnd())) {
                availableHours.add(new TimePeriod(workPeriod.getStart(), workPeriod.getStart().plusMinutes(work.getDuration())));
                workPeriod.setStart(workPeriod.getStart().plusMinutes(work.getDuration()));
                workPeriod.setEnd(workPeriod.getEnd().plusMinutes(work.getDuration()));
            }
        }
        return availableHours;
    }

    @Override
    public List<TimePeriod> excludeAppointmentsFromTimePeriods(List<TimePeriod> periods, List<Appointment> appointments) {

        List<TimePeriod> toAdd = new ArrayList();
        Collections.sort(appointments);
        for (Appointment appointment : appointments) {
            for (TimePeriod period : periods) {
                if ((appointment.getStart().toLocalTime().isBefore(period.getStart()) || appointment.getStart().equals(period.getStart())) && appointment.getEnd().toLocalTime().isAfter(period.getStart()) && appointment.getEnd().toLocalTime().isBefore(period.getEnd())) {
                    period.setStart(appointment.getEnd().toLocalTime());
                }
                if (appointment.getStart().toLocalTime().isAfter(period.getStart()) && appointment.getStart().toLocalTime().isBefore(period.getEnd()) && appointment.getEnd().toLocalTime().isAfter(period.getEnd()) || appointment.getEnd().equals(period.getEnd())) {
                    period.setEnd(appointment.getStart().toLocalTime());
                }
                if (appointment.getStart().toLocalTime().isAfter(period.getStart()) && appointment.getEnd().toLocalTime().isBefore(period.getEnd())) {
                    toAdd.add(new TimePeriod(period.getStart(), appointment.getStart().toLocalTime()));
                    period.setStart(appointment.getEnd().toLocalTime());
                }
            }
        }
        periods.addAll(toAdd);
        Collections.sort(periods);
        return periods;
    }

    @Override
    public List<Appointment> getCanceledAppointmentsByCustomerIdForCurrentMonth(int customerId) {
        return appointmentRepository.findByCustomerIdCanceledAfterDate(customerId, LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay());
    }

    @Override
    public void updateUserAppointmentsStatuses(int userId) {
        for (Appointment appointment : appointmentRepository.findScheduledByUserIdWithEndBeforeDate(LocalDateTime.now(), userId)) {
            appointment.setStatus(AppointmentStatus.FINISHED);
            updateAppointment(appointment);
        }

        for (Appointment appointment : appointmentRepository.findFinishedByUserIdWithEndBeforeDate(LocalDateTime.now().minusDays(1), userId)) {

            appointment.setStatus(AppointmentStatus.INVOICED);
            updateAppointment(appointment);
        }
    }

    @Override
    public void updateAllAppointmentsStatuses() {
        appointmentRepository.findScheduledWithEndBeforeDate(LocalDateTime.now())
                .forEach(appointment -> {
                    appointment.setStatus(AppointmentStatus.FINISHED);
                    updateAppointment(appointment);
                    if (LocalDateTime.now().minusDays(1).isBefore(appointment.getEnd())) {
                        notificationService.newAppointmentFinishedNotification(appointment, true);
                    }
                });

        appointmentRepository.findFinishedWithEndBeforeDate(LocalDateTime.now().minusDays(1))
                .forEach(appointment -> {
                    appointment.setStatus(AppointmentStatus.CONFIRMED);
                    updateAppointment(appointment);
                });
    }

    @Override
    public void updateAppointmentsStatusesWithExpiredExchangeRequest() {
        appointmentRepository.findExchangeRequestedWithStartBefore(LocalDateTime.now().plusDays(1))
                .forEach(appointment -> {
                    appointment.setStatus(AppointmentStatus.SCHEDULED);
                    updateAppointment(appointment);
                });
    }

    @Override
    public void cancelUserAppointmentById(int appointmentId, int userId) {
        Appointment appointment = appointmentRepository.getOne(appointmentId);
        if (appointment.getCustomer().getId() == userId || appointment.getProvider().getId() == userId) {
            appointment.setStatus(AppointmentStatus.CANCELED);
            User canceler = userService.getUserById(userId);
            appointment.setCanceler(canceler);
            appointment.setCanceledAt(LocalDateTime.now());
            appointmentRepository.save(appointment);
            if (canceler.equals(appointment.getCustomer())) {
                notificationService.newAppointmentCanceledByCustomerNotification(appointment, true);
            } else if (canceler.equals(appointment.getProvider())) {
                notificationService.newAppointmentCanceledByProviderNotification(appointment, true);
            }
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }


    }


    @Override
    public boolean isCustomerAllowedToRejectAppointment(int userId, int appointmentId) {
        User user = userService.getUserById(userId);
        Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);

        return appointment.getCustomer().equals(user) && appointment.getStatus().equals(AppointmentStatus.FINISHED) && !LocalDateTime.now().isAfter(appointment.getEnd().plusDays(1));
    }

    @Override
    public boolean requestAppointmentRejection(int appointmentId, int customerId) {
        if (isCustomerAllowedToRejectAppointment(customerId, appointmentId)) {
            Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);
            appointment.setStatus(AppointmentStatus.REJECTION_REQUESTED);
            notificationService.newAppointmentRejectionRequestedNotification(appointment, true);
            updateAppointment(appointment);
            return true;
        } else {
            return false;
        }

    }


    @Override
    public boolean requestAppointmentRejection(String token) {
        if (jwtTokenService.validateToken(token)) {
            int appointmentId = jwtTokenService.getAppointmentIdFromToken(token);
            int customerId = jwtTokenService.getCustomerIdFromToken(token);
            return requestAppointmentRejection(appointmentId, customerId);
        }
        return false;
    }


    @Override
    public boolean isProviderAllowedToAcceptRejection(int providerId, int appointmentId) {
        User user = userService.getUserById(providerId);
        Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);

        return appointment.getProvider().equals(user) && appointment.getStatus().equals(AppointmentStatus.REJECTION_REQUESTED);
    }

    @Override
    public boolean acceptRejection(int appointmentId, int customerId) {
        if (isProviderAllowedToAcceptRejection(customerId, appointmentId)) {
            Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);
            appointment.setStatus(AppointmentStatus.REJECTED);
            updateAppointment(appointment);
            notificationService.newAppointmentRejectionAcceptedNotification(appointment, true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean acceptRejection(String token) {
        if (jwtTokenService.validateToken(token)) {
            int appointmentId = jwtTokenService.getAppointmentIdFromToken(token);
            int providerId = jwtTokenService.getProviderIdFromToken(token);
            return acceptRejection(appointmentId, providerId);
        }
        return false;
    }

    @Override
    public String getCancelNotAllowedReason(int userId, int appointmentId) {
        User user = userService.getUserById(userId);
        Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);

        if (user.hasRole("ROLE_ADMIN")) {
            return "Only customer or provider can cancel appointments";
        }

        if (appointment.getProvider().equals(user)) {
            if (!appointment.getStatus().equals(AppointmentStatus.SCHEDULED)) {
                return "Only appoinmtents with scheduled status can be cancelled.";
            } else {
                return null;
            }
        }

        if (appointment.getCustomer().equals(user)) {
            if (!appointment.getStatus().equals(AppointmentStatus.SCHEDULED)) {
                return "Only appoinmtents with scheduled status can be cancelled.";
            } else if (LocalDateTime.now().plusDays(1).isAfter(appointment.getStart())) {
                return "Appointments which will be in less than 24 hours cannot be canceled.";
            } else if (!appointment.getWork().getEditable()) {
                return "This type of appointment can be canceled only by Provider.";
            } else if (getCanceledAppointmentsByCustomerIdForCurrentMonth(userId).size() >= NUMBER_OF_ALLOWED_CANCELATIONS_PER_MONTH) {
                return "You can't cancel this appointment because you exceeded maximum number of cancellations in this month.";
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public int getNumberOfCanceledAppointmentsForUser(int userId) {
        return appointmentRepository.findCanceledByUser(userId).size();
    }

    @Override
    public int getNumberOfScheduledAppointmentsForUser(int userId) {
        return appointmentRepository.findScheduledByUserId(userId).size();
    }

    @Override
    public boolean isAvailable(int workId, int providerId, int customerId, LocalDateTime start) {
        if (!workService.isWorkForCustomer(workId, customerId)) {
            return false;
        }
        Work work = workService.getWorkById(workId);
        TimePeriod timePeriod = new TimePeriod(start.toLocalTime(), start.toLocalTime().plusMinutes(work.getDuration()));
        
        return getAvailableHours(providerId, customerId, workId, start.toLocalDate()).contains(timePeriod);
    }

    @Override
    public List<Appointment> getConfirmedAppointmentsByCustomerId(int customerId) {
        return appointmentRepository.findConfirmedByCustomerId(customerId);
    }
}
