package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.response.PaymentResponse;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.util.stream.Stream;

import static com.payline.payment.cvconnect.bean.common.Transaction.State.*;

class NotificationServiceImplTest {
    @InjectMocks
    private NotificationServiceImpl service = new NotificationServiceImpl();

    @Mock
    private HttpClient client;


    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    private static Stream<Arguments> statusSet() {
        return Stream.of(
                Arguments.of(VALIDATED, PaymentResponseByNotificationResponse.class),
                Arguments.of(ABORTED, PaymentResponseByNotificationResponse.class),
                Arguments.of(EXPIRED, PaymentResponseByNotificationResponse.class),
                Arguments.of(CONSIGNED, TransactionStateChangedResponse.class),
                Arguments.of(REJECTED, TransactionStateChangedResponse.class),
                Arguments.of(PAID, TransactionStateChangedResponse.class),
                Arguments.of(CANCELLED, TransactionStateChangedResponse.class),
                Arguments.of(INITIALIZED, IgnoreNotificationResponse.class),
                Arguments.of(PROCESSING, IgnoreNotificationResponse.class),
                Arguments.of(AUTHORIZED, IgnoreNotificationResponse.class)
        );
    }

    @ParameterizedTest
    @MethodSource("statusSet")
    void parse(Transaction.State status, Class responseClass) {
        String transactionId = "123123123";
        String json = MockUtils.aCVCoResponse(status);

        NotificationRequest request = MockUtils.aPaylineNotificationRequestBuilder()
                .withContent(new ByteArrayInputStream(json.getBytes()))
                .withTransactionId(transactionId)
                .build();


        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);
        Mockito.doReturn(cvCoPaymentResponse).when(client).getTransactionStatus(Mockito.any(), Mockito.any());


        NotificationResponse response = service.parse(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(responseClass, response.getClass());
    }
}