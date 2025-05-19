package com.example.users.events;

import com.example.users.dto.UserEventDTO;
import com.example.users.external.AccountServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserManagerConsumer {
    private final AccountServiceClient accountServiceClient;

    public UserManagerConsumer(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    @KafkaListener( topics = "${spring.kafka.topic.name}",
                    groupId = "${spring.kafka.consumer.group-id}",
                    containerFactory = "kafkaListenerContainerFactory")
    public void consumeEvent(UserEventDTO userEventDTO) {
        log.info("User event received in account-manager service {}", userEventDTO);
        accountServiceClient.sendDataAccountService(userEventDTO.getUserDTO());
        log.info("Reprocessed successfully {}", true);
    }
}