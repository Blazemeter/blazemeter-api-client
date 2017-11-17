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

package com.blazemeter.api.explorer.base;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.utils.BlazeMeterUtils;


/**
 * Base entity for BlazeMeter explorer classes
 */
public class BZAObject {

    protected String id;
    protected String name;
    protected BlazeMeterUtils utils;
    protected Logger logger;

    public BZAObject(BlazeMeterUtils utils, String id, String name) {
        this.utils = utils;
        this.id = id;
        this.name = name;
        this.logger = utils.getLogger();
    }

    public BlazeMeterUtils getUtils() {
        return utils;
    }

    public void setUtils(BlazeMeterUtils utils) {
        this.utils = utils;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}