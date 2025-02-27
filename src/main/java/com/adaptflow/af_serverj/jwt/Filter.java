package com.adaptflow.af_serverj.jwt;

import java.io.IOException;
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
import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.adaptflow.af_serverj.model.dto.CustomUserDetails;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
            String jwtToken = request.getHeader("authorization");

            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {

                log.info("JWT Token in request: " + jwtToken);

                jwtToken = jwtToken.substring(7);
                if (!jwtValidator.validateToken(jwtToken)) {
                    throw new ServiceException(ErrorCode.UNAUTHORIZED_ACCESS, "Invalid token");
                }

                DecodedJWT decodedJWT = JWT.decode(jwtToken);
                Map<String, Claim> claims = decodedJWT.getClaims();

                userDetails.setToken(jwtToken);
                this.populateUserDetails(userDetails, claims);
            } else {
                throw new ServiceException(ErrorCode.BAD_REQUEST, "Authorization header is missing.");
            }
            UserContextHolder.set(userDetails);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // noinspection CallToPrintStackTrace
            e.printStackTrace();
            if (e.getClass().equals(ServiceException.class)) {
                ServiceException se = (ServiceException) e;
                response.setStatus(se.getHttpStatusCode());
                response.getWriter().write(se.getMessage());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(e.getMessage());
            }
        } finally {
            UserContextHolder.clear();
        }

    }

    private void populateUserDetails(CustomUserDetails userDetails, Map<String, Claim> claims) {
        userDetails.setEmail(claims.get("email").asString());
        userDetails.setUserId(claims.get("id").asString());
        userDetails.setUsername(claims.get("username").asString());
    }

}
