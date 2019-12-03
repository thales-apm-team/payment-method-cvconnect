package com.payline.payment.cvconnect.bean.common;

public class ApplicationContext {

    private String returnContext;
    private String customerId;

    public ApplicationContext() {
    }

    public ApplicationContext(String returnContext, String customerId) {
        this.returnContext = returnContext;
        this.customerId = customerId;
    }


    public String getReturnContext() {
        return returnContext;
    }

    public void setReturnContext(String returnContext) {
        this.returnContext = returnContext;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}


