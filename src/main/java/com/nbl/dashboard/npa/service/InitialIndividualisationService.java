package com.nbl.dashboard.npa.service;

import com.nbl.dashboard.npa.Domain.Individual.DTO.IntitialPamentIndividualDTO;

public interface InitialIndividualisationService {
    IntitialPamentIndividualDTO getPensionerDetails(String idNumber);
}
