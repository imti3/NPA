package com.nbl.npa.Controller.Api;

import com.google.gson.Gson;
import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import com.nbl.npa.Model.Repo.ConfigurationRepository;
import com.nbl.npa.Model.Repo.NpaCompanyRepository;
import com.nbl.npa.Model.Repo.NpaIndividualRepository;
import com.nbl.npa.Service.NpaLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/paymentstatus")
@RequiredArgsConstructor
public class PaymentStatusController {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentStatusController.class);

    private final ConfigurationRepository configurationRepository;
    private final NpaIndividualRepository individualPaymentRepo;
    private final NpaCompanyRepository companyPaymentRepo;  // New repo for company payments
    private final NpaLogService npaLogService;

    @PostMapping
    public ResponseEntity<?> getStatus(@RequestBody Map<String, String> request) {

        // Fetch username from configuration repository
        String username = getUsername();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        request.forEach(formData::add);


        Map<String, Object> response = new LinkedHashMap<>();
        String paymentRefNo = request.get("Payment_Ref_No");

        try {
            if (paymentRefNo == null || paymentRefNo.isBlank()) {
                response.put("code", "400");
                response.put("message", "Payment_Ref_No is required.");

                npaLogService.saveLog(null, null, paymentRefNo,
                        null, null,
                        "/paymentstatus", formData, new Gson().toJson(response), username);

                return ResponseEntity.badRequest().body(response);
            }

            if (paymentRefNo.length() > 50) {
                response.put("code", "400");
                response.put("message", "Invalid Payment_Ref_No format.");

                npaLogService.saveLog(null, null, paymentRefNo,
                        null, null,
                        "/paymentstatus", formData, new Gson().toJson(response), username);

                return ResponseEntity.badRequest().body(response);
            }

            // 1. Try to find in individual payment
            Optional<TblNpaPaymentIndividualEntity> individualOpt = individualPaymentRepo
                    .findByPaymentRefNoAndExpiredCaseSensitive(paymentRefNo, 1);

            if (individualOpt.isPresent()) {
                TblNpaPaymentIndividualEntity entity = individualOpt.get();

                response.put("code", (entity.getTransactionStatus() == 0 || entity.getTransactionStatus() == 1) ? "200" : "400");
                response.put("message", (entity.getTransactionStatus() == 0 || entity.getTransactionStatus() == 1) ? "Successful" : "Unsuccessful");
                response.put("TransactionId", entity.getBankTxnId());
                response.put("Paid_Amount", entity.getPaidAmount());
                response.put("Commission_Amount", entity.getCommissionAmount());
                response.put("Vat_Amount", entity.getVATAmount());
                response.put("Pay_Mode", entity.getPayMode());
                response.put("Additional_Amount", entity.getAdditionalAmount());

                npaLogService.saveLog(
                        entity.getNid(),
                        entity.getPid(),
                        entity.getPaymentRefNo(),
                        entity.getBankTxnId(),
                        null,
                        "/paymentstatus",
                        formData,
                        new Gson().toJson(response),
                        username
                );

                return ResponseEntity.ok(response);
            }

            // 2. Try to find in company payment
            Optional<TblNpaCompanyPaymentEntity> companyOpt = companyPaymentRepo
                    .findByPaymentRefNoAndExpiredCaseSensitive(paymentRefNo, 1);

            if (companyOpt.isPresent()) {
                TblNpaCompanyPaymentEntity entity = companyOpt.get();

                response.put("code", (entity.getTransactionStatus() == 0 || entity.getTransactionStatus() == 1) ? "200" : "400");
                response.put("message", (entity.getTransactionStatus() == 0 || entity.getTransactionStatus() == 1) ? "Successful" : "Unsuccessful");
                response.put("TransactionId", entity.getBankTxnId());
                response.put("Paid_Amount", entity.getPayingAmount());
                response.put("Commission_Amount", entity.getCommissionAmount());
                response.put("VAT_Amount", entity.getVatAmount());
                response.put("Pay_Mode", entity.getPayMode());
                response.put("Additional_Amount", BigDecimal.ZERO);  // If company entity has no additionalAmount

                npaLogService.saveLog(
                        null,
                        entity.getCompanyPID(),
                        entity.getPaymentRefNo(),
                        entity.getBankTxnId(),
                        null,
                        "/paymentstatus",
                        formData,
                        new Gson().toJson(response),
                        username
                );

                return ResponseEntity.ok(response);
            }

            // 3. If not found in both
            response.put("code", "400");
            response.put("message", "No payment found for Payment Ref: " + paymentRefNo);

            npaLogService.saveLog(null, null, paymentRefNo,
                    null, null,
                    "/paymentstatus", formData, new Gson().toJson(response), username);

            return ResponseEntity.badRequest().body(response);

        } catch (Exception ex) {
            LOG.error("Error checking payment status", ex);
            response.put("code", "400");
            response.put("message", "Error occurred: " + ex.getMessage());

            npaLogService.saveLog(
                    null, null, paymentRefNo, null, null,
                    "/paymentstatus", formData, new Gson().toJson(response), username);

            return ResponseEntity.badRequest().body(response);
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", "400");
        response.put("message", "Malformed JSON request");

        String username = getUsername();
        npaLogService.saveLog(null, null, null, null, null,
                "/paymentstatus", null, new Gson().toJson(response), username);

        return ResponseEntity.badRequest().body(response);
    }

    private String getUsername() {
        return Optional.ofNullable(configurationRepository.findFirstByOrderByIdAsc())
                .map(cfg -> cfg.getUserId())
                .orElse("unknown");
    }
}









