package com.konecta.service_example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/example")
public class controller {
    @GetMapping()
    public String getServiceMessage() {
        return "This is example service";
    }
}
