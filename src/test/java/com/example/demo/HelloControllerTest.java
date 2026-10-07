package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

class HelloControllerTest {

    @Test
    void rootReturnsGreeting() {
        Map<String, String> body = new HelloController("test").hello();

        assertEquals("Hello from HA Kubernetes!", body.get("message"));
        assertEquals("test", body.get("version"));
        assertNotNull(body.get("pod"));
    }
}
