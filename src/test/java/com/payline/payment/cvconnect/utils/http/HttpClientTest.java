package com.payline.payment.cvconnect.utils.http;

import com.payline.payment.cvconnect.MockUtils;
import com.payline.payment.cvconnect.bean.common.Transaction;
import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;
import com.payline.payment.cvconnect.bean.request.CancelRequest;
import com.payline.payment.cvconnect.bean.request.ConfirmTransactionRequest;
import com.payline.payment.cvconnect.bean.request.CreateTransactionRequest;
import com.payline.payment.cvconnect.bean.request.GetTransactionStatusRequest;
import com.payline.payment.cvconnect.bean.response.PaymentResponse;
import com.payline.payment.cvconnect.exception.PluginException;
import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpClientTest {

    @Spy
    @InjectMocks
    private HttpClient client = HttpClient.getInstance();

    @Mock
    private org.apache.http.client.HttpClient http;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createTransaction() {
        // init data
        RequestConfiguration configuration = new RequestConfiguration(
                MockUtils.aContractConfiguration()
                , MockUtils.anEnvironment()
                , MockUtils.aPartnerConfiguration()
        );
        CreateTransactionRequest request = new CreateTransactionRequest(MockUtils.aPaylinePaymentRequest());

        // create Mock
        StringResponse stringResponse = MockUtils.mockStringResponse(
                200
                , "OK"
                , MockUtils.aCVCoResponse(Transaction.State.AUTHORIZED)
                , null);
        Mockito.doReturn(stringResponse).when(client).execute(Mockito.any());


        // test method
        PaymentResponse response = client.createTransaction(configuration, request);

        // assertions
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getTransaction());
    }

    @Test
    void confirmTransaction() {
        // init data
        RequestConfiguration configuration = new RequestConfiguration(
                MockUtils.aContractConfiguration()
                , MockUtils.anEnvironment()
                , MockUtils.aPartnerConfiguration()
        );
        ConfirmTransactionRequest request = new ConfirmTransactionRequest(MockUtils.aPaylinePaymentRequest(), "1");

        // create Mock
        StringResponse stringResponse = MockUtils.mockStringResponse(
                200
                , "OK"
                , MockUtils.aCVCoResponse(Transaction.State.AUTHORIZED)
                , null);
        Mockito.doReturn(stringResponse).when(client).execute(Mockito.any());


        // test method
        PaymentResponse response = client.confirmTransaction(configuration, request);

        // assertions
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getTransaction());
    }

    @Test
    void cancelTransaction() {
        // init data
        RequestConfiguration configuration = new RequestConfiguration(
                MockUtils.aContractConfiguration()
                , MockUtils.anEnvironment()
                , MockUtils.aPartnerConfiguration()
        );
        CancelRequest request = new CancelRequest(MockUtils.aPaylineResetRequest());

        // create Mock
        StringResponse stringResponse = MockUtils.mockStringResponse(
                200
                , "OK"
                , MockUtils.aCVCoResponse(Transaction.State.AUTHORIZED)
                , null);
        Mockito.doReturn(stringResponse).when(client).execute(Mockito.any());


        // test method
        PaymentResponse response = client.cancelTransaction(configuration, request);

        // assertions
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getTransaction());
    }

    @Test
    void getTransactionStatus() {
        // init data
        RequestConfiguration configuration = new RequestConfiguration(
                MockUtils.aContractConfiguration()
                , MockUtils.anEnvironment()
                , MockUtils.aPartnerConfiguration()
        );
        GetTransactionStatusRequest request = new GetTransactionStatusRequest("1");

        // create Mock
        StringResponse stringResponse = MockUtils.mockStringResponse(
                200
                , "OK"
                , MockUtils.aCVCoResponse(Transaction.State.AUTHORIZED)
                , null);
        Mockito.doReturn(stringResponse).when(client).execute(Mockito.any());


        // test method
        PaymentResponse response = client.getTransactionStatus(configuration, request);

        // assertions
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getTransaction());
    }

    @Test
    void execute_nominal() throws IOException {
        // given: a properly formatted request, which gets a proper response
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        int expectedStatusCode = 200;
        String expectedStatusMessage = "OK";
        String expectedContent = "{\"content\":\"fake\"}";
        doReturn(mockHttpResponse(expectedStatusCode, expectedStatusMessage, expectedContent, null))
                .when(http).execute(request);

        // when: sending the request
        StringResponse stringResponse = client.execute(request);

        // then: the content of the StringResponse reflects the content of the HTTP response
        assertNotNull(stringResponse);
        assertEquals(expectedStatusCode, stringResponse.getStatusCode());
        assertEquals(expectedStatusMessage, stringResponse.getStatusMessage());
        assertEquals(expectedContent, stringResponse.getContent());
    }

    @Test
    void execute_retry() throws IOException {
        // given: the first 2 requests end up in timeout, the third request gets a response
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        when(http.execute(request))
                .thenThrow(ConnectTimeoutException.class)
                .thenThrow(ConnectTimeoutException.class)
                .thenReturn(mockHttpResponse(200, "OK", "content", null));

        // when: sending the request
        StringResponse stringResponse = client.execute(request);

        // then: the client finally gets the response
        assertNotNull(stringResponse);
    }

    @Test
    void execute_retryFail() throws IOException {
        // given: a request which always gets an exception
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        doThrow(IOException.class).when(http).execute(request);

        // when: sending the request, a PluginException is thrown
        assertThrows(PluginException.class, () -> client.execute(request));
    }

    @Test
    void execute_invalidResponse() throws IOException {
        // given: a request that gets an invalid response (null)
        HttpGet request = new HttpGet("http://domain.test.fr/malfunctioning-endpoint");
        doReturn(null).when(http).execute(request);

        // when: sending the request, a PluginException is thrown
        assertThrows(PluginException.class, () -> client.execute(request));
    }


    /**
     * Mock an HTTP Response with the given elements.
     *
     * @param statusCode    The HTTP status code (ex: 200)
     * @param statusMessage The HTTP status message (ex: "OK")
     * @param content       The response content/body
     * @return A mocked HTTP response
     */
    private static CloseableHttpResponse mockHttpResponse(int statusCode, String statusMessage, String content, Header[] headers) {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        doReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, statusMessage))
                .when(response).getStatusLine();
        doReturn(new StringEntity(content, StandardCharsets.UTF_8)).when(response).getEntity();
        if (headers != null && headers.length >= 1) {
            doReturn(headers).when(response).getAllHeaders();
        } else {
            doReturn(new Header[]{}).when(response).getAllHeaders();
        }
        return response;
    }
}