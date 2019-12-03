package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.bean.request.CVCoConfirmTransactionRequest;
import com.payline.payment.cvconnect.bean.request.CVCoCreateTransactionRequest;
import com.payline.payment.cvconnect.bean.response.CVCoPaymentResponse;
import com.payline.payment.cvconnect.exception.PluginException;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseActiveWaiting;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentService;
import org.apache.logging.log4j.Logger;

public class PaymentServiceImpl implements PaymentService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);
    private HttpClient client = HttpClient.getInstance();


    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {
        try {
            // init data
            RequestConfiguration requestConfiguration = new RequestConfiguration(
                    paymentRequest.getContractConfiguration()
                    ,paymentRequest.getEnvironment()
                    , paymentRequest.getPartnerConfiguration()
            );

            //  call httpClient to create the transaction
            CVCoCreateTransactionRequest createTransactionRequest = new CVCoCreateTransactionRequest(paymentRequest);
            CVCoPaymentResponse createResponse = client.createTransaction(requestConfiguration, createTransactionRequest);

            // check response object
            if (!createResponse.isOk()) {
                return PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withErrorCode(createResponse.getErrorCode())
                        .withFailureCause(createResponse.getFailureCause())
                        .build();
            }

            // get transactionId and state
            String partnerTransactionId = createResponse.getTransaction().getId();
            String state = createResponse.getTransaction().getState();

            if (!Transaction.State.INITIALIZED.equalsIgnoreCase(state)) {
                String errorMessage = "Invalid transaction State";
                LOGGER.error(errorMessage);
                return PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(partnerTransactionId)
                        .withErrorCode(errorMessage)
                        .withFailureCause(FailureCause.INVALID_DATA)
                        .build();
            }

            // call httpClient to confirm the transaction
            CVCoConfirmTransactionRequest confirmTransactionRequest = new CVCoConfirmTransactionRequest(paymentRequest, partnerTransactionId);
            CVCoPaymentResponse confirmResponse = client.confirmTransaction(requestConfiguration, confirmTransactionRequest);

            // check response object
            if (!confirmResponse.isOk()) {
                return PaymentResponseFailure.PaymentResponseFailureBuilder
                        .aPaymentResponseFailure()
                        .withPartnerTransactionId(partnerTransactionId)
                        .withErrorCode(confirmResponse.getErrorCode())
                        .withFailureCause(confirmResponse.getFailureCause())
                        .build();
            }

            return new PaymentResponseActiveWaiting();

        } catch (PluginException e) {
            return e.toPaymentResponseFailureBuilder().build();
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            return PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }

    }

}
