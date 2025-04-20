package com.nbl.dashboard.npa.Domain.IndividualPayment.DTO;

import com.nbl.dashboard.npa.Domain.Company.DTO.InvoiceData;
import lombok.Data;

@Data
public class IndividualPaymentDTO {
    private Long code;
    private String message;
    private IndividualPaymentData data;
    private String internalTransactionId;
}
