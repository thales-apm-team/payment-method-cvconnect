package com.payline.payment.cvconnect.bean.common;

public class Payer {

    private String beneficiaryId;
    private Amount amount;

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
}
