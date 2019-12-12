package com.payline.payment.cvconnect.service.impl;

import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.bean.request.CVCoCreateTransactionRequest;
import com.payline.payment.cvconnect.bean.response.CVCoResponse;
import com.payline.payment.cvconnect.utils.Constants;
import com.payline.payment.cvconnect.utils.http.HttpClient;
import com.payline.payment.cvconnect.utils.i18n.I18nService;
import com.payline.payment.cvconnect.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.service.ConfigurationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConfigurationServiceImpl implements ConfigurationService {
    private static final String LABEL = "shopId.label";
    private static final String DESCRIPTION = "shopId.description";

    // message keys
    private static final String EMPTY_SHOP_ID = "error.shopId.missing";
    private static final String WRONG_SHOP_ID = "error.shopId.unknown";

    private ReleaseProperties releaseProperties = ReleaseProperties.getInstance();
    private I18nService i18n = I18nService.getInstance();
    private HttpClient client = HttpClient.getInstance();

    @Override
    public List<AbstractParameter> getParameters(Locale locale) {
        List<AbstractParameter> parameters = new ArrayList<>();

        // shopId inputParameter
        AbstractParameter shopId = new InputParameter();
        shopId.setKey(Constants.ContractConfigurationKeys.SHOP_ID_KEY);
        shopId.setLabel(i18n.getMessage(LABEL, locale));
        shopId.setDescription(i18n.getMessage(DESCRIPTION, locale));
        shopId.setRequired(true);
        parameters.add(shopId);

        return parameters;
    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest request) {
        final Map<String, String> errors = new HashMap<>();
        try {
            // check fields
            if (request.getAccountInfo().get(Constants.ContractConfigurationKeys.SHOP_ID_KEY) == null ||
                    request.getAccountInfo().get(Constants.ContractConfigurationKeys.SHOP_ID_KEY).isEmpty()) {
                errors.put(Constants.ContractConfigurationKeys.SHOP_ID_KEY, i18n.getMessage(EMPTY_SHOP_ID, request.getLocale()));
            } else {

                // create a request to test the ShopId
                RequestConfiguration configuration = new RequestConfiguration(
                        request.getContractConfiguration()
                        , request.getEnvironment()
                        , request.getPartnerConfiguration()
                );
                CVCoCreateTransactionRequest createTransactionRequest = new CVCoCreateTransactionRequest(request);
                CVCoResponse response = client.createTransaction(configuration, createTransactionRequest);

                if (!response.isOk() && "MERCHANT_NOT_ALLOWED".equalsIgnoreCase(response.getErrorCode())) {
                    errors.put(Constants.ContractConfigurationKeys.SHOP_ID_KEY, i18n.getMessage(WRONG_SHOP_ID, request.getLocale()));
                }
            }
        }catch (RuntimeException e){
            errors.put(ContractParametersCheckRequest.GENERIC_ERROR, e.getMessage());
        }
        return errors;
    }

    @Override
    public ReleaseInformation getReleaseInformation() {
        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate(LocalDate.parse(releaseProperties.get("release.date"), DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .withVersion(releaseProperties.get("release.version"))
                .build();
    }

    @Override
    public String getName(Locale locale) {
        return i18n.getMessage("paymentMethod.name", locale);
    }
}
