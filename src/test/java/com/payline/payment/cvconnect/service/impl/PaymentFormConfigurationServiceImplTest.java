package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.utils.i18n.I18nService;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class PaymentFormConfigurationServiceImplTest {

    @InjectMocks
    private PaymentFormConfigurationServiceImpl service;

    @Mock
    private I18nService i18n;

    @BeforeEach
    void setup(){
        service = new PaymentFormConfigurationServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getPaymentFormConfiguration() {
        Mockito.doReturn("foo").when(i18n).getMessage(anyString(), any());

        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration(MockUtils.aPaymentFormConfigurationRequest());

        Assertions.assertEquals(PaymentFormConfigurationResponseSpecific.class, response.getClass());
    }

}