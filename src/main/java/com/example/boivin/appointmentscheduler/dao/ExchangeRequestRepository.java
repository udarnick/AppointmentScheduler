package com.example.boivin.appointmentscheduler.dao;

import com.example.boivin.appointmentscheduler.entity.ExchangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Integer> {
}
