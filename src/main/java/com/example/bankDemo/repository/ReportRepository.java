package com.example.bankDemo.repository;

import com.example.bankDemo.entity.Report;
import com.example.bankDemo.enums.ReportPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByPeriodOrderByStartAtDesc(ReportPeriod period, Pageable pageable);

    List<Report> findTop24ByPeriodOrderByStartAtDesc(ReportPeriod period);
}
