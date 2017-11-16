package com.blazemeter.api.logging;

public class UserNotifierTest implements UserNotifier {


    private StringBuilder logs = new StringBuilder();

    public void reset() {
        logs = new StringBuilder();
    }

    public StringBuilder getLogs() {
        return logs;
    }

    @Override
    public void notifyAbout(String info) {
        logs.append(info).append("\r\n");
    }
}