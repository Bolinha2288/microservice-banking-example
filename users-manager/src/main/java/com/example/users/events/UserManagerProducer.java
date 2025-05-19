package com.example.users.events;

import com.example.users.dto.UserDTO;
import com.example.users.dto.UserEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class UserManagerProducer {
    private final NewTopic topic;

    private final KafkaTemplate<String, UserEventDTO> kafkaTemplate;

    public UserManagerProducer(NewTopic topic, KafkaTemplate<String, UserEventDTO> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }



    public void sendMessage(UserDTO userDTO){

        UserEventDTO userEventDTO = createUserEventDTO(userDTO);

        log.info("Producing message to topic {}: {}", topic.name(), userEventDTO);

        Message<UserEventDTO> message = MessageBuilder
                .withPayload(userEventDTO)
                .setHeader(KafkaHeaders.TOPIC, topic.name())
                .build();

        CompletableFuture<SendResult<String, UserEventDTO>> future = kafkaTemplate.send(message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message [{}] to topic [{}] with offset [{}]",
                        userEventDTO, topic.name(), result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send message [{}] due to: {}", userEventDTO, ex.getMessage());
                throw new RuntimeException("Failed to send message to Kafka: " + ex.getMessage(), ex);
            }
        });
    }

    private UserEventDTO createUserEventDTO(UserDTO userDTO) {
        UserEventDTO userEventDTO = new UserEventDTO();
        userEventDTO.setUserDTO(userDTO);
        return userEventDTO;
    }

}
