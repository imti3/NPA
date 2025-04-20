package com.nbl.dashboard.npa.service.impl;

import com.nbl.dashboard.npa.Domain.Individual.DTO.IntitialPamentIndividualDTO;
import com.nbl.dashboard.npa.service.InitialIndividualisationService;
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
public class InitialIndividualisationServiceImpl implements InitialIndividualisationService {
    private final RestTemplate restTemplate;
    private final TokenService tokenService;

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.getpensionindividual}")
    private String pensionIndividualUrl;

    @Value("${APIUSR}")
    private String username;



    @Override
    public IntitialPamentIndividualDTO getPensionerDetails(String idNumber) {
        String token = tokenService.getToken();
        String url = baseUrl + pensionIndividualUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("nid_pid", idNumber);
        formData.add("UserId", username);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        ResponseEntity<IntitialPamentIndividualDTO> response = restTemplate.postForEntity(
                url,
                requestEntity,
                IntitialPamentIndividualDTO.class
        );

        System.out.println(response.getBody());
        return response.getBody();
    }
}
