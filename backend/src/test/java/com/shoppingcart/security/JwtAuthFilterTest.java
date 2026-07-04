package com.shoppingcart.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_tokenReferencesDeletedUser_continuesUnauthenticatedInsteadOfThrowing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer sometoken");
        when(jwtService.isTokenValid("sometoken")).thenReturn(true);
        when(jwtService.extractEmail("sometoken")).thenReturn("ghost@example.com");
        when(userDetailsService.loadUserByUsername("ghost@example.com"))
                .thenThrow(new UsernameNotFoundException("no user"));

        assertThatCode(() -> filter.doFilterInternal(request, response, filterChain)).doesNotThrowAnyException();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_noAuthorizationHeader_continuesChainWithoutTouchingJwtService() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService, never()).isTokenValid(any());
        verify(filterChain).doFilter(request, response);
    }
}
