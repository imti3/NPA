package com.nbl.npa.Model.Repo;

import com.nbl.npa.Model.Entities.TblConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigurationRepository extends JpaRepository<TblConfigurationEntity, Long> {
    Optional<TblConfigurationEntity> findByUserId(String userId);
    TblConfigurationEntity findFirstByOrderByIdAsc();
}
