package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal Jwt principal) {

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", principal.getClaimAsString("preferred_username"));
        userInfo.put("given_name", principal.getClaimAsString("given_name"));
        userInfo.put("family_name", principal.getClaimAsString("family_name"));
        userInfo.put("name", principal.getClaimAsString("name"));
        userInfo.put("nric", principal.getClaimAsString("nric"));

        return ResponseEntity.ok(userInfo);
    }

}

