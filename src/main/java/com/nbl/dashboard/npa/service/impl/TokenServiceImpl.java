package com.nbl.dashboard.npa.service.impl;

import com.nbl.dashboard.npa.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.token-path}")
    private String tokenPath;

    @Value("${APIUSR}")
    private String APIUSR;

    @Value("${password}")
    private String password;

    @Value("${grant_type}")
    private String grantType;

    private String accessToken;
    private String tokenType;
    private Integer expiresIn;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Scheduled(fixedRate = 50 * 60 * 1000)
    public void fetchToken() {
        String url = baseUrl + tokenPath;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", APIUSR);
        map.add("password", password);
        map.add("grant_type", grantType);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            System.out.println("Raw response: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                this.accessToken = (String) response.getBody().get("access_token");
                this.tokenType = (String) response.getBody().get("token_type");
                this.expiresIn = (Integer) response.getBody().get("expires_in");
            } else {
                System.err.println("Token request failed with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Exception while fetching token:");
            e.printStackTrace();
        }
    }

    @Override
    public String getToken() {
        if(accessToken == null) {
            fetchToken();
        }
        return accessToken;
    }
}

