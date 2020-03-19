package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.bean.request.GetTransactionStatusRequest;
import com.payline.payment.cvconnect.exception.PluginException;
import com.payline.payment.cvconnect.utils.PluginUtils;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.pmapi.bean.common.*;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.NotificationService;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);
    private HttpClient client = HttpClient.getInstance();

    @Override
    public NotificationResponse parse(NotificationRequest request) {
        NotificationResponse notificationResponse;
        String transactionId = request.getTransactionId();
        String partnerTransactionId = "UNKNOWN";
        try {
            // init data
            String content = PluginUtils.inputStreamToString(request.getContent());
            com.payline.payment.cvconnect.bean.response.PaymentResponse notificationPaymentResponse = com.payline.payment.cvconnect.bean.response.PaymentResponse.fromJson(content);
            partnerTransactionId = notificationPaymentResponse.getTransaction().getId();

            RequestConfiguration configuration = new RequestConfiguration(
                    request.getContractConfiguration()
                    , request.getEnvironment()
                    , request.getPartnerConfiguration());

            // get final status
            GetTransactionStatusRequest getTransactionStatusRequest = new GetTransactionStatusRequest(partnerTransactionId);
            com.payline.payment.cvconnect.bean.response.PaymentResponse response = client.getTransactionStatus(configuration, getTransactionStatusRequest);
            Transaction transaction = response.getTransaction();
            switch (transaction.getState()) {

                case VALIDATED:
                    Amount reservedAmount = new Amount(
                            transaction.getPayer().getFirstAuthorization().getAmount().getTotal()
                            , PluginUtils.getCurrencyFromCode(transaction.getPayer().getFirstAuthorization().getAmount().getCurrency() )
                    );
                    // PaymentResponseByNotificationResponse => Success
                    PaymentResponse paymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                            .aPaymentResponseSuccess()
                            .withPartnerTransactionId(partnerTransactionId)
                            .withStatusCode(transaction.getFullState())
                            .withReservedAmount(reservedAmount)  //
                            .withTransactionDetails(Email.EmailBuilder.anEmail().withEmail(transaction.getPayer().getBeneficiaryId()).build())
                            .build();

                    notificationResponse = createPaymentResponseByNotification(transactionId, paymentResponse);
                    break;

                case ABORTED:
                    // PaymentResponseByNotificationResponse => Failure
                    paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                            .aPaymentResponseFailure()
                            .withPartnerTransactionId(partnerTransactionId)
                            .withErrorCode(transaction.getFullState())
                            .withFailureCause(FailureCause.CANCEL)
                            .withTransactionDetails(new EmptyTransactionDetails())
                            .build();

                    notificationResponse = createPaymentResponseByNotification(transactionId, paymentResponse);
                    break;
                case EXPIRED:
                    // PaymentResponseByNotificationResponse => Failure
                    paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                            .aPaymentResponseFailure()
                            .withPartnerTransactionId(partnerTransactionId)
                            .withErrorCode(transaction.getFullState())
                            .withFailureCause(FailureCause.SESSION_EXPIRED)
                            .withTransactionDetails(new EmptyTransactionDetails())
                            .build();

                    notificationResponse = createPaymentResponseByNotification(transactionId, paymentResponse);
                    break;

                // TransactionStateChangedResponse
                case CONSIGNED:
                    TransactionStatus paylineStatus = OnHoldTransactionStatus.builder().onHoldCause(OnHoldCause.ASYNC_RETRY).build();
                    notificationResponse = createTransactionStateChanged(transactionId, partnerTransactionId, paylineStatus);
                    break;
                case REJECTED:
                    paylineStatus = FailureTransactionStatus.builder().failureCause(FailureCause.REFUSED).build();
                    notificationResponse = createTransactionStateChanged(transactionId, partnerTransactionId, paylineStatus);
                    break;
                case PAID:
                case CANCELLED:
                    paylineStatus = SuccessTransactionStatus.builder().build();
                    notificationResponse = createTransactionStateChanged(transactionId, partnerTransactionId, paylineStatus);
                    break;
                case INITIALIZED:
                case PROCESSING:
                case AUTHORIZED:
                default:
                    notificationResponse = new IgnoreNotificationResponse();
                    break;

            }

        } catch (PluginException e) {
            TransactionStatus failureStatus = FailureTransactionStatus.builder()
                    .failureCause(e.getFailureCause())
                    .build();

            notificationResponse = TransactionStateChangedResponse.TransactionStateChangedResponseBuilder
                    .aTransactionStateChangedResponse()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withTransactionId(transactionId)
                    .withTransactionStatus(failureStatus)
                    .withStatusDate(new Date())
                    .build();

        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            TransactionStatus failureStatus = FailureTransactionStatus.builder()
                    .failureCause(FailureCause.INTERNAL_ERROR)
                    .build();

            notificationResponse = TransactionStateChangedResponse.TransactionStateChangedResponseBuilder
                    .aTransactionStateChangedResponse()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withTransactionId(transactionId)
                    .withTransactionStatus(failureStatus)
                    .build();
        }

        return notificationResponse;
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        // does nothing
    }


    private PaymentResponseByNotificationResponse createPaymentResponseByNotification(String transactionId, PaymentResponse paymentResponse) {
        TransactionCorrelationId correlationId = TransactionCorrelationId.TransactionCorrelationIdBuilder
                .aCorrelationIdBuilder()
                .withType(TransactionCorrelationId.CorrelationIdType.TRANSACTION_ID)
                .withValue(transactionId)
                .build();

        return PaymentResponseByNotificationResponse.PaymentResponseByNotificationResponseBuilder
                .aPaymentResponseByNotificationResponseBuilder()
                .withPaymentResponse(paymentResponse)
                .withTransactionCorrelationId(correlationId)
                .build();
    }

    private TransactionStateChangedResponse createTransactionStateChanged(String transactionId, String partnerTransactionId, TransactionStatus status) {
        return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder
                .aTransactionStateChangedResponse()
                .withPartnerTransactionId(partnerTransactionId)
                .withTransactionId(transactionId)
                .withTransactionStatus(status)
                .withStatusDate(new Date())
                .build();
    }
}
