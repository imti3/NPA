package com.nbl.npa.Model.Repo;


import com.nbl.npa.Model.Entities.TblNpaCustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NpaCustomerRepository extends JpaRepository<TblNpaCustomerEntity, Long> {
    Optional<TblNpaCustomerEntity> findByPid(String pid);
}
