package com.nbl.dashboard.npa.controller;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class MainController {


    @GetMapping("/")
    public String showDashboard() {
        return "index";
    }







}
