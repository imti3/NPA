package com.nbl.npa.report;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface ReportMasterRepository extends JpaRepository<ReportMaster, Integer> {
    @Query(value = "SELECT branch_code, br_name FROM view_branch_info", nativeQuery = true)
    List<Map<String, Object>> findRawBranchInfo();
}
