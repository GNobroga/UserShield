package com.apiauth.configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.apiauth.abstraction.IUserRepository;
import com.apiauth.filters.JwtAuthenticationFilter;
import com.apiauth.interfaces.IAuthenticationService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final Map<String, List<String>> PERMITTED_ROUTES = new HashMap<>() 
    {
        {
            put("/h2-console/**", List.of("GET"));
            put("/account/**", List.of("POST"));
            put("/auth/**", List.of("POST"));
        }
    };
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, IAuthenticationService authenticationService, IUserRepository userRepository) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable());  
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.authorizeHttpRequests(authorizeHttpRequests -> {
            var allowedRoutes = PERMITTED_ROUTES.keySet().toArray(new String[] {});

            System.out.println(Arrays.copyOfRange(allowedRoutes, 0, 1)[0]); 
            authorizeHttpRequests.requestMatchers("/h2-console/**").permitAll();
            authorizeHttpRequests.requestMatchers(HttpMethod.POST, Arrays.copyOfRange(allowedRoutes, 1, allowedRoutes.length)).permitAll();
            authorizeHttpRequests.requestMatchers(HttpMethod.GET).hasAuthority("COMMON");
            authorizeHttpRequests.requestMatchers(HttpMethod.PUT).hasAuthority("ADMIN");
            authorizeHttpRequests.anyRequest().authenticated();
        });
        httpSecurity.headers(headers -> headers.frameOptions(options -> options.disable()));
        httpSecurity.addFilterBefore(new JwtAuthenticationFilter(authenticationService, userRepository), UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 
