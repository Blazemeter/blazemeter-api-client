package com.blazemeter.api.logging.impl;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileLoggerTest {

    @Test
    public void testFlow() throws IOException {
        File logfile = File.createTempFile("logfile", ".log");
        logfile.deleteOnExit();

        FileLogger logger = new FileLogger(logfile.getAbsolutePath());
        logger.debug("debug");
        logger.debug("debug", new RuntimeException());

        logger.info("info");
        logger.info("info", new RuntimeException());

        logger.warn("warning");
        logger.warn("warning", new RuntimeException());

        logger.error("error");
        logger.error("error", new RuntimeException());

        logger.close();

        String logs = FileUtils.readFileToString(logfile);
        System.out.println(logs);

        assertEquals(2, countSubstring("debug", logs));
        assertEquals(2, countSubstring("info", logs));
        assertEquals(2, countSubstring("warning", logs));
        assertEquals(2, countSubstring("error", logs));
        assertEquals(4, countSubstring("RuntimeException", logs));
    }

    private static int countSubstring(String subStr, String str) {
        return (str.length() - str.replace(subStr, "").length()) / subStr.length();
    }
}