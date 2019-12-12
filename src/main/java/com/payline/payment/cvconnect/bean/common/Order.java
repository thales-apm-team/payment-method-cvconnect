package com.payline.payment.cvconnect.bean.common;

public class Order {
    private String id;
    private String paymentId;
    private String label;
    private CVAmount amount;

    public Order(String id, String paymentId, String label, CVAmount amount) {
        this.id = id;
        this.paymentId = paymentId;
        this.label = label;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getLabel() {
        return label;
    }

    public CVAmount getAmount() {
        return amount;
    }
}
