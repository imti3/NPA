package com.nbl.npa.Service;

import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;

import java.math.BigDecimal;

public interface NpaPaymentIndividualService {
    TblNpaPaymentIndividualEntity initiateAndSave(
            String paymentRefNo,
            String nid,
            String PensionHolderName,
            String PensionPhoneNo,
            String PensionEmail,
            BigDecimal installAmount,
            BigDecimal paidAmount,
            String pid,
            BigDecimal payingInstallCount,
            BigDecimal payingInstallAmount,
            BigDecimal commissionAmount,
            BigDecimal vatAmount,
            String creditAccount,
            BigDecimal additionalAmount,
            String payIntervalType,
            String schemeName,
            BigDecimal totalDueInstallCount,
            BigDecimal totalDueInstallAmount,
            BigDecimal totalDueLoanCount,
            BigDecimal totalDueLoanAmount,
            BigDecimal totalFineAmount,
            BigDecimal grandTotalDueCount,
            BigDecimal grandTotalDueAmount,
            BigDecimal advanceInstallmentCount,
            BigDecimal advancePaymentTotal
    );


    TblNpaPaymentIndividualEntity initiateAndSave(TblNpaPaymentIndividualEntity payment);
}

