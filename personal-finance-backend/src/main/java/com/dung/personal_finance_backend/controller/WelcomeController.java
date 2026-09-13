package com.dung.personal_finance_backend.controller;

import com.dung.personal_finance_backend.response.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class WelcomeController {

    @GetMapping("/welcome")
    public ResponseEntity<Object> welcome() {
        return ResponseHandler.responseBuilder(
                "Welcome to Personal Finance API",
                HttpStatus.OK,
                "Personal Finance Backend is running"
        );
    }
}