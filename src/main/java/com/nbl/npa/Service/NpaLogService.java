package com.nbl.npa.Service;

import org.springframework.util.MultiValueMap;

public interface NpaLogService {
    void saveLog(String nid, String pid, String paymentRefNo, String bankTxnId,
                 Integer branchCode, String url, MultiValueMap<String, String> req,
                 String res, String entryBy);

}
