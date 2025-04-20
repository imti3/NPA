package com.nbl.dashboard.npa.Domain.Company.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class CreditAccDetails {

    @JsonProperty("AccNo")
    private Integer accNo;

    @JsonProperty("Payment_Ref_No")
    private String paymentRefNo;

}
