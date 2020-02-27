package com.payline.payment.cvconnect.bean.common;

public class Order {
    private String id;
    private String paymentId;
    private String label;
    private Amount amount;

    public Order(String id, String paymentId, String label, Amount amount) {
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

    public Amount getAmount() {
        return amount;
    }
}
