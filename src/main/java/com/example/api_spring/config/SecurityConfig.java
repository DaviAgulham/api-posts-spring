package com.example.api_spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.api_spring.user.UserRepository;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  @Bean
  BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(UserRepository repo) {
    return username -> repo.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  @Bean
  AuthenticationManager authenticationManager(UserDetailsService uds, BCryptPasswordEncoder enc) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(uds);
    provider.setPasswordEncoder(enc);
    return new ProviderManager(provider);
  }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    JwtService jwt,
                                    UserRepository repo,
                                    AuthenticationManager authManager) throws Exception {
    http.csrf(cs -> cs.disable());
    http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.authorizeHttpRequests(reg -> reg
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/uploads/**").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/posts/**").authenticated()
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .anyRequest().authenticated()
    );
    http.authenticationManager(authManager);
    http.addFilterBefore(new JwtAuthFilter(jwt, repo), UsernamePasswordAuthenticationFilter.class);
    return http.build();
    }

}
