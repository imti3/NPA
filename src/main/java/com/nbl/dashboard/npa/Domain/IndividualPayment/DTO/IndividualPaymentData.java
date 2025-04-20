package com.nbl.dashboard.npa.Domain.IndividualPayment.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IndividualPaymentData {

    @JsonProperty("PID")
    private String pid;

    @JsonProperty("Install_Paid_Count")
    private Float installPaidCount;

    @JsonProperty("Install_Paid_Amount")
    private Float installPaidAmount;

    @JsonProperty("Next_Due_Date")
    private String nextDueDate;

}
