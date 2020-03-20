package com.payline.payment.cvconnect.bean.common;

import java.math.BigInteger;

public class Amount {
    private BigInteger total;
    private int currency;

    public Amount(BigInteger total, int currency) {
        this.total = total;
        this.currency = currency;
    }


    public BigInteger getTotal() {
        return total;
    }

    public int getCurrency() {
        return currency;
    }
}
