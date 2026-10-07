package com.example.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${APP_VERSION:dev}")
    private String version;

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
