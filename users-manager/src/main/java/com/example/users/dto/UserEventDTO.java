package com.example.users.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserEventDTO {
    private UserDTO userDTO;
    private String eventType = "FALLBACK_ACCOUNT_SERVICE";
    private LocalDateTime eventDate = LocalDateTime.now();
}
