package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200") 
@RestController
@RequestMapping("/api")
public class AuthController {

    private final RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${keycloak.redirect-uri}")
    private String redirectUri;
    

    @Autowired
    public AuthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint!");
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
        logger.info("Received request at /api/login"); 
        Map<String, String> response = new HashMap<>();
        String url = keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/auth" +
                    "?client_id=" + clientId +
                    "&response_type=code" +
                    "&scope=openid" +
                    "&redirect_uri=" + redirectUri;
        response.put("login_url", url);

        logger.info("authurl=" + url);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    public ResponseEntity<Map> callback(@RequestBody Map<String, String> body) {
        String code = body.get("code");

        if (code == null || code.isEmpty()) {

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error");
            response.put("code", 401);
            return ResponseEntity.badRequest().body(response);
        }
    

        try {
            String tokenUrl = keycloakServerUrl + "/realms/"+ keycloakRealm + "/protocol/openid-connect/token";
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);
    
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
    
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            logger.info("logout_url=" + response);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to exchange code for token:" + e.getMessage());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> body) {
        String id_token = body.get("id_token");
        logger.info("Received request at /api/logout"); 
        Map<String, String> response = new HashMap<>();
        String url = keycloakServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/logout" +
                    "?id_token_hint=" + id_token +
                    "&client_id=" + clientId +
                    "&post_logout_redirect_uri=http://localhost:4200";
        response.put("logout_url", url);

        return ResponseEntity.ok(response);
    }

}
