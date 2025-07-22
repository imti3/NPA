package com.nbl.npa.Service.impl;

import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import com.nbl.npa.Model.Repo.NpaCompanyRepository;
import com.nbl.npa.Model.Repo.NpaIndividualRepository;
import com.nbl.npa.Service.PaymentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentHistoryServiceImpl implements PaymentHistoryService {

    private final NpaIndividualRepository individualRepo;
    private final NpaCompanyRepository companyRepo;

    // Existing individual payments filtering method
    public Page<TblNpaPaymentIndividualEntity> getFilteredIndividualPayments(String nid, String pid, String bankTxnId,
                                                                             Integer branchCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryDate"));

        nid = (nid != null && !nid.trim().isEmpty()) ? nid.trim() : null;
        pid = (pid != null && !pid.trim().isEmpty()) ? pid.trim() : null;
        bankTxnId = (bankTxnId != null && !bankTxnId.trim().isEmpty()) ? bankTxnId.trim() : null;

        if (nid != null && pid != null && bankTxnId != null) {
            return individualRepo.findByBranchCodeAndNidAndPidAndBankTxnId(branchCode, nid, pid, bankTxnId, pageable);
        }
        if (nid != null && pid != null) {
            return individualRepo.findByBranchCodeAndNidAndPid(branchCode, nid, pid, pageable);
        }
        if (nid != null && bankTxnId != null) {
            return individualRepo.findByBranchCodeAndNidAndBankTxnId(branchCode, nid, bankTxnId, pageable);
        }
        if (pid != null && bankTxnId != null) {
            return individualRepo.findByBranchCodeAndPidAndBankTxnId(branchCode, pid, bankTxnId, pageable);
        }
        if (nid != null) {
            return individualRepo.findByBranchCodeAndNid(branchCode, nid, pageable);
        }
        if (pid != null) {
            return individualRepo.findByBranchCodeAndPid(branchCode, pid, pageable);
        }
        if (bankTxnId != null) {
            return individualRepo.findByBranchCodeAndBankTxnId(branchCode, bankTxnId, pageable);
        }

        return individualRepo.findByBranchCode(branchCode, pageable);
    }

    // New company payments filtering method
    public Page<TblNpaCompanyPaymentEntity> getFilteredCompanyPayments(String companyPID, String bankTxnId,
                                                                       Integer branchCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryDate"));

        companyPID = (companyPID != null && !companyPID.trim().isEmpty()) ? companyPID.trim() : null;
        bankTxnId = (bankTxnId != null && !bankTxnId.trim().isEmpty()) ? bankTxnId.trim() : null;

        if (companyPID != null && bankTxnId != null) {
            return companyRepo.findByBranchCodeAndCompanyPIDAndBankTxnId(branchCode, companyPID, bankTxnId, pageable);
        }
        if (companyPID != null) {
            return companyRepo.findByBranchCodeAndCompanyPID(branchCode, companyPID, pageable);
        }
        if (bankTxnId != null) {
            return companyRepo.findByBranchCodeAndBankTxnId(branchCode, bankTxnId, pageable);
        }

        return companyRepo.findByBranchCode(branchCode, pageable);
    }
}


