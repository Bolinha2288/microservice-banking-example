package com.example.account.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDTO{
        private UUID idReference;
        private String name;
        private String email;
        private LocalDateTime dateCreated;
}
