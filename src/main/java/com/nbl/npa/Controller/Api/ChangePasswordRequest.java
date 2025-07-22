package com.nbl.npa.Controller.Api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
}
