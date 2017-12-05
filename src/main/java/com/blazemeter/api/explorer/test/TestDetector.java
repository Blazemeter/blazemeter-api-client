package com.blazemeter.api.explorer.test;

import com.blazemeter.api.exception.UnexpectedResponseException;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.utils.BlazeMeterUtils;

import java.io.IOException;

public class TestDetector {

    /**
     * @param utils - BlazeMeterUtils that contains logging and http setup
     * @param testId - test Id for detected
     * Detect test type by test id. If test not found that return null
     */
    public static AbstractTest detectTest(BlazeMeterUtils utils, String testId) throws IOException {
        final Logger logger = utils.getLogger();
        try {
            logger.info("Attempt to detect Single test type with id=" + testId);
            return SingleTest.getSingleTest(utils, testId);
        } catch (UnexpectedResponseException ex) {
            String msg = ex.getMessage();
            if (msg.toLowerCase().contains("test not found")) {
                logger.info("Single test with id=" + testId + " not found");
                return detectMultiTest(utils, testId);
            } else {
                logger.error("Fail for detect Single test type id=" + testId + ". Reason is: " + ex.getMessage(), ex);
                return null;
            }
        }
    }

    private static AbstractTest detectMultiTest(BlazeMeterUtils utils, String testId) throws IOException {
        final Logger logger = utils.getLogger();
        try {
            logger.info("Attempt to detect Multi test type with id=" + testId);
            return MultiTest.getMultiTest(utils, testId);
        } catch (UnexpectedResponseException ex) {
            logger.error("Fail for detect Multi test type id=" + testId + ". Reason is: " + ex.getMessage(), ex);
            return null;
        }
    }

}
