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
import com.adaptflow.af_serverj.configuration.db.adaptflow.service.login.LoginService;
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
    public ResponseEntity<Map<?, ?>> login(@RequestBody Map<String, String> request) throws ServiceException {
        try {
            return loginService.handleUserLogin(request);
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
     * Handles user logout.
     * Revokes the user's authentication tokens and ends the session.
     *
     * @return A response indicating successful logout.
     */
    @PostMapping(path = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<?, ?>> logoutUser() {
        return loginService.processLogout();
    }

}
