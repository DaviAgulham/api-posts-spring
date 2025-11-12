package com.example.api_spring.auth;

public record AuthResponse(String access_token, String token_type, long expires_in) {}
