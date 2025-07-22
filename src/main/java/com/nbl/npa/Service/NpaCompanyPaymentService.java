package com.nbl.npa.Service;


import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;

import java.math.BigDecimal;

public interface NpaCompanyPaymentService {
    TblNpaCompanyPaymentEntity initiateAndSave(
            String paymentRefNo,
            String companyPID,
            String invoiceNo,
            String invDate,
            String paymentForMonth,
            int totalEmp,
            BigDecimal totalAmount,
            String companyTitle,
            String contactPerson,
            String contactMobile,
            String companyAddress,
            int economicCode,
            String currency,
            String schemeName,
            String accNo
    );
}

