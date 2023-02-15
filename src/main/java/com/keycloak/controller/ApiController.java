package com.keycloak.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/api")

public class ApiController {

	@RolesAllowed("user")
	@GetMapping("/user")
	public ResponseEntity<String> helloUser() {
		return ResponseEntity.ok("Nice day , -user- with roles allowed user");
	}


	@GetMapping("/hello")
	public ResponseEntity<String> hello() {
		return ResponseEntity.ok("Nice day, non any auth");
	}


	@GetMapping("/admin")
	public ResponseEntity<String> helloAdmin() {
		return ResponseEntity.ok("Nice day , -admin- with roles allowed for admin");
	}
	

	
}