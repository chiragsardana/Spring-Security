package com.chirag.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
                // Create in-memory users
                UserDetails normalUser = User
                                .withUsername("chirag")
                                .password("{noop}password")
                                .roles("NORMAL")
                                .build();

                UserDetails adminUser = User
                                .withUsername("chiragAdmin")
                                .password("{noop}password")
                                .roles("ADMIN")
                                .build();

                return new InMemoryUserDetailsManager(adminUser, normalUser);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

                httpSecurity
                        .authorizeHttpRequests(authorize -> authorize
                                        .requestMatchers("/public").permitAll()
                                        // Allow unrestricted access to /public URL
                                        .requestMatchers("/admin").hasRole("ADMIN")
                                        // Only users with ADMIN role can access /admin URL
                                        .requestMatchers("/normal").hasAnyRole("NORMAL", "ADMIN")
                                        // Users with NORMAL or ADMIN role can access /normal URL
                                        .anyRequest().authenticated())
                                        // Any other requests require authentication
                        .formLogin(formLogin -> formLogin
                                        // .loginPage("/login")
                                        // Custom login page can be specified if needed
                                        .permitAll())
                                        // Allow unrestricted access to the form login functionality
                        .logout(logout -> logout
                                      .logoutUrl("/logout")
                                      // Specify the URL for logout action
                                      .logoutSuccessUrl("/login?logout")
                                      // Redirect to /login?logout after successful logout
                                        .permitAll());
                                        // Allow unrestricted access to the logout functionality

                return httpSecurity.build();
        }
}
