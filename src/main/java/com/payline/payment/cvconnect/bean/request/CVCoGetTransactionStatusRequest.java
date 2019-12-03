package com.payline.payment.cvconnect.bean.request;


import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.utils.PluginUtils;

import java.util.Arrays;
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
    public String getANCVSecurity(RequestConfiguration configuration) {
        List<String> sealFields = Arrays.asList(this.id);
        return PluginUtils.getSealHeader(configuration, sealFields);
    }
}
