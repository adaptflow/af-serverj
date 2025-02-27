package com.adaptflow.af_serverj.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adaptflow.af_serverj.common.exception.ErrorCode;
import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.adaptflow.af_serverj.configuration.db.login.service.LoginService;
import com.adaptflow.af_serverj.model.dto.UserRegistrationDTO;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Validated
public class AuthController {

    @Autowired
    LoginService loginService;

    /**
     * Handles user login requests.
     * Validates the credentials and returns access & refresh tokens if
     * authentication is successful.
     *
     * @param request A map containing "username" and "password" fields.
     * @return A response containing user details and authentication tokens.
     * @throws ServiceException If authentication fails.
     */
    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) throws ServiceException {
        try {
            return ResponseEntity.ok(loginService.handleUserLogin(request));
        } catch (ServiceException e) {
            throw e; // Let the global exception handler take care of this
        } catch (Exception e) {
            // Handle unexpected errors (log and return a generic error response)
            log.error("Unexpected error during login", e);
            throw new ServiceException(ErrorCode.SERVER_ERROR);
        }
    }

    /**
     * Handles new user registration.
     * Creates a new user in the system and returns a success message.
     *
     * @param userDTO User registration details.
     * @return A response indicating successful registration.
     * @throws ServiceException If user creation fails.
     */
    @PostMapping(path = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> registerUser(@NonNull @Valid @RequestBody UserRegistrationDTO userDTO)
            throws ServiceException {
        try {
            return ResponseEntity.ok().body(loginService.registerNewUser(userDTO));
        } catch (ServiceException e) {
            throw e; // Let the global exception handler take care of this
        } catch (Exception e) {
            // Handle unexpected errors (log and return a generic error response)
            log.error("Unexpected error during login", e);
            throw new ServiceException(ErrorCode.SERVER_ERROR);
        }
    }

    /**
     * Handles token refresh requests.
     * Verifies the refresh token and issues a new access token.
     *
     * @param request A map containing the "refreshToken" field.
     * @return A response containing the new access token & refresh token.
     * @throws ServiceException If the refresh token is invalid or expired.
     */
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> refreshTokens(@RequestBody Map<String, String> request)
            throws ServiceException {
        try {
            return ResponseEntity.ok(loginService.refreshTokens(request));
        } catch (ServiceException e) {
            throw e; // Let the global exception handler take care of this
        } catch (Exception e) {
            // Handle unexpected errors (log and return a generic error response)
            log.error("Unexpected error during login", e);
            throw new ServiceException(ErrorCode.SERVER_ERROR);
        }
    }

    /**
     * Handles user logout.
     * Revokes the user's authentication tokens and ends the session.
     *
     * @return A response indicating successful logout.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<?, ?>> logoutUser() {
        loginService.processLogout();
        return ResponseEntity.ok().body(Map.of("msg", "User logged out successfully."));
    }

}
