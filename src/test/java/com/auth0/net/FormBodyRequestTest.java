package com.auth0.net;

import com.auth0.client.MockServer;
import com.auth0.client.mgmt.TokenProvider;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.PushedAuthorizationResponse;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import com.auth0.net.client.HttpMethod;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.CompletableFuture;

import static com.auth0.client.MockServer.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

public class FormBodyRequestTest {
    private MockServer server;
    private Auth0HttpClient client;
    private TokenProvider tokenProvider;

    @SuppressWarnings("deprecation")
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private TypeReference<PushedAuthorizationResponse> pushedAuthorizationResponseTypeReference;

    @Before
    public void setUp() throws Exception {
        server = new MockServer();
        client = new DefaultHttpClient.Builder().withMaxRetries(0).build();
        pushedAuthorizationResponseTypeReference = new TypeReference<PushedAuthorizationResponse>() {
        };
        tokenProvider = new TokenProvider() {
            @Override
            public String getToken() throws Auth0Exception {
                return "xyz";
            }

            @Override
            public CompletableFuture<String> getTokenAsync() {
                return CompletableFuture.completedFuture("xyz");
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void shouldCreatePOSTRequest() throws Exception {
        FormBodyRequest<PushedAuthorizationResponse> request = new FormBodyRequest<>(client, tokenProvider, server.getBaseUrl(), HttpMethod.POST, pushedAuthorizationResponseTypeReference);
        assertThat(request, is(notNullValue()));
        request.addParameter("audience", "aud");
        request.addParameter("connection", "conn");

        server.jsonResponse(PUSHED_AUTHORIZATION_RESPONSE, 201);
        PushedAuthorizationResponse execute = request.execute().getBody();
        RecordedRequest recordedRequest = server.takeRequest();
        assertThat(recordedRequest.getMethod(), is(HttpMethod.POST.toString()));
        assertThat(execute, is(notNullValue()));
    }

    @Test
    public void shouldNotOverrideContentTypeHeader() throws Exception {
        FormBodyRequest<PushedAuthorizationResponse> request = new FormBodyRequest<>(client, tokenProvider, server.getBaseUrl(), HttpMethod.POST, pushedAuthorizationResponseTypeReference);
        request.addHeader("Content-Type", "plaintext");

        server.jsonResponse(PUSHED_AUTHORIZATION_RESPONSE, 201);
        request.execute().getBody();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest.getHeader("Content-Type"), containsString("application/x-www-form-urlencoded"));
    }

    @Test
    public void shouldAddHeaders() throws Exception {
        FormBodyRequest<PushedAuthorizationResponse> request = new FormBodyRequest<>(client, tokenProvider, server.getBaseUrl(), HttpMethod.POST, pushedAuthorizationResponseTypeReference);
        request.addHeader("Extra-Info", "this is a test");
        request.addHeader("Authorization", "Bearer my_access_token");

        server.jsonResponse(PUSHED_AUTHORIZATION_RESPONSE, 200);
        request.execute().getBody();
        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest.getHeader("Extra-Info"), is("this is a test"));
        assertThat(recordedRequest.getHeader("Authorization"), is("Bearer xyz"));
    }
}
