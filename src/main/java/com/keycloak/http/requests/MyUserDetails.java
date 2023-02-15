package com.keycloak.http.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class MyUserDetails {


    private String tokenID;
    private Long tokenExp;
    private String username;
    private Set<String> rolesUser;


}