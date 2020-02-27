package com.payline.payment.cvconnect.bean.response;

import com.payline.pmapi.bean.common.FailureCause;

public class Response {
    private String errorCode;
    private String errorMessage;

    public boolean isOk() {
        return (this.errorCode == null && this.errorMessage == null);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public FailureCause getFailureCause() {
        FailureCause cause;
        switch (this.errorCode) {
            case "BAD_REQUEST":
            case "INVALID_SEAL":
            case "MERCHANT_NOT_ALLOWED":
            case "OPERATION_TRANSACTION_NOT_ALLOWED":
            case "TRANSACTION_NOT_FOUND":
            case "BENEFICIARY_NOT_FOUND":
            case "OTHER_TRANSACTION_PENDING":
            case "INVALID_TRANSACTION_AMOUNT":
            case "INVALID_TRANSACTION_CURRENCY":
                cause = FailureCause.INVALID_DATA;
                break;
            case "INTERNAL_SERVER_ERROR":
            default:
                cause = FailureCause.PARTNER_UNKNOWN_ERROR;
        }
        return cause;
    }

}