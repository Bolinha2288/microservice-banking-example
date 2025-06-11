package com.example.users.external;

import com.example.users.dto.ResponseDTO;
import com.example.users.dto.UserDTO;
import com.example.users.events.UserManagerProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class AccountServiceClient {

    private final WebClient.Builder webClientBuilder;

    private final UserManagerProducer userManagerProducer;

    private static final String CIRCUIT_BREAKER_NAME = "accountServiceClient";

    @Value("${services.account-manager.base-url}")
    private String emailManagerBaseUrl;

    public AccountServiceClient(WebClient.Builder webClientBuilder, UserManagerProducer userManagerProducer) {
        this.webClientBuilder = webClientBuilder;
        this.userManagerProducer = userManagerProducer;
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
        log.warn("Fallback: fail '{}'. Cause: {}", userDTO, t.getMessage());

        //userManagerProducer.sendMessage(userDTO);

        ResponseDTO fallbackResponse = new ResponseDTO(
                "Fail communicate with account-manager.",
                List.of(false)
        );

        return fallbackResponse;
    }
}
