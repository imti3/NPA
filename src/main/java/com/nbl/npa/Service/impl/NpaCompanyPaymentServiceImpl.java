package com.nbl.npa.Service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nbl.npa.Config.AES256;
import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import com.nbl.npa.Model.Repo.NpaCompanyRepository;
import com.nbl.npa.Service.NpaCompanyPaymentService;
import com.nbl.npa.Service.NpaLogService;
import com.nbl.npa.Service.TokenService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class NpaCompanyPaymentServiceImpl implements NpaCompanyPaymentService {

    private final NpaCompanyRepository paymentRepo;
    private final TokenService tokenService;
    private final RestTemplate restTemplate;
    private final NpaLogService logService;

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${APIUSR}")

    private String APIUSR;

    @Value("${api.companypayment}")
    private String paymentUrl;

    @Value("${api.paymentstatus}")
    private String paymentStatusUrl;

    @Autowired
    HttpSession session;


    public Optional<TblNpaCompanyPaymentEntity> findById(Long id) {
        return paymentRepo.findById(id);
    }

    @Override
    public TblNpaCompanyPaymentEntity initiateAndSave(
            String paymentRefNo,
            String companyPID,
            String invoiceNo,
            String invDate,
            String paymentForMonth,
            int totalEmp,
            BigDecimal totalAmount,
            String companyTitle,
            String contactPerson,
            String contactMobile,
            String companyAddress,
            int economicCode,
            String currency,
            String schemeName,
            String accNo
    ) {
        TblNpaCompanyPaymentEntity payment = new TblNpaCompanyPaymentEntity();

        // Set payment data from parameters
        payment.setPaymentRefNo(paymentRefNo);
        payment.setCompanyPID(companyPID);
        payment.setInvoiceNo(invoiceNo);
        payment.setInvoiceDate(LocalDate.parse(invDate));
        payment.setPaymentForMonth(paymentForMonth);
        payment.setTotalEmployee(totalEmp);
        payment.setPayingAmount(totalAmount);
        payment.setCompanyTitle(companyTitle);
        payment.setContactPerson(contactPerson);
        payment.setContactMobile(contactMobile);
        payment.setCompanyAddress(companyAddress);
        payment.setEconomicCode(economicCode);
        payment.setCurrency(currency);
        payment.setSchemeName(schemeName);
        payment.setVatAmount(BigDecimal.ZERO);
        payment.setCommissionAmount(BigDecimal.ZERO);
        payment.setAccNo(accNo);

        // Set audit fields from session
        payment.setEntryBy(AES256.processCrypto(session.getAttribute("userId").toString(), Cipher.DECRYPT_MODE));
        payment.setBranchCode(Integer.parseInt(AES256.processCrypto(session.getAttribute("brCode").toString(),Cipher.DECRYPT_MODE)));
        payment.setPayMode("A02");
        payment.setTransactionStatus(0);
        payment.setExpired(0);
        payment.setEntryDate(LocalDateTime.now());

        try {
            // Check for previous pending payments for this company PID
            TblNpaCompanyPaymentEntity existingPending = paymentRepo.findTopByCompanyPIDAndTransactionStatusOrderByEntryDateDesc(companyPID, 0);
            if (existingPending != null) {
                log.info("Found pending company payment for PID {}: RefNo={}", existingPending.getCompanyPID(), existingPending.getPaymentRefNo());

                // Verify vendor status for existing pending payment
                Optional<Map<String, Object>> preStatus = verifyPaymentStatus(existingPending.getPaymentRefNo());
                if (preStatus.isPresent()) {
                    log.info("Vendor confirms previous company payment is successful for RefNo={}", existingPending.getPaymentRefNo());
                    return confirmAndSaveFromStatus(existingPending, preStatus.get());
                } else {
                    log.warn("Vendor did not confirm previous pending company payment. Deleting RefNo={}", existingPending.getPaymentRefNo());
                    paymentRepo.delete(existingPending);
                }
            }

            // Expire previous payments with same RefNo (precaution)
            paymentRepo.markExistingPaymentsExpired(paymentRefNo);
            payment.setExpired(1);

            // Generate transaction ID and save initial pending entry
            String txnId = generateTxnId(companyPID, paymentRefNo);
            payment.setBankTxnId(txnId);

            paymentRepo.save(payment);

            // Call vendor API to make payment
            makePaymentToVendor(payment, txnId);

            // Verify payment status post API call
            Optional<Map<String, Object>> postStatus = verifyPaymentStatus(paymentRefNo);
            if (postStatus.isPresent()) {
                log.info("Vendor confirms company payment after execution for RefNo={}", paymentRefNo);
                return confirmAndSaveFromStatus(payment, postStatus.get());
            } else {
                throw new RuntimeException("Company payment status confirmation failed after initiating payment.");
            }

        } catch (Exception ex) {
            log.error("Company payment failed for RefNo {}: {}", paymentRefNo, ex.getMessage());
//            cleanupFailedRecord(paymentRefNo);
            throw new RuntimeException("Company payment process failed: " + ex.getMessage(), ex);
        }
    }

    private Optional<Map<String, Object>> verifyPaymentStatus(String paymentRefNo) {
        String apiUrl = baseUrl + paymentStatusUrl;
        String apiToken = tokenService.getToken();

        TblNpaCompanyPaymentEntity payment = paymentRepo.findByPaymentRefNo(paymentRefNo);
        if (payment == null) {
            payment = new TblNpaCompanyPaymentEntity();
            payment.setPaymentRefNo(paymentRefNo);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(apiToken);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("UserId", APIUSR);
        formData.add("Payment_Ref_No", paymentRefNo);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            String responseBody = response.getBody();

            logService.saveLog(
                    null,
                    payment.getCompanyPID(),
                    paymentRefNo,
                    null,
                    payment.getBranchCode(),
                    apiUrl,
                    formData,
                    responseBody,
                    payment.getEntryBy()
            );

            if (response.getStatusCodeValue() == 200) {
                Map<String, Object> responseMap = new Gson().fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());

                String code = (String) responseMap.get("code");
                if ("200".equals(code)) {
                    payment.setTransactionStatus(1);
                    paymentRepo.save(payment);

                    return Optional.of(responseMap);
                }
            }
        } catch (Exception ex) {
            log.error("Exception calling company payment status for RefNo {}: {}", paymentRefNo, ex.getMessage(), ex);
        }

        return Optional.empty();
    }


    private TblNpaCompanyPaymentEntity confirmAndSaveFromStatus(TblNpaCompanyPaymentEntity payment, Map<String, Object> statusData) {
        payment.setTransactionStatus(1);
//        payment.setBankTxnId((String) statusData.get("TransactionId"));
        payment.setUpdated(LocalDateTime.now());
        return paymentRepo.save(payment);
    }

    private void makePaymentToVendor(TblNpaCompanyPaymentEntity payment, String txnId) {
        String apiUrl = baseUrl + paymentUrl;
        String apiToken = tokenService.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(apiToken);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("CompanyPID", payment.getCompanyPID());
        formData.add("InvoiceNo", payment.getInvoiceNo());
        formData.add("Paying_Amount", payment.getPayingAmount().toPlainString());
        formData.add("Commission_Amount", payment.getCommissionAmount() != null ? payment.getCommissionAmount().toPlainString() : "0.00");
        formData.add("VAT_Amount", payment.getVatAmount() != null ? payment.getVatAmount().toPlainString() : "0.00");
        formData.add("PayMode", payment.getPayMode());
        formData.add("TransactionId", txnId);
        formData.add("UserId", APIUSR);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
        String responseBody = response.getBody();

        log.info("Sent company payment to vendor for RefNo={}. Received: {}", payment.getPaymentRefNo(), responseBody);

        logService.saveLog(
                null,
                payment.getCompanyPID(),
                payment.getPaymentRefNo(),
                txnId,
                payment.getBranchCode(),
                apiUrl,
                formData,
                responseBody,
                payment.getEntryBy()
        );

        if (response.getStatusCodeValue() != 200) {
            throw new RuntimeException("Vendor API HTTP Error: " + response.getStatusCodeValue());
        }

        Map<String, Object> responseMap = new Gson().fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());
        if (!"200".equals(responseMap.get("code"))) {
            throw new RuntimeException("Vendor Error: " + responseMap.get("message"));
        }

        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        if (data != null) {
            if (data.get("invoiceNo") != null) {
                payment.setInvoiceNo(data.get("invoiceNo").toString());
            }
            if (data.get("totEmployeeCount") != null) {
                try {
                    int totEmp = ((Number) data.get("totEmployeeCount")).intValue();
                    payment.setTotalEmployee(totEmp);
                } catch (Exception e) {
                    log.warn("Failed to parse totEmployeeCount: {}", data.get("totEmployeeCount"));
                }
            }
            if (data.get("totReceivedAmount") != null) {
                try {
                    double totAmount = ((Number) data.get("totReceivedAmount")).doubleValue();
                    payment.setPayingAmount(BigDecimal.valueOf(totAmount));
                } catch (Exception e) {
                    log.warn("Failed to parse totReceivedAmount: {}", data.get("totReceivedAmount"));
                }
            }
        }
        paymentRepo.save(payment);
    }


//    private void cleanupFailedRecord(String paymentRefNo) {
//        try {
//            TblNpaCompanyPaymentEntity existing = paymentRepo.findByPaymentRefNo(paymentRefNo);
//            if (existing != null && (existing.getTransactionStatus() == null || existing.getTransactionStatus() <= 0)) {
//                paymentRepo.delete(existing);
//                log.info("Deleted failed company payment record for RefNo: {}", paymentRefNo);
//            }
//        } catch (Exception ex) {
//            log.error("Failed to delete failed company payment RefNo {}: {}", paymentRefNo, ex.getMessage());
//        }
//    }

    private String generateTxnId(String companyPID, String paymentRefNo) {
        String shortPid = companyPID.length() >= 4 ? companyPID.substring(companyPID.length() - 4) : companyPID;
        String shortRef = paymentRefNo.length() >= 6 ? paymentRefNo.substring(paymentRefNo.length() - 6) : paymentRefNo;
        int randomNum = new Random().nextInt(90) + 10;
        return "NBL" + shortPid + shortRef + String.format("%02d", randomNum);
    }


}

