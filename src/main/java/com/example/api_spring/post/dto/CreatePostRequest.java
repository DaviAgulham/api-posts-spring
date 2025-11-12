package com.example.api_spring.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
  @NotBlank @Size(max = 255) String title,
  @NotBlank String description,
  String imagePath
) {}
