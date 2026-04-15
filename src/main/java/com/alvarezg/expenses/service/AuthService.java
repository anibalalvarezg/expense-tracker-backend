package com.alvarezg.expenses.service;

import com.alvarezg.expenses.dto.AuthResponse;
import com.alvarezg.expenses.dto.LoginRequest;
import com.alvarezg.expenses.dto.RefreshRequest;
import com.alvarezg.expenses.dto.RegisterRequest;
import com.alvarezg.expenses.exception.EmailAlreadyExistsException;
import com.alvarezg.expenses.exception.ResourceNotFoundException;
import com.alvarezg.expenses.exception.UnauthorizedException;
import com.alvarezg.expenses.model.RefreshToken;
import com.alvarezg.expenses.model.User;
import com.alvarezg.expenses.repository.RefreshTokenRepository;
import com.alvarezg.expenses.repository.UserRepository;
import com.alvarezg.expenses.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Si este email no está registrado recibirás instrucciones"
            );
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Revocar tokens anteriores del usuario
        refreshTokenRepository.revokeAllByUser(user);

        return generateAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        // Buscar el refresh token en la BD
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        // Verificar que no esté revocado
        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token revocado");
        }

        // Verificar que no haya expirado
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expirado, inicia sesión de nuevo");
        }

        // Revocar el token usado y generar uno nuevo (rotación)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return generateAuthResponse(refreshToken.getUser());
    }

    public void logout(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        refreshTokenRepository.revokeAllByUser(refreshToken.getUser());
    }

    // Método privado que genera access + refresh token
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getEmail());

        // Crear y guardar el refresh token en la BD
        RefreshToken refreshToken = RefreshToken.builder()
                .token(jwtUtil.generateRefreshToken())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                user.getName(),
                user.getEmail()
        );
    }
}