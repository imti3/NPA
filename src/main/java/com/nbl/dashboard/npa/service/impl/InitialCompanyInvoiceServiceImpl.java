package com.nbl.dashboard.npa.service.impl;

import com.nbl.dashboard.npa.Domain.Company.DTO.InitialCompanyInvoiceDTO;
import com.nbl.dashboard.npa.Domain.Individual.DTO.IntitialPamentIndividualDTO;
import com.nbl.dashboard.npa.service.InitialCompanyInvoiceService;
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

@Service
@RequiredArgsConstructor
public class InitialCompanyInvoiceServiceImpl implements InitialCompanyInvoiceService {
    private final RestTemplate restTemplate;
    private final TokenService tokenService;

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.getpensioncompany}")
    private String pensionCompanyUrl;

    @Value("${APIUSR}")
    private String username;


    @Override
    public InitialCompanyInvoiceDTO getPensionerDetails(String idNumber, String InvoiceNO) {
        String token = tokenService.getToken();
        String url = baseUrl + pensionCompanyUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("companyPID", idNumber);
        formData.add("invoiceNo", InvoiceNO);
        formData.add("UserId", username);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        ResponseEntity<InitialCompanyInvoiceDTO> response = restTemplate.postForEntity(
                url,
                requestEntity,
                InitialCompanyInvoiceDTO.class
        );

        System.out.println(response.getBody());
        return response.getBody();
    }
}
