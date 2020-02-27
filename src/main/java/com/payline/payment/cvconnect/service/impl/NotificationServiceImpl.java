package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.bean.request.GetTransactionStatusRequest;
import com.payline.payment.cvconnect.bean.response.PaymentResponse;
import com.payline.payment.cvconnect.exception.PluginException;
import com.payline.payment.cvconnect.utils.PluginUtils;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.FailureTransactionStatus;
import com.payline.pmapi.bean.common.TransactionStatus;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.NotificationService;
import org.apache.logging.log4j.Logger;

public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);
    private HttpClient client = HttpClient.getInstance();

    @Override
    public NotificationResponse parse(NotificationRequest request) {
        String transactionId = request.getTransactionId();
        String partnerTransactionId = "UNKNOWN";

        try {
            // init data
            String content = PluginUtils.inputStreamToString(request.getContent());
            PaymentResponse cvcoNotificationResponse = PaymentResponse.fromJson(content);
            partnerTransactionId = cvcoNotificationResponse.getTransaction().getId();
            RequestConfiguration configuration = new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());

            // get final status
            GetTransactionStatusRequest getTransactionStatusRequest = new GetTransactionStatusRequest(partnerTransactionId);
            return getTransactionStateChangedResponseFromNotificationRequest(transactionId, partnerTransactionId, configuration, getTransactionStatusRequest);

        } catch (PluginException e) {
            TransactionStatus failureStatus = FailureTransactionStatus.builder()
                    .failureCause(e.getFailureCause())
                    .build();

            return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder
                    .aTransactionStateChangedResponse()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withTransactionId(transactionId)
                    .withTransactionStatus(failureStatus)
                    .build();

        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            TransactionStatus failureStatus = FailureTransactionStatus.builder()
                    .failureCause(FailureCause.INTERNAL_ERROR)
                    .build();

            return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder
                    .aTransactionStateChangedResponse()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withTransactionId(transactionId)
                    .withTransactionStatus(failureStatus)
                    .build();
        }
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        // does nothing
    }


    /**
     * Call CVConnect API to get the final transaction Status and return a TransactionStateChangedResponse
     *
     * @param transactionId
     * @param partnerTransactionId
     * @param configuration
     * @param request
     * @return
     */
    private TransactionStateChangedResponse getTransactionStateChangedResponseFromNotificationRequest(String transactionId, String partnerTransactionId, RequestConfiguration configuration, GetTransactionStatusRequest request) {
        PaymentResponse response = client.getTransactionStatus(configuration, request);
        // check response object
        if (!response.isOk()) {
            TransactionStatus failureStatus = FailureTransactionStatus.builder()
                    .failureCause(FailureCause.INVALID_DATA)
                    .build();

            return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder
                    .aTransactionStateChangedResponse()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withTransactionId(transactionId)
                    .withTransactionStatus(failureStatus)
                    .build();
        } else {
            return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder
                    .aTransactionStateChangedResponse()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withTransactionId(transactionId)
                    .withTransactionStatus(response.getPaylineStatus())
                    .build();
        }
    }
}
