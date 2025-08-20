package com.nbl.npa.Model.DTO;

import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data

public class PensionSlipRow {

    private String paymentRefNo;
    private String pensionHolderName;
    private String nid;
    private String phoneNo;
    private String schemeName;
    private BigDecimal installmentAmount;
    private BigDecimal paidAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalDueAmount;
    private Date entryDate;
    private String branchName;
    private String pid;

    public static PensionSlipRow from(IndividualPensionDuesDTO.PensionerData d, String branchNameOverride) {
        PensionSlipRow r = new PensionSlipRow();

        r.setPaymentRefNo(
                d.getCreditAccDetails() != null ? d.getCreditAccDetails().getPaymentRefNo() : null
        );
        r.setPensionHolderName(d.getPensionHolderName());
        r.setPid(d.getPid() != null ? d.getPid().toString() : null);
        r.setNid(d.getNid());
        r.setPhoneNo(d.getPhoneNo());
        r.setSchemeName(d.getSchemeName());

        r.setInstallmentAmount(nz((double) d.getPayablePerInstallment()));
        r.setPaidAmount(nz((double) d.getGrandTotalDueAmount()));
        r.setBranchName(branchNameOverride);

        return r;
    }


    public static PensionSlipRow fromEntity(TblNpaPaymentIndividualEntity e, String branchName) {
        PensionSlipRow row = new PensionSlipRow();
        row.setPensionHolderName(e.getCustomer().getPensionHolderName());
        row.setNid(e.getNid());
        row.setPid(e.getPid());
        row.setPhoneNo(e.getCustomer().getPhoneNo());
        row.setSchemeName(e.getSchemeName());
        row.setPaidAmount(e.getPaidAmount());
        row.setBranchName(branchName);
        row.setPaymentRefNo(e.getPaymentRefNo());
        // ... map other fields required in JRXML
        return row;
    }


    private static BigDecimal nz(Double v) {
        return v == null ? BigDecimal.ZERO : BigDecimal.valueOf(v);
    }
    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}



