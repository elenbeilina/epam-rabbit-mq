package com.aqualen.epamrabbitmq.service;

import com.aqualen.epamrabbitmq.entities.Message;
import com.aqualen.epamrabbitmq.properties.RabbitProperties;
import com.aqualen.epamrabbitmq.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.cloud.stream.binder.rabbit.RabbitExpressionEvaluatingInterceptor.ROUTING_KEY_HEADER;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

  private final StreamBridge streamBridge;
  private final MessageRepository messageRepository;
  private final RabbitProperties rabbitProperties;

  @SneakyThrows
  public void publishMessage(String message) {
    streamBridge.send("messageProducer-out-0",
        MessageBuilder
            .withPayload(message)
            .setHeader(ROUTING_KEY_HEADER, rabbitProperties.getRoutingKey())
            .build());
    log.info("Message: {} was sent to the exchange: {}.", message, rabbitProperties.getExchangeName());
  }

  public List<Message> getMessages() {
    return messageRepository.findAll();
  }

  public void sendMessageToFailedQueue(String message, Exception e) {
    streamBridge.send("failedProducer-out-0",
        MessageBuilder
            .withPayload(message)
            .build());
    log.error("Message: {} was sent to the failed exchange: {} with error:",
        message, rabbitProperties.getExchangeName(), e);
  }

  public void saveMessage(String message) {
    log.info("Saving message: {} to the persistent store.", message);
    messageRepository.save(Message.builder().message(message).build());
  }
}
