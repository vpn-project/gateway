package com.example.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenResponse {

    String login;
    List<Role> authorities;
    boolean isAuthenticated;
}

