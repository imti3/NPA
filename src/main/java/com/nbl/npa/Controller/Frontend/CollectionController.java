package com.nbl.npa.Controller.Frontend;


import com.nbl.npa.Config.AES256;
import com.nbl.npa.Model.DTO.CompanyInvoiceDTO;
import com.nbl.npa.Model.DTO.IndividualPensionDuesDTO;
import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import com.nbl.npa.Service.InitialCompanyInvoiceService;
import com.nbl.npa.Service.IndividualPensionDueService;
import com.nbl.npa.Service.NpaCompanyPaymentService;
import com.nbl.npa.Service.NpaPaymentIndividualService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.Cipher;
import java.math.BigDecimal;

@RequiredArgsConstructor
@Controller
public class CollectionController {

    private final IndividualPensionDueService initialPaymentIndividualService;
    private final NpaCompanyPaymentService npaCompanyPaymentService;
    private final InitialCompanyInvoiceService initialCompanyInvoiceService;
    private final NpaPaymentIndividualService npaPaymentIndividualService;
    private  final HttpSession session;


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
        } else {
            model.addAttribute("errorMessage", response.getMessage());
        }
        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));

        return "pension_indivi"; // View name
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
            @RequestParam("PensionHolderName") String PensionHolderName,
            @RequestParam("PensionPhoneNo") String PensionPhoneNo,
            @RequestParam("PensionEmail") String PensionEmail,
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
            RedirectAttributes redirectAttributes
    ) {
        try {
            TblNpaPaymentIndividualEntity saved = npaPaymentIndividualService.initiateAndSave(
                    paymentRefNo, nid, PensionHolderName, PensionPhoneNo, PensionEmail,
                    installAmount, paidAmount, pid, payingInstallCount, payingInstallAmount,
                    commissionAmount, vatAmount, creditAccount, additionalAmount,
                    payIntervalType, schemeName, totalDueInstallCount, totalDueInstallAmount,
                    totalDueLoanCount, totalDueLoanAmount, totalFineAmount,
                    grandTotalDueCount, grandTotalDueAmount, advanceInstallmentCount, advancePaymentTotal
            );

            if (saved.getTransactionStatus() == 1) {
                redirectAttributes.addFlashAttribute("paymentData", saved);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Payment was initiated but not confirmed. Please try again.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment failed: " + e.getMessage());
        }

        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));

        return "redirect:/payment_confirmation";
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
                redirectAttributes.addFlashAttribute("companyPaymentData", saved);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Company payment was initiated but not confirmed.");
            }

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment failed: " + ex.getMessage());
        }

        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));

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


