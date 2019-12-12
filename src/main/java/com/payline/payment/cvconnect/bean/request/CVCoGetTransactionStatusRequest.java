package com.payline.payment.cvconnect.bean.request;


import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.utils.PluginUtils;

import java.util.Collections;
import java.util.List;

public class CVCoGetTransactionStatusRequest extends CVCoRequest {
    private String id;

    public CVCoGetTransactionStatusRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public List<String> getANCVSecurity() {
        return Collections.singletonList(this.id);
    }
}
