package com.nbl.npa.report;


import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;


public interface ReportMasterService {
    ReportMaster save(ReportMaster obj) throws IOException;

    ReportMaster update(ReportMaster obj) throws IOException;

    ReportMaster delete(ReportMaster obj) throws IOException;

    List<ReportMaster> getAll();

    ReportMaster getById(Integer id);

    List<ReportMaster> getAllActive();

    Page<ReportMaster> getPageableList(int page, int size);
}
