package com.example.boivin.appointmentscheduler.service;

import com.example.boivin.appointmentscheduler.entity.Appointment;
import com.example.boivin.appointmentscheduler.entity.ChatMessage;
import com.example.boivin.appointmentscheduler.entity.ExchangeRequest;
import com.example.boivin.appointmentscheduler.entity.Invoice;
import org.thymeleaf.context.Context;

import java.io.File;

public interface EmailService {
    void sendEmail(String to, String subject, String templateName, Context templateContext, File attachment);

    void sendAppointmentFinishedNotification(Appointment appointment);

    void sendAppointmentRejectionRequestedNotification(Appointment appointment);

    void sendNewAppointmentScheduledNotification(Appointment appointment);

    void sendAppointmentCanceledByCustomerNotification(Appointment appointment);

    void sendAppointmentCanceledByProviderNotification(Appointment appointment);

    void sendInvoice(Invoice invoice);

    void sendAppointmentRejectionAcceptedNotification(Appointment appointment);

    void sendNewChatMessageNotification(ChatMessage appointment);

    void sendNewExchangeRequestedNotification(Appointment oldAppointment, Appointment newAppointment);

    void sendExchangeRequestAcceptedNotification(ExchangeRequest exchangeRequest);

    void sendExchangeRequestRejectedNotification(ExchangeRequest exchangeRequest);
}
