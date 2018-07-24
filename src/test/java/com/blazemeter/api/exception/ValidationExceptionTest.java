package com.blazemeter.api.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValidationExceptionTest {

    @Test
    public void test() throws Exception {
        ValidationException exception = new ValidationException();
        assertNotNull(exception);
        exception = new ValidationException("msg");
        assertEquals("msg", exception.getMessage());
        exception = new ValidationException("msg1", new RuntimeException("nested exception"));
        assertEquals("nested exception", exception.getCause().getMessage());
    }
}