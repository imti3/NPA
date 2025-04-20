package com.nbl.dashboard.npa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VerifyController {
    @GetMapping("/pension_verify")
    public String verify() {
        return "pension_verify";
    }
}
