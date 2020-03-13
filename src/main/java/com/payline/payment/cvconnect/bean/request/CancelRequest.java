package com.payline.payment.cvconnect.bean.request;

import com.google.gson.annotations.Expose;
import com.payline.pmapi.bean.refund.request.RefundRequest;

import java.util.ArrayList;
import java.util.List;

public class CancelRequest extends Request {
    @Expose(serialize = false, deserialize = false)
    private String id;
    private String reason;


    public CancelRequest(RefundRequest request) {
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
    public List<String> getANCVSecurity() {
        List<String> sealFields = new ArrayList<>();
        sealFields.add(this.id);
        sealFields.add(this.reason);

        return sealFields;
    }

}
