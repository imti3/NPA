package com.nbl.npa.report;



import com.nbl.npa.Model.DTO.BranchInfo;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class ReportMasterServiceImpl implements ReportMasterService {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ReportMasterServiceImpl.class);

    private final ReportMasterRepository repo;
    private final ReportMasterRepository reportMasterRepository;

    @PostConstruct
    public void init() {
    }

    @Transactional
    @Override
    public ReportMaster save(ReportMaster entity) throws IOException {
        return repo.save(entity);
    }

    @Transactional
    @Override
    public ReportMaster update(ReportMaster entity) throws IOException {
        Optional<ReportMaster> data = repo.findById(Math.toIntExact(entity.getId()));
        if (ObjectUtils.isEmpty(data)) {
            return null;
        }
        return repo.save(entity);
    }

    @Transactional
    @Override
    public ReportMaster delete(ReportMaster entity) throws IOException {
        ReportMaster data = repo.findById(Math.toIntExact(entity.getId())).get();
        if (ObjectUtils.isEmpty(data)) {
            return null;
        }
        repo.delete(entity);
        return entity;
    }

    @Override
    public List<ReportMaster> getAll() {
        return repo.findAll();
    }

    @Override
    public List<ReportMaster> getAllActive() {
        return null;
    }

    @Override
    public Page<ReportMaster> getPageableList(int page, int size) {
       return null;
    }

    @Override
    public ReportMaster getById(Integer id) {
        return repo.findById(id).get();
    }

    @Override
    public List<BranchInfo> getAllBranch() {
        return reportMasterRepository.findRawBranchInfo().stream()
                .map(row -> new BranchInfo(
                        (String) row.get("BranchCode"),
                        (String) row.get("Br_Name")
                ))
                .collect(Collectors.toList());
    }



}
