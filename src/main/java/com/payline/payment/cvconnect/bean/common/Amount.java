package com.payline.payment.cvconnect.bean.common;

public class Amount {
    private String total;
    private int currency;

    public Amount(String total, int currency) {
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
