package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.response.PaymentResponse;
import com.payline.payment.cvconnect.exception.PluginException;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class RefundServiceImplTest {
    @InjectMocks
    private RefundServiceImpl service = new RefundServiceImpl();

    @Mock
    private HttpClient client;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void refundRequest() {
        RefundRequest request = MockUtils.aPaylineRefundRequest();

        String json = MockUtils.aCVCoResponse(Transaction.State.CANCELLED);
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);

        // create mock
        Mockito.doReturn(cvCoPaymentResponse).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        RefundResponse response = service.refundRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(RefundResponseSuccess.class, response.getClass());
    }

    @Test
    void refundRequestErrorResponse() {
        RefundRequest request = MockUtils.aPaylineRefundRequest();

        String json = MockUtils.anErrorCVCoResponse("foo");
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);

        // create mock
        Mockito.doReturn(cvCoPaymentResponse).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        RefundResponse response = service.refundRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(RefundResponseFailure.class, response.getClass());
    }

    @Test
    void refundRequestWrongStatus() {
        RefundRequest request = MockUtils.aPaylineRefundRequest();

        String json = MockUtils.aCVCoResponse(Transaction.State.ABORTED);
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);

        // create mock
        Mockito.doReturn(cvCoPaymentResponse).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        RefundResponse response = service.refundRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(RefundResponseFailure.class, response.getClass());
    }

    @Test
    void refundRequestPluginException() {
        RefundRequest request = MockUtils.aPaylineRefundRequest();
        PluginException e = new PluginException("foo", FailureCause.INVALID_DATA);
        Mockito.doThrow(e).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        RefundResponse response = service.refundRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(RefundResponseFailure.class, response.getClass());
    }

    @Test
    void refundRequestRuntimeException() {
        RefundRequest request = MockUtils.aPaylineRefundRequest();
        RuntimeException e = new RuntimeException("foo");
        Mockito.doThrow(e).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        RefundResponse response = service.refundRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(RefundResponseFailure.class, response.getClass());
    }

    @Test
    void canMultiple() {
        Assertions.assertFalse(service.canMultiple());
    }

    @Test
    void canPartial() {
        Assertions.assertFalse(service.canPartial());
    }
}