package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.response.CVCoPaymentResponse;
import com.payline.payment.cvconnect.exception.PluginException;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseActiveWaiting;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.service.PaymentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class PaymentServiceImplTest {

    @InjectMocks
    PaymentService service = new PaymentServiceImpl();

    @Mock
    HttpClient client = HttpClient.getInstance();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void paymentRequest() {
        // create Mock
        CVCoPaymentResponse createResponse = CVCoPaymentResponse.fromJson(MockUtils.aCVCoResponse(Transaction.State.INITIALIZED));
        Mockito.doReturn(createResponse).when(client).createTransaction(Mockito.any(), Mockito.any());

        CVCoPaymentResponse confirmResponse = CVCoPaymentResponse.fromJson(MockUtils.aCVCoResponse("foo"));
        Mockito.doReturn(confirmResponse).when(client).confirmTransaction(Mockito.any(), Mockito.any());

        PaymentRequest request = MockUtils.aPaylinePaymentRequest();
        PaymentResponse response = service.paymentRequest(request);
        Assertions.assertEquals(PaymentResponseActiveWaiting.class, response.getClass());
    }

    @Test
    void paymentRequestErrorCreateResponse() {
        // create Mock
        CVCoPaymentResponse createResponse = CVCoPaymentResponse.fromJson(MockUtils.aCVCoResponse("foo"));
        Mockito.doReturn(createResponse).when(client).createTransaction(Mockito.any(), Mockito.any());


        PaymentRequest request = MockUtils.aPaylinePaymentRequest();
        PaymentResponse response = service.paymentRequest(request);
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
    }


    @Test
    void paymentRequestInvalidCreateResponse() {
        // create Mock
        CVCoPaymentResponse createResponse = CVCoPaymentResponse.fromJson(MockUtils.anErrorCVCoResponse(Transaction.State.ABORTED));
        Mockito.doReturn(createResponse).when(client).createTransaction(Mockito.any(), Mockito.any());

        PaymentRequest request = MockUtils.aPaylinePaymentRequest();
        PaymentResponse response = service.paymentRequest(request);
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
    }


    @Test
    void paymentRequestErrorConfirmResponse() {
        // create Mock
        CVCoPaymentResponse createResponse = CVCoPaymentResponse.fromJson(MockUtils.aCVCoResponse(Transaction.State.INITIALIZED));
        Mockito.doReturn(createResponse).when(client).createTransaction(Mockito.any(), Mockito.any());

        CVCoPaymentResponse confirmResponse = CVCoPaymentResponse.fromJson(MockUtils.anErrorCVCoResponse("foo"));
        Mockito.doReturn(confirmResponse).when(client).confirmTransaction(Mockito.any(), Mockito.any());

        PaymentRequest request = MockUtils.aPaylinePaymentRequest();
        PaymentResponse response = service.paymentRequest(request);
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
    }

    @Test
    void paymentRequestPluginException() {
        // create Mock
        PluginException e = new PluginException("foo", FailureCause.FRAUD_DETECTED);
        Mockito.doThrow(e).when(client).createTransaction(Mockito.any(), Mockito.any());

        PaymentRequest request = MockUtils.aPaylinePaymentRequest();
        PaymentResponse response = service.paymentRequest(request);
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
    }

    @Test
    void paymentRequestRuntimeException() {
        // create Mock
        RuntimeException e = new RuntimeException("foo");
        Mockito.doThrow(e).when(client).createTransaction(Mockito.any(), Mockito.any());

        PaymentRequest request = MockUtils.aPaylinePaymentRequest();
        PaymentResponse response = service.paymentRequest(request);
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
    }
}