package com.medexpress.consultation.controller;

import com.medexpress.consultation.dto.ErrorResponse;
import com.medexpress.consultation.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgument_returnsBadRequestStatus() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("some message"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleIllegalArgument_returnsExceptionMessageInBody() {
        String message = "some bad argument";

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException(message));

        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void handleProductNotFound_returnsNotFoundStatus() {
        ResponseEntity<ErrorResponse> response = handler.handleProductNotFound(
                new ProductNotFoundException("unknown-product"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleProductNotFound_returnsUserFriendlyMessageInBody() {
        ResponseEntity<ErrorResponse> response = handler.handleProductNotFound(
                new ProductNotFoundException("unknown-product"));

        assertNotNull(response.getBody());
        assertEquals("Product not found for productId: unknown-product", response.getBody().getMessage());
    }

    @Test
    void handleProductNotFound_populatesTimestamp() {
        ResponseEntity<ErrorResponse> response = handler.handleProductNotFound(
                new ProductNotFoundException("unknown-product"));

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleIllegalArgument_populatesTimestamp() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("some message"));

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleIllegalArgument_withNoMessage_returnsNullMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getMessage());
    }

    // --- handleMethodNotSupported ---

    @Test
    void handleMethodNotSupported_returnsMethodNotAllowedStatus() {
        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("POST"));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    void handleMethodNotSupported_returnsMessageContainingMethod() {
        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("POST"));

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("POST"));
    }

    @Test
    void handleMethodNotSupported_populatesTimestamp() {
        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("POST"));

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    // --- handleNoResourceFound ---

    @Test
    void handleNoResourceFound_returnsNotFoundStatus() throws Exception {
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "/unknown", "No static resource /unknown"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleNoResourceFound_returnsUserFriendlyMessage() throws Exception {
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "/unknown", "No static resource /unknown"));

        assertNotNull(response.getBody());
        assertEquals("The requested endpoint does not exist", response.getBody().getMessage());
    }

    @Test
    void handleNoResourceFound_populatesTimestamp() throws Exception {
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "/unknown", "No static resource /unknown"));

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    // --- handleMessageNotReadable ---

    @Test
    void handleMessageNotReadable_returnsBadRequestStatus() {
        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(
                new HttpMessageNotReadableException("bad body", new MockHttpInputMessage(new byte[0])));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleMessageNotReadable_returnsUserFriendlyMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(
                new HttpMessageNotReadableException("bad body", new MockHttpInputMessage(new byte[0])));

        assertNotNull(response.getBody());
        assertEquals("Request body is missing or malformed", response.getBody().getMessage());
    }

    @Test
    void handleMessageNotReadable_populatesTimestamp() {
        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadable(
                new HttpMessageNotReadableException("bad body", new MockHttpInputMessage(new byte[0])));

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    // --- handleMediaTypeNotSupported ---

    @Test
    void handleMediaTypeNotSupported_returnsUnsupportedMediaTypeStatus() {
        ResponseEntity<ErrorResponse> response = handler.handleMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)));

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    }

    @Test
    void handleMediaTypeNotSupported_returnsMessageContainingContentType() {
        ResponseEntity<ErrorResponse> response = handler.handleMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)));

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("text/plain"));
    }

    @Test
    void handleMediaTypeNotSupported_populatesTimestamp() {
        ResponseEntity<ErrorResponse> response = handler.handleMediaTypeNotSupported(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)));

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    // --- handleUnexpected ---

    @Test
    void handleUnexpected_returnsInternalServerErrorStatus() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new RuntimeException("something went wrong"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleUnexpected_returnsGenericMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new RuntimeException("something went wrong"));

        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later", response.getBody().getMessage());
    }

    @Test
    void handleUnexpected_populatesTimestamp() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new RuntimeException("something went wrong"));

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }
}
