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