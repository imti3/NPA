package com.nbl.npa.Model.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_npa_payment_individual")
public class TblNpaPaymentIndividualEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "CustTblId")
    private Long custTblId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustTblId", referencedColumnName = "Id", insertable = false, updatable = false)
    private TblNpaCustomerEntity customer;


    @Column(name = "Pid")
    private String pid;

    @Column(name = "Nid")
    private String nid;

    @Column(name = "PaymentRefNo")
    private String paymentRefNo;

    @Column(name = "BankTxnId", length = 50)
    private String bankTxnId;

    @Column(name = "TransactionStatus")
    private Integer transactionStatus;

    @Column(name = "PaidAmount", precision = 38, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "CommissionAmount", precision = 38, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "VATAmount", precision = 38, scale = 2)
    private BigDecimal vATAmount;

    @Column(name = "AdditionalAmount", precision = 38, scale = 2)
    private BigDecimal additionalAmount;

    @Column(name = "PayMode")
    private String payMode;

    @Column(name = "InstallAmount", precision = 38, scale = 2)
    private BigDecimal installAmount;

    @Column(name = "PayIntervalType")
    private String payIntervalType;

    @Column(name = "PayablePerInstallment", precision = 38, scale = 2)
    private BigDecimal payablePerInstallment;

    @Column(name = "SchemeName")
    private String schemeName;

    @Column(name = "TotalDueInstallCount", precision = 38, scale = 2)
    private BigDecimal totaldueinstallCount;

    @Column(name = "TotalDueInstallAmount", precision = 38, scale = 2)
    private BigDecimal totalDueInstallAmount;

    @Column(name = "TotalDueLoanCount", precision = 38, scale = 2)
    private BigDecimal totalDueLoanCount;

    @Column(name = "TotalDueLoanAmount", precision = 38, scale = 2)
    private BigDecimal totalDueLoanAmount;

    @Column(name = "TotalFineAmount", precision = 38, scale = 2)
    private BigDecimal totalFineAmount;

    @Column(name = "GrandTotalDueCount", precision = 38, scale = 2)
    private BigDecimal grandTotalDueCount;

    @Column(name = "AccNo")
    private String accNo;

    @Column(name = "NextDueDate")
    private LocalDate nextDueDate;

    @Column(name = "BranchCode")
    private Integer branchCode;

    @Column(name = "EntryDate")
    private LocalDateTime entryDate;

    @Column(name = "Updated")
    private LocalDateTime updated;

    @Column(name = "EntryBy")
    private String entryBy;

    @Column(name= "GrandTotalDueAmount")
    private BigDecimal grandTotalDueAmount;

    @Column(name = "AdvanceInstallmentCount")
    private BigDecimal advanceInstallmentCount;

    @Column (name = "AdvanceInstallmentAmount")
    private BigDecimal advanceInstallmentAmount;

    @Column(name = "PaidCount")
    private BigDecimal paidCount;

    @Column(name = "Expired")
    private Integer expired;


    @PrePersist
    public void prePersist() {
        this.entryDate = LocalDateTime.now();
        this.updated = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updated = LocalDateTime.now();
    }

}