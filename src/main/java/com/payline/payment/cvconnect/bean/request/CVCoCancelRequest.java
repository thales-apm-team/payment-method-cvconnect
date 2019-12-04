package com.payline.payment.cvconnect.bean.request;

import com.google.gson.annotations.Expose;
import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.utils.PluginUtils;
import com.payline.pmapi.bean.reset.request.ResetRequest;

import java.util.ArrayList;
import java.util.List;

public class CVCoCancelRequest extends CVCoRequest {
    @Expose(serialize = false, deserialize = false)
    private transient String id;
    private String reason;


    public CVCoCancelRequest(ResetRequest request) {
        this.id = request.getPartnerTransactionId();
        this.reason = "OTHER";
    }

    public String getId() {
        return this.id;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getANCVSecurity(RequestConfiguration configuration) {
        List<String> sealFields = new ArrayList<>();
        sealFields.add(this.id);
        sealFields.add(this.reason);

        return PluginUtils.getSealHeader(configuration, sealFields);
    }


}
