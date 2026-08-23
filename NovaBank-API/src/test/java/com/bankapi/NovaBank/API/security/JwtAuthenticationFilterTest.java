package com.bankapi.NovaBank.API.security;

import com.bankapi.NovaBank.API.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        userDetails = User.builder()
                .username("john@gmail.com")
                .password("password")
                .authorities(List.of())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterWhenAuthorizationHeaderIsMissing()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void shouldContinueFilterWhenAuthorizationHeaderDoesNotStartWithBearer()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid()
            throws ServletException, IOException {

        String token = "valid-jwt";
        String email = "john@gmail.com";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtUtil.extractEmail(token))
                .thenReturn(email);

        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);

        // IMPORTANT:
        // Validate against UserDetails, not the email string.
        when(jwtUtil.isTokenValid(token, email))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        assertEquals(
                email,
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );

        assertEquals(
                userDetails,
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
        );

        verify(userDetailsService)
                .loadUserByUsername(email);

        verify(jwtUtil)
                .isTokenValid(token,email);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid()
            throws ServletException, IOException {

        String token = "invalid-jwt";
        String email = "john@gmail.com";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtUtil.extractEmail(token))
                .thenReturn(email);

        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);

        when(jwtUtil.isTokenValid(token, email))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(userDetailsService)
                .loadUserByUsername(email);

        verify(jwtUtil)
                .isTokenValid(token, email);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldClearSecurityContextWhenJwtThrowsException()
            throws ServletException, IOException {

        String token = "invalid";

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + token);

        when(jwtUtil.extractEmail(token))
                .thenThrow(new RuntimeException("Invalid JWT"));

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(jwtUtil).extractEmail(token);

        verifyNoInteractions(userDetailsService);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateIfAuthenticationAlreadyExists()
            throws ServletException, IOException {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token");

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(
                authentication,
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(jwtUtil).extractEmail("token");

        verify(userDetailsService, never())
                .loadUserByUsername(anyString());

        verify(filterChain)
                .doFilter(request, response);

        verify(filterChain)
                .doFilter(request, response);
    }
}