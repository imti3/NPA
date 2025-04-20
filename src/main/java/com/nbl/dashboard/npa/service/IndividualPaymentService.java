package com.nbl.dashboard.npa.service;

import com.nbl.dashboard.npa.Domain.Individual.DTO.IntitialPamentIndividualDTO;
import com.nbl.dashboard.npa.Domain.IndividualPayment.DTO.IndividualPaymentDTO;

public interface IndividualPaymentService {
    IndividualPaymentDTO getPaymentDetails(String Payment_Ref_No,String PID,Long Paying_Install_Count,
    Long Paying_Amount,Long Commission_Amount,Long VAT_Amount,String CreditAccount,String Additional_Amount);

}
