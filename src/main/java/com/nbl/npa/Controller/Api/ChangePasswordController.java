package com.nbl.npa.Controller.Api;

import com.google.gson.Gson;
import com.nbl.npa.Config.AES256;
import com.nbl.npa.Model.Entities.TblConfigurationEntity;
import com.nbl.npa.Model.Repo.ConfigurationRepository;
import com.nbl.npa.Service.NpaLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/change-password")
@RequiredArgsConstructor
public class ChangePasswordController {

    private static final Logger LOG = LoggerFactory.getLogger(ChangePasswordController.class);

    private final ConfigurationRepository configRepo;
    private final NpaLogService npaLogService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new LinkedHashMap<>();


        String endpoint = "/change-password";

        try {
            // Fetch user by username
            TblConfigurationEntity user = configRepo.findByUserId(request.getUsername())
                    .orElse(null);

            if (user == null) {
                response.put("code", 400);
                response.put("message", "User not found.");
                log(endpoint, request, response, request.getUsername());
                return ResponseEntity.badRequest().body(response);
            }

            // Decrypt stored password and compare
            String decryptedOldPassword = AES256.processCrypto(user.getPassword(), Cipher.DECRYPT_MODE);
            if (!decryptedOldPassword.equals(request.getOldPassword())) {
                response.put("code", 401);
                response.put("message", "Old password is incorrect.");
                log(endpoint, request, response, user.getUserId());
                return ResponseEntity.status(401).body(response);
            }

            // Set and save new password
            String encryptedNewPassword = AES256.processCrypto(request.getNewPassword(), Cipher.ENCRYPT_MODE);
            user.setPassword(encryptedNewPassword);
            user.setUpdateDate(String.valueOf(LocalDateTime.now()));
            user.setLastIp(httpRequest.getRemoteAddr());
            configRepo.save(user);

            response.put("code", 200);
            response.put("message", "Password changed successfully.");
            response.put("data", null);

            log(endpoint, request, response, user.getUserId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOG.error("Internal server error during password change", e);
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            log(endpoint, request, response, request.getUsername());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private void log(String endpoint, ChangePasswordRequest request, Map<String, Object> response, String userId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", request.getUsername());
        formData.add("oldPassword", request.getOldPassword());
        formData.add("newPassword", request.getNewPassword());

        npaLogService.saveLog(
                null,
                null,
                null,
                null,
                null,
                endpoint,
                formData,
                new Gson().toJson(response),
                userId
        );
    }
}

