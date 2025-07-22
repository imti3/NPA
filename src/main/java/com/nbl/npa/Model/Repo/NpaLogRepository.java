package com.nbl.npa.Model.Repo;

import com.nbl.npa.Model.Entities.TblNpaLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NpaLogRepository extends JpaRepository<TblNpaLogEntity, Long> {
}