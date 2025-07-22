package com.nbl.npa.report;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReportMasterRepository extends JpaRepository<ReportMaster, Integer> {
}
