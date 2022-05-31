package com.poc.httpConnectionPooling.controller;

import com.poc.httpConnectionPooling.service.DemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("/")
public class DemoController {

    DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("execute/")
    public ResponseEntity execute() {
        try {
            demoService.execute();
            return ResponseEntity.ok().build();
        } catch (Exception e)   {
            System.out.println("error: " + e.getMessage());
        }
        return ResponseEntity.internalServerError().build();
    }

}
