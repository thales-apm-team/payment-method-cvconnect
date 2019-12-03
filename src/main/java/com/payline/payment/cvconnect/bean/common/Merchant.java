package com.payline.payment.cvconnect.bean.common;

public class Merchant {
    private String shopId;
    private String serviceProviderId;

    public Merchant(String shopId, String serviceProviderId) {
        this.shopId = shopId;
        this.serviceProviderId = serviceProviderId;
    }

    public String getShopId() {
        return shopId;
    }

    public String getServiceProviderId() {
        return serviceProviderId;
    }
}
