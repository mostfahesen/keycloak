package com.keycloak.controller;


import com.keycloak.config.KeycloakProvider;
import com.keycloak.http.requests.*;
import com.keycloak.httpresponse.IntrospectResponse;
import com.keycloak.service.ServiceApp;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping("/keycloak")
public class KeycloakController {
    private final ServiceApp kcAdminClient;

    private final KeycloakProvider kcProvider;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(KeycloakController.class);

    @Autowired
    ServiceApp serviceApp;

    public KeycloakController(ServiceApp kcAdminClient, KeycloakProvider kcProvider) {
        this.kcProvider = kcProvider;
        this.kcAdminClient = kcAdminClient;
    }

    @PostMapping(value = "/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest user) {
        Response createdResponseRequest = kcAdminClient.createKeycloakUser(user);
        return ResponseEntity.status(createdResponseRequest.getStatus()).build();
    }


    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login (@RequestBody LoginRequest loginRequest) {
        Keycloak keycloak = kcProvider.newKeycloakBuilderWithPasswordCredentials(loginRequest.getUsername(), loginRequest.getPassword()).build();
        AccessTokenResponse accessTokenResponse = null;
        try {
            accessTokenResponse = keycloak.tokenManager().getAccessToken();
            return ResponseEntity.status(HttpStatus.OK).body(accessTokenResponse);
        } catch (BadRequestException ex) {
            LOG.warn("invalid account. User probably hasn't verified email.", ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(accessTokenResponse);
        }
    }

    //pass bearer authorization token in header
    @GetMapping(value = "/details")
    public ResponseEntity<Object> getUserDetails(Principal principal) throws IOException {

        KeycloakAuthenticationToken kp = (KeycloakAuthenticationToken) principal;

        SimpleKeycloakAccount simpleKeycloakAccount = (SimpleKeycloakAccount) kp.getDetails();

        //get data
        AccessToken token = simpleKeycloakAccount.getKeycloakSecurityContext().getToken();
        String id = token.getId();
        String username = token.getPreferredUsername();
        Long exp = token.getExp();
        Set<String> roles = token.getRealmAccess().getRoles();
        MyUserDetails myUserDetails = new MyUserDetails();
        myUserDetails.setTokenID(id);
        myUserDetails.setUsername(username);
        myUserDetails.setTokenExp(exp);
        myUserDetails.setRolesUser(roles);
        return new ResponseEntity<Object>( myUserDetails, HttpStatus.OK);
        //return new ;
    }

    //check token active or not
    @PostMapping("/introspect")
    public ResponseEntity<IntrospectResponse> introspect(@RequestBody TokenRequest token) {
        return serviceApp.introspect(token);
    }



     //send refresh token of user in body worked
    @PostMapping("/logout")
    public ResponseEntity<ResponseRequest> logout (@RequestBody TokenRequest token) {
        return serviceApp.logout(token);
    }

    @GetMapping(path = "/get")
    public HashMap index(KeycloakAuthenticationToken authentication, HttpSession httpSession) {
        SimpleKeycloakAccount account = (SimpleKeycloakAccount) authentication.getDetails();
        AccessToken token = account.getKeycloakSecurityContext().getToken();
        httpSession.setAttribute(token.getSessionId(), token.getPreferredUsername());
        return new HashMap() {{
            put("user", token.getPreferredUsername());
            put("access roles", token.getRealmAccess().getRoles());
            put("token", account.getKeycloakSecurityContext().getTokenString());

        }};



    }




}
