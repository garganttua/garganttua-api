package com.garganttua.api.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.spec.caller.ICaller;

public class MethodConciliatorTest {

	@Test
	public void test() throws EngineException, NoSuchMethodException, SecurityException {
        Method exampleMethod = ExampleService.class.getMethod("exampleMethod", ICaller.class, String.class, int.class);

        MethodConciliator conciliator = new MethodConciliator(exampleMethod)
                .setCaller(Caller.createSuperCaller())
                .setReferencePath("/api/devices/{uuid}/alarms/{alarmId}")
                .setValuedPath("/api/devices/123456789/alarms/154851245")
                .setCustomParameters(Map.of("extra", "customValue"))
                .setBody("example body".getBytes());

        Object[] parameters = conciliator.getParameters();
       
        assertNotNull(parameters[0]);
        assertEquals("123456789", parameters[1]);
        assertEquals(154851245, parameters[2]);
	}
	
}
