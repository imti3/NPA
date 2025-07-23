package com.nbl.npa.Config;

import com.nbl.npa.Service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Value("${NPA.USERNAME}")
    private String username;

    @Value("${NPA.PASSWORD}")
    private String password;


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationService authenticationService) {
        return new JwtAuthenticationFilter(authenticationService);
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = User.builder()
//                .username(username) // This should be 'NPA@056Tst%'
//                .password(passwordEncoder().encode(password))
//                .roles("USER")
//                .build();
//        System.out.println("DEBUG: SecurityConfig UserDetailsService configured with username: '" + user.getUsername() + "'");
//        return new InMemoryUserDetailsManager(user);
//    }

    @Bean // Your custom UserDetailsService bean defined directly here
    public UserDetailsService userDetailsService() {
        // Encode the password once when the bean is created
        final String encodedPassword = passwordEncoder().encode(password);
        final String configuredUsername = username; // Capture for use in inner class

        System.out.println("DEBUG: SecurityConfig UserDetailsService Bean initialized. Configured Username: '" + configuredUsername + "'");


        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String inputUsername) throws UsernameNotFoundException {
                System.out.println("DEBUG: Anonymous UserDetailsService attempting to load username: '" + inputUsername + "' (comparing with stored: '" + configuredUsername + "')");

                // Perform explicit case-sensitive comparison
                if (inputUsername.equals(configuredUsername)) {
                    return User.builder()
                            .username(configuredUsername) // Use the exact configured username
                            .password(encodedPassword)   // Use the pre-encoded password
                            .roles("USER")               // Assign roles as needed
                            .build();
                } else {
                    System.out.println("DEBUG: Username '" + inputUsername + "' not found or case mismatch.");
                    throw new UsernameNotFoundException("User not found with username: " + inputUsername);
                }
            }
        };
    }









    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/resources/**", "/assets/**", "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**," +
                                "/templates/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/npa").permitAll()
                        .requestMatchers(HttpMethod.GET, "/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pension_collect/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pension_report/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pension_verify/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/payment_history/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/verify/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/verify_payment/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/initial/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/initial_company/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/make_company_payment/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/company_payment_confirmation/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/make_individual_payment/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/payment_confirmation/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/payment_confirmation/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/test/secured").authenticated()
                        .requestMatchers(HttpMethod.POST, "/paymentstatus").authenticated()
                        .requestMatchers(HttpMethod.POST, "/change-password").authenticated()
                        .requestMatchers(HttpMethod.POST,"/report-print/**").permitAll()
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
