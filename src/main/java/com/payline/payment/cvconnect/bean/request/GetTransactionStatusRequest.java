package com.payline.payment.cvconnect.bean.request;


import java.util.Collections;
import java.util.List;

public class GetTransactionStatusRequest extends Request {
    private String id;

    public GetTransactionStatusRequest(String id) {
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
