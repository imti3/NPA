package com.nbl.npa.Service.impl;

import com.google.gson.Gson;
import com.nbl.npa.Config.AES256;
import com.nbl.npa.Service.NpaLogService;
import com.nbl.npa.Service.TokenService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenServiceImpl.class);
    private final NpaLogService npaLogService;

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.token-path}")
    private String tokenPath;

    @Value("${APIUSR}")
    private String apiUser;

    @Value("${password}")
    private String password;

    @Value("${grant_type}")
    private String grantType;

    private final AtomicReference<String> accessToken = new AtomicReference<>();
    private final AtomicLong expiresIn = new AtomicLong();
    private final AtomicLong tokenFetchTime = new AtomicLong();

    private static final Gson GSON = new Gson();

    private final RestTemplate restTemplate;
    private final HttpSession session;

    @Override
    public void fetchToken() {
        String url = baseUrl + tokenPath;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", apiUser);
        map.add("password", password);
        map.add("grant_type", grantType);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, request, Map.class);

            Integer brcode = null;
            String userId = null;
            Object brAttr = session.getAttribute("brCode");
            Object userAttr = session.getAttribute("userId");
            if (brAttr instanceof String brEnc) {
                String brDecrypted = AES256.processCrypto(brEnc, Cipher.DECRYPT_MODE);
                if (brDecrypted != null) {
                    brcode = Integer.parseInt(brDecrypted);
                }
            }
            if (userAttr instanceof String userEnc) {
                userId = AES256.processCrypto(userEnc, Cipher.DECRYPT_MODE);
            }
            npaLogService.saveLog(null, null, null, null, brcode, url, map, GSON.toJson(response.getBody()), userId);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                accessToken.set((String) response.getBody().get("access_token"));
                Integer expires = (Integer) response.getBody().get("expires_in");
                if (expires != null) {
                    expiresIn.set(expires.longValue());
                }
                tokenFetchTime.set(System.currentTimeMillis());
            } else {
                LOG.error("Token request failed with status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            LOG.error("Exception while fetching token", e);
        }
    }

    @Override
    public String getToken() {
        long currentTime = System.currentTimeMillis();
        long bufferMillis = 10 * 60 * 1000; // 10 minutes

        long expires = expiresIn.get();
        long fetched = tokenFetchTime.get();
        String token = accessToken.get();
        boolean tokenExpired = token == null || fetched == 0 || expires == 0
                || (currentTime - fetched) >= ((expires * 1000L) - bufferMillis);

        if (tokenExpired) {
            fetchToken();
        }
        return accessToken.get();
    }



}

