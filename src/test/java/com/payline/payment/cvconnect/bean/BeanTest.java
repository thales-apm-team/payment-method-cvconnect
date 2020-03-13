package com.payline.payment.cvconnect.bean;

import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.request.CancelRequest;
import com.payline.payment.cvconnect.bean.request.ConfirmTransactionRequest;
import com.payline.payment.cvconnect.bean.request.CreateTransactionRequest;
import com.payline.payment.cvconnect.bean.request.GetTransactionStatusRequest;
import com.payline.payment.cvconnect.bean.response.PaymentResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class BeanTest {

    @Test
    void createTransactionRequestTest() {
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();

        CreateTransactionRequest request = new CreateTransactionRequest(paymentRequest);
        Assertions.assertNotNull(request.getMerchant());
        Assertions.assertNotNull(request.getOrder());
        Assertions.assertNotNull(request.getPaymentMethod());
        Assertions.assertNotNull(request.getRedirectUrls());
        Assertions.assertNotNull(request.getANCVSecurity());
    }

    @Test
    void createTransactionRequestFromConfigurationRequestTest() {
        ContractParametersCheckRequest contractRequest = MockUtils.aContractParametersCheckRequest();

        CreateTransactionRequest request = new CreateTransactionRequest(contractRequest);
        Assertions.assertNotNull(request.getMerchant());
        Assertions.assertNotNull(request.getOrder());
        Assertions.assertNotNull(request.getPaymentMethod());
        Assertions.assertNotNull(request.getRedirectUrls());
        Assertions.assertNotNull(request.getANCVSecurity());
    }


    @Test
    void confirmTransactionRequestTest() {
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();

        ConfirmTransactionRequest request = new ConfirmTransactionRequest(paymentRequest, "1");
        Assertions.assertNotNull(request.getId());
        Assertions.assertNotNull(request.getPayer());
        Assertions.assertNotNull(request.getANCVSecurity());
    }

    @Test
    void getTransactionStatusRequestTest() {
        GetTransactionStatusRequest request = new GetTransactionStatusRequest("1");
        Assertions.assertNotNull(request.getId());
        Assertions.assertNotNull(request.getANCVSecurity());
    }

    @Test
    void cancelRequestTest() {
        RefundRequest refundRequest = MockUtils.aPaylineRefundRequest();

        CancelRequest request = new CancelRequest(refundRequest);
        Assertions.assertNotNull(request.getId());
        Assertions.assertNotNull(request.getReason());
        Assertions.assertNotNull(request.getANCVSecurity());
    }

    @Test
    void PaymentResponseTest() {
        String json = MockUtils.aCVCoResponse(Transaction.State.AUTHORIZED);
        PaymentResponse response = PaymentResponse.fromJson(json);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getTransaction());
//        Assertions.assertNotNull(response.getPaylineStatus());
//        Assertions.assertNotNull(response.getPaylinePaymentResponse());
    }

    @Test
    void PaymentErrorResponseTest() {
        String json = MockUtils.anErrorCVCoResponse("foo");
        PaymentResponse response = PaymentResponse.fromJson(json);

        Assertions.assertFalse(response.isOk());
        Assertions.assertNotNull(response.getErrorCode());
        Assertions.assertNotNull(response.getErrorMessage());
    }


    private static Stream<Arguments> subStatefund() {
        return Stream.of(
                Arguments.of("foo", "PAID.foo"),
                Arguments.of("", "PAID"),
                Arguments.of(null, "PAID")
        );
    }

    @ParameterizedTest
    @MethodSource("subStatefund")
    void getFullStateTest(String subState, String expected) {
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(MockUtils.aCVCoResponse(Transaction.State.PAID, subState));
        Assertions.assertEquals(expected, cvCoPaymentResponse.getTransaction().getFullState());
    }


    private static Stream<Arguments> statusSet() {
        return Stream.of(
                Arguments.of("BAD_REQUEST", FailureCause.INVALID_DATA),
                Arguments.of("INVALID_SEAL", FailureCause.INVALID_DATA),
                Arguments.of("MERCHANT_NOT_ALLOWED", FailureCause.INVALID_DATA),
                Arguments.of("OPERATION_TRANSACTION_NOT_ALLOWED", FailureCause.INVALID_DATA),
                Arguments.of("TRANSACTION_NOT_FOUND", FailureCause.INVALID_DATA),

                Arguments.of("BENEFICIARY_NOT_FOUND", FailureCause.INVALID_DATA),
                Arguments.of("OTHER_TRANSACTION_PENDING", FailureCause.INVALID_DATA),
                Arguments.of("INVALID_TRANSACTION_AMOUNT", FailureCause.INVALID_DATA),
                Arguments.of("INVALID_TRANSACTION_CURRENCY", FailureCause.INVALID_DATA),
                Arguments.of("INTERNAL_SERVER_ERROR", FailureCause.PARTNER_UNKNOWN_ERROR),
                Arguments.of("UNKNOWN", FailureCause.PARTNER_UNKNOWN_ERROR)
        );
    }

    @ParameterizedTest
    @MethodSource("statusSet")
    void PaymentResponseFailureCause(String errorCode, FailureCause expectedFailureCause) {
        String json = MockUtils.anErrorCVCoResponse(errorCode);
        PaymentResponse response = PaymentResponse.fromJson(json);

        Assertions.assertEquals(expectedFailureCause, response.getFailureCause());

    }
}
