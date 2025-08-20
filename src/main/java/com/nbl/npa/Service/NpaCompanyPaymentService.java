package com.nbl.npa.Service;


import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;

import java.math.BigDecimal;
import java.util.Optional;

public interface NpaCompanyPaymentService {

    Optional<TblNpaCompanyPaymentEntity> findById(Long id);
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

