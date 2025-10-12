package org.example.gactifs.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.gactifs.auth.Model.User;
import org.example.gactifs.auth.enums.Role;
import org.example.gactifs.auth.repository.UserRepository;
import org.example.gactifs.auth.token.Token;
import org.example.gactifs.auth.token.TokenRepository;
import org.example.gactifs.auth.token.TokenType;
import org.example.gactifs.config.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationResponse signup(RegisterRequest request) {
    if (repository.existsByEmail(request.getEmail())) {
      System.out.println("L'email existe déjà");
    }

    var user = User.builder()
            .firstName(request.getFirstname())
            .lastName(request.getLastname())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .build();
    var savedUser = repository.save(user);

    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);

    return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken)
            .build();
  }

  // ✅ SIMPLE : Ajouter juste l'ID et le rôle dans la réponse
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    try {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getEmail(),
                      request.getPassword()
              )
      );
      var user = repository.findByEmail(request.getEmail())
              .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.getEmail()));

      var jwtToken = jwtService.generateToken(user);
      var refreshToken = jwtService.generateRefreshToken(user);
      revokeAllUserTokens(user);
      saveUserToken(user, jwtToken);

      System.out.println("✅ Auth OK: " + user.getEmail() + " | ID: " + user.getId() + " | Role: " + user.getRole());

      return AuthenticationResponse.builder()
              .accessToken(jwtToken)
              .refreshToken(refreshToken)
              .userId(user.getId())           // ✅ Ajouter l'ID
              .role(user.getRole().toString()) // ✅ Ajouter le rôle
              .build();
    } catch (Exception e) {
      System.err.println("Authentication failed: " + e.getMessage());
      throw e;
    }
  }

  private void saveUserToken(User user, String jwtToken) {
    Token token = Token.builder()
            .user(user)
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .build();
    tokenRepository.save(token);
  }

  public User getCurrentUserInfo() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    String email;
    if (principal instanceof UserDetails) {
      email = ((UserDetails) principal).getUsername();
    } else {
      email = principal.toString();
    }

    return repository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email));
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.repository.findByEmail(userEmail)
              .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        // ✅ SIMPLE : Ajouter juste l'ID et le rôle
        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .role(user.getRole().toString())
                .build();

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }
}