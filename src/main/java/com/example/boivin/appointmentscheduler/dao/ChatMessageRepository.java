package com.example.boivin.appointmentscheduler.dao;

import com.example.boivin.appointmentscheduler.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

}
