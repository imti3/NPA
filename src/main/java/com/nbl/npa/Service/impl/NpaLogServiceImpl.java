package com.nbl.npa.Service.impl;

import com.google.gson.Gson;
import com.nbl.npa.Model.Entities.TblNpaLogEntity;
import com.nbl.npa.Model.Repo.NpaLogRepository;
import com.nbl.npa.Service.NpaLogService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@AllArgsConstructor
public class NpaLogServiceImpl implements NpaLogService {
    HttpSession session;
    private NpaLogRepository logRepo;
    private static final Logger logger = LoggerFactory.getLogger(NpaLogServiceImpl.class);

    public void saveLog(String nid, String pid, String paymentRefNo, String bankTxnId,
                        Integer branchCode, String url, MultiValueMap<String, String> req,
                        String res, String entryBy) {

        final int MAX_LENGTH = 50; // Adjust based on your DB schema

        if (paymentRefNo != null && paymentRefNo.length() > MAX_LENGTH) {
            logger.warn("PaymentRefNo too long ({} chars). Truncating before save.", paymentRefNo.length());
            paymentRefNo = paymentRefNo.substring(0, MAX_LENGTH);
        }

        logger.info("Saving log for PaymentRefNo: {}", paymentRefNo);

        TblNpaLogEntity log = new TblNpaLogEntity();
        log.setPid(pid);
        log.setNid(nid);
        log.setPaymentRefNo(paymentRefNo);
        log.setBankTxnId(bankTxnId);
        log.setRequestURL(url);
        log.setRequestBody(new Gson().toJson(req));
        log.setResponseBody(res);
        log.setBranchCode(branchCode);
        log.setEntryBy(entryBy);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        log.setEntryDate(LocalDateTime.now().format(formatter));

        logRepo.save(log);
        logger.info("Log saved for PaymentRefNo: {}", paymentRefNo);
    }
}



