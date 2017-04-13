package com.amazonaws.serverless.proxy.internal.servlet;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;

import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;


public class AwsHttpServletRequestTest {

    private static final AwsProxyRequest contentTypeRequest = new AwsProxyRequestBuilder("/test", "GET")
            .header(HttpHeaders.CONTENT_TYPE, "application/xml; charset=utf-8").build();
    private static final AwsProxyRequest validCookieRequest = new AwsProxyRequestBuilder("/cookie", "GET")
            .header(HttpHeaders.COOKIE, "yummy_cookie=choco; tasty_cookie=strawberry").build();
    private static final AwsProxyRequest complexAcceptHeader = new AwsProxyRequestBuilder("/accept", "GET")
            .header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").build();
    private static final AwsProxyRequest queryString = new AwsProxyRequestBuilder("/test", "GET")
            .queryString("one", "two").queryString("three", "four").build();

    private static final MockLambdaContext mockContext = new MockLambdaContext();

    @Test
    public void headers_parseHeaderValue_multiValue() {
        AwsProxyHttpServletRequest request = new AwsProxyHttpServletRequest(contentTypeRequest, mockContext, null);
        // I'm also using this to double-check that I can get a header ignoring case
        List<Map.Entry<String, String>> values = request.parseHeaderValue(request.getHeader("content-type"));

        assertEquals(2, values.size());
        assertEquals("application/xml", values.get(0).getValue());
        assertNull(values.get(0).getKey());

        assertEquals("charset", values.get(1).getKey());
        assertEquals("utf-8", values.get(1).getValue());
    }

    @Test
    public void headers_parseHeaderValue_validMultipleCookie() {
        AwsProxyHttpServletRequest request = new AwsProxyHttpServletRequest(validCookieRequest, mockContext, null);
        List<Map.Entry<String, String>> values = request.parseHeaderValue(request.getHeader(HttpHeaders.COOKIE));

        assertEquals(2, values.size());
        assertEquals("yummy_cookie", values.get(0).getKey());
        assertEquals("choco", values.get(0).getValue());
        assertEquals("tasty_cookie", values.get(1).getKey());
        assertEquals("strawberry", values.get(1).getValue());
    }

    @Test
    public void headers_parseHeaderValue_complexAccept() {
        AwsProxyHttpServletRequest request = new AwsProxyHttpServletRequest(complexAcceptHeader, mockContext, null);
        List<Map.Entry<String, String>> values = request.parseHeaderValue(request.getHeader(HttpHeaders.ACCEPT));

        try {
            System.out.println(new ObjectMapper().writeValueAsString(values));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertEquals(3, values.size());
    }

    @Test
    public void queyrString_generateQueryString_validQuery() {
        AwsProxyHttpServletRequest request = new AwsProxyHttpServletRequest(queryString, mockContext, null);

        String parsedString = request.generateQueryString(queryString.getQueryStringParameters());
        assertEquals("one=two&three=four", parsedString);

        // TODO test url encoding, wrong parameters
    }
}
