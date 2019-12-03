package com.payline.payment.cvconnect.bean.request;

import com.payline.payment.cvconnect.bean.common.*;
import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.utils.Constants;
import com.payline.payment.cvconnect.utils.PluginUtils;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.ArrayList;
import java.util.List;

public class CVCoCreateTransactionRequest extends CVCoRequest {

    private Merchant merchant;
    private Order order;
    private PaymentMethod paymentMethod;
    private RedirectUrls redirectUrls;
    private ApplicationContext applicationContext;


    public CVCoCreateTransactionRequest(ContractParametersCheckRequest request) {
        super();
        this.merchant = new Merchant(
                request.getAccountInfo().get(Constants.ContractConfigurationKeys.SHOP_ID_KEY)
                , request.getPartnerConfiguration().getProperty(Constants.PartnerConfigurationKeys.SERVICE_PROVIDER_ID)
        );
        this.order = new Order("01", "anId", "aLabel",
                new CVAmount("2000", 978));

        this.paymentMethod = new PaymentMethod("001", "NORMAL", null);
        this.redirectUrls = new RedirectUrls("anUrl", "anUrl");
        this.applicationContext = new ApplicationContext();
    }


    public CVCoCreateTransactionRequest(PaymentRequest request) {
        super();
        this.merchant = new Merchant(
                request.getContractConfiguration().getProperty(Constants.ContractConfigurationKeys.SHOP_ID_KEY).getValue()
                , request.getPartnerConfiguration().getProperty(Constants.PartnerConfigurationKeys.SERVICE_PROVIDER_ID)
        );
        this.order = new Order(
                PluginUtils.truncate(request.getOrder().getReference(), 20)
                , request.getTransactionId()
                , request.getSoftDescriptor()
                , new CVAmount(
                request.getAmount().getAmountInSmallestUnit().toString()
                , request.getAmount().getCurrency().getNumericCode()
        )
        );

        this.paymentMethod = new PaymentMethod(
                "001" // todo voir Q7
                , request.isCaptureNow() ? "NORMAL" : "DEFERRED"
                , request.isCaptureNow() ? null : request.getDifferedActionDate()
        );
        this.redirectUrls = new RedirectUrls(
                request.getEnvironment().getRedirectionReturnURL()
                , request.getEnvironment().getRedirectionCancelURL()
        );
        this.applicationContext = new ApplicationContext();

    }

    public Merchant getMerchant() {
        return merchant;
    }

    public Order getOrder() {
        return order;
    }


    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public RedirectUrls getRedirectUrls() {
        return redirectUrls;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public String getANCVSecurity(RequestConfiguration configuration) {
        List<String> sealFields = new ArrayList<>();
        sealFields.add(this.merchant.getShopId());
        sealFields.add(this.merchant.getServiceProviderId());
        sealFields.add(this.order.getId());
        sealFields.add(this.order.getPaymentId());
        sealFields.add(this.order.getAmount().getTotal());
        return PluginUtils.getSealHeader(configuration, sealFields);
    }

}
