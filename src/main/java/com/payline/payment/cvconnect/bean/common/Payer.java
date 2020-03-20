package com.payline.payment.cvconnect.bean.common;

import java.util.List;

public class Payer {

    private String beneficiaryId;
    private Amount amount;
    List<Authorization> authorizations;

    public Payer(String beneficiaryId, Amount amount) {
        this.beneficiaryId = beneficiaryId;
        this.amount = amount;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public Amount getAmount() {
        return amount;
    }

    public Authorization getFirstAuthorization(){
        return this.authorizations.get(0);
    }
}
