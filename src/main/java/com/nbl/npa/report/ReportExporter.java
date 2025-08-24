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
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.engine.export.*;


@Component
public class ReportExporter {

    private static final Logger LOG = LoggerFactory.getLogger(ReportExporter.class);

    @Autowired
    DataSource dataSource;

    /**
     * Exports the given JasperReport to PDF.
     */
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
                LOG.error("Error exporting PDF report", e);
            }
            return bytes;
        } catch (Exception e) {
            LOG.error("Error exporting PDF report", e);
        } finally {
            connection.close();
        }
        return bytes;
    }

    /**
     * Exports the given JasperReport to XLSX.
     */
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
                LOG.error("Error exporting XLSX report", e);
            }
            return bytes;
        } catch (Exception e) {
            LOG.error("Error exporting XLSX report", e);
        } finally {
            connection.close();
        }
        return bytes;
    }

    /**
     * Exports the given JasperReport to RTF.
     */
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
                LOG.error("Error exporting RTF report", e);
            }
            return bytes;
        } catch (Exception e) {
            LOG.error("Error exporting RTF report", e);
        } finally {
            connection.close();
        }
        return bytes;
    }

    /**
     * Exports the given JasperReport to DOCX.
     */
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
                LOG.error("Error exporting DOCX report", e);
            }
            return bytes;
        } catch (Exception e) {
            LOG.error("Error exporting DOCX report", e);
        } finally {
            connection.close();
        }
        return bytes;
    }

}
