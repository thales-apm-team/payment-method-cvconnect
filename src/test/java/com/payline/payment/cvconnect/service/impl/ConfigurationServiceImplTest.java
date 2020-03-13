package com.payline.payment.cvconnect.service.impl;


import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.response.PaymentResponse;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.payment.cvconnect.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.*;

class ConfigurationServiceImplTest {

    @InjectMocks
    private ConfigurationServiceImpl service;


    @Mock
    private ReleaseProperties releaseProperties;
    @Mock
    private HttpClient client;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void getParameters() {
        List<AbstractParameter> parameters = service.getParameters(Locale.FRANCE);

        Assertions.assertEquals(1, parameters.size());
    }

    @Test
    void check() {
        String json = MockUtils.aCVCoResponse(Transaction.State.AUTHORIZED);
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);
        Mockito.doReturn(cvCoPaymentResponse).when(client).createTransaction(Mockito.any(), Mockito.any());

        Map<String, String> errors = service.check(MockUtils.aContractParametersCheckRequest());

        Assertions.assertEquals(0, errors.size());
    }

    @Test
    void checkEmptyField() {
        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder()
                .withAccountInfo(new HashMap<>())
                .build();
        Map<String, String> errors = service.check(request);

        Assertions.assertEquals(1, errors.size());
    }

    @Test
    void checkWrongField() {
        String json = MockUtils.anErrorCVCoResponse("MERCHANT_NOT_ALLOWED");
        PaymentResponse cvCoPaymentResponse = PaymentResponse.fromJson(json);
        Mockito.doReturn(cvCoPaymentResponse).when(client).createTransaction(Mockito.any(), Mockito.any());

        Map<String, String> errors = service.check(MockUtils.aContractParametersCheckRequest());

        Assertions.assertEquals(1, errors.size());
    }


    @Test
    void getReleaseInformation() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String version = "M.m.p";

        // given: the release properties are OK
        Mockito.doReturn(version).when(releaseProperties).get("release.version");
        Calendar cal = new GregorianCalendar();
        cal.set(2019, Calendar.AUGUST, 19);
        Mockito.doReturn(formatter.format(cal.getTime())).when(releaseProperties).get("release.date");

        // when: calling the method getReleaseInformation
        ReleaseInformation releaseInformation = service.getReleaseInformation();

        // then: releaseInformation contains the right values
        Assertions.assertEquals(version, releaseInformation.getVersion());
        Assertions.assertEquals(2019, releaseInformation.getDate().getYear());
        Assertions.assertEquals(Month.AUGUST, releaseInformation.getDate().getMonth());
        Assertions.assertEquals(19, releaseInformation.getDate().getDayOfMonth());
    }

    @Test
    void getName() {
        // when: calling the method getName
        String name = service.getName(Locale.getDefault());

        // then: the method returns the name
        Assertions.assertNotNull(name);
    }
}