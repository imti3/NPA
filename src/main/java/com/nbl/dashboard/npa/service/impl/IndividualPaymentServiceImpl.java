package com.nbl.dashboard.npa.service.impl;

import com.nbl.dashboard.npa.Domain.IndividualPayment.DTO.IndividualPaymentDTO;
import com.nbl.dashboard.npa.service.IndividualPaymentService;
import com.nbl.dashboard.npa.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class IndividualPaymentServiceImpl implements IndividualPaymentService {
    private final RestTemplate restTemplate;
    private final TokenService tokenService;

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.getpaymentindividual}")
    private String pensionIndividualUrl;

    @Value("${APIUSR}")
    private String username;


    @Override
    public IndividualPaymentDTO getPaymentDetails(String Payment_Ref_No, String PID, Long Paying_Install_Count,
                                                  Long Paying_Install_Amount, Long Commission_Amount, Long VAT_Amount, String CreditAccount, String Additional_Amount) {
        String token = tokenService.getToken();
        String url = baseUrl + pensionIndividualUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);
        String transactionId = generateTransactionId(PID);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("Payment_Ref_No", Payment_Ref_No);
        formData.add("PID", PID);
        formData.add("UserId", username);
        formData.add("TransactionId", transactionId);
        formData.add("Paying_Install_Count", String.valueOf(Paying_Install_Count));
        formData.add("Paying_Amount", String.valueOf(Paying_Install_Amount));
        formData.add("Commission_Amount", String.valueOf(Commission_Amount));
        formData.add("VAT_Amount", String.valueOf(VAT_Amount));
        formData.add("Pay_Mode", "A02");
        formData.add("Credit_Account", CreditAccount);
        formData.add("Additional_Amount", String.valueOf(Additional_Amount));


        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        ResponseEntity<IndividualPaymentDTO> response = restTemplate.postForEntity(
                url,
                requestEntity,
                IndividualPaymentDTO.class
        );

        System.out.println(response.getBody());
        if (response.getBody() != null) {
            response.getBody().setInternalTransactionId(transactionId);
        }

        return response.getBody();
    }

    private String generateTransactionId(String PID) {
        String prefix = "NBL-";
        String postfix = "-NPA";

        // Get Unix timestamp in seconds
        long unixTimestamp = Instant.now().getEpochSecond();

        // Generate a short alphanumeric string from UUID
//        String randomAlphaNumeric = UUID.randomUUID()
//                .toString()
//                .replaceAll("-", "")
//                .substring(0, 6)
//                .toUpperCase();
        return prefix + unixTimestamp + "-" + PID + postfix;
    }



}

