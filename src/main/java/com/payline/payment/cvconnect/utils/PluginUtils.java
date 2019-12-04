package com.payline.payment.cvconnect.utils;


import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.exception.InvalidDataException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class PluginUtils {
    private static final String SEAL_ALGORITHM = "HmacSHA256";

    private PluginUtils() {
        // ras.
    }

    public static String truncate(String value, int length) {
        if (value != null && value.length() > length) {
            value = value.substring(0, length);
        }
        return value;
    }

    /**
     * Convert an InputStream into a String
     *
     * @param stream the InputStream to convert
     * @return the converted String encoded in UTF-8
     */
    public static String inputStreamToString(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * @param fields
     * @return
     */
    public static String getSealHeader(RequestConfiguration configuration, List<String> fields) {
        String sealKey = configuration.getPartnerConfiguration().getProperty(Constants.PartnerConfigurationKeys.SEAL_KEY);
        String sealKeyVersion = configuration.getPartnerConfiguration().getProperty(Constants.PartnerConfigurationKeys.SEAL_KEY_VERSION);

        try {
            Mac hmac = Mac.getInstance(SEAL_ALGORITHM);
            hmac.init(new SecretKeySpec(sealKey.getBytes(StandardCharsets.UTF_8), SEAL_ALGORITHM));
            byte[] seal = hmac.doFinal(String.join("&", fields).getBytes(StandardCharsets.UTF_8));

            String base64Seal = Base64.getUrlEncoder().withoutPadding().encodeToString(seal);

            return String.join(".", SEAL_ALGORITHM, sealKeyVersion, base64Seal);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

}