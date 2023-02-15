package com.keycloak.http.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    String username;
    String password;
}
