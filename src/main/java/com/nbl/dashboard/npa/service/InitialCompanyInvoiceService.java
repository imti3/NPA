package com.nbl.dashboard.npa.service;

import com.nbl.dashboard.npa.Domain.Company.DTO.InitialCompanyInvoiceDTO;
import com.nbl.dashboard.npa.Domain.Individual.DTO.IntitialPamentIndividualDTO;

public interface InitialCompanyInvoiceService {
    InitialCompanyInvoiceDTO getPensionerDetails(String idNumber, String invoiceNOInput);
}
