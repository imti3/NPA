package com.nbl.npa.Controller.Api;

import com.google.gson.Gson;
import com.nbl.npa.Model.DTO.LoginRequest;
import com.nbl.npa.Service.AuthenticationService;
import com.nbl.npa.Service.NpaLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/token")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final NpaLogService npaLogService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new LinkedHashMap<>();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", loginRequest.getUsername());
        formData.add("password", loginRequest.getPassword());
        formData.add("grant_type", loginRequest.getGrant_type());

        try {
            if (!"password".equals(loginRequest.getGrant_type())) {
                response.put("code", 400);
                response.put("message", "Invalid grant type");
                response.put("data", null);
                npaLogService.saveLog(null, null, null, null, null, "/token", formData, new Gson().toJson(response), loginRequest.getUsername());
                return ResponseEntity.badRequest().body(response);
            }

            // Authenticate user
            UserDetails userDetails = authenticationService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            // Generate JWT token
            String token = authenticationService.generateToken(userDetails);
            long expiresIn = authenticationService.getLastExpiresIn();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("access_token", token);
            data.put("token_type", "Bearer");
            data.put("expires_in", expiresIn);

            response.put("code", 200);
            response.put("message", "Successful");
            response.put("data", data);

            npaLogService.saveLog(null, null, null, null, null, "/token", formData, new Gson().toJson(response), loginRequest.getUsername());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            response.put("code", 401);
            response.put("message", "Invalid username or password");
            response.put("data", null);
            npaLogService.saveLog(null, null, null, null, null, "/token", formData, new Gson().toJson(response), loginRequest.getUsername());
            return ResponseEntity.status(401).body(response);

        } catch (Exception e) {
            LOG.error("Internal server error during token generation", e);
            response.put("code", 500);
            response.put("message", "Internal Server Error");
            response.put("data", null);
            npaLogService.saveLog(null, null, null, null, null, "/token", formData, new Gson().toJson(response), loginRequest.getUsername());
            return ResponseEntity.status(500).body(response);
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseError(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 400);
        response.put("message", "Malformed JSON request");
        response.put("data", null);
        npaLogService.saveLog(null, null, null, null, null, "/token", null, new Gson().toJson(response), null);
        return ResponseEntity.badRequest().body(response);
    }
}







