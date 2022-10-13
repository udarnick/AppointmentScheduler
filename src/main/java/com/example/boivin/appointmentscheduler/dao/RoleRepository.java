package com.example.boivin.appointmentscheduler.dao;

import com.example.boivin.appointmentscheduler.entity.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String roleName);
}
