package com.example.boivin.appointmentscheduler.dao;

import com.example.boivin.appointmentscheduler.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query("select N from Notification N join N.user u where u.id = :userId and N.isRead=false")
    List<Notification> getAllUnreadNotifications(@Param("userId") int userId);
}
