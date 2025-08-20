package com.nbl.npa.Controller.Frontend;

import com.nbl.npa.Config.AES256;
import com.nbl.npa.Config.Credentials;
import com.nbl.npa.report.ReportCompilation;
import com.nbl.npa.report.ReportExporter;
import com.nbl.npa.report.ReportMasterService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.crypto.Cipher;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@Controller
public class ReportController {
    private  final HttpSession session;
    private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    ReportCompilation reportCompilation;



    @Autowired
    ReportExporter reportExporter;



    @Autowired
    ReportMasterService reportService;

    @Autowired
    Credentials creds;

//    @GetMapping("/pension_report")
//    public String report(Model model) {
//        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(), Cipher.DECRYPT_MODE));
//        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));
//        return "pension_report";
//    }








    @GetMapping(value = "/pension_report")
    public String report(@RequestParam(required = false) String fromDate,
                         @RequestParam(required = false) String toDate,
                         Model model, HttpSession session) {
        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(), Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(), Cipher.DECRYPT_MODE));

        try {
            String userType = AES256.processCrypto(session.getAttribute("userType").toString(), Cipher.DECRYPT_MODE);

            // Default to current date if not provided
            LocalDate today = LocalDate.now();
            if (fromDate == null) {
                fromDate = today.toString(); // format yyyy-MM-dd
            }
            if (toDate == null) {
                toDate = today.toString();
            }

            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            model.addAttribute("reportList", reportService.getAll());

            if (Objects.equals(userType, "Admin")) {
                model.addAttribute("branchList", reportService.getAllBranch());
            }

            return "pension_report";
        } catch (Exception e) {
            session.invalidate();
            return creds.getRedirectURL();
        }
    }


    @PostMapping("/report-print")
    public ResponseEntity<byte[]> printReport(@RequestParam(value = "fromDate", required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fromDate,
                                              @RequestParam(value = "toDate", required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date toDate,
                                              @RequestParam(value = "branchInfo", required = false, defaultValue = "") String branchInfo,
                                              @RequestParam(value = "reportId", required = false) Integer reportId,
                                              HttpSession session, HttpServletResponse response) throws IOException, JRException, SQLException, ParseException {
        String branch = (branchInfo == null || branchInfo.isEmpty()) ? null : branchInfo;
        String userType = AES256.processCrypto(session.getAttribute("userType").toString(), Cipher.DECRYPT_MODE);
        if(Objects.equals(userType, "User")){
            branch = AES256.processCrypto(session.getAttribute("brCode").toString(), Cipher.DECRYPT_MODE);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("p_from_date", fromDate);
        parameters.put("p_to_date", toDate);
        parameters.put("p_br_code", branch);
        JasperReport report = null;
        try {
            report = reportCompilation.compileReports(reportId);
        } catch (FileNotFoundException e){
            System.out.println("|EX-SA-RC-01: " + e.getMessage());
        } catch (IOException e){
            System.out.println("|EX-SA-RC-02: " + e.getMessage());
        } catch (JRException e){
            System.out.println("|EX-SA-RC-03: " + e.getMessage());
        } catch (Exception e){
            System.out.println("|EX-SA-RC-04: " + e.getMessage());
        }

        byte[]              bytes      = reportExporter.uploadPDFReport(reportId, report, parameters, response);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("users.pdf").build());
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf; charset=UTF-8")
                .headers(headers)
                .body(bytes);
    }
}
