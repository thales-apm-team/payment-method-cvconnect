package com.payline.payment.cvconnect.utils.http;


import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.bean.request.*;
import com.payline.payment.cvconnect.bean.response.PaymentResponse;
import com.payline.payment.cvconnect.exception.InvalidDataException;
import com.payline.payment.cvconnect.exception.PluginException;
import com.payline.payment.cvconnect.utils.Constants;
import com.payline.payment.cvconnect.utils.PluginUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.logger.LogManager;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;


public class HttpClient {
    private static final Logger LOGGER = LogManager.getLogger(HttpClient.class);

    //Headers
    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json";
    private static final String ANCV_SECURITY = "ANCV-Security";

    // Paths
    private static final String BASE_PATH = "payment-transactions";
    private static final String CONFIRM_TRANSACTION_PATH = "payer";
    private static final String CANCELLATION_PATH = "cancellation";

    // Exceptions messages
    private static final String SERVICE_URL_ERROR = "Service URL is invalid";

    private org.apache.http.client.HttpClient client;

    // --- Singleton Holder pattern + initialization BEGIN

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    private HttpClient() {
        // instantiate Apache HTTP client
        this.client = HttpClientBuilder.create()
                .useSystemProperties()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), SSLConnectionSocketFactory.getDefaultHostnameVerifier()))
                .build();
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    private static class Holder {
        private static final HttpClient instance = new HttpClient();
    }


    public static HttpClient getInstance() {
        return Holder.instance;
    }
    // --- Singleton Holder pattern + initialization END


    private String createPath(String... path) {
        StringBuilder sb = new StringBuilder("/");
        if (path != null && path.length > 0) {
            for (String aPath : path) {
                sb.append(aPath).append("/");
            }
        }
        return sb.toString();
    }

    private Header[] createHeaders(RequestConfiguration configuration, Request request) {
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
        headers[1] = new BasicHeader(ANCV_SECURITY, PluginUtils.getSealHeader(configuration, request.getANCVSecurity()));
        return headers;
    }


    /**
     * Send the request, with a retry system in case the client does not obtain a proper response from the server.
     *
     * @param httpRequest The request to send.
     * @return The response converted as a {@link StringResponse}.
     * @throws PluginException If an error repeatedly occurs and no proper response is obtained.
     */
    StringResponse execute(HttpRequestBase httpRequest) {
        StringResponse strResponse = null;
        int attempts = 1;

//          The number of time the client must retry to send the request if it doesn't obtain a response.
        int retries = 3;
        while (strResponse == null && attempts <= retries) {
            LOGGER.info("Start call to partner API [{} {}] (attempt {})", httpRequest.getMethod(), httpRequest.getURI(), attempts);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.client.execute(httpRequest)) {
                strResponse = StringResponse.fromHttpResponse(httpResponse);
            } catch (IOException e) {
                LOGGER.error("An error occurred during the HTTP call :", e);
                strResponse = null;
            } finally {
                attempts++;
            }
        }

        if (strResponse == null) {
            throw new PluginException("Failed to contact the partner API", FailureCause.COMMUNICATION_ERROR);
        }
        LOGGER.info("Response obtained from partner API [{} {}]", strResponse.getStatusCode(), strResponse.getStatusMessage());
        return strResponse;
    }


    /**
     * Manage GET API call
     *
     * @param configuration Contain variables needed to construct the request (url, timeOut etc...)
     * @param path The path to call
     * @param headers Headers of the http request
     * @return
     */
    StringResponse get(RequestConfiguration configuration, String path, Header[] headers) {
        String url = configuration.getPartnerConfiguration().getProperty(Constants.PartnerConfigurationKeys.URL);

        URI uri;
        try {
            // Add the createOrderId to the url
            uri = new URI(url + path);
        } catch (URISyntaxException e) {
            throw new InvalidDataException(SERVICE_URL_ERROR, e);
        }

        final HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeaders(headers);

        // Execute request
        return this.execute(httpGet);
    }

    /**
     * Manage Post API call
     *
     * @param configuration Contain variables needed to construct the request (url, timeOut etc...)
     * @param path The path to call
     * @param headers Headers of the http request
     * @param body The body of the request
     * @return
     */
    StringResponse post(RequestConfiguration configuration, String path, Header[] headers, StringEntity body) {
        String url = configuration.getPartnerConfiguration().getProperty(Constants.PartnerConfigurationKeys.URL);
        URI uri;
        try {
            // Add the createOrderId to the url
            uri = new URI(url + path);
        } catch (URISyntaxException e) {
            throw new InvalidDataException(SERVICE_URL_ERROR, e);
        }

        final HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeaders(headers);
        httpPost.setEntity(body);

        // Timeout
        final String readTimeout = configuration.getPartnerConfiguration().getProperty(Constants.PartnerConfigurationKeys.READ_TIMEOUT);
        final String connectTimeout = configuration.getPartnerConfiguration().getProperty(Constants.PartnerConfigurationKeys.CONNECT_TIMEOUT);


        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Integer.valueOf(connectTimeout))
                .setSocketTimeout(Integer.valueOf(readTimeout))
                .build();

        httpPost.setConfig(requestConfig);


        // Execute request
        return this.execute(httpPost);
    }


    // create transaction POST request
    public PaymentResponse createTransaction(RequestConfiguration requestConfiguration, CreateTransactionRequest request) {

        // create all data needed to do the call
        String body = request.toString();
        String path = createPath(BASE_PATH);
        Header[] headers = createHeaders(requestConfiguration, request);

        // do the http call
        StringResponse response = post(requestConfiguration, path, headers, new StringEntity(body, StandardCharsets.UTF_8));
        return PaymentResponse.fromJson(response.getContent());
    }


    // confirm transaction POST request
    public PaymentResponse confirmTransaction(RequestConfiguration requestConfiguration, ConfirmTransactionRequest request) {

        // create all data needed to do the call
        String body = request.toString();
        String path = createPath(BASE_PATH, request.getId(), CONFIRM_TRANSACTION_PATH);
        Header[] headers = createHeaders(requestConfiguration, request);

        // do the http call
        StringResponse response = post(requestConfiguration, path, headers, new StringEntity(body, StandardCharsets.UTF_8));
        return PaymentResponse.fromJson(response.getContent());
    }

    // cancel transaction
    public PaymentResponse cancelTransaction(RequestConfiguration requestConfiguration, CancelRequest request) {
        // create all data needed to do the call
        String body = request.toString();
        String path = createPath(BASE_PATH, request.getId(), CANCELLATION_PATH);
        Header[] headers = createHeaders(requestConfiguration, request);

        // do the http call
        StringResponse response = post(requestConfiguration, path, headers, new StringEntity(body, StandardCharsets.UTF_8));
        return PaymentResponse.fromJson(response.getContent());
    }

    // get transaction status GET request
    public PaymentResponse getTransactionStatus(RequestConfiguration requestConfiguration, GetTransactionStatusRequest request) {
        // create all data needed to do the call
        String path = createPath(BASE_PATH, request.getId());
        Header[] headers = createHeaders(requestConfiguration, request);

        // do the http call
        StringResponse response = get(requestConfiguration, path, headers);
        return PaymentResponse.fromJson(response.getContent());
    }
}
