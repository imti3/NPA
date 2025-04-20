package com.nbl.dashboard.npa.controller;


import com.nbl.dashboard.npa.Domain.Company.DTO.InitialCompanyInvoiceDTO;
import com.nbl.dashboard.npa.Domain.Individual.DTO.IntitialPamentIndividualDTO;
import com.nbl.dashboard.npa.Domain.IndividualPayment.DTO.IndividualPaymentDTO;
import com.nbl.dashboard.npa.service.IndividualPaymentService;
import com.nbl.dashboard.npa.service.InitialCompanyInvoiceService;
import com.nbl.dashboard.npa.service.InitialIndividualisationService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
public class CollectionController {

    private final InitialIndividualisationService initialPaymentIndividualService;
    private final InitialCompanyInvoiceService initialCompanyInvoiceService;
    private final IndividualPaymentService individualPaymentService;


    @GetMapping("/pension_collect")
    public String showForm() {
        return "pension";
    }



    @PostMapping("/initial")
    public String handleFormSubmission(@RequestParam("idNumber") String idNumber, Model model) {
        IntitialPamentIndividualDTO response = initialPaymentIndividualService.getPensionerDetails(idNumber);

        if (response.getCode() == 200) {
            model.addAttribute("pensionData", response.getData());
        } else {
            model.addAttribute("errorMessage", response.getMessage());
        }

        return "pension_indivi"; // View name
    }

    @PostMapping("/initial_company")
    public String handleFormSubmissionCompany(
            @RequestParam("idNumber") String idNumber,
            @RequestParam("invoiceNOInput") String invoiceNo,
            Model model) {

        InitialCompanyInvoiceDTO response = initialCompanyInvoiceService.getPensionerDetails(idNumber,invoiceNo);
        if (response.getCode() == 200) {
            model.addAttribute("invoiceData", response.getData());
        } else {
            model.addAttribute("errorMessage", response.getMessage());
        }

        // You can use idNumber and invoiceNo here
        System.out.println("ID Number: " + idNumber);
        System.out.println("Invoice No: " + invoiceNo);

        return "company"; // Return view name
    }
    // POST endpoint
    @PostMapping("/make_individual_payment")
    public String handleIndividualPayment(
            @RequestParam("Payment_Ref_No") String paymentRefNo,
            @RequestParam("PID") String pid,
            @RequestParam("Paying_Install_Count") Long payingInstallCount,
            @RequestParam("Paying_Install_Amount") Long payingInstallAmount,
            @RequestParam("Commission_Amount") Long commissionAmount,
            @RequestParam("VAT_Amount") Long vatAmount,
            @RequestParam("CreditAccount") String creditAccount,
            @RequestParam("Additional_Amount") String additionalAmount,
            RedirectAttributes redirectAttributes) {

        IndividualPaymentDTO paymentResponse = individualPaymentService.getPaymentDetails(
                paymentRefNo,
                pid,
                payingInstallCount,
                payingInstallAmount,
                commissionAmount,
                vatAmount,
                creditAccount,
                additionalAmount
        );

        if (paymentResponse.getCode() == 200
        && paymentResponse.getMessage().equals("Successful")) {
            redirectAttributes.addFlashAttribute("paymentData", paymentResponse);
        } else {
            String message = paymentResponse.getMessage() != null
                    ? paymentResponse.getMessage()
                    : "Payment processing failed due to an unknown error.";
            redirectAttributes.addFlashAttribute("errorMessage", message);
        }

        return "redirect:/payment_confirmation";
    }

    @GetMapping("/payment_confirmation")
    public String showPaymentConfirmation() {
        return "payment_confirmation"; // Just returns the Thymeleaf view
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


