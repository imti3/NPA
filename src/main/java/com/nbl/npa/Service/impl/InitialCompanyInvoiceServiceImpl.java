package com.nbl.npa.Service.impl;

import com.google.gson.Gson;
import com.nbl.npa.Model.DTO.CompanyInvoiceDTO;
import com.nbl.npa.Service.InitialCompanyInvoiceService;
import com.nbl.npa.Service.NpaLogService;
import com.nbl.npa.Service.TokenService;
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
    private final NpaLogService npaLogService;


    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.getpensioncompany}")
    private String pensionCompanyUrl;

    @Value("${APIUSR}")
    private String username;




    @Override
    public CompanyInvoiceDTO getPensionerDetails(String idNumber, String InvoiceNO) {
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


        ResponseEntity<CompanyInvoiceDTO> response = restTemplate.postForEntity(
                url,
                requestEntity,
                CompanyInvoiceDTO.class
        );

        //System.out.println(response.getBody());

//        npaLogService.saveLog(
//                InvoiceNO, idNumber, null
//                , null,
//                null, url, formData, new Gson().toJson(response.getBody()), username);

        return response.getBody();
    }
}
