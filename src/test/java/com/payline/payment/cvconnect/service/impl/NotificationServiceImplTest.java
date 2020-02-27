package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.response.PaymentResponse;
import com.payline.payment.cvconnect.exception.PluginException;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.FailureTransactionStatus;
import com.payline.pmapi.bean.common.SuccessTransactionStatus;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;

class NotificationServiceImplTest {

    @InjectMocks
    NotificationServiceImpl service;

    @Mock
    private HttpClient client;


    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse() {
        String transactionId  ="123123123";
        String json = MockUtils.aCVCoResponse(Transaction.State.PAID);
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);

        NotificationRequest request = MockUtils.aPaylineNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(json.getBytes()))
                .withTransactionId(transactionId)
                .build();

        Mockito.doReturn(cvCoPaymentResponse).when(client).getTransactionStatus(Mockito.any(), Mockito.any());

        NotificationResponse response = service.parse(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(TransactionStateChangedResponse.class, response.getClass());
        TransactionStateChangedResponse transactionStateChangedResponse = (TransactionStateChangedResponse) response;
        Assertions.assertNotNull(transactionStateChangedResponse.getTransactionStatus());
        Assertions.assertEquals(SuccessTransactionStatus.class, transactionStateChangedResponse.getTransactionStatus().getClass());
        Assertions.assertEquals(transactionId, transactionStateChangedResponse.getTransactionId());
    }

    @Test
    void parseErrorResponse() {
        String transactionId  ="123123123";
        String json = MockUtils.aCVCoResponse("foo");
        String json2= MockUtils.anErrorCVCoResponse("foo");
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json2);


        NotificationRequest request = MockUtils.aPaylineNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(json.getBytes()))
                .withTransactionId(transactionId)
                .build();

        Mockito.doReturn(cvCoPaymentResponse).when(client).getTransactionStatus(Mockito.any(), Mockito.any());

        NotificationResponse response = service.parse(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(TransactionStateChangedResponse.class, response.getClass());
        TransactionStateChangedResponse transactionStateChangedResponse = (TransactionStateChangedResponse) response;
        Assertions.assertNotNull(transactionStateChangedResponse.getTransactionStatus());
        Assertions.assertEquals(FailureTransactionStatus.class, transactionStateChangedResponse.getTransactionStatus().getClass());
        Assertions.assertEquals(transactionId, transactionStateChangedResponse.getTransactionId());
    }

    @Test
    void parsePluginException() {
        String transactionId  ="123123123";
        String json = MockUtils.aCVCoResponse("foo");

        NotificationRequest request = MockUtils.aPaylineNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(json.getBytes()))
                .withTransactionId(transactionId)
                .build();

        PluginException e = new PluginException("foo", FailureCause.FRAUD_DETECTED);
        Mockito.doThrow(e).when(client).getTransactionStatus(Mockito.any(), Mockito.any());

        NotificationResponse response = service.parse(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(TransactionStateChangedResponse.class, response.getClass());
        TransactionStateChangedResponse transactionStateChangedResponse = (TransactionStateChangedResponse) response;
        Assertions.assertNotNull(transactionStateChangedResponse.getTransactionStatus());
        Assertions.assertEquals(FailureTransactionStatus.class, transactionStateChangedResponse.getTransactionStatus().getClass());
        Assertions.assertEquals(transactionId, transactionStateChangedResponse.getTransactionId());
    }

    @Test
    void parseRuntimeException() {
        String transactionId  ="123123123";
        String json = MockUtils.aCVCoResponse("foo");

        NotificationRequest request = MockUtils.aPaylineNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(json.getBytes()))
                .withTransactionId(transactionId)
                .build();

        RuntimeException e = new RuntimeException("foo");
        Mockito.doThrow(e).when(client).getTransactionStatus(Mockito.any(), Mockito.any());

        NotificationResponse response = service.parse(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(TransactionStateChangedResponse.class, response.getClass());
        TransactionStateChangedResponse transactionStateChangedResponse = (TransactionStateChangedResponse) response;
        Assertions.assertNotNull(transactionStateChangedResponse.getTransactionStatus());
        Assertions.assertEquals(FailureTransactionStatus.class, transactionStateChangedResponse.getTransactionStatus().getClass());
        Assertions.assertEquals(transactionId, transactionStateChangedResponse.getTransactionId());
    }
}