package com.nbl.npa.Service.impl;

import com.google.gson.Gson;
import com.nbl.npa.Model.DTO.IndividualPensionDuesDTO;
import com.nbl.npa.Service.IndividualPensionDueService;
import com.nbl.npa.Service.NpaLogService;
import com.nbl.npa.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class IndividualPensionDueServiceImpl implements IndividualPensionDueService {
    private final RestTemplate restTemplate;
    private final TokenService tokenService;
    private final NpaLogService npaLogService;

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.getpensionindividual}")
    private String pensionIndividualUrl;

    @Value("${APIUSR}")
    private String username;


    @Override
    public IndividualPensionDuesDTO getPensionerDetails(String idNumber, String decryptedUserID, Integer decryptedBrCode) {
        String token = tokenService.getToken();
        String url = baseUrl + pensionIndividualUrl;



        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("nid_pid", idNumber);
        formData.add("UserId", username);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        // Perform the request
        ResponseEntity<IndividualPensionDuesDTO> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    IndividualPensionDuesDTO.class
            );
        } catch (Exception e) {
            IndividualPensionDuesDTO errorDto = new IndividualPensionDuesDTO();
            errorDto.setCode(500L);
            errorDto.setMessage("Error while calling external service: " + e.getMessage());
            return errorDto;
        }

        // Get the response body
        IndividualPensionDuesDTO dto = response.getBody();


        if (dto != null && dto.getCode() == 200) {
            String pid = dto.getData().getPid();
            String paymentRefNo = dto.getData().getCreditAccDetails().getPaymentRefNo();

            //System.out.println(response.getBody());
            npaLogService.saveLog(
                    idNumber, pid, paymentRefNo,
                    null, decryptedBrCode, url, formData,
                    new Gson().toJson(response.getBody()), decryptedUserID
            );

        } else {
            System.err.println("Error: " + dto.getMessage());
            npaLogService.saveLog(
                    idNumber, null, null,
                    null, null, url, formData,
                    new Gson().toJson(dto), username
            );
        }

        return dto;
    }

}
