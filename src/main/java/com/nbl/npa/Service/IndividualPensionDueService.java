package com.nbl.npa.Service;

import com.nbl.npa.Model.DTO.IndividualPensionDuesDTO;

public interface IndividualPensionDueService {
    IndividualPensionDuesDTO getPensionerDetails(String idNumber, String decryptedUserID, Integer decryptedBrCode);
}
