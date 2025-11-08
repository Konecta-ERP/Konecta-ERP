package com.konecta.service_example;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/example")
public class controller {
    @GetMapping
    @PreAuthorize("hasAuthority('ASSOCIATE')")
    public String getServiceMessage() {
        return "This is example service";
    }
}
