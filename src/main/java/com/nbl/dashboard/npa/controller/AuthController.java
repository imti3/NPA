package com.nbl.dashboard.npa.controller;

import com.nbl.dashboard.npa.Domain.AuthResponse;
import com.nbl.dashboard.npa.Domain.DTO.LoginRequest;
import com.nbl.dashboard.npa.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/token")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {

        // Optional: validate grantType
        if (!"password".equalsIgnoreCase(loginRequest.getGrantType())) {
            return ResponseEntity
                    .badRequest()
                    .body(AuthResponse.builder()
                            .responseMessage("Invalid username or password")
                            .responseCode(403)
                            .build());
        }

        // Proceed with authentication
        UserDetails userDetails = authenticationService.authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        // Generate the token
        String tokenValue = authenticationService.generateToken(userDetails);

        // Return the response with the token
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(tokenValue)
                .expiresIn(3600)
                .tokenType("Bearer")
                .responseCode(200)
                .responseMessage("Operation Successful")
                .build();

        return ResponseEntity.ok(authResponse);
    }
}


