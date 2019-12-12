package com.payline.payment.cvconnect.utils;

/**
 * Support for constants used everywhere in the plugin sources.
 */
public class Constants {

    /**
     * Keys for the entries in ContractConfiguration map.
     */
    public static class ContractConfigurationKeys {
        public static final String SHOP_ID_KEY = "shopId";


        /* Static utility class : no need to instantiate it (Sonar bug fix) */
        private ContractConfigurationKeys() {
        }
    }

    /**
     * Keys for the entries in PartnerConfiguration maps.
     */
    public static class PartnerConfigurationKeys {
        public static final String URL = "apiBaseUrl";
        public static final String SERVICE_PROVIDER_ID = "serviceProviderId";
        public static final String SEAL_KEY = "sealKey";
        public static final String SEAL_KEY_VERSION = "sealKeyVersion";


        /* Static utility class : no need to instantiate it (Sonar bug fix) */
        private PartnerConfigurationKeys() {
        }
    }


    /**
     * Keys for the entries in RequestContext data.
     */
    public static class RequestContextKeys {

        /* Static utility class : no need to instantiate it (Sonar bug fix) */
        private RequestContextKeys() {
        }
    }

    /**
     * Keys for the entries in PaymentFormContext data
     */
    public static class PaymentFormKeys {
        public static final String CVCO_ID_KEY = "CVCoBeneficiaryId";


        private PaymentFormKeys() {
        }
    }

    /* Static utility class : no need to instantiate it (Sonar bug fix) */
    private Constants() {
    }

}
