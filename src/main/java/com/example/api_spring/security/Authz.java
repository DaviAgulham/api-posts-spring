package com.example.api_spring.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.api_spring.post.PostRepository;
import com.example.api_spring.user.User;

@Component
public class Authz {
  private final PostRepository posts;
  public Authz(PostRepository posts){ this.posts = posts; }

  public boolean isPostOwnerOrAdmin(Long id){
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) return false;
    var u = (User) auth.getPrincipal();
    if (u.isAdmin()) return true;
    return posts.findById(id).map(p -> p.getUser().getId().equals(u.getId())).orElse(false);
  }
}
