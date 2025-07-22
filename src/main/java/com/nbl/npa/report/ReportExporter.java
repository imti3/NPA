package com.nbl.npa.report;


import jakarta.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.pdf.JRPdfExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.engine.export.*;


@Component
public class ReportExporter {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ReportExporter.class);

    @Autowired
    DataSource dataSource;

    public byte[] uploadPDFReport(Integer id, JasperReport report, Map<String, Object> parameters, HttpServletResponse response) throws JRException, SQLException, IOException {
        byte[] bytes = null;
        Connection connection = dataSource.getConnection();
        try {
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);
            var input = new SimpleExporterInput(print);
            try (var byteArray = new ByteArrayOutputStream()) {
                var output = new SimpleOutputStreamExporterOutput(byteArray);
                var exporter = new JRPdfExporter();
                exporter.setExporterInput(input);
                exporter.setExporterOutput(output);
                exporter.exportReport();
                bytes = byteArray.toByteArray();
                output.close();
            } catch (IOException e) {
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return bytes;
    }

    public byte[] uploadXlsxReport(Integer id, JasperReport report, Map<String, Object> parameters, HttpServletResponse response) throws JRException, SQLException, IOException {
        byte[] bytes = null;
        Connection connection = dataSource.getConnection();
        try {
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);
            var input = new SimpleExporterInput(print);
            try (var byteArray = new ByteArrayOutputStream()) {
                var output = new SimpleOutputStreamExporterOutput(byteArray);

                SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
                config.setOnePagePerSheet(false);
                config.setRemoveEmptySpaceBetweenRows(true);
                config.setDetectCellType(true);
                config.setIgnoreGraphics(false);

                var exporter = new JRXlsxExporter();
                exporter.setExporterInput(input);
                exporter.setExporterOutput(output);
                exporter.setConfiguration(config);
                exporter.exportReport();
                bytes = byteArray.toByteArray();
                output.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return bytes;
    }

    public byte[] uploadRTF(Integer id, JasperReport report, Map<String, Object> parameters, HttpServletResponse response) throws JRException, SQLException, IOException {
        byte[] bytes = null;
        Connection connection = dataSource.getConnection();
        try {
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);
            var input = new SimpleExporterInput(print);
            try (var byteArray = new ByteArrayOutputStream()) {
                var output = new SimpleWriterExporterOutput(byteArray);
                var exporter = new JRRtfExporter();
                exporter.setExporterInput(input);
                exporter.setExporterOutput(output);
                exporter.exportReport();
                bytes = byteArray.toByteArray();
                output.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return bytes;
    }

    public byte[] getReportBytes(JasperReport report, Map<String, Object> parameters) throws JRException, SQLException, IOException {
        byte[]     bytes      = null;
        Connection connection = dataSource.getConnection();
        try {
            JasperPrint         print = JasperFillManager.fillReport(report, parameters, connection);
            SimpleExporterInput input = new SimpleExporterInput(print);
            try (var byteArray = new ByteArrayOutputStream()) {
                /*
                JRDocxExporter             exporter = new JRDocxExporter();
                SimpleWriterExporterOutput output   = new SimpleWriterExporterOutput(byteArray);

                exporter.setExporterInput(input);
                exporter.setExporterOutput((OutputStreamExporterOutput) output);

                exporter.exportReport();
                bytes = byteArray.toByteArray();
                output.close();
                */

                /*
                JRDocxExporter exporter = new JRDocxExporter();
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
                exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, "myreport.docx");
                exporter.exportReport();
                bytes = byteArray.toByteArray();
                */

                /*
                JRDocxExporter export = new JRDocxExporter();
                export.setExporterInput(new SimpleExporterInput(print));
                export.setExporterOutput(new SimpleOutputStreamExporterOutput(new File("report.docx")));

                SimpleDocxReportConfiguration config = new SimpleDocxReportConfiguration();
                //config.setFlexibleRowHeight(true); //Set desired configuration

                export.setConfiguration(config);
                export.exportReport();
                bytes = byteArray.toByteArray();
                */


                //Export to PDF
                // JasperExportManager.exportReportToPdfFile(jasperPrint, "/home/test/fileName.pdf");

                //Export to Word
                JRDocxExporter exporter = new JRDocxExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                File exportReportFile = new File("MyTestDocxFile" + ".docx");
                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(exportReportFile));
                exporter.exportReport();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return bytes;
    }

    public byte[] uploadDocxReport(JasperReport report, Map<String, Object> parameters) throws JRException, SQLException, IOException {
        byte[] bytes = null;
        Connection connection = dataSource.getConnection();
        try {
            JasperPrint print = JasperFillManager.fillReport(report, parameters, connection);
            var input = new SimpleExporterInput(print);
            try (var byteArray = new ByteArrayOutputStream()) {
                var output = new SimpleOutputStreamExporterOutput(byteArray);
                var exporter = new JRDocxExporter();
                exporter.setExporterInput(input);
                exporter.setExporterOutput(output);
                exporter.exportReport();
                bytes = byteArray.toByteArray();
                output.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return bytes;
    }

}
