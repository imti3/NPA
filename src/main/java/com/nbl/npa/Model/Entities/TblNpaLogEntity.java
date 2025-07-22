package com.nbl.npa.Model.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "tbl_npa_log")
public class TblNpaLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @Column(name = "Nid", length = 17)
    private String nid;

    @Column(name = "PID", length = 50)
    private String pid;

    @Column(name = "PaymentRefNo", length = 50)
    private String paymentRefNo;

    @Column(name = "BankTxnId", length = 50)
    private String bankTxnId;

    @Nationalized
    @Lob
    @Column(name = "RequestURL")
    private String requestURL;

    @Nationalized
    @Lob
    @Column(name = "RequestBody")
    private String requestBody;

    @Nationalized
    @Lob
    @Column(name = "ResponseBody")
    private String responseBody;

    @Column(name = "BranchCode")
    private Integer branchCode;

    @Column(name = "EntryBy")
    private String entryBy;

    @Column(name = "EntryDate")
    private String entryDate;

}