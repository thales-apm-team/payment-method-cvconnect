package com.payline.payment.cvconnect.bean.common;

public class RedirectUrls {

    private String returnUrl;
    private String cancelUrl;

    public RedirectUrls(String returnUrl, String cancelUrl) {
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }
}




