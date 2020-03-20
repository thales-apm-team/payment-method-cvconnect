package com.payline.payment.cvconnect.bean.response;


import com.google.gson.Gson;
import com.payline.payment.cvconnect.bean.common.Transaction;

public class PaymentResponse extends Response {
    private Transaction transaction;

    public Transaction getTransaction() {
        return transaction;
    }

    public static PaymentResponse fromJson(String json){
        return new Gson().fromJson(json, PaymentResponse.class);
    }
}
