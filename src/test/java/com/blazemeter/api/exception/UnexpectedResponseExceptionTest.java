package com.blazemeter.api.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnexpectedResponseExceptionTest {

    @Test
    public void test() throws Exception {
        UnexpectedResponseException exception = new UnexpectedResponseException();
        assertNotNull(exception);
        exception = new UnexpectedResponseException("msg");
        assertEquals("msg", exception.getMessage());
        exception = new UnexpectedResponseException("msg1", new RuntimeException("nested exception"));
        assertEquals("nested exception", exception.getCause().getMessage());
    }
}