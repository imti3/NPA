package com.nbl.npa.Service;

import com.nbl.npa.Model.Entities.TblNpaCompanyPaymentEntity;
import com.nbl.npa.Model.Entities.TblNpaPaymentIndividualEntity;
import org.springframework.data.domain.Page;

public interface PaymentHistoryService {

    Page<TblNpaPaymentIndividualEntity> getFilteredIndividualPayments(String nid, String pid, String bankTxnId,
                                                                      Integer branchCode, int page, int size);
    Page<TblNpaCompanyPaymentEntity> getFilteredCompanyPayments(String companyPID, String bankTxnId,
                                                                Integer branchCode, int page, int size);
}
