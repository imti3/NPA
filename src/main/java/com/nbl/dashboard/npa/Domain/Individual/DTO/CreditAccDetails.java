package com.nbl.dashboard.npa.Domain.Individual.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class CreditAccDetails {

    @JsonProperty("AccNo")
    private String accNo;

    @JsonProperty("Payment_Ref_No")
    private String paymentRefNo;

}
