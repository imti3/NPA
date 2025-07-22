package com.nbl.npa.Model.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_npa_configuation_user")
public class TblConfigurationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;

    @Column(name = "UserId", length = 250)
    private String userId;

    @Column(name = "Password", length = 500)
    private String password;

    @Column(name = "UpdateDate", length = 50)
    private String updateDate;

    @Column(name = "LastIp", length = 20)
    private String lastIp;

}