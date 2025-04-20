package com.nbl.dashboard.npa.Domain.Company.DTO;

import com.nbl.dashboard.npa.Domain.Individual.DTO.PensionerData;
import lombok.Data;

@Data
public class InitialCompanyInvoiceDTO {
    private Long code;
    private String message;
    private InvoiceData data;

}
