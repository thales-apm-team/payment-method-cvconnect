package com.payline.payment.cvconnect.bean.common;

import java.util.Date;

public class PaymentMethod {

    private String tspdMode;
    private String captureMode;
    private Date captureDate;

    public PaymentMethod(String tspdMode, String captureMode, Date captureDate) {
        this.tspdMode = tspdMode;
        this.captureMode = captureMode;
        this.captureDate = captureDate;
    }

    public String getTspdMode() {
        return tspdMode;
    }

    public String getCaptureMode() {
        return captureMode;
    }

    public Date getCaptureDate() {
        return captureDate;
    }
}
