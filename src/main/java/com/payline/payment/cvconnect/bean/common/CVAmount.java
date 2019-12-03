package com.payline.payment.cvconnect.bean.common;

public class CVAmount {
    private String total;
    private int currency;

    public CVAmount(String total, int currency) {
        this.total = total;
        this.currency = currency;
    }


    public String getTotal() {
        return total;
    }

    public int getCurrency() {
        return currency;
    }
}
