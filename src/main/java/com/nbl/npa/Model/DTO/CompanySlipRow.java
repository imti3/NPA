package com.nbl.npa.Model.DTO;

import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompanySlipRow {
    private String paymentRefNo;
    private String companyPID;
    private String companyTitle;
    private String invoiceNo;
    private String paymentForMonth;
    private String phoneNo;
    private int totalEmp;
    private BigDecimal totalAmount;
    private String branchName;

    public static CompanySlipRow fromEntity(TblNpaCompanyPaymentEntity e, String branchName) {
        CompanySlipRow row = new CompanySlipRow();
        row.setPaymentRefNo(e.getPaymentRefNo());
        row.setCompanyPID(e.getCompanyPID());
        row.setCompanyTitle(e.getCompanyTitle());
        row.setInvoiceNo(e.getInvoiceNo());
        row.setPaymentForMonth(e.getPaymentForMonth());
        row.setTotalEmp(e.getTotalEmployee());
        row.setPhoneNo(e.getContactMobile());
        row.setTotalAmount(e.getPayingAmount());
        row.setBranchName(branchName);
        return row;
    }
}

