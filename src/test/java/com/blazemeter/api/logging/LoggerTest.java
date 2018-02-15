/**
 * Copyright 2018 BlazeMeter Inc.
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

package com.blazemeter.api.logging;


public class LoggerTest implements Logger {

    private StringBuilder logs = new StringBuilder();

    public void reset() {
        logs = new StringBuilder();
    }

    public StringBuilder getLogs() {
        return logs;
    }

    @Override
    public void debug(String message) {
        logs.append(message).append("\r\n");
    }

    @Override
    public void debug(String message, Throwable throwable) {
        logs.append(message).append("\r\n");
        logs.append(throwable.getMessage()).append("\r\n");
    }

    @Override
    public void info(String message) {
        logs.append(message).append("\r\n");
    }

    @Override
    public void info(String message, Throwable throwable) {
        logs.append(message).append("\r\n");
        logs.append(throwable.getMessage()).append("\r\n");
    }

    @Override
    public void warn(String message) {
        logs.append(message).append("\r\n");
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logs.append(message).append("\r\n");
        logs.append(throwable.getMessage()).append("\r\n");
    }

    @Override
    public void error(String message) {
        logs.append(message).append("\r\n");
    }

    @Override
    public void error(String message, Throwable throwable) {
        logs.append(message).append("\r\n");
        logs.append(throwable.getMessage()).append("\r\n");
    }
}