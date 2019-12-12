package com.payline.payment.cvconnect.bean.common;

public class Payer {

    private String beneficiaryId;
    private CVAmount amount;

    public Payer(String beneficiaryId, CVAmount amount) {
        this.beneficiaryId = beneficiaryId;
        this.amount = amount;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public CVAmount getAmount() {
        return amount;
    }
}
