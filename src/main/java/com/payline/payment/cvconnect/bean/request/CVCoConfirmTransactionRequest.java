package com.payline.payment.cvconnect.bean.request;

import com.payline.payment.cvconnect.bean.common.CVAmount;
import com.payline.payment.cvconnect.bean.common.Payer;
import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.utils.Constants;
import com.payline.payment.cvconnect.utils.PluginUtils;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.ArrayList;
import java.util.List;

public class CVCoConfirmTransactionRequest extends CVCoRequest {
    private Payer payer;
    private transient String id;

    public CVCoConfirmTransactionRequest(PaymentRequest request, String id) {
        CVAmount amount = new CVAmount(
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
    public String getANCVSecurity(RequestConfiguration configuration) {
        List<String> sealFields = new ArrayList<>();
        sealFields.add(this.id);
        sealFields.add(this.payer.getBeneficiaryId());
        sealFields.add(this.payer.getAmount().getTotal());

        return PluginUtils.getSealHeader(configuration, sealFields);
    }
}
