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
