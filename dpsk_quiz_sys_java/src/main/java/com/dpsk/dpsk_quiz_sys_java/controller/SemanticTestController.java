package com.dpsk.dpsk_quiz_sys_java.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("/semantic-test")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5175", "http://localhost:3000"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowCredentials = "true")
public class SemanticTestController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Semantic test controller is working!";
    }
    
    @GetMapping("/search")
    public String testSearch(@RequestParam("query") String query) {
        return "Test search for: " + query;
    }
}