/**
 * Copyright 2017 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazemeter.api.explorer.test;

import com.blazemeter.api.exception.UnexpectedResponseException;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.utils.BlazeMeterUtils;

import java.io.IOException;

public class TestDetector {

    /**
     * @param utils  - BlazeMeterUtils that contains logging and http setup
     * @param testId - test Id for detected
     *               Detect test type by test id. If test not found that return null
     */
    public static AbstractTest detectTest(BlazeMeterUtils utils, String testId) throws IOException {
        final Logger logger = utils.getLogger();
        try {
            AbstractTest test = detectTestByPrefix(utils, testId);
            if (test != null) {
                return test;
            }
        } catch (Exception e) {
            logger.info("Failed to detect test with id = " + testId);
        }

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
                throw ex;
            }
        }
    }

    public static AbstractTest detectMultiTest(BlazeMeterUtils utils, String testId) throws IOException {
        final Logger logger = utils.getLogger();
        try {
            logger.info("Attempt to detect Multi test type with id=" + testId);
            return MultiTest.getMultiTest(utils, testId);
        } catch (UnexpectedResponseException ex) {
            String msg = ex.getMessage();
            if (msg.toLowerCase().contains("collection not found")) {
                logger.info("Multi test with id=" + testId + " not found");
                return null;
            } else {
                logger.error("Fail for detect Multi test type id=" + testId + ". Reason is: " + ex.getMessage(), ex);
                throw ex;
            }
        }
    }


    public static AbstractTest detectTestByPrefix(BlazeMeterUtils utils, String test) throws IOException {
        String suffix = getTestTypeSuffix(test);
        String testId = getTestId(test);
        AbstractTest detectedTest = null;
        switch (suffix) {
            case "http":
                detectedTest = SingleTest.getSingleTest(utils, testId);
                break;
            case "followme":
                detectedTest = SingleTest.getSingleTest(utils, testId);
                break;
            case "jmeter":
                detectedTest = SingleTest.getSingleTest(utils, testId);
                break;
            case "multi":
                detectedTest = MultiTest.getMultiTest(utils, testId);
                break;
            case "multi-location":
                detectedTest = MultiTest.getMultiTest(utils, testId);
                break;
            case "taurus":
                detectedTest = SingleTest.getSingleTest(utils, testId);
                break;
            case "webdriver":
                detectedTest = SingleTest.getSingleTest(utils, testId);
                break;
        }
        return detectedTest;
    }

    public static String getTestTypeSuffix(String testId) {
        try {
            int dot = testId.lastIndexOf(".");
            if (dot < 0) {
                return "";
            }
            return testId.substring(testId.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getTestId(String testId) {
        try {
            int dot = testId.lastIndexOf(".");
            if (dot < 0) {
                return testId;
            }
            return testId.substring(0, testId.lastIndexOf("."));
        } catch (Exception e) {
            return testId;
        }
    }
}
