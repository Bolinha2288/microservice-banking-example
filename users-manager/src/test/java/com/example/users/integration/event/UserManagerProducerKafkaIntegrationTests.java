package com.example.users.integration.event;

import com.example.users.controller.UserController;
import com.example.users.domain.repository.UserRepository;
import com.example.users.dto.ResponseDTO;
import com.example.users.dto.UserDTO;
import com.example.users.dto.UserEventDTO;
import com.example.users.events.UserManagerProducer;
import com.example.users.external.AccountServiceClient;
import com.example.users.utils.KafkaEventTrackerInMemory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ImportAutoConfiguration(
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@EmbeddedKafka(topics = "${kafka.topic.user-topic}", partitions = 1)
@ActiveProfiles("test")
@Slf4j
class UserManagerProducerKafkaIntegrationTests {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserController userController;


    @Autowired
    private UserManagerProducer userManagerProducer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockitoBean
    private KafkaEventTrackerInMemory kafkaEventTrackerInMemory;

    @MockitoBean
    private AccountServiceClient accountServiceClient;

    @Value("${kafka.topic.user-topic}")
    private String topicName;


    @Test
    void shouldProduceUserDTOToKafkaTopic() {

        UserDTO userDTO = new UserDTO();
        userDTO.setIdReference(UUID.randomUUID());
        userDTO.setName("edu");
        userDTO.setEmail("edu@teste.com.br");

        UserEventDTO userEventDTO = new UserEventDTO();
        userEventDTO.setUserDTO(userDTO);

        when(kafkaEventTrackerInMemory.alreadySent(userDTO.getIdReference())).thenReturn(false);
        doNothing().when(kafkaEventTrackerInMemory).markAsSent(userDTO.getIdReference());
        when(accountServiceClient.sendDataAccountService(userDTO)).thenReturn(new ResponseDTO("user created", List.of(true)));

        userManagerProducer.sendMessage(userDTO);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Consumer<String, UserEventDTO> consumer = new DefaultKafkaConsumerFactory<>(consumerProps,
                                                                                new StringDeserializer(),
                                                                                new JsonDeserializer<>(UserEventDTO.class, false)
                                                                              ).createConsumer();
        consumer.subscribe(Collections.singletonList(topicName));

        ConsumerRecords<String, UserEventDTO> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(50));
        assertThat(records.isEmpty()).isFalse();

        UserEventDTO received = records.iterator().next().value();
        assertThat(received.getEventType()).isEqualTo("FALLBACK_ACCOUNT_SERVICE");
        assertThat(received.getUserDTO()).isNotNull();
        assertThat(received.getUserDTO().getName()).isEqualTo("edu");
        assertThat(received.getUserDTO().getEmail()).isEqualTo("edu@teste.com.br");
    }
}