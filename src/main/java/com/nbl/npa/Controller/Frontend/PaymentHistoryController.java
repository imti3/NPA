package com.nbl.npa.Controller.Frontend;

import com.nbl.npa.Config.AES256;
import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import com.nbl.npa.Service.PaymentHistoryService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.crypto.Cipher;


@Controller
@RequiredArgsConstructor
public class PaymentHistoryController {

    private final PaymentHistoryService paymentService;




    @GetMapping("/payment_history")
    public String paymentHistory(
            @RequestParam(required = false) String nid,
            @RequestParam(required = false) String pid,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String bankTxnId,
            @RequestParam(required = false, defaultValue = "individual") String paymentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session,
            Model model
    ) {
        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(), Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(), Cipher.DECRYPT_MODE));

        Integer decryptedBrCode = Integer.parseInt(AES256.processCrypto(session.getAttribute("brCode").toString(), Cipher.DECRYPT_MODE));

        if ("company".equalsIgnoreCase(paymentType)) {
            // Company payment uses companyPID (usually pid), bankTxnId, branchCode, page, size
            Page<TblNpaCompanyPaymentEntity> payments = paymentService.getFilteredCompanyPayments(pid, bankTxnId, decryptedBrCode, page, size);
            model.addAttribute("companyPaymentPage", payments);
        } else {
            // Individual payment uses nid, pid, bankTxnId, branchCode, page, size
            Page<TblNpaPaymentIndividualEntity> payments = paymentService.getFilteredIndividualPayments(nid, pid, bankTxnId, decryptedBrCode, page, size);
            model.addAttribute("individualPaymentPage", payments);
        }

        model.addAttribute("nid", nid);
        model.addAttribute("pid", pid);
        model.addAttribute("transactionId", transactionId);
        model.addAttribute("bankTxnId", bankTxnId);
        model.addAttribute("paymentType", paymentType);

        return "payment_history";
    }





}

