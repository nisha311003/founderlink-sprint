package com.founderlink.startupService.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class HeaderAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");
        String userId = request.getHeader("X-User-Id");

        System.out.println("🔐 HeaderAuthFilter - Email: " + email + ", Role: " + role + ", UserId: " + userId);

        if(email != null && role != null){
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    email, null, List.of(new SimpleGrantedAuthority(role))
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            System.out.println("✅ Authentication set for user: " + email + " with role: " + role);
            System.out.println("🔍 Authorities in token: " + authenticationToken.getAuthorities());
            System.out.println("🔍 Is Authenticated: " + authenticationToken.isAuthenticated());
        } else {
            System.out.println("❌ Email or Role header missing!");
        }
        filterChain.doFilter(request,response);
    }
}
