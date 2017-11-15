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

    }

    @Override
    public void debug(String message, Throwable throwable) {

    }

    @Override
    public void info(String message) {

    }

    @Override
    public void info(String message, Throwable throwable) {

    }

    @Override
    public void warn(String message) {

    }

    @Override
    public void warn(String message, Throwable throwable) {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void error(String message, Throwable throwable) {

    }
}