package com.payline.payment.cvconnect.bean.request;

import com.google.gson.annotations.Expose;
import com.payline.payment.cvconnect.bean.common.Amount;
import com.payline.payment.cvconnect.bean.common.Payer;
import com.payline.payment.cvconnect.utils.Constants;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.ArrayList;
import java.util.List;

public class ConfirmTransactionRequest extends Request {
    private Payer payer;
    @Expose(serialize = false, deserialize = false)
    private String id;

    public ConfirmTransactionRequest(PaymentRequest request, String id) {
        Amount amount = new Amount(
                request.getAmount().getAmountInSmallestUnit().toString()
                , request.getAmount().getCurrency().getNumericCode()
        );

        this.payer = new Payer(
                request.getPaymentFormContext().getPaymentFormParameter().get(Constants.PaymentFormKeys.CVCO_ID_KEY)
                , amount
        );

        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Payer getPayer() {
        return payer;
    }

    @Override
    public List<String> getANCVSecurity() {
        List<String> sealFields = new ArrayList<>();
        sealFields.add(this.id);
        sealFields.add(this.payer.getBeneficiaryId());
        sealFields.add(this.payer.getAmount().getTotal());

        return sealFields;
    }
}
