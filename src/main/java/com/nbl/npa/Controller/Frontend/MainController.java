package com.nbl.npa.Controller.Frontend;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nbl.npa.Config.AES256;
import com.nbl.npa.Config.Credentials;
import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import com.nbl.npa.Model.Repo.NpaCompanyRepository;
import com.nbl.npa.Model.Repo.NpaCustomerRepository;
import com.nbl.npa.Model.Repo.NpaIndividualRepository;
import com.nbl.npa.Service.NpaLogService;
import com.nbl.npa.Service.NpaPaymentIndividualService;
import com.nbl.npa.Service.TokenService;
import com.nbl.npa.Service.impl.NpaCompanyPaymentServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MainController {

    private final HttpSession session;

    private final Credentials Creds;

    private final NpaLogService npaLogService;

    private final NpaCompanyRepository companyPaymentRepo;
    private final NpaIndividualRepository individualPaymentRepo;
    private final TokenService tokenService;
    private final RestTemplate restTemplate;
    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${APIUSR}")

    private String username;

    @Value("${api.paymentstatus}")
    private String paymentStatusUrl;


    @GetMapping("/npa")
    public String ssoLogin(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException {
        String userName = request.getParameter("userName");
        String userId = request.getParameter("userId");
        String userType = request.getParameter("userType");
        String brName = request.getParameter("brName");
        String brCode = request.getParameter("brCode");
        String timeStamp = request.getParameter("timestamp");

        if (userName != null && session.getAttribute("is_session") == null) {
            try {

                session.setMaxInactiveInterval(30 * 60);
                session.setAttribute("userName", userName);
                session.setAttribute("userId", userId);
                session.setAttribute("userType", userType);
                session.setAttribute("brCode", brCode);
                session.setAttribute("brName", brName);
                session.setAttribute("timestamp", timeStamp);
                session.setAttribute("is_session", true);

                // Decrypt the values from session attributes
                String decryptedUser = AES256.processCrypto(session.getAttribute("userName").toString(), Cipher.DECRYPT_MODE);
                String decryptedBranch = AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE);
                String decryptedUserID = AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE);
                String decryptedUserType = AES256.processCrypto(session.getAttribute("userType").toString(),Cipher.DECRYPT_MODE);
                Integer decryptedBrCode = Integer.parseInt(AES256.processCrypto(session.getAttribute("brCode").toString(),Cipher.DECRYPT_MODE));
                session.setAttribute("decryptedBrCode", decryptedBrCode);
                String decryptedTimeStamp = AES256.processCrypto(session.getAttribute("timestamp").toString(),Cipher.DECRYPT_MODE);


                // If any decryption fails (returns null), invalidate session and redirect
                if (decryptedUser == null || decryptedBranch == null || decryptedUserID == null || decryptedUserType == null
                        || decryptedBrCode == null || decryptedTimeStamp == null) {
                    session.invalidate();
                    response.sendRedirect(Creds.getRedirectURL());
                    return null;
                }

                request.setAttribute("decryptedUserID", decryptedUserID);
                request.setAttribute("userNameDecrypted", decryptedUser);
                request.setAttribute("brNameDecrypted", decryptedBranch);

                MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
                request.getParameterMap().forEach((key, values) -> {
                    for (String value : values) {
                        requestMap.add(key, value);
                    }
                });

                npaLogService.saveLog(
                        null,
                        null,
                       null,
                        null,
                        decryptedBrCode,
                        "/sso",
                        requestMap,
                        null,
                        decryptedUserID
                );



            } catch (Exception e) {
                // Log the error for debugging purposes
                log.error("Decryption or session error: {}", e.getMessage(), e);
                session.invalidate();
                response.sendRedirect(Creds.getRedirectURL());  // Redirect to the appropriate URL
                return null; // Prevent further execution
            }
        }


        return "redirect:/";  // Redirect to the dashboard
    }



    @GetMapping("/")
    public String showDashboard(Model model) {
        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(),Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));
        model.addAttribute("userType", AES256.processCrypto(session.getAttribute("userType").toString(), Cipher.DECRYPT_MODE));
        return "index";
    }



    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:" + Creds.getRedirectURL();
    }
    public Optional<String> findPaymentRefNoByBankTxnId(String bankTxnId) {
        Optional<TblNpaPaymentIndividualEntity> individual = individualPaymentRepo.findTopByBankTxnIdOrderByEntryDateDesc(bankTxnId);
        if (individual.isPresent()) {
            return Optional.ofNullable(individual.get().getPaymentRefNo());
        }

        Optional<TblNpaCompanyPaymentEntity> company = companyPaymentRepo.findTopByBankTxnIdOrderByEntryDateDesc(bankTxnId);
        if (company.isPresent()) {
            return Optional.ofNullable(company.get().getPaymentRefNo());
        }

        return Optional.empty();
    }


    public Optional<Map<String, Object>> verifyPaymentStatusIndivi(String paymentRefNo) {
        String apiUrl = baseUrl + paymentStatusUrl;
        String apiToken = tokenService.getToken();

        Optional<TblNpaPaymentIndividualEntity> optionalPayment = individualPaymentRepo.findTopByPaymentRefNoOrderByEntryDateDesc(paymentRefNo);

        TblNpaPaymentIndividualEntity payment = optionalPayment.orElseGet(() -> {
            log.warn("No individual payment found with PaymentRefNo {}", paymentRefNo);
            TblNpaPaymentIndividualEntity fallback = new TblNpaPaymentIndividualEntity();
            fallback.setPaymentRefNo(paymentRefNo);
            return fallback;
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(apiToken);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("UserId", username);
        formData.add("Payment_Ref_No", paymentRefNo);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            String responseBody = response.getBody();

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

            if (response.getStatusCodeValue() == 200 && responseBody != null) {
                Map<String, Object> responseMap = new Gson().fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());

                String code = (String) responseMap.get("code");
                if ("200".equals(code)) {
                    Map<String, Object> data = (Map<String, Object>) responseMap.get("data");

                    if (data != null) {
                        String transactionId = (String) data.get("TransactionId");
                        Double paidAmount = data.get("PaidAmount") instanceof Number
                                ? ((Number) data.get("PaidAmount")).doubleValue()
                                : null;

                        payment.setBankTxnId(transactionId);
                        if (paidAmount != null) {
                            payment.setPaidAmount(BigDecimal.valueOf(paidAmount));
                        }
                        payment.setTransactionStatus(1); // Mark as successful

                        // Only save if payment was found in DB
                        if (optionalPayment.isPresent()) {
                            individualPaymentRepo.save(payment);
                        }

                        return Optional.of(data);
                    }
                } else {
                    log.warn("Payment verification failed for {}: Code={}, Message={}",
                            paymentRefNo, responseMap.get("code"), responseMap.get("message"));
                }
            } else {
                log.error("Failed payment verification for {}, HTTP Status: {}", paymentRefNo, response.getStatusCodeValue());
            }
        } catch (Exception ex) {
            log.error("Exception during payment status verification for {}: {}", paymentRefNo, ex.getMessage(), ex);
            npaLogService.saveLog(
                    payment.getNid(),
                    payment.getPid(),
                    payment.getPaymentRefNo(),
                    null,
                    payment.getBranchCode(),
                    apiUrl,
                    formData,
                    "Exception: " + ex.getMessage(),
                    payment.getEntryBy()
            );
        }

        return Optional.empty();
    }


    public Optional<Map<String, Object>> verifyPaymentStatusCompany(String paymentRefNo) {
        String apiUrl = baseUrl + paymentStatusUrl;
        String apiToken = tokenService.getToken();

        Optional<TblNpaCompanyPaymentEntity> optionalPayment = companyPaymentRepo.findTopByPaymentRefNoOrderByEntryDateDesc(paymentRefNo);

        TblNpaCompanyPaymentEntity payment = optionalPayment.orElseGet(() -> {
            log.warn("No company payment found with PaymentRefNo {}", paymentRefNo);
            TblNpaCompanyPaymentEntity fallback = new TblNpaCompanyPaymentEntity();
            fallback.setPaymentRefNo(paymentRefNo);
            return fallback;
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(apiToken);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("UserId", username);
        formData.add("Payment_Ref_No", paymentRefNo);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            String responseBody = response.getBody();

            npaLogService.saveLog(
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

            if (response.getStatusCodeValue() == 200 && responseBody != null) {
                Map<String, Object> responseMap = new Gson().fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());

                String code = (String) responseMap.get("code");
                if ("200".equals(code)) {
                    payment.setTransactionStatus(1);

                    // Save only if it originally existed
                    if (optionalPayment.isPresent()) {
                        companyPaymentRepo.save(payment);
                    }

                    return Optional.of(responseMap);
                }
            }
        } catch (Exception ex) {
            log.error("Exception calling company payment status for RefNo {}: {}", paymentRefNo, ex.getMessage(), ex);
        }

        return Optional.empty();
    }






    @GetMapping("/verify")
    public String verifyPaymentPage(Model model) {
        // Add attributes to the model for the view
        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(), Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(), Cipher.DECRYPT_MODE));
        model.addAttribute("userType", AES256.processCrypto(session.getAttribute("userType").toString(), Cipher.DECRYPT_MODE));

        return "verify_payment";
    }



    @PostMapping("/verify_payment")
    public String verifyPayment(
            @RequestParam("bankTxnId") String bankTxnId,
            Model model) {

        Optional<String> paymentRefNoOpt = findPaymentRefNoByBankTxnId(bankTxnId);

        if (paymentRefNoOpt.isEmpty()) {
            model.addAttribute("verificationResult", "No payment found with the provided Bank Transaction ID.");
        } else {
            String paymentRefNo = paymentRefNoOpt.get();
            Optional<Map<String, Object>> responseOpt = Optional.empty();

            TblNpaCompanyPaymentEntity companyPayment = companyPaymentRepo.findByPaymentRefNo(paymentRefNo);
            if (companyPayment != null) {
                responseOpt = verifyPaymentStatusCompany(paymentRefNo);
            } else {
                Optional<TblNpaPaymentIndividualEntity> individualPayment = individualPaymentRepo.findTopByBankTxnIdOrderByEntryDateDesc(bankTxnId);
                if (individualPayment != null) {

                    responseOpt = verifyPaymentStatusIndivi(paymentRefNo);
                }
            }

            if (responseOpt.isPresent()) {
                model.addAttribute("verificationResult", "Payment is successful.");
            } else {
                model.addAttribute("verificationResult", "Payment is failed.");
            }
        }

        // Add other needed attributes for the view
        model.addAttribute("branchName", AES256.processCrypto(session.getAttribute("brName").toString(), Cipher.DECRYPT_MODE));
        model.addAttribute("userName", AES256.processCrypto(session.getAttribute("userId").toString(), Cipher.DECRYPT_MODE));
        model.addAttribute("userType", AES256.processCrypto(session.getAttribute("userType").toString(), Cipher.DECRYPT_MODE));

        return "verify_payment";
    }


}
