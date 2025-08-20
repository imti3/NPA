package com.nbl.npa.Controller.Frontend;


import com.nbl.npa.Config.AES256;
import com.nbl.npa.Model.DTO.CompanyInvoiceDTO;
import com.nbl.npa.Model.DTO.CompanySlipRow;
import com.nbl.npa.Model.DTO.IndividualPensionDuesDTO;
import com.nbl.npa.Model.DTO.PensionSlipRow;
import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import com.nbl.npa.Model.Repo.NpaCustomerRepository;
import com.nbl.npa.Model.Repo.NpaIndividualRepository;
import com.nbl.npa.Service.InitialCompanyInvoiceService;
import com.nbl.npa.Service.IndividualPensionDueService;
import com.nbl.npa.Service.NpaCompanyPaymentService;
import com.nbl.npa.Service.NpaPaymentIndividualService;
import com.nbl.npa.report.ReportSources;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class CollectionController {


    private final ReportSources reportSources;

    private final IndividualPensionDueService initialPaymentIndividualService;
    private final NpaCompanyPaymentService npaCompanyPaymentService;
    private final InitialCompanyInvoiceService initialCompanyInvoiceService;
    private final NpaPaymentIndividualService npaPaymentIndividualService;
    private  final HttpSession session;
    private final NpaCustomerRepository npaCustomerRepository;
    private final NpaIndividualRepository npaIndividualRepository;


    @GetMapping("/pension_collect")
    public String showForm(Model model) {

        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(), Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));
        return "pension";
    }



    @PostMapping("/initial")
    public String handleFormSubmission(@RequestParam("idNumber") String idNumber, Model model) {

        String decryptedUserID = AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE);
        Integer decryptedBrCode = Integer.parseInt(AES256.processCrypto(session.getAttribute("brCode").toString(),Cipher.DECRYPT_MODE));
        IndividualPensionDuesDTO response = initialPaymentIndividualService.getPensionerDetails
                (idNumber,decryptedUserID,decryptedBrCode);

        if (response.getCode() == 200) {
            model.addAttribute("pensionData", response.getData());
            session.setAttribute("pensionerData", response.getData());
        } else {
            model.addAttribute("errorMessage", response.getMessage());
        }
        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));

        return "pension_indivi";
    }

    @GetMapping("/pension/slip")
    public ResponseEntity<byte[]> printPensionSlip(
            @RequestParam(value = "id", required = false) Long paymentId,
            @RequestParam(value = "finalPaidAmount", required = false) BigDecimal finalPaidAmount,
            @RequestParam(value = "finalPaidCount", required = false) Integer finalPaidCount,
            HttpSession session) throws Exception {

        String branchName = AES256.processCrypto(session.getAttribute("brName").toString(), Cipher.DECRYPT_MODE);
        String userName   = AES256.processCrypto(session.getAttribute("userId").toString(), Cipher.DECRYPT_MODE);


        IndividualPensionDuesDTO.PensionerData pensionData =
                (IndividualPensionDuesDTO.PensionerData) session.getAttribute("pensionerData");
        session.removeAttribute("pensionerData");

        TblNpaPaymentIndividualEntity entity = null;
        if (paymentId != null) {
            entity = npaPaymentIndividualService.findIndividualById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found for id=" + paymentId));
        }
        if (pensionData == null && entity == null) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .header("X-Reason", "No pensioner or payment data found.")
                    .build();
        }

        // Build Jasper params
        Map<String, Object> params = new HashMap<>();
        params.put("p_branch_name", branchName);
        params.put("p_user_name", userName);
        params.put("p_print_date", new java.util.Date());

        // Prefer URL params if provided
        if (finalPaidAmount != null) {
            params.put("p_final_paid_amount", finalPaidAmount);
        } else if (entity != null) {
            params.put("p_final_paid_amount", entity.getPaidAmount() != null ? entity.getPaidAmount() : BigDecimal.ZERO);
        } else {
            params.put("p_final_paid_amount", BigDecimal.ZERO);
        }

        if (finalPaidCount != null) {
            params.put("p_final_paid_count", finalPaidCount);
        } else if (entity != null) {
            params.put("p_final_paid_count", entity.getPaidCount() != null ? entity.getPaidCount().intValue() : 0);
        } else {
            params.put("p_final_paid_count", 0);
        }

        JRBeanCollectionDataSource ds = (pensionData != null)
                ? new JRBeanCollectionDataSource(List.of(PensionSlipRow.from(pensionData, branchName)))
                : new JRBeanCollectionDataSource(List.of(PensionSlipRow.fromEntity(entity, branchName)));

        File reportFile = ResourceUtils.getFile(reportSources.getSourceReport("pension_ack_slip.jrxml"));
        JasperReport report = JasperCompileManager.compileReport(reportFile.getAbsolutePath());
        JasperPrint print   = JasperFillManager.fillReport(report, params, ds);
        byte[] pdf          = JasperExportManager.exportReportToPdf(print);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("pension-ack-slip.pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }



    @GetMapping("/company/slip")
    public ResponseEntity<byte[]> printCompanySlip(
            @RequestParam("id") Long paymentId,
            HttpSession session) throws Exception {

        String branchName = AES256.processCrypto(session.getAttribute("brName").toString(), Cipher.DECRYPT_MODE);
        String userName   = AES256.processCrypto(session.getAttribute("userId").toString(), Cipher.DECRYPT_MODE);

        TblNpaCompanyPaymentEntity entity = npaCompanyPaymentService.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Company payment not found for id=" + paymentId));

        Map<String, Object> params = new HashMap<>();
        params.put("p_branch_name", branchName);
        params.put("p_user_name", userName);
        params.put("p_print_date", new java.util.Date());


        CompanySlipRow row = CompanySlipRow.fromEntity(entity, branchName);

        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(List.of(row));

        File reportFile = ResourceUtils.getFile(reportSources.getSourceReport("pension_ack_slip_company.jrxml"));
        JasperReport report = JasperCompileManager.compileReport(reportFile.getAbsolutePath());
        JasperPrint print   = JasperFillManager.fillReport(report, params, ds);
        byte[] pdf          = JasperExportManager.exportReportToPdf(print);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("company-ack-slip.pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }










    @PostMapping("/initial_company")
    public String handleFormSubmissionCompany(
            @RequestParam("idNumber") String idNumber,
            @RequestParam("invoiceNOInput") String invoiceNo,
            Model model) {

        CompanyInvoiceDTO response = initialCompanyInvoiceService.getPensionerDetails(idNumber,invoiceNo);
        if (response.getCode() == 200) {
            model.addAttribute("invoiceData", response.getData());
        } else {
            model.addAttribute("errorMessage", response.getMessage());
        }

        // You can use idNumber and invoiceNo here
        System.out.println("ID Number: " + idNumber);
        System.out.println("Invoice No: " + invoiceNo);

        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));

        return "company"; // Return view name
    }
    // POST endpoint
    @PostMapping("/make_individual_payment")
    public String handleIndividualPayment(
            Model model,
            @RequestParam("Payment_Ref_No") String paymentRefNo,
            @RequestParam("NID") String nid,
            @RequestParam("PensionHolderName") String pensionHolderName,
            @RequestParam("PensionPhoneNo") String pensionPhoneNo,
            @RequestParam("PensionEmail") String pensionEmail,
            @RequestParam("InstallAmount") BigDecimal installAmount,
            @RequestParam("Paid_Amount") BigDecimal paidAmount,
            @RequestParam("PID") String pid,
            @RequestParam("Paying_Install_Count") BigDecimal payingInstallCount,
            @RequestParam("Paying_Install_Amount") BigDecimal payingInstallAmount,
            @RequestParam("Commission_Amount") BigDecimal commissionAmount,
            @RequestParam("VAT_Amount") BigDecimal vatAmount,
            @RequestParam("CreditAccount") String creditAccount,
            @RequestParam("Additional_Amount") BigDecimal additionalAmount,
            @RequestParam("PayIntervalType") String payIntervalType,
            @RequestParam("SchemeName") String schemeName,
            @RequestParam("TotalDueInstallCount") BigDecimal totalDueInstallCount,
            @RequestParam("TotalDueInstallAmount") BigDecimal totalDueInstallAmount,
            @RequestParam("TotalDueLoanCount") BigDecimal totalDueLoanCount,
            @RequestParam("TotalDueLoanAmount") BigDecimal totalDueLoanAmount,
            @RequestParam("TotalFineAmount") BigDecimal totalFineAmount,
            @RequestParam("GrandTotalDueAmount") BigDecimal grandTotalDueAmount,
            @RequestParam("GrandTotalDueCount") BigDecimal grandTotalDueCount,
            @RequestParam("advance_installments") BigDecimal advanceInstallmentCount,
            @RequestParam("advance_payment_total") BigDecimal advancePaymentTotal,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        try {
            TblNpaPaymentIndividualEntity saved;

            boolean testMode = false; // 🔹 toggle this (or use @Value from application.properties)

            if (testMode) {
                // ✅ Create fake object
                saved = new TblNpaPaymentIndividualEntity();
                saved.setTransactionStatus(1);
                saved.setPaymentRefNo("TEST-REF-" + System.currentTimeMillis());
                saved.setPaidAmount(paidAmount);
                saved.setPaidCount(payingInstallCount);
                saved.setNid(nid);
                saved.setSchemeName(schemeName);
                saved.setCommissionAmount(commissionAmount);
                saved.setAdditionalAmount(additionalAmount);
                saved.setExpired(0);
                saved.setInstallAmount(installAmount);

                // ✅ Save into DB so /pension/slip can find it
                saved = npaIndividualRepository.save(saved);

            } else {

                saved = npaPaymentIndividualService.initiateAndSave(
                        paymentRefNo, nid, pensionHolderName, pensionPhoneNo, pensionEmail,
                        installAmount, paidAmount, pid, payingInstallCount, payingInstallAmount,
                        commissionAmount, vatAmount, creditAccount, additionalAmount,
                        payIntervalType, schemeName, totalDueInstallCount, totalDueInstallAmount,
                        totalDueLoanCount, totalDueLoanAmount, totalFineAmount,
                        grandTotalDueCount, grandTotalDueAmount, advanceInstallmentCount, advancePaymentTotal
                );
            }



            if (saved.getTransactionStatus() == 1) {
                redirectAttributes.addFlashAttribute("slipUrl", "/pension/slip?id=" + saved.getId());
                return "redirect:/payment_confirmation";

            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Payment was initiated but not confirmed. Please try again.");
                return "redirect:/payment_confirmation";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment failed: " + e.getMessage());
            return "redirect:/payment_confirmation";
        }
    }





    @GetMapping("/payment_confirmation")
    public String showPaymentConfirmation() {
        return "payment_confirmation"; // Just returns the Thymeleaf view
    }


    @PostMapping("/make_company_payment")
    public String handleCompanyPayment(
            Model model,
            @RequestParam("Payment_Ref_No") String paymentRefNo,
            @RequestParam("CompanyPID") String companyPID,
            @RequestParam("invoiceNo") String invoiceNo,
            @RequestParam("invDate") String invDate,
            @RequestParam("paymentForMonth") String paymentForMonth,
            @RequestParam("totalEmp") int totalEmp,
            @RequestParam("totalAmount") BigDecimal totalAmount,
            @RequestParam("companyTitle") String companyTitle,
            @RequestParam("contactPerson") String contactPerson,
            @RequestParam("contactMobile") String contactMobile,
            @RequestParam("companyAddress") String companyAddress,
            @RequestParam("economicCode") int economicCode,
            @RequestParam("Currency") String currency,
            @RequestParam("Scheme_name") String schemeName,
            @RequestParam("AccNo") String accNo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            TblNpaCompanyPaymentEntity saved = npaCompanyPaymentService.initiateAndSave(
                    paymentRefNo, companyPID, invoiceNo, invDate, paymentForMonth, totalEmp,
                    totalAmount, companyTitle, contactPerson, contactMobile, companyAddress,
                    economicCode, currency, schemeName, accNo
            );

            if (saved.getTransactionStatus() == 1) {
                // Pass the slip URL for auto-open
                redirectAttributes.addFlashAttribute("slipUrl", "/company/slip?id=" + saved.getId());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Company payment was initiated but not confirmed.");
            }

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment failed: " + ex.getMessage());
        }

        // (Optional) these are for showing user/branch on the confirmation page
        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(), Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(), Cipher.DECRYPT_MODE));

        return "redirect:/company_payment_confirmation";

    }


    @GetMapping("/company_payment_confirmation")
    public String showCompanyPaymentConfirmationPage() {
        // No need to add model attributes here since flash attributes are automatically added
        return "company_payment_confirmation";  // your Thymeleaf template name without `.html`
    }
















    // Handle POST request from Step 1 form submission
//    @PostMapping("/pension_collect")
//    public String processForm(
//            @RequestParam("type") String pensionerType,
//            @RequestParam("nidType") String nidType,
//            @RequestParam("idNumber") String idNumber,
//            Model model) {
//        // Log or process pensionerType and nidType if needed
//        System.out.println("Pensioner Type: " + pensionerType);
//        System.out.println("ID Type: " + nidType);
//
//        // Fetch pension data using idNumber
//        IntitialPamentIndividualDTO response = intialPamentIndividualService.getPensionerDetails(idNumber);
//        model.addAttribute("pensionData", response.getData());
//
//        // Pass pensionerType and nidType to the view if needed
//        model.addAttribute("pensionerType", pensionerType);
//        model.addAttribute("nidType", nidType);
//
//        return "pension";
//    }


}


