package com.nbl.npa.Service;

import com.nbl.npa.Model.DTO.CompanyInvoiceDTO;

public interface InitialCompanyInvoiceService {
    CompanyInvoiceDTO getPensionerDetails(String idNumber, String invoiceNOInput);
}
