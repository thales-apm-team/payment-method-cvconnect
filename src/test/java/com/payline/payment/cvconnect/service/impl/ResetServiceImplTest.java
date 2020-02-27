package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.response.PaymentResponse;
import com.payline.payment.cvconnect.exception.PluginException;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.reset.request.ResetRequest;
import com.payline.pmapi.bean.reset.response.ResetResponse;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseFailure;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ResetServiceImplTest {
    @InjectMocks
    private ResetServiceImpl service = new ResetServiceImpl();

    @Mock
    private HttpClient client;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void resetRequest() {
        ResetRequest request = MockUtils.aPaylineResetRequest();

        String json = MockUtils.aCVCoResponse(Transaction.State.CANCELLED);
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);

        // create mock
        Mockito.doReturn(cvCoPaymentResponse).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        ResetResponse response = service.resetRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(ResetResponseSuccess.class, response.getClass());
    }

    @Test
    void resetRequestErrorResponse() {
        ResetRequest request = MockUtils.aPaylineResetRequest();

        String json = MockUtils.anErrorCVCoResponse("foo");
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);

        // create mock
        Mockito.doReturn(cvCoPaymentResponse).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        ResetResponse response = service.resetRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(ResetResponseFailure.class, response.getClass());
    }

    @Test
    void resetRequestWrongStatus() {
        ResetRequest request = MockUtils.aPaylineResetRequest();

        String json = MockUtils.aCVCoResponse(Transaction.State.ABORTED);
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);

        // create mock
        Mockito.doReturn(cvCoPaymentResponse).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        ResetResponse response = service.resetRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(ResetResponseFailure.class, response.getClass());
    }

    @Test
    void resetRequestPluginException() {
        ResetRequest request = MockUtils.aPaylineResetRequest();
        PluginException e = new PluginException("foo", FailureCause.INVALID_DATA);
        Mockito.doThrow(e).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        ResetResponse response = service.resetRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(ResetResponseFailure.class, response.getClass());
    }

    @Test
    void resetRequestRuntimeException() {
        ResetRequest request = MockUtils.aPaylineResetRequest();
        RuntimeException e = new RuntimeException("foo");
        Mockito.doThrow(e).when(client).cancelTransaction(Mockito.any(), Mockito.any());

        ResetResponse response = service.resetRequest(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(ResetResponseFailure.class, response.getClass());
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