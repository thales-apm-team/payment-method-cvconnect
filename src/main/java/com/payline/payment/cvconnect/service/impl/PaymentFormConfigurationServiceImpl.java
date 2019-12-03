package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.service.LogoPaymentFormConfigurationService;
import com.payline.payment.cvconnect.utils.Constants;
import com.payline.payment.cvconnect.utils.i18n.I18nService;
import com.payline.pmapi.bean.paymentform.bean.field.FieldIcon;
import com.payline.pmapi.bean.paymentform.bean.field.InputType;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormField;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormInputFieldText;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class PaymentFormConfigurationServiceImpl extends LogoPaymentFormConfigurationService {
    private static final String BUTTON_TEXT = "payment.form.config.button.text";
    private static final String BUTTON_DESCRIPTION = "payment.form.config.description";

    private static final String CVCO_ID_LABEL = "payment.form.config.id.label";
    private static final String CVCO_ID_PLACEHOLDER = "payment.form.config.id.placeholder";
    private static final String CVCO_ID_VALIDATION_ERROR = "payment.form.config.validation.error";

    private static final String emailPattern = "([a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*\\@[a-zA-Z0-9_]+\\.[a-zA-Z]{2,4})";
    private static final String phonePattern = "(\\d+)";
    private static final String fullPattern = "^(" + phonePattern + "|" + emailPattern + ")$";
    private static final Pattern pattern = Pattern.compile(fullPattern);

    private I18nService i18n = I18nService.getInstance();


    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
        Locale locale = paymentFormConfigurationRequest.getLocale();

        List<PaymentFormField> paymentFormFields = new ArrayList<>();

        PaymentFormInputFieldText field = PaymentFormInputFieldText.PaymentFormFieldTextBuilder
                .aPaymentFormFieldText()
                .withFieldIcon(FieldIcon.USER)
                .withKey(Constants.PaymentFormKeys.CVCO_ID_KEY)
                .withLabel(CVCO_ID_LABEL)
                .withInputType(InputType.TEXT)
                .withValidation(pattern)
                .withValidationErrorMessage(CVCO_ID_VALIDATION_ERROR)
                .withPlaceholder(CVCO_ID_PLACEHOLDER)
                .build();
        paymentFormFields.add(field);

        CustomForm form = CustomForm.builder()
                .withButtonText(i18n.getMessage(BUTTON_TEXT, locale))
                .withDescription(i18n.getMessage(BUTTON_DESCRIPTION, locale))
                .withDisplayButton(true)
                .withCustomFields(paymentFormFields)
                .build();

        return PaymentFormConfigurationResponseSpecific
                .PaymentFormConfigurationResponseSpecificBuilder
                .aPaymentFormConfigurationResponseSpecific()
                .withPaymentForm(form)
                .build();
    }
}
