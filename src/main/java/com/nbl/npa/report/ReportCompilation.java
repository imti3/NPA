package com.nbl.npa.report;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Component
public class ReportCompilation {



    @Autowired
    ReportSources reportSources;

    @Autowired
    ReportMasterService reportMasterservice;


    public JasperReport compileReports(Integer id) throws JRException, IOException, FileNotFoundException {
        ReportMaster masterReport = reportMasterservice.getById(id);
        if (ObjectUtils.isEmpty(masterReport)) {
            return null;
        }

        String compileDir = reportSources.REPORT_COMPILE_DIR;
        Path compilePath = Paths.get(compileDir);
        if (!Files.exists(compilePath)) {
            Files.createDirectories(Paths.get(compileDir));
        }

        //Compile Master Report
        File reportFile = ResourceUtils.getFile(reportSources.getSourceReport(masterReport.getFileName()));

        if (!reportFile.exists()) {
            throw new FileNotFoundException("Sorry your requested file '" + reportFile.getName() + "' is not found at path '" + reportFile.getParent() + "'!");
        }

        JasperReport report = JasperCompileManager.compileReport(reportFile.getAbsolutePath());
        JRSaver.saveObject(report, reportSources.getCompiledReport(masterReport.getFileNameJasper()));


        return report;
    }
}
