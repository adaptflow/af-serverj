package com.adaptflow.af_serverj.configuration.db.adaptflow.service.login;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.adaptflow.af_serverj.common.exception.ErrorCode;
import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.adaptflow.af_serverj.configuration.db.adaptflow.entity.User;
import com.adaptflow.af_serverj.configuration.db.adaptflow.repository.login.UserRepository;
import com.adaptflow.af_serverj.jwt.JwtService;
import com.adaptflow.af_serverj.jwt.UserContextHolder;
import com.adaptflow.af_serverj.model.dto.UserRegistrationDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoginService extends JwtService {

    private final UserRepository userRepository;

    public LoginService(
            @Value("${jwt.access_token.expire}") int jwtAccessTokenExpireDuration,
            @Value("${jwt.refresh_token.expire}") int jwtRefreshTokenExpireDuration,
            @Value("${jwt.public.key}") String publicKeyPEM,
            @Value("${jwt.private.key}") String privateKeyPEM,
            RedissonClient redissonClient,
            UserRepository userRepository) {

        super(jwtAccessTokenExpireDuration, jwtRefreshTokenExpireDuration, publicKeyPEM, privateKeyPEM, redissonClient);
        this.userRepository = userRepository;
    }

    @Transactional
    public ResponseEntity<Map<?, ?>> handleUserLogin(Map<String, String> request) throws ServiceException {
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

        // generate the tokens
        Map<String, String> tokens = createTokens(user);
        // persist the refresh token in redis
        addRefreshTokenInRedis(user.getUsername(), tokens.get(JwtService.REFRESH_TOKEN));

        // Create cookies for Access Token and Refresh Token
        ResponseCookie accessTokenCookie = getCookieValue(tokens, false);

        // Return response with cookies
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(Map.of());

    }

    @Transactional
    public Map<String, String> refreshTokens(String refreshToken) throws Exception {
        if (refreshToken == null) {
            throw new ServiceException(ErrorCode.INVALID_INPUT, "Refresh token is required.");
        }
        if (!validateToken(refreshToken)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED_ACCESS, "Refresh token expired.");
        }
        // Decode JWT only after successful validation
        DecodedJWT decodedJWT = JWT.decode(refreshToken);
        String username = decodedJWT.getClaim("username").asString();

        // Convert userId to UUID and fetch user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException(ErrorCode.INVALID_INPUT, "User could not be found."));

        // Generate and return new tokens
        Map<String, String> tokens = createTokens(user);
        // update the new refresh token in redis for the user and blacklist older one
        String newRefreshToken = tokens.get(JwtService.REFRESH_TOKEN);
        addRefreshTokenInRedis(user.getUsername(), newRefreshToken);
        // blacklist older refresh token
        blacklistToken(username, refreshToken, true);

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

    public ResponseEntity<Map<?, ?>> processLogout() {

        String accessToken = UserContextHolder.get().getToken();
        String username = UserContextHolder.get().getUsername();
        log.info("Logging out user: {}", username);
        // blacklist the access token & refresh token
        if (accessToken != null)
            blacklistToken(username, accessToken, false);

        // gets all the refresh tokens of users stored in redis
        Map<String, String> refreshTokensMap = getAllRefreshTokens();
        if (refreshTokensMap.containsKey(username))
            blacklistToken(username, refreshTokensMap.get(username), true);

        ResponseCookie accessTokenCookie = getCookieValue(Map.of(), true);

        // Return response with cookies
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(Map.of("msg", "User logged out successfully."));

    }

    /**
     * Creates a cookie for the given token.
     *
     * @param tokens    A map containing access and refresh tokens.
     * @param isRefresh A boolean flag indicating if the token is a refresh token.
     * @param isLogout  A boolean flag indicating cookie to be set for logout case
     * @return ResponseCookie A cookie object.
     */
    private ResponseCookie getCookieValue(Map<String, String> tokens, boolean isLogout) {
        if (isLogout) {
            return ResponseCookie
                    .from(ACCESS_TOKEN, "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(0))
                    .sameSite("Lax")
                    .build();
        }
        return ResponseCookie
                .from(ACCESS_TOKEN, tokens.get(ACCESS_TOKEN))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(jwtAccessTokenExpireDuration))
                .sameSite("Lax")
                .build();
    }

}
