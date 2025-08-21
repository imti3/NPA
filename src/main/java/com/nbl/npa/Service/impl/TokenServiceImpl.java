package com.nbl.npa.Service.impl;

import com.google.gson.Gson;
import com.nbl.npa.Config.AES256;
import com.nbl.npa.Service.NpaLogService;
import com.nbl.npa.Service.TokenService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final NpaLogService npaLogService;

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
    private Long tokenFetchTime;

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    HttpSession session;

    @Override
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
            //System.out.println("Raw response: " + response.getBody());

            Integer brcode=(Integer.parseInt(AES256.processCrypto(session.getAttribute("brCode").toString(), Cipher.DECRYPT_MODE)));

            String userId=(AES256.processCrypto(session.getAttribute("userId").toString(),Cipher.DECRYPT_MODE));
            npaLogService.saveLog(
                    null, null, null
                    , null,
                    brcode, url, map, new Gson().toJson(response.getBody()), userId);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                this.accessToken = (String) response.getBody().get("access_token");
                this.tokenType = (String) response.getBody().get("token_type");
                this.expiresIn = (Integer) response.getBody().get("expires_in");
                this.tokenFetchTime = System.currentTimeMillis();
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
        long currentTime = System.currentTimeMillis();
        long bufferMillis = 10 * 60 * 1000; // 10 minutes

        boolean tokenExpired = (accessToken == null) ||
                (tokenFetchTime == 0) ||
                ((currentTime - tokenFetchTime) >= ((expiresIn * 1000L) - bufferMillis));

        if (tokenExpired) {
            //System.out.println("Token expired or about to expire within 10 minutes. Fetching new token...");
            fetchToken();
        }

        if (accessToken != null && tokenFetchTime > 0) {
            long elapsedTimeMillis = currentTime - tokenFetchTime;
            long remainingMillis = (expiresIn * 1000L) - elapsedTimeMillis;
            long remainingMinutes = remainingMillis / (60 * 1000);
            long remainingSeconds = (remainingMillis / 1000) % 60;

            //System.out.println("Token remaining time: " + remainingMinutes + " minutes " + remainingSeconds + " seconds");

            if (remainingMillis <= bufferMillis) {
                //System.out.println("Token is within the 10-minute buffer window and will be refreshed soon.");
            }
        } else {
            //System.out.println("No token fetched yet.");
        }

        return accessToken;
    }



}

