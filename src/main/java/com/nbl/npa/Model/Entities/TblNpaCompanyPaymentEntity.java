package com.nbl.npa.Model.Entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_npa_company_payment")
public class TblNpaCompanyPaymentEntity {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SUCCESS = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "InvoiceNo", nullable = false)
    private String invoiceNo;

    @Column(name = "InvoiceDate")
    private LocalDate invoiceDate;

    @Column(name = "PaymentForMonth")
    private String paymentForMonth;

    @Column(name = "CompanyPID")
    private String companyPID;

    @Column(name = "CompanyTitle")
    private String companyTitle;

    @Column(name = "ContactPerson")
    private String contactPerson;

    @Column(name = "ContactMobile")
    private String contactMobile;

    @Column(name = "CompanyAddress", columnDefinition = "TEXT")
    private String companyAddress;

    @Column(name = "EconomicCode")
    private Integer economicCode;

    @Column(name = "Currency")
    private String currency;

    @Column(name = "SchemeName")
    private String schemeName;

    @Column(name = "TotalEmployee")
    private Integer totalEmployee;

    @Column(name = "PaymentRefNo")
    private String paymentRefNo;

    @Column(name = "BranchCode")
    private Integer branchCode;

    @Column(name = "AccNo")
    private String accNo;


    @Column(name = "PayingAmount", precision = 38, scale = 2)
    private BigDecimal payingAmount;

    @Column(name = "CommissionAmount", precision = 38, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "VATAmount", precision = 38, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "PayMode")
    private String payMode;

    @Column(name = "BankTxnId")
    private String bankTxnId;

    @Column(name = "TransactionStatus")
    private Integer transactionStatus;

    @Column(name = "EntryBy")
    private String entryBy;

    @Column(name = "EntryDate")
    private LocalDateTime entryDate;

    @Column(name = "Updated")
    private LocalDateTime updated;

    @Column(name = "Expired")
    private Integer expired;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.entryDate = now;
        this.updated = now;
        if (transactionStatus == null) {
            transactionStatus = STATUS_PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updated = LocalDateTime.now();
    }
}
