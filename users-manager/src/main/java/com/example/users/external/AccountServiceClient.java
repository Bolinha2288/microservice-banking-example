package com.example.users.external;

import com.example.users.dto.ResponseDTO;
import com.example.users.dto.UserDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@Slf4j
public class AccountServiceClient {

    private final WebClient.Builder webClientBuilder;

    private static final String CIRCUIT_BREAKER_NAME = "accountServiceClient";

    @Value("${services.account-manager.base-url}")
    private String emailManagerBaseUrl;

    public AccountServiceClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackResendAccount")
    public ResponseDTO sendDataAccountService(UserDTO userDTO) {
        return webClientBuilder.build()
                .post()
                .uri(emailManagerBaseUrl)
                .bodyValue(userDTO)
                .retrieve()
                .bodyToMono(ResponseDTO.class)
                .doOnNext(response -> log.info("log response account-manager: {}", response))
                .block();
    }

    private ResponseDTO fallbackResendAccount(UserDTO userDTO, Throwable t) {
        log.warn("Fallback: fail '{}'. Cause: {}", userDTO.getEmail(), t.getMessage());
        ResponseDTO fallbackResponse = new ResponseDTO(
                "Fail communicate with account-manager.",
                List.of(false)
        );

        return fallbackResponse;
    }
}
