package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.bean.request.CVCoCancelRequest;
import com.payline.payment.cvconnect.bean.response.CVCoPaymentResponse;
import com.payline.payment.cvconnect.exception.PluginException;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.reset.request.ResetRequest;
import com.payline.pmapi.bean.reset.response.ResetResponse;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseFailure;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.ResetService;
import org.apache.logging.log4j.Logger;

public class ResetServiceImpl implements ResetService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);
    private HttpClient client = HttpClient.getInstance();

    @Override
    public ResetResponse resetRequest(ResetRequest resetRequest) {
        String partnerTransactionId = resetRequest.getPartnerTransactionId();
        try {
            CVCoCancelRequest cvCoCancelRequest = new CVCoCancelRequest(resetRequest);
            RequestConfiguration configuration = new RequestConfiguration(
                    resetRequest.getContractConfiguration()
                    , resetRequest.getEnvironment()
                    , resetRequest.getPartnerConfiguration()
            );
            CVCoPaymentResponse response = client.cancelTransaction(configuration, cvCoCancelRequest);

            if (!response.isOk()) {
                // return a failure
                return ResetResponseFailure.ResetResponseFailureBuilder
                        .aResetResponseFailure()
                        .withErrorCode(response.getErrorCode())
                        .withFailureCause(response.getFailureCause())
                        .withPartnerTransactionId(partnerTransactionId)
                        .build();
            }

            if (!Transaction.State.CANCELLED.equalsIgnoreCase(response.getTransaction().getState())) {
                // return a failure
                String errorMessage = "Invalid transaction State";
                LOGGER.info(errorMessage);
                return ResetResponseFailure.ResetResponseFailureBuilder
                        .aResetResponseFailure()
                        .withErrorCode(errorMessage)
                        .withFailureCause(FailureCause.INVALID_DATA)
                        .withPartnerTransactionId(partnerTransactionId)
                        .build();
            }

            // return a success
            return ResetResponseSuccess.ResetResponseSuccessBuilder
                    .aResetResponseSuccess()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withStatusCode(response.getTransaction().getFullState())
                    .build();
        } catch (PluginException e) {
            return ResetResponseFailure.ResetResponseFailureBuilder
                    .aResetResponseFailure()
                    .withErrorCode(e.getErrorCode())
                    .withFailureCause(e.getFailureCause())
                    .withPartnerTransactionId(resetRequest.getPartnerTransactionId())
                    .build();
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            return ResetResponseFailure.ResetResponseFailureBuilder
                    .aResetResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .withPartnerTransactionId(partnerTransactionId)
                    .build();
        }
    }

    @Override
    public boolean canMultiple() {
        return false;
    }

    @Override
    public boolean canPartial() {
        return false;
    }
}
