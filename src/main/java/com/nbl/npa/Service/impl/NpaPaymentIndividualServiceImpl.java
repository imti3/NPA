package com.nbl.npa.Service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nbl.npa.Config.AES256;
import com.nbl.npa.Model.Entities.TblNpaCustomerEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import com.nbl.npa.Model.Repo.NpaCustomerRepository;
import com.nbl.npa.Model.Repo.NpaIndividualRepository;
import com.nbl.npa.Service.NpaLogService;
import com.nbl.npa.Service.NpaPaymentIndividualService;
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
import java.util.Map;
import java.util.Optional;
import java.util.Random;


@Service
@RequiredArgsConstructor
@Slf4j
public class NpaPaymentIndividualServiceImpl implements NpaPaymentIndividualService {

    private final NpaIndividualRepository paymentRepo;
    private final TokenService tokenService;
    private final RestTemplate restTemplate;
    private final NpaLogService npaLogService;
    private final NpaCustomerRepository customerRepo;

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${APIUSR}")

    private String username;

    @Value("${api.getpaymentindividual}")
    private String PaymentUrl;

    @Value("${api.paymentstatus}")
    private String paymentStatusUrl;

    @Autowired
    HttpSession session;

    @Override
    public TblNpaPaymentIndividualEntity initiateAndSave(
            String paymentRefNo,
            String nid,
            String PensionHolderName,
            String PensionPhoneNo,
            String PensionEmail,
            BigDecimal installAmount,
            BigDecimal paidAmount,
            String pid,
            BigDecimal payingInstallCount,
            BigDecimal payingInstallAmount,
            BigDecimal commissionAmount,
            BigDecimal vatAmount,
            String creditAccount,
            BigDecimal additionalAmount,
            String payIntervalType,
            String schemeName,
            BigDecimal totalDueInstallCount,
            BigDecimal totalDueInstallAmount,
            BigDecimal totalDueLoanCount,
            BigDecimal totalDueLoanAmount,
            BigDecimal totalFineAmount,
            BigDecimal grandTotalDueCount,
            BigDecimal grandTotalDueAmount,
            BigDecimal advanceInstallmentCount,
            BigDecimal advancePaymentTotal
    ) {
        TblNpaPaymentIndividualEntity payment = new TblNpaPaymentIndividualEntity();

        // Setting initial payment data from request parameters
        payment.setPaymentRefNo(paymentRefNo);
        payment.setNid(nid);
        payment.setInstallAmount(installAmount);
        payment.setPaidAmount(paidAmount);
        payment.setPid(pid);
        payment.setEntryBy(AES256.processCrypto(session.getAttribute("userId").toString(), Cipher.DECRYPT_MODE));
        payment.setBranchCode(Integer.parseInt(AES256.processCrypto(session.getAttribute("brCode").toString(),Cipher.DECRYPT_MODE)));
        payment.setPayMode("A02");
        payment.setGrandTotalDueCount(grandTotalDueCount);
        payment.setPayablePerInstallment(payingInstallAmount);
        payment.setCommissionAmount(commissionAmount);
        payment.setVATAmount(vatAmount);
        payment.setAccNo(creditAccount);
        payment.setAdditionalAmount(additionalAmount);
        payment.setPayIntervalType(payIntervalType);
        payment.setSchemeName(schemeName);
        payment.setTotaldueinstallCount(totalDueInstallCount);
        payment.setTotalDueInstallAmount(totalDueInstallAmount);
        payment.setTotalDueLoanCount(totalDueLoanCount);
        payment.setTotalDueLoanAmount(totalDueLoanAmount);
        payment.setTotalFineAmount(totalFineAmount);
        payment.setGrandTotalDueAmount(grandTotalDueAmount);
        payment.setAdvanceInstallmentCount(advanceInstallmentCount);
        payment.setAdvanceInstallmentAmount(advancePaymentTotal);
        payment.setPaidCount(payingInstallCount);


        TblNpaCustomerEntity customer = customerRepo.findByPid(pid).orElseGet(() -> {
            TblNpaCustomerEntity newCustomer = new TblNpaCustomerEntity();
            newCustomer.setPid(pid);
            newCustomer.setNid(nid);
            newCustomer.setPensionHolderName(PensionHolderName);
            newCustomer.setEmail(PensionEmail);
            newCustomer.setPhoneNo(PensionPhoneNo);
            newCustomer.setAccountNo(creditAccount);
            newCustomer.setBranchCode(Integer.parseInt(AES256.processCrypto(session.getAttribute("brCode").toString(),Cipher.DECRYPT_MODE)));
            newCustomer.setEntryBy(AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));
            newCustomer.setEntryDate(LocalDate.now().toString());
            newCustomer.setSchemeName(schemeName);
            return customerRepo.save(newCustomer);
        });

        payment.setCustTblId(customer.getId());

        // Save the payment initially
        return initiateAndSave(payment);
    }


    public Optional<Map<String, Object>> verifyPaymentStatus(String paymentRefNo) {
        String apiUrl = baseUrl + paymentStatusUrl;
        String apiToken = tokenService.getToken();

        // Fetch payment by PaymentRefNo from DB for logging and updates
        TblNpaPaymentIndividualEntity payment = paymentRepo.findByPaymentRefNo(paymentRefNo);
        if (payment == null) {
            log.warn("No payment found with PaymentRefNo {} for status verification", paymentRefNo);
            // Optionally, you can create a dummy payment object or just proceed with null fields in logs
            payment = new TblNpaPaymentIndividualEntity();
            payment.setPaymentRefNo(paymentRefNo);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(apiToken);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("UserId", username);
        formData.add("Payment_Ref_No", paymentRefNo);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        log.info("Calling /paymentstatus for RefNo: {}, UserId: {}", paymentRefNo, username);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            String responseBody = response.getBody();

            // Log the API response always
            npaLogService.saveLog(
                    payment.getNid(),
                    payment.getPid(),
                    payment.getPaymentRefNo(),
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
                    Map<String, Object> data = (Map<String, Object>) responseMap.get("data");

                    String transactionId = (String) data.get("TransactionId");
                    Double paidAmount = (Double) data.get("PaidAmount");
                    String refNo = (String) data.get("Payment_Ref_No");

                    payment.setBankTxnId(transactionId);
                    payment.setPaidAmount(BigDecimal.valueOf(paidAmount));
                    payment.setTransactionStatus(1);

                    paymentRepo.save(payment); // <-- Save the updated payment here
                    // Optionally update payment entity here, if needed
                    // e.g., updatePaymentFromStatus(payment, data);

                    return Optional.of(data);
                } else {
                    log.warn("Vendor error on payment status for RefNo {}: Code={}, Message={}",
                            payment.getPaymentRefNo(), responseMap.get("code"), responseMap.get("message"));
                }
            } else {
                log.error("Non-200 HTTP status on payment status for RefNo {}: {}", payment.getPaymentRefNo(), response.getStatusCodeValue());
            }

        } catch (Exception ex) {
            String errorMessage = "Exception occurred: " + ex.getMessage();
            log.error("Exception calling payment status for RefNo {}: {}", payment.getPaymentRefNo(), errorMessage, ex);

            npaLogService.saveLog(
                    payment.getNid(),
                    payment.getPid(),
                    payment.getPaymentRefNo(),
                    null,
                    payment.getBranchCode(),
                    apiUrl,
                    formData,
                    errorMessage,
                    payment.getEntryBy()
            );
        }

        return Optional.empty();
    }




    @Override
    public TblNpaPaymentIndividualEntity initiateAndSave(TblNpaPaymentIndividualEntity newPayment) {
        log.info("Starting payment initiation for PID: {}", newPayment.getPid());

        try {
            // STEP 1: Check for previous pending payment for this PID
            TblNpaPaymentIndividualEntity existingPending = paymentRepo.findTopByPidAndTransactionStatusOrderByEntryDateDesc(newPayment.getPid(), 0);
            if (existingPending != null) {
                log.info("Found pending payment for PID {}: RefNo={}", existingPending.getPid(), existingPending.getPaymentRefNo());

                // STEP 2: Verify vendor status for the existing pending payment
                Optional<Map<String, Object>> preStatus = verifyPaymentStatus(existingPending.getPaymentRefNo());
                if (preStatus.isPresent()) {
                    log.info("Vendor confirms previous payment is successful for RefNo={}", existingPending.getPaymentRefNo());
                    return confirmAndSaveFromStatus(existingPending, preStatus.get());
                } else {
                    log.warn("Vendor did not confirm previous pending payment. Deleting RefNo={}", existingPending.getPaymentRefNo());
                    paymentRepo.delete(existingPending);
                }
            }

            // STEP 3: Expire any previous payment records with the same RefNo (precaution)
            paymentRepo.markExistingPaymentsExpired(newPayment.getPaymentRefNo());
            newPayment.setExpired(1);

            // STEP 4: Generate transaction ID and save initial pending entry
            String txnId = generateTxnId(newPayment.getPid(), newPayment.getPaymentRefNo());
            newPayment.setBankTxnId(txnId);
            newPayment.setTransactionStatus(0);
            paymentRepo.save(newPayment);

            // STEP 5: Call vendor API to make payment
            makePaymentToVendor(newPayment, txnId);

            // STEP 6: Post-check vendor status after attempting payment
            Optional<Map<String, Object>> postStatus = verifyPaymentStatus(newPayment.getPaymentRefNo());
            if (postStatus.isPresent()) {
                log.info("Vendor confirms payment after execution for RefNo={}", newPayment.getPaymentRefNo());
                return confirmAndSaveFromStatus(newPayment, postStatus.get());
            } else {
                throw new RuntimeException("Payment status confirmation failed after initiating payment.");
            }

        } catch (Exception ex) {
            log.error("Payment failed for RefNo {}: {}", newPayment.getPaymentRefNo(), ex.getMessage());
//            cleanupFailedRecord(newPayment.getPaymentRefNo());
            throw new RuntimeException("Payment process failed: " + ex.getMessage(), ex);
        }
    }


    private TblNpaPaymentIndividualEntity confirmAndSaveFromStatus(TblNpaPaymentIndividualEntity payment, Map<String, Object> statusData) {
        String txnId = (String) statusData.get("TransactionId");
        BigDecimal paidAmount = BigDecimal.valueOf(((Double) statusData.get("PaidAmount")).doubleValue());

        payment.setTransactionStatus(1);
        payment.setBankTxnId(txnId);
        payment.setPaidAmount(paidAmount);

        return paymentRepo.save(payment);
    }

    private void makePaymentToVendor(TblNpaPaymentIndividualEntity payment, String txnId) {
        String apiUrl = baseUrl + PaymentUrl;
        String apiToken = tokenService.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(apiToken);

        MultiValueMap<String, String> formData = getFormData(payment, username, txnId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
        String responseBody = response.getBody();

        log.info("Sent makePayment to vendor for RefNo={}. Received: {}", payment.getPaymentRefNo(), responseBody);

        npaLogService.saveLog(
                payment.getNid(), payment.getPid(), payment.getPaymentRefNo(), txnId,
                payment.getBranchCode(), apiUrl, formData, responseBody, payment.getEntryBy()
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
            if (data.get("Install_Paid_Count") != null) {
                payment.setPaidCount(BigDecimal.valueOf(((Double) data.get("Install_Paid_Count")).doubleValue()));
            }
            if (data.get("Install_Paid_Amount") != null) {
                payment.setPaidAmount(BigDecimal.valueOf(((Double) data.get("Install_Paid_Amount")).doubleValue()));
            }
            if (data.get("Next_Due_Date") != null) {
                try {
                    payment.setNextDueDate(LocalDate.parse((String) data.get("Next_Due_Date")));
                } catch (Exception ex) {
                    log.warn("Invalid Next_Due_Date format: {}", data.get("Next_Due_Date"));
                }
            }
        }
    }
//    private void cleanupFailedRecord(String paymentRefNo) {
//        try {
//            TblNpaPaymentIndividualEntity existing = paymentRepo.findByPaymentRefNo(paymentRefNo);
//            if (existing != null && (existing.getTransactionStatus() == null || existing.getTransactionStatus() <= 0)) {
//                paymentRepo.delete(existing);
//                log.info("Deleted failed payment record for RefNo: {}", paymentRefNo);
//            }
//        } catch (Exception ex) {
//            log.error("Failed to delete failed payment RefNo {}: {}", paymentRefNo, ex.getMessage());
//        }
//    }


    public Optional<TblNpaPaymentIndividualEntity> findIndividualById(Long id) {
        return paymentRepo.findById(id);
    }

    private MultiValueMap<String, String> getFormData(TblNpaPaymentIndividualEntity payment, String username, String transactionId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("Payment_Ref_No", payment.getPaymentRefNo());
        formData.add("PID", payment.getPid());
        formData.add("UserId", username);
        formData.add("TransactionId", transactionId);
        formData.add("Paying_Install_Count", payment.getPaidCount().toPlainString());
        formData.add("Paying_Amount", payment.getPaidAmount().toPlainString());
        formData.add("Commission_Amount", payment.getCommissionAmount().toPlainString());
        formData.add("VAT_Amount", payment.getVATAmount() != null ? payment.getVATAmount().toPlainString() : "0.00");
        formData.add("PayMode", "A02");
        formData.add("CreditAccount", payment.getAccNo());
        formData.add("Additional_Amount", payment.getAdditionalAmount().toPlainString());
        return formData;
    }

    private String generateTxnId(String pid, String paymentRefNo) {
        String shortPid = pid.length() >= 4 ? pid.substring(pid.length() - 4) : pid;
        String shortRef = paymentRefNo.length() >= 6 ? paymentRefNo.substring(paymentRefNo.length() - 6) : paymentRefNo;
        Random random = new Random();
        int randomNum = 10 + random.nextInt(99);
        return "NBL" + shortPid + shortRef + String.format("%02d", randomNum);
    }


}

