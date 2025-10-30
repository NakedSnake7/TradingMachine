package com.Machine.demo.repository;

import com.Machine.demo.model.SignalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignalLogRepository extends JpaRepository<SignalLog, Long> {
}
