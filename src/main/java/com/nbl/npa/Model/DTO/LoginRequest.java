package com.nbl.npa.Model.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String grant_type;
}
