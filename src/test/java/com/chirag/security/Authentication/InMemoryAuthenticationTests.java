package com.chirag.security.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
public class InMemoryAuthenticationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    private AuthenticationManager authenticationManager;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // By setting up the MockMvc instance with the WebApplicationContext and applying Spring 
    // Security, you can use the mvc object to perform HTTP requests and test the behavior 
    // of your Spring Security configuration.

    @Test
    public void testValidCredentials() throws Exception {
        UserDetails user = User
                .withUsername("chirag")
                .password("{noop}password")
                .roles("NORMAL")
                .build();
        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager(user);
        mvc.perform(formLogin().user("chirag").password("password")) // Use plain password, without {noop}
                .andDo(print())
                .andExpect(authenticated().withUsername("chirag")) // Assert the authentication success
                .andExpect(authenticated().withRoles("NORMAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/")); // Assert the redirection URL after successful login
    }

    @Test
    public void testInvalidCredentials() throws Exception {
        UserDetails user = User
                .withUsername("chirag")
                .password("{noop}password")
                .roles("NORMAL")
                .build();

        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(user);

        mvc
                .perform(formLogin().user("chirag").password("password1"))
                .andDo(print())
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    public void testRoleBasedAccessControl() throws Exception {
        MvcResult adminAccessResult = mvc
                .perform(MockMvcRequestBuilders.get("/admin").with(user("chiragAdmin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult userAccessResult = mvc.perform(MockMvcRequestBuilders.get("/admin").with(user("chirag")))
                // .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        MvcResult publicAccessResult = mvc.perform(MockMvcRequestBuilders.get("/public"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("Status: " + adminAccessResult.getResponse().getStatus());
        System.out.println("Content Type: " + userAccessResult.getResponse().getContentType());
        System.out.println("Headers: " + userAccessResult.getResponse().getHeaderNames());

    }

    @Test
    public void testAuthenticationWithValidCredentials() throws Exception {

        UserDetails user = User
                .withUsername("chirag")
                .password("{noop}password")
                .roles("NORMAL")
                .build();

        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(user);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken("chirag", "password");

        Authentication authResult = authenticationManager.authenticate(authRequest);

        assertTrue(authResult.isAuthenticated());
        assertEquals("chirag", authResult.getName());

        assertTrue(authResult.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_NORMAL")));

        // Authentication authResult1 = authenticationManager.authenticate(authRequest);
    }

    @Test
    public void testAuthenticationWithInValidCredentials() throws Exception {

        UserDetails user = User
                .withUsername("chirag")
                .password("{noop}password")
                .roles("NORMAL")
                .build();

        InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();
        userDetailsManager.createUser(user);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken("chirag",
                "password1");

        // Perform authentication
        try {
            Authentication authResult = authenticationManager.authenticate(authRequest);

            // If authentication succeeds, fail the test
            fail("Authentication succeeded with invalid credentials");
        } catch (AuthenticationException ex) {
            // If authentication fails, assert the expected exception type
            System.out.println("Exception Occurs");
            assertTrue(ex instanceof BadCredentialsException);
        }
    }

}
