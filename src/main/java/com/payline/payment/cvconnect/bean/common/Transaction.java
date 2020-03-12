package com.payline.payment.cvconnect.bean.common;

import com.payline.payment.cvconnect.utils.PluginUtils;

public class Transaction {
    private String id;
    private State state;
    private String subState;
    private String creationDate;
    private String updateDate;
    private String expirationDate;

    private Merchant merchant;

    public String getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public String getSubState() {
        return subState;
    }

    public String getFullState() {
        String fullState = String.valueOf(state);
        if (!PluginUtils.isEmpty(subState)){
            fullState += "." + subState;
        }
        return fullState;
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

    public enum State {
        INITIALIZED, PROCESSING, AUTHORIZED, VALIDATED, CONSIGNED, PAID, REJECTED, ABORTED, CANCELLED, EXPIRED

        }
}
