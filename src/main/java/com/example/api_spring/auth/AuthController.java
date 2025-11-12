package com.example.api_spring.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api_spring.config.JwtService;
import com.example.api_spring.user.Role;
import com.example.api_spring.user.User;
import com.example.api_spring.user.UserRepository;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users;
  private final AuthenticationManager authManager;
  private final JwtService jwt;

  private final PasswordEncoder enc;
  public AuthController(UserRepository users, PasswordEncoder enc, AuthenticationManager am, JwtService jwt) {
    this.users = users; this.enc = enc; this.authManager = am; this.jwt = jwt;
  }

  @PostMapping("/register")
  @PermitAll
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
    if (users.existsByEmail(req.email())) return ResponseEntity.badRequest().body("Email in use");
    User u = User.builder()
        .name(req.name())
        .email(req.email().toLowerCase())
        .password(enc.encode(req.password()))
        .role(Role.USER)
        .build();
    users.save(u);
    var token = jwt.generate(u);
    return ResponseEntity.status(201).body(new AuthResponse(token, "bearer", 3600));
  }

  @PostMapping("/login")
  @PermitAll
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
    var auth = new UsernamePasswordAuthenticationToken(req.email(), req.password());
    authManager.authenticate(auth);
    var u = users.findByEmail(req.email()).orElseThrow();
    var token = jwt.generate(u);
    return ResponseEntity.ok(new AuthResponse(token, "bearer", 3600));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(@AuthenticationPrincipal User u) {
    if (u == null) return ResponseEntity.status(401).build();
    return ResponseEntity.ok(new Object(){
        public final Long id = u.getId();
        public final String name = u.getName();
        public final String email = u.getEmail();
        public final String role = u.getRole().name();
    });
  }
}
