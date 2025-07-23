package com.nbl.npa.Model.Repo;

import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NpaCompanyRepository  extends JpaRepository<TblNpaCompanyPaymentEntity, Long> {


        // Find payment by exact PaymentRefNo (case sensitive if needed)
        @Query(
                value = "SELECT * FROM tbl_npa_company_payment t WHERE t.PaymentRefNo COLLATE SQL_Latin1_General_CP1_CS_AS = :paymentRefNo AND t.Expired = :expired",
                nativeQuery = true
        )
        Optional<TblNpaCompanyPaymentEntity> findByPaymentRefNoAndExpiredCaseSensitive(
                @Param("paymentRefNo") String paymentRefNo,
                @Param("expired") Integer expired
        );

        // Mark payments with this refNo as expired
        @Modifying
        @Transactional
        @Query("UPDATE TblNpaCompanyPaymentEntity p SET p.expired = 1, p.updated = CURRENT_TIMESTAMP " +
                "WHERE p.paymentRefNo = :paymentRefNo AND p.expired = 0")
        int markExistingPaymentsExpired(@Param("paymentRefNo") String paymentRefNo);

        // Find payment by paymentRefNo
        TblNpaCompanyPaymentEntity findByPaymentRefNo(String paymentRefNo);
        Optional<TblNpaCompanyPaymentEntity> findTopByBankTxnIdOrderByEntryDateDesc(String bankTxnId);


        // Find latest pending payment for this company PID
        TblNpaCompanyPaymentEntity findTopByCompanyPIDAndTransactionStatusOrderByEntryDateDesc(String companyPID, int transactionStatus);

        // Paging methods if needed (e.g., by branchCode, companyPID, etc)
        Page<TblNpaCompanyPaymentEntity> findByBranchCodeAndCompanyPIDAndBankTxnId(Integer branchCode, String companyPID, String bankTxnId, Pageable pageable);
        Page<TblNpaCompanyPaymentEntity> findByBranchCodeAndCompanyPID(Integer branchCode, String companyPID, Pageable pageable);
        Page<TblNpaCompanyPaymentEntity> findByBranchCodeAndBankTxnId(Integer branchCode, String bankTxnId, Pageable pageable);
        Page<TblNpaCompanyPaymentEntity> findByBranchCode(Integer branchCode, Pageable pageable);

        // In NpaIndividualRepository.java

        @Query("SELECT t FROM TblNpaCompanyPaymentEntity t " +
                "WHERE (:pid IS NULL OR t.companyPID = :pid) " +
                "AND (:bankTxnId IS NULL OR t.bankTxnId = :bankTxnId)")
        Page<TblNpaCompanyPaymentEntity> findByFiltersAllBranches(
                @Param("pid") String pid,
                @Param("bankTxnId") String bankTxnId,
                Pageable pageable
        );


        // Add more query methods as needed
    }

