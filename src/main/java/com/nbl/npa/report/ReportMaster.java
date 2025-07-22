package com.nbl.npa.report;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.Nationalized;



@Getter
@Setter
@Entity
@Table(name = "ReportMaster", schema = "dbo")
public class ReportMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;


    @NotNull
    @Nationalized
    @Column(name = "ReprotTitle", nullable = false)
    private String reprotTitle;

    @NotNull
    @Column(name = "Serial", nullable = false)
    private Integer serial;

    @Column(name = "DocFormatAllowed")
    private Boolean docFormatAllowed;


    @Nationalized
    @Column(name = "FileName")
    private String fileName;


    @Nationalized
    @Column(name = "FileNameJasper")
    private String fileNameJasper;

}