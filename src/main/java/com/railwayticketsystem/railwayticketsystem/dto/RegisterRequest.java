package com.railwayticketsystem.railwayticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Pattern(regexp = "^[0-9]{9}[VvXx]$", message = "Invalid NIC format")
    private String nic;

    @NotBlank
    private String fullName;

    @NotBlank
    private String email;

    @NotBlank
    private String mobile;

    @NotBlank
    private String password;
}