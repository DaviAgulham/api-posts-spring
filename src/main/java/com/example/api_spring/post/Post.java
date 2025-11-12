package com.example.api_spring.post;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.api_spring.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false, fetch = FetchType.LAZY)
  @JoinColumn(name="user_id")
  @JsonIgnoreProperties({"password","posts","hibernateLazyInitializer","handler"})
  private User user;

  @Column(nullable=false, length=255)
  private String title;

  @Lob @Column(nullable=false)
  private String description;

  @Column(nullable = true, length=255)
  private String imagePath;

  @JsonProperty("image_url")
  public String getImageUrl() {
    if (imagePath == null || imagePath.isBlank()) return null;
    String p = imagePath.replace("\\", "/");
    if (!p.startsWith("/uploads/") && !p.startsWith("http")) p = "/uploads/" + p;
    if (p.startsWith("http")) return p;
    return ServletUriComponentsBuilder.fromCurrentContextPath().path(p).toUriString();
  }

  @CreationTimestamp private Instant createdAt;
  @UpdateTimestamp  private Instant updatedAt;
}
