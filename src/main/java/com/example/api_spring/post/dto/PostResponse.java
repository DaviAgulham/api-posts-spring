package com.example.api_spring.post.dto;

import java.time.Instant;

public record PostResponse(
  Long id, String title, String description, String imagePath,
  Long userId, String userName,
  Instant createdAt, Instant updatedAt
) {}
