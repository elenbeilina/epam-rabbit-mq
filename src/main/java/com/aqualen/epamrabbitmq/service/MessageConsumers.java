package com.aqualen.epamrabbitmq.service;

import com.aqualen.epamrabbitmq.properties.RabbitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageConsumers {

  private final RabbitProperties rabbitProperties;
  private final MessageService messageService;
  @Value("${spring.cloud.stream.bindings.queue1Consumer-in-0.group}")
  private String queue1Name;

  @Bean
  public Consumer<String> queue1Consumer() {
    return (message) -> log.info("Received message: {} from the queue: {}", message,
        rabbitProperties.getExchangeName() + "." + queue1Name);
  }

  @Bean
  public Consumer<Message<String>> queue2Consumer() {
    return (message) -> {
      List<Map<String, Object>> deathHeaders = (List<Map<String, Object>>) message.getHeaders()
          .get("x-death", List.class);
      Map<String, Object> deathHeader = CollectionUtils.isEmpty(deathHeaders) ? null : deathHeaders.get(0);
      if (Objects.nonNull(deathHeader) &&
          (Long) deathHeader.get("count") > rabbitProperties.getRetries()) {
        messageService.saveMessage(message.getPayload());
      } else {
        throwExceptionWhileProcessing(message.getPayload());
      }
    };
  }

  @Bean
  public Consumer<String> failedConsumer() {
    return messageService::saveMessage;
  }


  private String throwExceptionWhileProcessing(String message) {
    throw new ImmediateRequeueAmqpException(
        String.format("Exception happened, while processing the message: %s!", message
        ));
  }
}
