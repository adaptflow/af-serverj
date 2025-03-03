package com.adaptflow.af_serverj.jwt;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.adaptflow.af_serverj.common.exception.ErrorCode;
import com.adaptflow.af_serverj.common.exception.ErrorResponse;
import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.adaptflow.af_serverj.configuration.db.adaptflow.service.login.LoginService;
import com.adaptflow.af_serverj.model.dto.CustomUserDetails;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class Filter extends OncePerRequestFilter {

    @Value("${jwt.excluded-paths}")
    private String excludedPaths;

    private final List<AntPathRequestMatcher> excludedMatchers = new ArrayList<>();

    @Autowired
    private JwtValidator jwtValidator;

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        String[] excludedPathsArray = excludedPaths.split(",");

        // Create AntPathRequestMatcher objects for each path pattern
        for (String path : excludedPathsArray) {
            this.excludedMatchers.add(new AntPathRequestMatcher(path.trim()));
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // unauthenticated endpoints that should not be filtered
        return excludedMatchers.stream().anyMatch(matcher -> matcher.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {

            CustomUserDetails userDetails = new CustomUserDetails();
            Cookie[] cookies = request.getCookies();
            String jwtToken = extractValueFromCookies(cookies, JwtService.ACCESS_TOKEN);
            DecodedJWT decodedJWT;

            if (jwtToken != null) {

                log.info("[+] JWT Token from cookie: " + jwtToken);
                decodedJWT = JWT.decode(jwtToken);
                // validate the token from cookie
                if (!jwtValidator.validateToken(jwtToken)) {
                    // token is expired , refresh the tokens
                    log.info("[-] JWT Token is expired.");
                    String username = decodedJWT.getClaim("username").asString();

                    Map<String, String> refreshTokensMap = jwtService.getAllRefreshTokens();
                    String userRefreshToken = refreshTokensMap.get(username);
                    if (userRefreshToken != null && !userRefreshToken.isBlank()) {
                        Map<String, String> tokens = loginService.refreshTokens(userRefreshToken);
                        response.addCookie(createCookie(JwtService.ACCESS_TOKEN,
                                tokens.get(JwtService.ACCESS_TOKEN), loginService.jwtAccessTokenExpireDuration));
                        jwtToken = tokens.get(JwtService.ACCESS_TOKEN);
                    } else {
                        // delete the cookies in this case
                        // forcing the user to login again
                        log.info("[-] Refresh token is missing for user: " + username);
                        response.addCookie(createCookie(JwtService.ACCESS_TOKEN, "", 0));
                        throw new ServiceException(ErrorCode.UNAUTHORIZED_ACCESS, "Jwt Expired.");
                    }
                }

                Map<String, Claim> claims = decodedJWT.getClaims();
                userDetails.setToken(jwtToken);
                userDetails.setUsername(claims.get("username").asString());
            } else {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Authorization is missing in the request.");
            }
            UserContextHolder.set(userDetails);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // noinspection CallToPrintStackTrace
            e.printStackTrace();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // Enable Java 8 date/time support
            if (e.getClass().equals(ServiceException.class)) {
                ServiceException se = (ServiceException) e;
                response.setContentType("application/json");
                response.setStatus(se.getHttpStatusCode());
                response.getWriter().write(objectMapper.writeValueAsString(
                        new ErrorResponse(se.getErrorCode(), e.getMessage())));
            } else {
                response.setStatus(ErrorCode.SERVER_ERROR.getHttpStatusCode());
                response.getWriter().write(objectMapper
                        .writeValueAsString(new ErrorResponse(ErrorCode.SERVER_ERROR.name(), e.getMessage())));
            }
        } finally {
            UserContextHolder.clear();
        }
    }

    /**
     * Extracts the values of cookies with the given name.
     * 
     * @param cookies    An array of cookies.
     * @param cookieName The name of the cookie to extract.
     * @return The value of the cookie with the given name.
     */
    private String extractValueFromCookies(Cookie[] cookies, String cookieName) {
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Creates a cookie with the given name, value, and max age.
     * 
     * @param name   The name of the cookie.
     * @param value  The value of the cookie.
     * @param maxAge The maximum age of the cookie.
     * @return A cookie object.
     */
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge((int) Duration.ofMinutes(maxAge).getSeconds());
        cookie.setPath("/");
        return cookie;
    }

}
