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

package com.blazemeter.api.explorer.base;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.utils.BlazeMeterUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Base entity for BlazeMeter explorer classes
 * Contains settings(id,name) which are common for all
 * server objects(single-tests, multi-tests, master, etc)
 */
public class BZAObject {

    public static final String UTF_8 = "UTF-8";
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

    public String encode(String text) {
        return encode(text, UTF_8);
    }

    public String encode(String text, String encoding) {
        return encode(logger, text, encoding);
    }

    public static String encode(Logger logger, String text) {
        return encode(logger, text, UTF_8);
    }

    public static String encode(Logger logger, String text, String encoding) {
        try {
            return URLEncoder.encode(text, encoding);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Cannot encode " + text + " to '" + encoding + "' encoding", e);
            return text;
        }
    }

    protected String addParamToUrl(String url, String paramName, Object paramValue) {
        if (paramValue == null) {
            return url;
        } else {
            return url.contains("?") ?
                    (url + '&' + paramName + '=' + paramValue) :
                    (url + '?' + paramName + '=' + paramValue);
        }
    }
}
