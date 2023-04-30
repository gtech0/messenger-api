package com.labs.java_lab1.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class RestTemplateErrorHandler implements ResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().is5xxServerError()) {
            if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                throw new ServiceUnavailableException("Service is currently unavailable");
            }
        } else if (response.getStatusCode().is4xxClientError()) {
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new UnauthorizedException("Unauthorized access");
            }
        }
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

}
