package com.employee_management_system.Employee_Management.config;

import com.employee_management_system.Employee_Management.Model.User;
import com.employee_management_system.Employee_Management.Repository.UserRepository;
import com.employee_management_system.Employee_Management.Service.UserDetailsServiceImpl;
import com.employee_management_system.Employee_Management.filter.JwtFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@EnableWebSecurity
public class SpringSecurity {

    private static final Logger logger = LoggerFactory.getLogger(SpringSecurity.class);  // Initialize the logger


    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Define URL access rules
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/customer/**").authenticated()   // Secure /customer endpoints
                        .requestMatchers("/user/**", "/oauth2/**").permitAll() // Allow public access to /user and OAuth2 routes
                        .anyRequest().permitAll() // Permit all other requests
                )
                // Disable CSRF
                .csrf(csrf -> csrf.disable())
                // Configure OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login.html") // Custom login page (if any)
                        .defaultSuccessUrl("/index.html", true) // Redirect to home on success
                        .failureUrl("/login.html?error=true") // Redirect to login page with error on failure
                        .successHandler(new AuthenticationSuccessHandler() {
                            @Override
                            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                                DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
                                String name = oAuth2User.getAttribute("name");
                                String email = oAuth2User.getAttribute("email");

                                // Check if a user with the same email exists
                                Optional<User> existingUser = userRepository.findByEmail(email);

                                if (existingUser.isEmpty()) {
                                    // If the user does not exist, create a new one
                                    logger.info("User with email {} does not exist, creating a new user.", email);

                                    // You don't need to check if a username exists here since it's a new user creation
                                    User newUser = new User();
                                    newUser.setEmail(email);
                                    newUser.setUsername(name);
                                    newUser.setPassword(UUID.randomUUID().toString()); // Use a random password or handle it securely

                                    userRepository.save(newUser);
                                    logger.info("New user with email {} created successfully.", email);

                                    // Set session attribute with the new user's username
                                    request.getSession().setAttribute("username", newUser.getUsername());
                                } else {
                                    // If the user already exists, just log them in by setting the session
                                    request.getSession().setAttribute("username", existingUser.get().getUsername());
                                    logger.info("Existing user {} logged in successfully.", name);
                                }

                                // Set session expiry for persistence (e.g., 7 days)
                                request.getSession().setMaxInactiveInterval(60 * 60 * 24 * 7); // 7 days session duration

                                // Redirect to the dashboard
                                response.sendRedirect("/index.html");
                                logger.info("User {} redirected to the dashboard.", name);

                            }
                        })
                )
                // Add JWT filter before username/password filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Session created only when required
                        .maximumSessions(1) // Limit to one active session per user
                        // Add session fixation protection

                )
                .build();
    }

    // Configure global authentication
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    // Password encoder for encoding passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean for Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
        return auth.getAuthenticationManager();
    }
}