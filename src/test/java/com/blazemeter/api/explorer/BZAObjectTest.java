package com.blazemeter.api.explorer;

import com.blazemeter.api.explorer.base.BZAObject;
import com.blazemeter.api.logging.LoggerTest;
import com.blazemeter.api.logging.UserNotifierTest;
import com.blazemeter.api.utils.BlazeMeterUtils;

import static org.junit.Assert.*;

public class BZAObjectTest {

    @org.junit.Test
    public void test() throws Exception {
        BlazeMeterUtils utils = new BlazeMeterUtils("", "", new UserNotifierTest(), new LoggerTest());
        BZAObject entity = new BZAObject(utils, "id", "name");
        assertEquals(utils, entity.getUtils());
        assertEquals("id", entity.getId());
        assertEquals("name", entity.getName());
        entity.setUtils(null);
        entity.setId("id1");
        entity.setName("name1");
        assertNull(entity.getUtils());
        assertEquals("id1", entity.getId());
        assertEquals("name1", entity.getName());
    }

}