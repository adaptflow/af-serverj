package com.adaptflow.af_serverj.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Login user request model")
public class LoginRequestDTO {
	@Schema(description = "Unique userId", requiredMode = RequiredMode.REQUIRED)
    private String username;
	@Schema(description = "Password", requiredMode = RequiredMode.REQUIRED)
    private String password;
}