package com.nbl.dashboard.npa.Domain.Individual.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PensionerData {

    // Getters and setters
    @JsonProperty("NID")
    private String nid;

    @JsonProperty("PID")
    private String pid;

    @JsonProperty("PhoneNo")
    private String phoneNo;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Pension_holder_Name")
    private String pensionHolderName;

    @JsonProperty("Install_Amount")
    private float installAmount;

    @JsonProperty("Pay_Interval_Type")
    private String payIntervalType;

    @JsonProperty("Payable_Per_Installment")
    private float payablePerInstallment;

    @JsonProperty("Scheme_name")
    private String schemeName;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Total_Due_Install_Count")
    private float totalDueInstallCount;

    @JsonProperty("Total_Due_Install_Amount")
    private float totalDueInstallAmount;

    @JsonProperty("Total_Due_Loan_Count")
    private float totalDueLoanCount;

    @JsonProperty("Total_Due_Loan_Amount")
    private float totalDueLoanAmount;

    @JsonProperty("Total_Fine_Amount")
    private float totalFineAmount;

    @JsonProperty("Grand_Total_Due_Count")
    private float grandTotalDueCount;

    @JsonProperty("Grand_Total_Due_Amount")
    private float grandTotalDueAmount;

    @JsonProperty("Due_Installments")
    private List<Object> dueInstallments;

    @JsonProperty("CreditAccDetails")
    private CreditAccDetails creditAccDetails;

}


