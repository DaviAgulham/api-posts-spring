package com.example.api_spring.user;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {
  private final UserRepository repo;
  private final BCryptPasswordEncoder enc;

  public UserService(UserRepository repo, BCryptPasswordEncoder enc) {
    this.repo = repo; this.enc = enc;
  }

  public User getOrThrow(Long id) {
    return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  public User update(User auth, Long id, String name, String email, String rawPassword, Role role) {
    User target = getOrThrow(id);

    boolean admin = auth.isAdmin();
    boolean self = auth.getId().equals(target.getId());
    if (!admin && !self) throw new AccessDeniedException("Forbidden");

    if (email != null && repo.existsByEmailAndIdNot(email, id))
      throw new IllegalArgumentException("Email already taken");

    if (name != null) target.setName(name);
    if (email != null) target.setEmail(email.toLowerCase());
    if (rawPassword != null && !rawPassword.isBlank()) target.setPassword(enc.encode(rawPassword));
    if (role != null && admin) target.setRole(role);

    return repo.save(target);
  }

  public void delete(User auth, Long id) {
    User target = getOrThrow(id);
    boolean admin = auth.isAdmin();
    boolean self = auth.getId().equals(target.getId());
    if (!admin && !self) throw new AccessDeniedException("Forbidden");
    repo.delete(target);
  }
}
