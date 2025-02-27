package com.adaptflow.af_serverj.configuration.db.login.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.adaptflow.af_serverj.common.exception.ErrorCode;
import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.adaptflow.af_serverj.configuration.db.login.entity.User;
import com.adaptflow.af_serverj.configuration.db.login.repository.UserRepository;
import com.adaptflow.af_serverj.jwt.JwtService;
import com.adaptflow.af_serverj.jwt.JwtValidator;
import com.adaptflow.af_serverj.jwt.UserContextHolder;
import com.adaptflow.af_serverj.model.dto.UserRegistrationDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtValidator jwtValidator;

    @Transactional
    public Map<String, Object> handleUserLogin(Map<String, String> request) throws ServiceException {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || password == null) {
            throw new ServiceException(ErrorCode.INVALID_INPUT, "Username and password are required fields.");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT, "User could not be found."));

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new ServiceException(ErrorCode.INVALID_INPUT);
        }

        // update last-login everytime
        userRepository.updateLastLogin(user.getId(), System.currentTimeMillis());

        Map<String, Object> response = new HashMap<>();
        Map<?, ?> userMap = objectMapper.convertValue(user, Map.class);
        userMap.remove("password");
        response.put("user", userMap);

        // generate the tokens
        Map<String, String> tokens = jwtService.createTokens(user);
        // persist the refresh token in redis
        jwtService.addRefreshTokenInRedis(user.getId().toString(), tokens.get(JwtService.REFRESH_TOKEN));

        response.putAll(tokens);

        return response;

    }

    @Transactional
    public Map<String, String> refreshTokens(Map<String, String> request) throws Exception {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            throw new ServiceException(ErrorCode.INVALID_INPUT, "Refresh token is required.");
        }

        // Validate token before decoding to prevent tampering
        if (!jwtValidator.validateToken(refreshToken)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED_ACCESS, "Invalid or expired refresh token.");
        }

        // Decode JWT only after successful validation
        DecodedJWT decodedJWT = JWT.decode(refreshToken);
        String userId = decodedJWT.getClaim("id").asString();

        // Convert userId to UUID and fetch user
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT, "User could not be found."));

        // Generate and return new tokens
        Map<String, String> tokens = jwtService.createTokens(user);
        // update the new refresh token in redis for the user and blacklist older one
        String newRefreshToken = tokens.get(JwtService.REFRESH_TOKEN);
        jwtService.addRefreshTokenInRedis(user.getId().toString(), newRefreshToken);
        // blacklist both old access token & refresh token
        jwtService.blacklistToken(refreshToken);
        jwtService.blacklistToken(UserContextHolder.get().getToken());

        return tokens;
    }

    @Transactional
    public Map<String, String> registerNewUser(@Valid UserRegistrationDTO userDTO) throws ServiceException {
        // Check if the username or email already exists
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Username already taken.");
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Email already registered.");
        }

        // Generate a salt and hash the password
        String salt = BCrypt.gensalt(10);
        String hashedPassword = BCrypt.hashpw(userDTO.getPassword(), salt);

        // Create a new user entity
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(hashedPassword);
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setEmail(userDTO.getEmail());
        user.setCreatedAt(Instant.now().toEpochMilli());

        // Save the user
        userRepository.save(user);

        return Map.of("msg", "User registered successfully.");
    }

    public void processLogout() {

        String token = UserContextHolder.get().getToken();
        String userId = UserContextHolder.get().getUserId();
        log.info("Logging out user: {}", userId);
        if (token != null)
            jwtService.blacklistToken(token);
        Map<String, String> refreshTokensMap = jwtService.getAllRefreshTokens();
        if (refreshTokensMap.containsKey(userId))
            jwtService.blacklistToken(refreshTokensMap.get(userId));

    }

}
