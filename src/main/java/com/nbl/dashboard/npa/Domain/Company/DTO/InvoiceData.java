package com.nbl.dashboard.npa.Domain.Company.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nbl.dashboard.npa.Domain.Individual.DTO.CreditAccDetails;
import lombok.Data;

@Data
public class InvoiceData {

    @JsonProperty("invoiceNo")
    private String invoiceNo;

    @JsonProperty("invDate")
    private String invDate;

    @JsonProperty("paymentForMonth")
    private String paymentForMonth;

    @JsonProperty("totalEmp")
    private int totalEmp;

    @JsonProperty("totalAmount")
    private double totalAmount;

    @JsonProperty("companyPID")
    private String companyPID;

    @JsonProperty("comapnyTitle")
    private String comapnyTitle;

    @JsonProperty("contactPerson")
    private String contactPerson;

    @JsonProperty("contactMobile")
    private String contactMobile;

    @JsonProperty("companyAddress")
    private String companyAddress;

    @JsonProperty("economicCode")
    private int economicCode;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Scheme_name")
    private String schemeName;

    @JsonProperty("CreditAccDetails")
    private CreditAccDetails creditAccDetails;
}
