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

package com.blazemeter.ciworkflow;

import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;

import java.io.IOException;

public class CiBuild {

    public final AbstractTest test;

    public final String properties;

    public final String notes;


    public CiBuild(AbstractTest test, String properties, String notes) {
        this.test = test;
        this.properties = properties;
        this.notes = notes;
    }

    public void execute() {

        try {
            this.test.start();
        } catch (IOException e) {
        }
    }

    public void cancel() {

    }
}
