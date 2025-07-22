package com.nbl.npa.Model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CompanyInvoiceDTO {


        private Long code;
        private String message;

        @JsonProperty("data")
        private CompanyData data;

        @Data
        public static class CompanyData {

            @JsonProperty("invoiceNo")
            private String invoiceNo;

            @JsonProperty("invDate")
            private String invDate;

            @JsonProperty("paymentForMonth")
            private String paymentForMonth;

            @JsonProperty("totalEmp")
            private int totalEmp;

            @JsonProperty("totalAmount")
            private float totalAmount;

            @JsonProperty("companyPID")
            private String companyPID;

            @JsonProperty("comapnyTitle")
            private String companyTitle;

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

        @Data
        public static class CreditAccDetails {

            @JsonProperty("AccNo")
            private String accNo;

            @JsonProperty("Payment_Ref_No")
            private String paymentRefNo;
        }
    }


