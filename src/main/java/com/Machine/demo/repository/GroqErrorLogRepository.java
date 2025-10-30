package com.Machine.demo.repository;

import com.Machine.demo.model.GroqErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroqErrorLogRepository extends JpaRepository<GroqErrorLog, Long> {
}
