package com.chirag.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class RolesController {
    
    // @PreAuthorize("hasRole('NORMAL')")
    @GetMapping("/normal")
    public ResponseEntity<String> normalUser(){
        return ResponseEntity.ok("Yes, I'm a normal User");
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> adminUser(){
        return ResponseEntity.ok("Yes, I'm a admin User");
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicUser(){
        return ResponseEntity.ok("Yes, I'm a public User");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'NORMAL')")
    @GetMapping("/role")
    public ResponseEntity<String> publicRole(){
        return ResponseEntity.ok("the User is either Normal or Admin");
    }

}
