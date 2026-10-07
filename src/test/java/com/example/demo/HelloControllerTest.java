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
package com.example.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final String version;

    public HelloController(@Value("${APP_VERSION:dev}") String version) {
        this.version = version;
    }

    @GetMapping("/")
    public Map<String, String> hello() {
        return Map.of(
                "message", "Hello from HA Kubernetes!",
                "version", version,
                "pod", podName());
    }

    private String podName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
