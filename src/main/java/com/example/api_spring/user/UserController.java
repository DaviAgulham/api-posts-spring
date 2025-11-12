package com.example.api_spring.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserRepository repo;
  private final UserService service;

  public UserController(UserRepository repo, UserService service) {
    this.repo = repo; this.service = service;
  }

  public record UserDTO(Long id, String name, String email, String role) {}
  public record UpdateUserRequest(String name, @Email String email, String password, String passwordConfirmation, Role role) {}

  @GetMapping
  public ResponseEntity<?> index() {
    List<UserDTO> out = repo.findAll().stream()
        .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole().name()))
        .toList();
    return ResponseEntity.ok(out);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> show(@PathVariable Long id) {
    var u = repo.findById(id).orElse(null);
    if (u == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole().name()));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
  public ResponseEntity<?> update(
      @AuthenticationPrincipal User auth,
      @PathVariable Long id,
      @Valid @RequestBody UpdateUserRequest req
  ) {
    if (req.password() != null && !req.password().equals(req.passwordConfirmation()))
      return ResponseEntity.badRequest().body("password confirmation mismatch");

    var updated = service.update(auth, id, req.name(), req.email(), req.password(), req.role());
    return ResponseEntity.ok(new UserDTO(updated.getId(), updated.getName(), updated.getEmail(), updated.getRole().name()));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> destroy(@AuthenticationPrincipal User auth, @PathVariable Long id) {
    service.delete(auth, id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> destroyViaPost(
      @AuthenticationPrincipal User auth,
      @PathVariable Long id,
      @RequestParam(name = "_method", required = false) String method
  ) {
    if (!"DELETE".equalsIgnoreCase(method))
      throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
    service.delete(auth, id);
    return ResponseEntity.noContent().build();
  }
}
