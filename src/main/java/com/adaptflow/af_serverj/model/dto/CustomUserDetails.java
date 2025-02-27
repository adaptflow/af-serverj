package com.adaptflow.af_serverj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomUserDetails {

    private String token;
    private String userId;
    private String username;
    private String email;

}