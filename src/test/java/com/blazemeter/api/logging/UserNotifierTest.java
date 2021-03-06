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

public class UserNotifierTest implements UserNotifier {


    private StringBuilder logs = new StringBuilder();

    public void reset() {
        logs = new StringBuilder();
    }

    public StringBuilder getLogs() {
        return logs;
    }

    @Override
    public void notifyInfo(String info) {
        logs.append(info).append("\r\n");
    }

    @Override
    public void notifyWarning(String warn) {
        logs.append(warn).append("\r\n");
    }

    @Override
    public void notifyError(String error) {
        logs.append(error).append("\r\n");
    }
}