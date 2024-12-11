package com.garganttua.api.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.caller.IGGAPICaller;

public class GGAPIMethodConciliatorTest {

	@Test
	public void test() throws GGAPIEngineException, NoSuchMethodException, SecurityException {
        Method exampleMethod = ExampleService.class.getMethod("exampleMethod", IGGAPICaller.class, String.class, int.class);

        GGAPIMethodConciliator conciliator = new GGAPIMethodConciliator(exampleMethod)
                .setCaller(GGAPICaller.createSuperCaller())
                .setReferencePath("/api/devices/{uuid}/alarms/{alarmId}")
                .setValuedPath("/api/devices/123456789/alarms/154851245")
                .setCustomParameters(Map.of("extra", "customValue"))
                .setBody("example body");

        Object[] parameters = conciliator.getParameters();
       
        assertNotNull(parameters[0]);
        assertEquals("123456789", parameters[1]);
        assertEquals(154851245, parameters[2]);
	}
	
}
