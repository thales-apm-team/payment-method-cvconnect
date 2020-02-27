package com.payline.payment.cvconnect.bean.response;


import com.google.gson.Gson;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.pmapi.bean.common.*;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.BuyerPaymentId;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;

public class PaymentResponse extends Response {
    private Transaction transaction;

    public Transaction getTransaction() {
        return transaction;
    }


    public com.payline.pmapi.bean.payment.response.PaymentResponse getPaylinePaymentResponse() {
        BuyerPaymentId buyerPaymentId = new EmptyTransactionDetails();
        com.payline.pmapi.bean.payment.response.PaymentResponse response;

        switch (this.transaction.getState()) {
            case Transaction.State.INITIALIZED:

            // non final states
            case Transaction.State.PROCESSING:
            case Transaction.State.AUTHORIZED:
            case Transaction.State.VALIDATED:
            case Transaction.State.CONSIGNED:
                response = PaymentResponseOnHold.PaymentResponseOnHoldBuilder
                        .aPaymentResponseOnHold()
                        .withOnHoldCause(OnHoldCause.ASYNC_RETRY)
                        .withStatusCode(this.transaction.getFullState())
                        .withPartnerTransactionId(this.transaction.getId())
                        .build();
                break;

                // final states
            case Transaction.State.CANCELLED:
            case Transaction.State.PAID:
                response = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                        .aPaymentResponseSuccess()
                        .withPartnerTransactionId(this.transaction.getId())
                        .withStatusCode(transaction.getFullState())
                        .withTransactionDetails(buyerPaymentId)
                        .build();

                break;
            case Transaction.State.REJECTED:
                response = PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(this.transaction.getId())
                        .withErrorCode(transaction.getFullState())  // concatenation ou message
                        .withFailureCause(FailureCause.REFUSED)
                        .withTransactionDetails(buyerPaymentId)
                        .build();
                break;
            case Transaction.State.ABORTED:
                response = PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(this.transaction.getId())
                        .withErrorCode(transaction.getFullState())
                        .withFailureCause(FailureCause.CANCEL)
                        .withTransactionDetails(buyerPaymentId)
                        .build();
                break;

            case Transaction.State.EXPIRED:
                response = PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(this.transaction.getId())
                        .withErrorCode(transaction.getFullState())
                        .withFailureCause(FailureCause.SESSION_EXPIRED)
                        .withTransactionDetails(buyerPaymentId)
                        .build();
                break;
            default:
                response = PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(this.transaction.getId())
                        .withErrorCode(transaction.getFullState())
                        .withFailureCause(FailureCause.INVALID_DATA)
                        .withTransactionDetails(buyerPaymentId)
                        .build();
                break;
        }

        return response;
    }


    public TransactionStatus getPaylineStatus() {
        TransactionStatus paylineStatus;

        switch (this.transaction.getState()) {
            case Transaction.State.INITIALIZED:

                // non final states
            case Transaction.State.PROCESSING:
            case Transaction.State.AUTHORIZED:
            case Transaction.State.VALIDATED:
            case Transaction.State.CONSIGNED:
                paylineStatus = OnHoldTransactionStatus.builder()
                        .onHoldCause(OnHoldCause.ASYNC_RETRY)
                        .build();
                break;

            // final states
            case Transaction.State.CANCELLED:
            case Transaction.State.PAID:
                paylineStatus = SuccessTransactionStatus.builder().build();
                break;
            case Transaction.State.REJECTED:
                paylineStatus = FailureTransactionStatus.builder()
                        .failureCause(FailureCause.REFUSED)
                        .build();
                break;
            case Transaction.State.ABORTED:
                paylineStatus = FailureTransactionStatus.builder()
                        .failureCause(FailureCause.CANCEL)
                        .build();
                break;
            case Transaction.State.EXPIRED:
                paylineStatus = FailureTransactionStatus.builder()
                        .failureCause(FailureCause.SESSION_EXPIRED)
                        .build();
                break;
            default:
                paylineStatus = FailureTransactionStatus.builder()
                        .failureCause(FailureCause.INVALID_DATA)
                        .build();

                break;
        }
        return paylineStatus;
    }

    public static PaymentResponse fromJson(String json){
        return new Gson().fromJson(json, PaymentResponse.class);
    }


}
