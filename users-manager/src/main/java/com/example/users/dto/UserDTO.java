package com.example.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDTO{

        @Schema(hidden = true)
        private Long id;

        @Schema(hidden = true)
        private UUID idReference;

        @Size(min = 3, max = 50, message = "The name must be between 3 and 50 characters.")
        private String name;

        @Email(message = "The email must be valid.")
        private String email;

        @Schema(hidden = true)
        private LocalDateTime dateCreated;
}
