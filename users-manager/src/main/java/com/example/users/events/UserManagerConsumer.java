package com.example.users.events;

import com.example.users.dto.ResponseDTO;
import com.example.users.dto.UserEventDTO;
import com.example.users.exception.UserException;
import com.example.users.external.AccountServiceClient;
import com.example.users.utils.KafkaEventTrackerInMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserManagerConsumer {


    private final AccountServiceClient accountServiceClient;
    private final KafkaEventTrackerInMemory kafkaEventTracker;

    public UserManagerConsumer(AccountServiceClient accountServiceClient, KafkaEventTrackerInMemory kafkaEventTracker) {
        this.accountServiceClient = accountServiceClient;
        this.kafkaEventTracker = kafkaEventTracker;
    }

    @KafkaListener( topics = "${spring.kafka.topic.name}",
                    groupId = "${spring.kafka.consumer.group-id}",
                    containerFactory = "kafkaListenerContainerFactory")
    public void consumeEvent(UserEventDTO userEventDTO, Acknowledgment ack) {
        log.info("User event received in account-manager service {}", userEventDTO);
        try {
            ResponseDTO response = accountServiceClient.sendDataAccountService(userEventDTO.getUserDTO());

            boolean success = response != null
                    && response.data() != null
                    && !response.data().isEmpty()
                    && !Boolean.FALSE.equals(response.data().get(0));

            if (success) {
                kafkaEventTracker.removeSent(userEventDTO.getUserDTO().getIdReference());
                ack.acknowledge();
                log.info("Reprocessed successfully and removed idReference");
            } else {
                log.warn("Response returned but not successful: {}", response);
                throw new UserException("Processing failed for user event, triggering retry. Response: " + response);
            }
        } catch (Exception e) {
            log.error("Failed to process user: {}", userEventDTO.getUserDTO(), e);
            throw e;
        }
    }
}