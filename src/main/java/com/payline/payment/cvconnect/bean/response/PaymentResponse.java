package com.payline.payment.cvconnect.bean.response;


import com.google.gson.Gson;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.pmapi.bean.common.*;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.BuyerPaymentId;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;

import static com.payline.payment.cvconnect.bean.common.Transaction.*;

public class PaymentResponse extends Response {
    private Transaction transaction;

    public Transaction getTransaction() {
        return transaction;
    }

    public static PaymentResponse fromJson(String json){
        return new Gson().fromJson(json, PaymentResponse.class);
    }
}
