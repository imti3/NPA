package com.nbl.dashboard.npa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class ReportController {

    @GetMapping("/pension_report")
    public String report() {
        return "pension_report";
    }
}
