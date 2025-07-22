package com.nbl.npa.Model.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tbl_npa_customer")
public class TblNpaCustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Column(name = "Nid", length = 50)
    private String nid;

    @Column(name = "Pid", length = 50)
    private String pid;

    @Column(name = "PhoneNo", length = 20)
    private String phoneNo;

    @Column(name = "Email")
    private String email;

    @Column(name = "PensionHolderName")
    private String pensionHolderName;

    @Column(name = "SchemeName")
    private String schemeName;

    @Column(name = "AccountNo", length = 50)
    private String accountNo;

    @Column(name = "BranchCode")
    private Integer branchCode;

    @Column(name = "EntryBy")
    private String entryBy;

    @Column(name = "EntryDate")
    private String entryDate;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<TblNpaPaymentIndividualEntity> payments;


}