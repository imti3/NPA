package com.nbl.npa.Model.Repo;

import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NpaIndividualRepository extends JpaRepository<TblNpaPaymentIndividualEntity, Long> {
//    Optional<TblNpaPayment> findByPaymentRefNo(String PaymentRefNo);

    @Query(
            value = "SELECT * FROM tbl_npa_payment_individual t WHERE t.PaymentRefNo COLLATE SQL_Latin1_General_CP1_CS_AS = :paymentRefNo AND t.Expired = :expired",
            nativeQuery = true
    )
    Optional<TblNpaPaymentIndividualEntity> findByPaymentRefNoAndExpiredCaseSensitive(
            @Param("paymentRefNo") String paymentRefNo,
            @Param("expired") Integer expired
    );

//    Optional<TblNpaPayment> findByPaymentRefNoAndExpired(String paymentRefNo, Integer expired);

    @Modifying
    @Transactional
    @Query("UPDATE TblNpaPaymentIndividualEntity p SET p.expired = 1, p.updated = CURRENT_TIMESTAMP " +
            "WHERE p.paymentRefNo = :paymentRefNo AND p.expired = 0")
    int markExistingPaymentsExpired(@Param("paymentRefNo") String paymentRefNo);


    TblNpaPaymentIndividualEntity findByPaymentRefNo(String paymentRefNo);

    Optional<TblNpaPaymentIndividualEntity> findTopByPaymentRefNoOrderByEntryDateDesc(String paymentRefNo);


    Optional<TblNpaPaymentIndividualEntity> findTopByBankTxnIdOrderByEntryDateDesc(String bankTxnId);

    TblNpaPaymentIndividualEntity findTopByPidAndTransactionStatusOrderByEntryDateDesc(String pid, int transactionStatus);

    Page<TblNpaPaymentIndividualEntity> findByBranchCode(Integer branchCode, Pageable pageable);

    Page<TblNpaPaymentIndividualEntity> findByBranchCodeAndNid(Integer branchCode, String nid, Pageable pageable);

    Page<TblNpaPaymentIndividualEntity> findByBranchCodeAndPid(Integer branchCode, String pid, Pageable pageable);

    Page<TblNpaPaymentIndividualEntity> findByBranchCodeAndBankTxnId(Integer branchCode, String bankTxnId, Pageable pageable);

    Page<TblNpaPaymentIndividualEntity> findByBranchCodeAndNidAndPid(Integer branchCode, String nid, String pid, Pageable pageable);

    Page<TblNpaPaymentIndividualEntity> findByBranchCodeAndNidAndBankTxnId(Integer branchCode, String nid, String bankTxnId, Pageable pageable);

    Page<TblNpaPaymentIndividualEntity> findByBranchCodeAndPidAndBankTxnId(Integer branchCode, String pid, String bankTxnId, Pageable pageable);

    Page<TblNpaPaymentIndividualEntity> findByBranchCodeAndNidAndPidAndBankTxnId(Integer branchCode, String nid, String pid, String bankTxnId, Pageable pageable);

    // In NpaIndividualRepository.java

    @Query("SELECT t FROM TblNpaPaymentIndividualEntity t " +
            "WHERE (:nid IS NULL OR t.nid = :nid) " +
            "AND (:pid IS NULL OR t.pid = :pid) " +
            "AND (:bankTxnId IS NULL OR t.bankTxnId = :bankTxnId)")
    Page<TblNpaPaymentIndividualEntity> findByFiltersAllBranches(
            @Param("nid") String nid,
            @Param("pid") String pid,
            @Param("bankTxnId") String bankTxnId,
            Pageable pageable
    );

}