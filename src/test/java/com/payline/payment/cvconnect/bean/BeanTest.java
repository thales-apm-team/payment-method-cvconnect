package com.payline.payment.cvconnect.bean;

import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.bean.request.CVCoCancelRequest;
import com.payline.payment.cvconnect.bean.request.CVCoConfirmTransactionRequest;
import com.payline.payment.cvconnect.bean.request.CVCoCreateTransactionRequest;
import com.payline.payment.cvconnect.bean.request.CVCoGetTransactionStatusRequest;
import com.payline.payment.cvconnect.bean.response.CVCoPaymentResponse;
import com.payline.pmapi.bean.common.*;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.bean.reset.request.ResetRequest;
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

        CVCoCreateTransactionRequest request = new CVCoCreateTransactionRequest(paymentRequest);
        Assertions.assertNotNull(request.getMerchant());
        Assertions.assertNotNull(request.getOrder());
        Assertions.assertNotNull(request.getPaymentMethod());
        Assertions.assertNotNull(request.getRedirectUrls());
        Assertions.assertNotNull(request.getANCVSecurity());
    }

    @Test
    void createTransactionRequestFromConfigurationRequestTest() {
        ContractParametersCheckRequest contractRequest = MockUtils.aContractParametersCheckRequest();
        RequestConfiguration configuration = new RequestConfiguration(
                contractRequest.getContractConfiguration()
                , contractRequest.getEnvironment()
                , contractRequest.getPartnerConfiguration()
        );

        CVCoCreateTransactionRequest request = new CVCoCreateTransactionRequest(contractRequest);
        Assertions.assertNotNull(request.getMerchant());
        Assertions.assertNotNull(request.getOrder());
        Assertions.assertNotNull(request.getPaymentMethod());
        Assertions.assertNotNull(request.getRedirectUrls());
        Assertions.assertNotNull(request.getANCVSecurity());
    }


    @Test
    void confirmTransactionRequestTest() {
        PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        RequestConfiguration configuration = new RequestConfiguration(
                paymentRequest.getContractConfiguration()
                , paymentRequest.getEnvironment()
                , paymentRequest.getPartnerConfiguration()
        );

        CVCoConfirmTransactionRequest request = new CVCoConfirmTransactionRequest(paymentRequest, "1");
        Assertions.assertNotNull(request.getId());
        Assertions.assertNotNull(request.getPayer());
        Assertions.assertNotNull(request.getANCVSecurity());
    }

    @Test
    void getTransactionStatusRequestTest() {
        RequestConfiguration configuration = new RequestConfiguration(
                MockUtils.aContractConfiguration()
                , MockUtils.anEnvironment()
                , MockUtils.aPartnerConfiguration()
        );

        CVCoGetTransactionStatusRequest request = new CVCoGetTransactionStatusRequest("1");
        Assertions.assertNotNull(request.getId());
        Assertions.assertNotNull(request.getANCVSecurity());
    }

    @Test
    void cancelRequestTest() {
        ResetRequest resetRequest = MockUtils.aPaylineResetRequest();

        CVCoCancelRequest request = new CVCoCancelRequest(resetRequest);
        Assertions.assertNotNull(request.getId());
        Assertions.assertNotNull(request.getReason());
        Assertions.assertNotNull(request.getANCVSecurity());
    }

    @Test
    void PaymentResponseTest() {
        String json = MockUtils.aCVCoResponse("foo");
        CVCoPaymentResponse response = CVCoPaymentResponse.fromJson(json);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getTransaction());
        Assertions.assertNotNull(response.getPaylineStatus());
        Assertions.assertNotNull(response.getPaylinePaymentResponse());
    }

    @Test
    void PaymentErrorResponseTest(){
        String json = MockUtils.anErrorCVCoResponse("foo");
        CVCoPaymentResponse response = CVCoPaymentResponse.fromJson(json);

        Assertions.assertFalse(response.isOk());
        Assertions.assertNotNull(response.getErrorCode());
        Assertions.assertNotNull(response.getErrorMessage());
    }


    private static Stream<Arguments> statusSet() {
        return Stream.of(
                Arguments.of(Transaction.State.INITIALIZED, OnHoldTransactionStatus.class, null),
                Arguments.of(Transaction.State.PROCESSING, OnHoldTransactionStatus.class, null),
                Arguments.of(Transaction.State.AUTHORIZED, OnHoldTransactionStatus.class, null),
                Arguments.of(Transaction.State.VALIDATED, OnHoldTransactionStatus.class, null),
                Arguments.of(Transaction.State.CONSIGNED, OnHoldTransactionStatus.class, null),

                Arguments.of(Transaction.State.CANCELLED, SuccessTransactionStatus.class, null),
                Arguments.of(Transaction.State.PAID, SuccessTransactionStatus.class, null),
                Arguments.of(Transaction.State.REJECTED, FailureTransactionStatus.class, FailureCause.REFUSED),
                Arguments.of(Transaction.State.ABORTED, FailureTransactionStatus.class, FailureCause.CANCEL),
                Arguments.of(Transaction.State.EXPIRED, FailureTransactionStatus.class, FailureCause.SESSION_EXPIRED),
                Arguments.of("UNKNOWN", FailureTransactionStatus.class, FailureCause.INVALID_DATA)
        );
    }

    @ParameterizedTest
    @MethodSource("statusSet")
    void PaymentResponsePaylineStatusTest(String status, Class expectedStatusClass, FailureCause expectedCause) {
        String json = MockUtils.aCVCoResponse(status);
        CVCoPaymentResponse response = CVCoPaymentResponse.fromJson(json);

        Assertions.assertNotNull(response);
        TransactionStatus transactionStatus = response.getPaylineStatus();
        Assertions.assertEquals(expectedStatusClass, transactionStatus.getClass());
        if (expectedStatusClass.equals(FailureTransactionStatus.class)){
            FailureTransactionStatus failureStatus = (FailureTransactionStatus) transactionStatus;
            Assertions.assertEquals(expectedCause, failureStatus.getFailureCause());
        }
    }

    private static Stream<Arguments> statusSet2() {
        return Stream.of(
                Arguments.of(Transaction.State.INITIALIZED, PaymentResponseOnHold.class, null),
                Arguments.of(Transaction.State.PROCESSING, PaymentResponseOnHold.class, null),
                Arguments.of(Transaction.State.AUTHORIZED, PaymentResponseOnHold.class, null),
                Arguments.of(Transaction.State.VALIDATED, PaymentResponseOnHold.class, null),
                Arguments.of(Transaction.State.CONSIGNED, PaymentResponseOnHold.class, null),

                Arguments.of(Transaction.State.CANCELLED, PaymentResponseSuccess.class, null),
                Arguments.of(Transaction.State.PAID, PaymentResponseSuccess.class, null),
                Arguments.of(Transaction.State.REJECTED, PaymentResponseFailure.class, FailureCause.REFUSED),
                Arguments.of(Transaction.State.ABORTED, PaymentResponseFailure.class, FailureCause.CANCEL),
                Arguments.of(Transaction.State.EXPIRED, PaymentResponseFailure.class, FailureCause.SESSION_EXPIRED),
                Arguments.of("UNKNOWN", PaymentResponseFailure.class, FailureCause.INVALID_DATA)
        );
    }

    @ParameterizedTest
    @MethodSource("statusSet2")
    void PaymentResponsePaylinePaymentResponseTest(String status, Class expectedStatusClass, FailureCause expectedCause) {
        String json = MockUtils.aCVCoResponse(status);
        CVCoPaymentResponse response = CVCoPaymentResponse.fromJson(json);

        Assertions.assertNotNull(response);
        PaymentResponse paymentResponse = response.getPaylinePaymentResponse();
        Assertions.assertEquals(expectedStatusClass, paymentResponse.getClass());
        if (expectedStatusClass.equals(PaymentResponseFailure.class)){
            PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponse;
            Assertions.assertEquals(expectedCause, responseFailure.getFailureCause());
        }
    }


    private static Stream<Arguments> statusSet3() {
        return Stream.of(
                Arguments.of("BAD_REQUEST", FailureCause.INVALID_DATA),
                Arguments.of("INVALID_SEAL", FailureCause.INVALID_DATA),
                Arguments.of("MERCHANT_NOT_ALLOWED", FailureCause.INVALID_DATA),
                Arguments.of("OPERATION_TRANSACTION_NOT_ALLOWED", FailureCause.INVALID_DATA),
                Arguments.of("TRANSACTION_NOT_FOUND", FailureCause.INVALID_DATA),

                Arguments.of("BENEFICIARY_NOT_FOUND", FailureCause.INVALID_DATA),
                Arguments.of("OTHER_TRANSACTION_PENDING", FailureCause.INVALID_DATA),
                Arguments.of("INVALID_TRANSACTION_AMOUNT",  FailureCause.INVALID_DATA),
                Arguments.of("INVALID_TRANSACTION_CURRENCY", FailureCause.INVALID_DATA),
                Arguments.of("INTERNAL_SERVER_ERROR",  FailureCause.PARTNER_UNKNOWN_ERROR),
                Arguments.of("UNKNOWN",  FailureCause.PARTNER_UNKNOWN_ERROR)
        );
    }

    @ParameterizedTest
    @MethodSource("statusSet3")
    void PaymentResponseFailureCause(String errorCode, FailureCause expectedFailureCause){
        String json = MockUtils.anErrorCVCoResponse(errorCode);
        CVCoPaymentResponse response = CVCoPaymentResponse.fromJson(json);

        Assertions.assertEquals(expectedFailureCause, response.getFailureCause());

    }
}
