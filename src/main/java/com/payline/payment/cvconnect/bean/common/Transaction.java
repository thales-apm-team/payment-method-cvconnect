package com.payline.payment.cvconnect.bean.common;

public class Transaction {
    private String id;
    private String state;
    private String subState;
    private String creationDate;
    private String updateDate;
    private String expirationDate;

    private Merchant merchant;

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getSubState() {
        return subState;
    }

    public String getFullState(){
       return String.join(".", state, subState);
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public static class State{
        public static final String INITIALIZED = "INITIALIZED";
        public static final String PROCESSING = "PROCESSING";
        public static final String AUTHORIZED = "AUTHORIZED";
        public static final String VALIDATED = "VALIDATED";
        public static final String CONSIGNED = "CONSIGNED";
        public static final String PAID = "PAID";
        public static final String REJECTED = "REJECTED";
        public static final String ABORTED = "ABORTED";
        public static final String CANCELLED = "CANCELLED";
        public static final String EXPIRED = "EXPIRED";
    }
}
