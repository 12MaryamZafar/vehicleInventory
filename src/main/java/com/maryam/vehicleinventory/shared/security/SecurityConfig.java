package com.maryam.vehicleinventory.shared.security;

import com.maryam.vehicleinventory.shared.tenant.TenantFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TenantFilter tenantFilter;

    public SecurityConfig(TenantFilter tenantFilter) {
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole(RoleConstants.GLOBAL_ADMIN)
                        .anyRequest().hasRole(RoleConstants.TENANT_USER)
                )
                .httpBasic(httpBasic -> {})
                .addFilterBefore(tenantFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // In-memory users for now — replace with DB/JWT later
        var admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin123")
                .roles(RoleConstants.GLOBAL_ADMIN)
                .build();

        var tenant = User.withDefaultPasswordEncoder()
                .username("tenant")
                .password("tenant123")
                .roles(RoleConstants.TENANT_USER)
                .build();

        return new InMemoryUserDetailsManager(admin, tenant);
    }
}
